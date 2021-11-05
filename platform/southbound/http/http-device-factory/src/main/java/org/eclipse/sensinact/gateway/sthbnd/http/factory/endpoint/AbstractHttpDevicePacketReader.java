/*
 * Copyright (c) 2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.http.factory.endpoint;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.sensinact.gateway.generic.packet.PacketReader;
import org.eclipse.sensinact.gateway.generic.packet.PayloadFragment;
import org.eclipse.sensinact.gateway.generic.packet.PayloadFragmentImpl;
import org.eclipse.sensinact.gateway.generic.packet.PayloadResourceFragmentImpl;
import org.eclipse.sensinact.gateway.generic.packet.PayloadServiceFragment;
import org.eclipse.sensinact.gateway.generic.packet.PayloadServiceFragmentImpl;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.packet.TaskAwareHttpResponsePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHttpDevicePacketReader implements PacketReader<TaskAwareHttpResponsePacket>, Iterator<PayloadFragment> {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractHttpDevicePacketReader.class);
	
	private static final String TIMESTAMP = "::timestamp::";
	private static final String PROVIDER_IDENTIFIER = "::provider::";
	private static final String PROVIDER_IDENTIFIER_REGEX = "::(provider)::\\[([0-9])\\]";
	private static final String PROVIDER_IDENTIFIER_JOIN = ":";
	private static final String CONCATENATION_FUNCTION_REGEX = "\\$(concat)\\(([^,]+(,[^,]+)+)\\)";
	
	private static final Pattern PROVIDER_IDENTIFIER_PATTERN = Pattern.compile(PROVIDER_IDENTIFIER_REGEX);
	private static final Pattern CONCATENATION_FUNCTION_PATTERN = Pattern.compile(CONCATENATION_FUNCTION_REGEX);
	
	private final SimpleDateFormat timestampFormat;
	private final String serviceProviderIdPattern;
	
	public AbstractHttpDevicePacketReader(SimpleDateFormat timestampFormat, String serviceProviderIdPattern) {
		this.timestampFormat = timestampFormat;
		this.serviceProviderIdPattern = serviceProviderIdPattern;
	}
	
	@Override
	public Iterator<PayloadFragment> iterator() {
		return this;
	}

	private String substitute(Map<String, Object> data, Map<String, String> mapping, String expression) {
		String variable = expression;
    	int startVariable = variable.indexOf("${");
    	int endVariable = variable.indexOf("}");
    	
    	if(startVariable > -1 && endVariable > -1 && endVariable > startVariable) {
			try {
				String pathVariable = variable.substring(startVariable + 2, endVariable);
				
				String searchVariable;
				Function<Object,String> transform;
				if(pathVariable.indexOf(':') >= 0) {
					String[] split = pathVariable.split(":");
					searchVariable = split[0];
					int start = parseInt(split[1]);
					if(split.length == 2) {
						transform = o -> o == null ? "null" :
								String.valueOf(o).substring(start);
					} else if (split.length == 3) {
						transform = o -> o == null ? "null" :
								String.valueOf(o).substring(start, start + parseInt(split[2]));
					} else {
						LOG.error("Unable to validate variable {}", pathVariable);
						throw new IllegalArgumentException("The variable " + pathVariable + " is not valid");
					}
				} else {
					searchVariable = pathVariable;
					transform = String::valueOf;
				}
				
				String key = findKeyForValue(mapping, String.format("__%s", searchVariable));
				String pathResult = transform.apply(getFromDataMap(data,key));
				variable = String.format("%s%s%s", expression.substring(0,startVariable), pathResult, expression.substring(endVariable+1));
			} catch (Exception e) {
				LOG.error(e.getMessage(),e);
			}
    	}
    	return variable;
	}
	
	private Object resolveValue(Map<String, Object> data, Map<String, String> mapping, String key) {		
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
				} else if (result instanceof Map) {
					result = ((Map<?,?>) result).get(k);
				} else {
					result = null;
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
	
	protected PayloadFragment createFragments(Map<String, Object> data, Map<String, String> mapping) {
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
	
	protected void safeClose(AutoCloseable toClose) {
		try {
			toClose.close();
		} catch (Exception e) {}
	}
}