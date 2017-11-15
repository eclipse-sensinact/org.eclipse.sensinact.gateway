/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */

package org.eclipse.sensinact.gateway.nthbnd.rest.internal;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundEndpoint;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRecipient;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestBuilder;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is the REST interface between each others classes 
 * that perform a task and jersey
 */
public abstract class RestAccess
{
	//********************************************************************//
	//						NESTED DECLARATIONS			  			      //
	//********************************************************************//

	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//
	
    /**
     * @param builder
     * @return 
     * @throws IOException 
     */
    protected abstract boolean respond(
    		NorthboundRequestBuilder<JSONObject> builder) 
    		throws IOException;

	/**
	 * @param i
	 * @param string
	 * @throws IOException 
	 */
	protected abstract void sendError(int i, String string)
			throws IOException;
	
	
	//********************************************************************//
	//						STATIC DECLARATIONS							  //
	//********************************************************************//

	public static String RAW_QUERY_PARAMETER = "#RAW#";
	
	public static final String ROOT = "\\/sensinact";

	public static final String ELEMENT_SCHEME = "\\/([^\\/]+)";
	
	public static final String SERVICEPROVIDERS_SCHEME =
		ROOT + "\\/providers";
	
	public static final String SERVICEPROVIDER_SCHEME = 
		SERVICEPROVIDERS_SCHEME + ELEMENT_SCHEME;
	
	public static final String  SIMPLIFIED_SERVICEPROVIDER_SCHEME =
		ROOT + "\\/(([^p]|p[^r]|pr[^o]|pro[^v]|prov[^i]|provi[^d]|provid[^e]|provide[^r]|provider[^s]|providers[^\\/])[^\\/]*)";
	
	public static final String SERVICES_SCHEME = 
		SERVICEPROVIDER_SCHEME + "\\/services";
	
	public static final String SERVICE_SCHEME = 
		SERVICES_SCHEME + ELEMENT_SCHEME;

	public static final String  SIMPLIFIED_SERVICE_SCHEME =
		SIMPLIFIED_SERVICEPROVIDER_SCHEME + ELEMENT_SCHEME;
	
	public static final String RESOURCES_SCHEME = 
		SERVICE_SCHEME + "\\/resources";
	
	public static final String RESOURCE_SCHEME = 
		RESOURCES_SCHEME + ELEMENT_SCHEME;
	
	public static final String  SIMPLIFIED_RESOURCE_SCHEME =
		SIMPLIFIED_SERVICE_SCHEME + ELEMENT_SCHEME;
	
	public static final String METHOD_SCHEME = 
		RESOURCE_SCHEME + "\\/(GET|SET|ACT|SUBSCRIBE|UNSUBSCRIBE)";

	public static final String  SIMPLIFIED_METHOD_SCHEME =
		SIMPLIFIED_RESOURCE_SCHEME + "\\/(GET|SET|ACT|SUBSCRIBE|UNSUBSCRIBE)";

	private static final Pattern SERVICEPROVIDERS_PATTERN = 
		Pattern.compile(SERVICEPROVIDERS_SCHEME);

	private static final Pattern SERVICEPROVIDER_PATTERN = 
		Pattern.compile(SERVICEPROVIDER_SCHEME);
	
	private static final Pattern SIMPLIFIED_SERVICEPROVIDER_PATTERN = 
		Pattern.compile(SIMPLIFIED_SERVICEPROVIDER_SCHEME);
	
	private static final Pattern SERVICES_PATTERN = 
		Pattern.compile(SERVICES_SCHEME);
	
	private static final Pattern SERVICE_PATTERN = 
		Pattern.compile(SERVICE_SCHEME);

	private static final Pattern SIMPLIFIED_SERVICE_PATTERN = 
		Pattern.compile(SIMPLIFIED_SERVICE_SCHEME);
	
	private static final Pattern RESOURCES_PATTERN = 
		Pattern.compile(RESOURCES_SCHEME);
	
	private static final Pattern RESOURCE_PATTERN = 
		Pattern.compile(RESOURCE_SCHEME);

	private static final Pattern SIMPLIFIED_RESOURCE_PATTERN = 
		Pattern.compile(SIMPLIFIED_RESOURCE_SCHEME);
	
	private static final Pattern METHOD_PATTERN = 
		Pattern.compile(METHOD_SCHEME);
	
	private static final Pattern SIMPLIFIED_METHOD_PATTERN = 
		Pattern.compile(SIMPLIFIED_METHOD_SCHEME);
	
	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//
    
	protected NorthboundMediator mediator = null;
	
	private String serviceProvider = null;
	private String service = null;
	private String resource = null;
	private String attribute = null;
	
	private String content = null;
	private boolean isElementList = false;
	private boolean match = false;
	
	protected AccessMethod.Type method = null;
	private Map<String, List<String>> query;
	
	protected NorthboundEndpoint endpoint;
	private RequestWrapper request;
	
	/**
	 * Constructor
	 */
	public RestAccess()
	{
		this.query = new HashMap<String,List<String>>();
	}

	/**
	 * @param requestURI
	 */
	private void processRequestURI(String requestURI)
	{
		String path = null;
		try
		{
			path = UriUtils.formatUri(URLDecoder.decode(requestURI, "UTF-8"));
			
		} catch (UnsupportedEncodingException e)
		{
			mediator.error(e.getMessage(),e);
		}		
		this.serviceProvider = null;
		this.service = null;
		this.resource = null;	
		this.attribute = null;			
		this.method = null;
		this.match = false;
		
		Matcher matcher = METHOD_PATTERN.matcher(path);
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);
			this.service = matcher.group(2);
			this.resource = matcher.group(3);			
			this.method = AccessMethod.Type.valueOf(matcher.group(4));
			this.match = true;
			return;
		}	   
		matcher = SIMPLIFIED_METHOD_PATTERN.matcher(path);
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);
			this.service = matcher.group(3);
			this.resource = matcher.group(4);			
			this.method = AccessMethod.Type.valueOf(matcher.group(5));
			this.match = true;
			return;
		}	
	    this.method = AccessMethod.Type.valueOf(AccessMethod.DESCRIBE);		    
		matcher = RESOURCE_PATTERN.matcher(path);
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);
			this.service = matcher.group(2);
			this.resource = matcher.group(3);		
			this.match = true;
			return;
		}
		matcher = SIMPLIFIED_RESOURCE_PATTERN.matcher(path);
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);
			this.service = matcher.group(3);
			this.resource = matcher.group(4);		
			this.match = true;
			return;
		}		
		matcher = RESOURCES_PATTERN.matcher(path);		
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);
			this.service = matcher.group(2);		
			this.isElementList = true;
			this.match = true;
			return;
		}		
		matcher = SERVICE_PATTERN.matcher(path);
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);
			this.service = matcher.group(2);
			this.match = true;
			return;
		}		
		matcher = SIMPLIFIED_SERVICE_PATTERN.matcher(path);	
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);
			this.service = matcher.group(3);
			this.match = true;
			return;
		}		
		matcher = SERVICES_PATTERN.matcher(path);
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);
			this.isElementList = true;		
			this.match = true;
			return;
		}
		matcher = SERVICEPROVIDER_PATTERN.matcher(path);
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);		
			this.match = true;
			return;
		}
		matcher = SIMPLIFIED_SERVICEPROVIDER_PATTERN.matcher(path);
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);		
			this.match = true;
			return;
		}		
		matcher = SERVICEPROVIDERS_PATTERN.matcher(path);
		if(matcher.matches())
		{
			this.match = true;		
			this.isElementList = true;
		}	
	}
	
	/**
	 * @param content
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	private Parameter[] processContent(String content)
	throws IOException, JSONException
	{
		this.content = content;
    	JSONArray parameters;
    	try
    	{
    		JSONObject jsonObject = new JSONObject(content);
    		parameters = jsonObject.optJSONArray("parameters");
    		
    	} catch(JSONException e)
    	{    		
    		try
    		{
    			parameters = new JSONArray(content);
    			
    		} catch(JSONException je)
        	{
    			return null;
        	}
    	}
    	int index = 0;
    	int length = parameters==null?0:parameters.length();
    	
        Parameter[] parametersArray = new Parameter[length];

        for(; index < length; index++) 
        {
        	Parameter parameter = null;
            try
			{
            	parameter = new Parameter(
					mediator, parameters.optJSONObject(
							index));
			}
			catch (InvalidValueException e)
			{
        		throw new JSONException(e);
			}
			if("attributeName".equals(parameter.getName()) &&
					String.class == parameter.getType())
			{
        		this.attribute = (String)parameter.getValue();
        		continue;
			}
			parametersArray[index] = parameter;
        }
        return parametersArray;
	}

	/**
	 * @param builder
	 */
	private void processAttribute(NorthboundRequestBuilder<?> builder)
	{
		String attribute = this.attribute;
		if(attribute == null)
		{
			List<String> list = this.query.get("attributeName");						
			if(list != null && !list.isEmpty())
			{
				attribute = list.get(0);
			}
		}
		if(attribute != null)
		{
			builder.withAttribute(attribute);
			
		} else
		{
			builder.withAttribute(DataResource.VALUE);
		}
	}
	
	/**
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public boolean init(RequestWrapper request) throws IOException
	{
		this.mediator = request.getMediator();
		if(this.mediator == null)
		{
			sendError(500, "Unable to process the request");
			return false;
		}
		this.processRequestURI(request.getRequestURI());

		if(!match)
		{
			sendError(404, "Not found");
			return false;
		}

		Authentication<?> authentication = request.getAuthentication();
		try {
			this.endpoint = new NorthboundEndpoint(mediator, authentication);
		} catch (InvalidCredentialException e) {
			sendError(401, "Unauthorized");
			return false;
		}

		this.query = request.getQueryMap();
		this.request = request;
		return true;
	}
	
	/**
	 * @return
	 * @throws IOException
	 */
	public boolean handle() throws IOException 
	{		
		Parameter[] parameters = null;
		try
		{
			parameters = processContent(request.getContent());
			
		} catch(IOException e)
		{
			mediator.error(e.getMessage(),e);
			sendError(500, "Error processing the request content");
			return false;
			
		} catch(JSONException e)
		{
			mediator.error(e.getMessage(),e);
			if(this.content != null && !this.content.isEmpty())
			{
				sendError(400,"Invalid parameter(s) format");
				return false;
			}
		}
		return handle(parameters);
	}
    
    /**
     * @param parameters
     * @return
     * @throws IOException
     */
    private boolean handle(Parameter[] parameters) 
    		throws IOException
	{	    	
		NorthboundRequestBuilder<JSONObject> builder = 
				new NorthboundRequestBuilder<JSONObject>(
						mediator);
		
		builder.withMethod(method
				).withServiceProvider(serviceProvider
				).withService(service
				).withResource(resource);
		
		if(!method.name().equals(AccessMethod.ACT))
		{
			this.processAttribute(builder);
		}
		switch(method.name())
		{
			case "DESCRIBE":
				builder.isElementsList(isElementList);
			case "GET":
				break;
			case "ACT":
				int index = 0;
				int length = parameters==null?
					0:parameters.length;
				
				Object[] arguments = length==0
						?null:new Object[length];
				for(;index < length; index++)
				{
					arguments[index] = parameters[index].getValue();
				}
				builder.withArgument(arguments);
				break;
			case "UNSUBSCRIBE":
				if(parameters == null || parameters.length!=1
				|| parameters[0] == null)
				{
					sendError(400, "A Parameter was expected");
					return false;
				}
				if(parameters[0].getType() != String.class)
				{
					sendError(400, "Invalid parameter format");
					return false;
				}
				builder.withArgument(parameters[0].getValue());
				break;
			case "SET":
				if(parameters == null || parameters.length!=1
						|| parameters[0] == null)
				{
					sendError(400, "A Parameter was expected");
					return false;
				}
				builder.withArgument(parameters[0].getValue());
				break;
			case "SUBSCRIBE":				
				NorthboundRecipient recipient = this.request.createRecipient(parameters);
				if(recipient == null)
				{	
					//still handle Long Polling
					//NorthboundRecipient recipient = new LongPollingCallback()
					sendError(400, "Unable to create the appropriate recipient");
					return false;
				} 
		    	builder.withArgument(recipient);
				break;
			default:
				break;
		}	
		return respond(builder);
	}

	/**
	 * 
	 */
	public void destroy()
	{
		this.query = null;		
		this.endpoint = null;
		this.request = null;
	}
}
