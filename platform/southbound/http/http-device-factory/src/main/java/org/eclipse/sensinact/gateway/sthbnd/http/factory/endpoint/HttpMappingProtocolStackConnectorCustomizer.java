package org.eclipse.sensinact.gateway.sthbnd.http.factory.endpoint;

import static com.fasterxml.jackson.core.StreamReadFeature.AUTO_CLOSE_SOURCE;
import static java.util.stream.Collectors.toList;
import static org.eclipse.sensinact.gateway.sthbnd.http.task.config.MappingDescription.ROOT;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.DefaultConnectorCustomizer;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.PacketReader;
import org.eclipse.sensinact.gateway.generic.packet.PayloadFragment;
import org.eclipse.sensinact.gateway.generic.packet.PayloadFragmentImpl;
import org.eclipse.sensinact.gateway.generic.packet.PayloadResourceFragmentImpl;
import org.eclipse.sensinact.gateway.generic.packet.PayloadServiceFragment;
import org.eclipse.sensinact.gateway.generic.packet.PayloadServiceFragmentImpl;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.config.HttpMappingProtocolStackEndpointDescription;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.packet.TaskAwareHttpResponsePacket;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.NestedMappingDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpMappingProtocolStackConnectorCustomizer extends DefaultConnectorCustomizer<TaskAwareHttpResponsePacket> {

	static final Logger LOG = LoggerFactory.getLogger(HttpMappingProtocolStackConnectorCustomizer.class);

	static final String TIMESTAMP = "::timestamp::";
	static final String PROVIDER_IDENTIFIER = "::provider::";
	static final String PROVIDER_IDENTIFIER_REGEX = "::(provider)::\\[([0-9])\\]";
	static final String PROVIDER_IDENTIFIER_JOIN = ":";
	static final String CONCATENATION_FUNCTION_REGEX = "\\$(concat)\\(([^,]+(,[^,]+)+)\\)";
	
	static final Pattern PROVIDER_IDENTIFIER_PATTERN = Pattern.compile(PROVIDER_IDENTIFIER_REGEX);
	static final Pattern CONCATENATION_FUNCTION_PATTERN = Pattern.compile(CONCATENATION_FUNCTION_REGEX);
	
	private final MappingJsonFactory factory;
	private final SimpleDateFormat timestampFormat;
	private final String serviceProviderIdPattern;

	
	public HttpMappingProtocolStackConnectorCustomizer(Mediator mediator,
			@SuppressWarnings("rawtypes") ExtModelConfiguration ExtModelConfiguration,
			HttpMappingProtocolStackEndpointDescription config) {
		super(mediator,ExtModelConfiguration);
		this.timestampFormat = config.getTimestampPattern() == null ? null : new SimpleDateFormat(config.getTimestampPattern());
		this.serviceProviderIdPattern = config.getServiceProviderIdPattern();
		JsonFactory jsonFactory = new JsonFactoryBuilder().configure(AUTO_CLOSE_SOURCE, true)
				.build();
		factory = new MappingJsonFactory(jsonFactory, new ObjectMapper(jsonFactory));
	}

	@Override
	public PacketReader<TaskAwareHttpResponsePacket> newPacketReader(TaskAwareHttpResponsePacket packet) throws InvalidPacketException {
		PacketReader<TaskAwareHttpResponsePacket> reader = new JsonPacketReader();
		reader.load(packet);
		return reader;
	}
	
	protected String substitute(Map<String, Object> data, Map<String, String> mapping, String expression) {
		String variable = expression;
    	int startVariable = variable.indexOf("${");
    	int endVariable = variable.indexOf("}");
    	
    	if(startVariable > -1 && endVariable > -1 && endVariable > startVariable) {
			try {
				String pathVariable = variable.substring(startVariable + 2, endVariable);
				String key = findKeyForValue(mapping, String.format("__%s", pathVariable));
				Object pathResult = getFromDataMap(data,key);
				variable = String.format("%s%s%s", expression.substring(0,startVariable), pathResult, expression.substring(endVariable+1));
			} catch (Exception e) {
				LOG.error(e.getMessage(),e);
			}
    	}
    	return variable;
	}
	
	protected Object resolveValue(Map<String, Object> data, Map<String, String> mapping, String key) {		
		Matcher matcher = CONCATENATION_FUNCTION_PATTERN.matcher(key);
		if(matcher.matches()) {
			String argument = matcher.group(2);
			String[] arguments = argument.split(",");
			StringBuilder builder = new StringBuilder();
			for(int i =0;i<arguments.length;i++) {
				String arg = arguments[i];
				if(arg.charAt(0) == '\'' && arg.charAt(arg.length() - 1) == '\'') {
					builder.append(arg.substring(1, arg.length() - 1));
				} else {
					builder.append(substitute(data, mapping, arguments[i]).trim());
				}
			}
			return builder.toString();
 		}
    	return getFromDataMap(data, key);
	}
	
	private String buildProviderId(Map<String, Object> data, Map<String, String> mapping) {
	    String identifier = null;
	    try {
		    identifier = mapping.entrySet(
			).stream().filter(e -> {return e.getValue().startsWith(PROVIDER_IDENTIFIER);}
				).sorted((e1,e2)->{
					Matcher m1 = PROVIDER_IDENTIFIER_PATTERN.matcher(e1.getValue());
					Matcher m2 = PROVIDER_IDENTIFIER_PATTERN.matcher(e2.getValue());
					if(m1.matches() && m2.matches()) {
						String v1 = m1.group(2);
						String v2 = m2.group(2);
						return Integer.valueOf(v1).compareTo(Integer.valueOf(v2));
					} else 
						return 0;}
				).<StringBuilder>collect( 
					StringBuilder::new,
					(s,e)->{
						String id = String.valueOf(getFromDataMap(data, e.getKey()));
						id = id.trim().replace(' ', '_');
						if(s.length() > 0)
							s.append(PROVIDER_IDENTIFIER_JOIN);
						s.append(id);
					},
					(sb1,sb2)->{sb1.append(sb2.toString());}).toString();
	    } catch(Exception e) {
	    	LOG.error(e.getMessage(),e);
	    }
	    return identifier;
	}
	
	private long resolveTimestamp(Map<String, Object> data, Map<String, String> mapping) {
		long l = 0;		
		String timestampPath = findKeyForValue(mapping, TIMESTAMP);
		
		if(timestampPath == null)
			return l;
		Object resultTimestamp = resolveValue(data, mapping, timestampPath);
		if(resultTimestamp != null) {
			if(this.timestampFormat != null) {
				try {
					Date d = this.timestampFormat.parse(String.valueOf(resultTimestamp));
					l = d.getTime();
				} catch(ParseException ex) {
					LOG.error(ex.getMessage(), ex);
				}
			} else {
				try {
					l = Long.parseLong(String.valueOf(resultTimestamp));
					String lstr = String.valueOf(l);
					switch(lstr.length()) {
					case 10:
						l = l*1000;
					case 13:
						break;
					case 16:
						l = l/1000;
						break;
					case 19:
						l = l/1000000;
						break;
					default:
						throw new IllegalArgumentException();
					}			
				} catch(IllegalArgumentException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	    return l;
	}

	private String findKeyForValue(Map<String, String> map, String value) {
		return map.entrySet().stream()
				.filter(e -> e.getValue().equals(value))
				.map(Entry::getKey)
				.findFirst()
				.orElse(null);
	}
	
	private Object getFromDataMap(Map<String, Object> data, String key) {
		String[] keys = key.split("/");
		Object result = data;
		
		for(String k : keys) {
			if(k.charAt(0) == '[' && k.charAt(k.length() - 1) == ']') {
				if(result instanceof List) {
					result = ((List<?>)result).get(Integer.parseInt(k.substring(1, k.length() - 1)));
				}
			} else {
				if(result instanceof Map) {
					result = ((Map<?,?>) result).get(k);
				} else {
					result = null;
					break;
				}
			}
		}
		return result;
	}
	
	private PayloadFragment createFragments(Map<String, Object> data, Map<String, String> mapping) {
		String providerId = buildProviderId(data, mapping);
		String serviceProviderId = String.format(serviceProviderIdPattern, providerId);
		long timestamp = resolveTimestamp(data, mapping);
		
		List<PayloadServiceFragment> serviceFragments = mapping.entrySet().stream()
			.filter(e -> {
				String value = e.getValue();
				return !TIMESTAMP.equals(value) && 
		    			!value.startsWith(PROVIDER_IDENTIFIER) && 
		    			!value.startsWith("__");
			}).map(e -> {
				Object value = resolveValue(data, mapping, e.getKey());
				String path = substitute(data, mapping, e.getValue());
				
				if(path.startsWith("/"))
					path = path.substring(1);		
				if(path.endsWith("/"))
					path = path.substring(0,path.length()-1);
				
				String[] pathElements = path.split("/");
				
				String serviceId = pathElements[0];
				String resourceId = pathElements[1];
				String attributeId = null;
				String metadataId = null;
				if(pathElements.length > 2)
					attributeId = pathElements[2];
				if(pathElements.length > 3)
					metadataId = pathElements[3];
				
		        PayloadServiceFragmentImpl payloadFragment = new PayloadServiceFragmentImpl();
		        payloadFragment.setServiceId(serviceId);
		        payloadFragment.setResourceId(resourceId);
		        
		        if (attributeId != null || value != null) {
	                PayloadResourceFragmentImpl payloadAttributeFragment = new PayloadResourceFragmentImpl(attributeId, metadataId, value);
	                payloadAttributeFragment.setTimestamp(timestamp);
	                payloadFragment.addPayloadAttributeFragment(payloadAttributeFragment);
	            }
		        return payloadFragment;
			}).collect(toList());
		
		PayloadFragmentImpl subPacket = new PayloadFragmentImpl(serviceFragments);
		subPacket.setServiceProviderIdentifier(serviceProviderId);
		subPacket.isGoodbyeMessage(false);
		subPacket.isHelloMessage(false);
		
		return subPacket;
	}
	
	private class JsonPacketReader implements PacketReader<TaskAwareHttpResponsePacket>, Iterator<PayloadFragment> {

		private final LinkedList<Object> context = new LinkedList<>();
		
		
		private final Map<String, Map<String, String>> mappings = new HashMap<>();
		private JsonParser parser;
		private RootMappingTree tree;
		

		@Override
		public Iterator<PayloadFragment> iterator() {
			return this;
		}

		@Override
		public void load(TaskAwareHttpResponsePacket packet) throws InvalidPacketException {
			Arrays.stream(packet.getMapping())
					.forEach(m -> mappings.put(m.getMappingType() == ROOT ? "" :
						((NestedMappingDescription)m).getPath(), m.getMapping()));
			
			tree = new RootMappingTree(mappings.keySet());
			
			try {
				InputStream is = packet.getInputStream();
				if(is == null) {
					parser = factory.createParser(packet.getBytes());
				} else {
					parser = factory.createParser(is);
				}
				// Prime the parser by advancing to the first token
				parser.nextToken();
			} catch (Exception e) {
				throw new InvalidPacketException("Failed to load packet", e);
			}
		}

		@Override
		public void parse() throws InvalidPacketException {
		}

		@Override
		public void reset() {
			if(parser != null) {
				try {
					parser.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			context.clear();
			tree = null;
			mappings.clear();
		}

		@Override
		public boolean hasNext() {
			if(parser == null)
				return false;
			
			JsonToken currentToken = parser.currentToken();
			// This is necessary as #next() consumes the END_OBJECT token
			// by clearing it
			if(currentToken == null) {
				currentToken = JsonToken.NOT_AVAILABLE;
			}
			try {
				while(currentToken != null) {
					if(currentToken.isStructStart()) {
						switch(tree.getAction(context)) {
							case Ignore:
								parser.skipChildren();
								context.removeLast();
								break;
							case Select:
								return true;
							default:
								break;
						}
					} 
						
					currentToken = parser.nextToken();
					if(currentToken != null) {
						if(currentToken.isStructStart()) {
							String currentName = parser.currentName();
							if(currentName == null) {
								Object last = context.peekLast();
								if(last instanceof Integer) {
									context.removeLast();
									context.addLast(((Integer) last) + 1);
								} else {
									context.addLast(1);
								}
							} else { 
								context.addLast(currentName);
							}
						} else if (currentToken.isStructEnd() && !context.isEmpty()) {
							Object o = context.removeLast();
							// If this is the end of an array field, and the context was inside the array,
							// then we remove the parent context too. Needed because readValueAs
							// consumes and clears the END_OBJECT token.
							if(o instanceof Integer && currentToken == JsonToken.END_ARRAY && !context.isEmpty()) {
								context.removeLast();
							}
						}
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return false;
		}

		@Override
		public PayloadFragment next() {
			if(!hasNext()) {
				throw new NoSuchElementException("No more elements");
			}
			try {
				Map<String, Object> data = parser.readValueAs(new TypeReference<Map<String, Object>>() {});
				String matchedPath = tree.getMappedPath(context);
				
				// The readValueAs call eats the final END_OBJECT, so we clear the context
				// but only if it isn't an array (Integer context segment)
				if(context.peekLast() instanceof String) {
					context.removeLast();
				}
				
				return createFragments(data, mappings.get(matchedPath));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
