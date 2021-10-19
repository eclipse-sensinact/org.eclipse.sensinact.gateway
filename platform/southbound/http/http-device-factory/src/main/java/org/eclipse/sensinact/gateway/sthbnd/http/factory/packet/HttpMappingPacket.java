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
package org.eclipse.sensinact.gateway.sthbnd.http.factory.packet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponsePacket;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.MappingDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extended {@link HttpResponsePacket} allowing to map json
 * data structures to sensiNact's inner data model
 */
public abstract class HttpMappingPacket<M extends MappingDescription>  extends HttpResponsePacket {
	
	static final Logger LOG = LoggerFactory.getLogger(HttpMappingPacket.class);

	static final String PROVIDER_IDENTIFIER = "::provider::";
	static final String PROVIDER_IDENTIFIER_REGEX = "::(provider)::\\[([0-9])\\]";
	static final String PROVIDER_IDENTIFIER_JOIN = ":";
	static final String CONCATENATION_FUNCTION_REGEX = "\\$(concat)\\(([^,]+(,[^,]+)+)\\)";
	
	static final Pattern PROVIDER_IDENTIFIER_PATTERN = Pattern.compile(PROVIDER_IDENTIFIER_REGEX);
	static final Pattern CONCATENATION_FUNCTION_PATTERN = Pattern.compile(CONCATENATION_FUNCTION_REGEX);
	
	protected int index;

	protected String serviceProviderMapping = null;
	protected String serviceProviderIdPattern = null;	
	protected String serviceProviderId = null;

	protected Map<String,String> modelMapping;
	protected Map<String,List<String>> jsonMapping;
	
	protected Map<String,String> resultMapping;
	protected Iterator<Map.Entry<String,String>> iterator;
	protected Map.Entry<String,String> current;

	protected String providerId = null;
	protected String serviceId = null;
	protected String resourceId = null;
	protected String attributeId = null;
	protected String metadataId = null;
	
	/**
	 * Constructor
	 * 
	 * @param response the {@link HttpResponse} processed
	 * by the HttpMappingPacket to be initialized
	 */
	public HttpMappingPacket(HttpResponse response, boolean save, boolean consume) {
		super(response, save, consume);
	}	
	
	/**
	 * Defines the {@link MappingDescription}s for this HttpMappingPacket
	 * 
	 * @param mappings the {@link MappingDescription}s applying
	 */
	public abstract void doSetMapping(M[] mappings) ;
	
	protected abstract void initParsing();
	
	protected abstract Map<String,String> getEvent ();
	
	/**
	 * Defines the {@link MappingDescription}s for this HttpMappingPacket
	 * 
	 * @param mappings the {@link MappingDescription}s applying
	 */
	public void setMapping(M[] mappings) {
		if(mappings == null || mappings.length == 0)
			return;
		
		this.modelMapping = new HashMap<>();
		this.jsonMapping = new HashMap<>();
		
		doSetMapping(mappings);
		initParsing();
	}

	/**
	 * Defines the ServiceProvider identifier 
	 * 
	 * @param serviceProviderId the ServiceProvider identifier to set
	 */
	public void setServiceProviderId(String serviceProviderId) {
		this.serviceProviderId = serviceProviderId;
	}

	/**
	 * Defines the ServiceProvider identifier pattern applying
	 * 
	 * @param serviceProviderIdPattern the ServiceProvider identifier pattern to set
	 */
	public void setServiceProviderIdPattern(String serviceProviderIdPattern) {
		this.serviceProviderIdPattern = serviceProviderIdPattern;
	}
		
	protected String reverseModelMapping(String mapping) {
	    Optional<Entry<String, String>> entry = this.modelMapping.entrySet(
		).stream().filter(e -> {return mapping.equals(e.getValue());}
			).findFirst();
	    return entry.isPresent()?entry.get().getKey():null;
	}	

	protected String substitute(String expression) {
		String variable = expression;
    	int startVariable = variable.indexOf("${");
    	int endVariable = variable.indexOf("}");
    	
    	if(startVariable > -1 && endVariable > -1 && endVariable > startVariable) {
			try {
				String pathVariable = variable.substring(startVariable + 2, endVariable);
				String pathResult = resultMapping.get(reverseModelMapping(String.format("__%s", pathVariable)));
				if(pathResult.startsWith("\"") && pathResult.endsWith("\""))
					pathResult = pathResult.replace('"', ' ').trim();
				if(pathResult.startsWith("'") && pathResult.endsWith("'"))
					pathResult = pathResult.replace('\'', ' ').trim();
				variable = String.format("%s%s%s", expression.substring(0,startVariable), pathResult, expression.substring(endVariable+1));
			} catch (Exception e) {
				LOG.error(e.getMessage(),e);
			}
    	}
    	return variable;
	}
	
	protected String buildProviderId() {
	    String identifier = null;
	    try {
		    identifier = this.modelMapping.entrySet(
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
				).<StringBuilder>collect( StringBuilder::new,
						(s,e)->{
							if(s.length() > 0)
								s.append(PROVIDER_IDENTIFIER_JOIN);
							s.append(resultMapping.get(e.getKey()));},
						(sb1,sb2)->{sb1.append(sb2.toString());}).toString();
	    }catch(Exception e) {
	    	LOG.error(e.getMessage(),e);
	    }
	    return identifier;
	}
	
	private void iterate() {
		if(iterator!=null) {
		    if(iterator.hasNext()) 
		    	current = iterator.next();
			else {
				current = null;
				iterator = null;
			}
		}
		if(iterator == null) {
			iterator = modelMapping.entrySet().iterator();
			if(iterator.hasNext()) {
				current = iterator.next();
				index = 0;
			}
		}
	}
	
	protected boolean wasLast() {	
		iterate();
		String path = current.getValue();		
    	while(true) { 
    		if(path!=null && !path.startsWith(PROVIDER_IDENTIFIER) && !path.startsWith("__"))
    			break;
    		iterate();
    		path = current.getValue(); 
		}
    	
    	path = substitute(path);
    	if(this.serviceProviderMapping == null) 
    		providerId = this.serviceProviderId;
    	else
    		providerId = this.serviceProviderMapping;

		if(path.startsWith("/"))
			path = path.substring(1);		
		if(path.endsWith("/"))
			path = path.substring(0,path.length()-1);
		
		String[] pathElements = path.split("/");
		
		serviceId = pathElements[0];
		resourceId = pathElements[1];
		attributeId = null;
		metadataId = null;
		if(pathElements.length > 2)
			attributeId = pathElements[2];
		if(pathElements.length > 3)
			metadataId = pathElements[3];

		if(index > 0)
			return false;
		
		index=1;
		this.resultMapping = getEvent();
		return this.resultMapping == null;
	}

	protected String getServiceProviderId() {
		if(resultMapping == null)
			return null;
		if(serviceProviderIdPattern!=null)
			return String.format(serviceProviderIdPattern, providerId);
		return providerId;
	}
	
	protected String getServiceId() {
		if(resultMapping == null)
			return null;
		return serviceId;
	}
	
	protected String getResourceId() {
		if(resultMapping == null)
			return null;
		return resourceId;
	}

	protected String getAttributeId() {
		if(resultMapping == null)
			return null;
		return attributeId;
	}

	protected String getMetadataId() {
		if(resultMapping == null)
			return null;
		return metadataId;
	}

	protected Object getData() {
		if(resultMapping == null || this.current ==null)
			return null;
		Matcher matcher =CONCATENATION_FUNCTION_PATTERN.matcher(this.current.getKey());
		if(matcher.matches()) {
			String argument = matcher.group(2);
			String[] arguments = argument.split(",");
			StringBuilder builder = new StringBuilder();
			for(int i =0;i<arguments.length;i++) {
					String s = substitute(arguments[i]).trim();
					if(s.startsWith("\"") && s.endsWith("\""))
						s = s.replace('"', ' ').trim();
					if(s.startsWith("'") && s.endsWith("'"))
						s = s.replace('\'', ' ').trim();
					builder.append(s);
			}
			return builder.toString();
 		}
		String value = resultMapping.get(this.current.getKey());
		if(value == null)
			return null;
		if(value.startsWith("\"") && value.endsWith("\""))
			return value.replace('"', ' ').trim();
	    else if("TRUE".equals(value.toUpperCase()) || "FALSE".equals(value.toUpperCase()))
	    	return Boolean.parseBoolean(value);
	    else 
	    	return Double.parseDouble(value);
	}
}
