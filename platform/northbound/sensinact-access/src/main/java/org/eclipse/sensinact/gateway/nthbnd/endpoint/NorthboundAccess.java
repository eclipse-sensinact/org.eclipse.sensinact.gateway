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

package org.eclipse.sensinact.gateway.nthbnd.endpoint;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is the interface between each others classes 
 * that perform a task and jersey
 */
public abstract class NorthboundAccess
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
	public static final String PROVIDERS_SCHEME = ROOT + "\\/providers";
	public static final String PROVIDER_SCHEME = PROVIDERS_SCHEME + ELEMENT_SCHEME;
	public static final String SIMPLIFIED_PROVIDER_SCHEME = ROOT + "\\/(([^p]|p[^r]|pr[^o]|pro[^v]|prov[^i]|provi[^d]|provid[^e]|provide[^r]|provider[^s]|providers[^\\/])[^\\/]*)";
	public static final String SERVICES_SCHEME = PROVIDER_SCHEME + "\\/services";
	public static final String SERVICE_SCHEME = SERVICES_SCHEME + ELEMENT_SCHEME;
	public static final String SIMPLIFIED_SERVICE_SCHEME = SIMPLIFIED_PROVIDER_SCHEME + ELEMENT_SCHEME;
	public static final String RESOURCES_SCHEME = SERVICE_SCHEME + "\\/resources";
	public static final String RESOURCE_SCHEME = RESOURCES_SCHEME + ELEMENT_SCHEME;
	public static final String SIMPLIFIED_RESOURCE_SCHEME = SIMPLIFIED_SERVICE_SCHEME + ELEMENT_SCHEME;
	
    //**************************************************************************
	//**************************************************************************
	private static final String METHOD_SCHEME = "\\/(GET|SET|ACT|SUBSCRIBE|UNSUBSCRIBE)";
	
	public static final String  GENERIC_METHOD_SCHEME =
		ROOT + "(" + ELEMENT_SCHEME + ")*" + METHOD_SCHEME;

	public static final String  ROOT_PROPAGATED_METHOD_SCHEME =
		ROOT + METHOD_SCHEME;
	
	public static final String  PROVIDERS_PROPAGATED_METHOD_SCHEME =
		PROVIDERS_SCHEME + METHOD_SCHEME;

	public static final String  PROVIDER_PROPAGATED_METHOD_SCHEME =
		PROVIDER_SCHEME + METHOD_SCHEME;
	
	public static final String  SIMPLIFIED_PROVIDER_PROPAGATED_METHOD_SCHEME =
		SIMPLIFIED_PROVIDER_SCHEME + METHOD_SCHEME;

	public static final String  SERVICE_PROPAGATED_METHOD_SCHEME =
		SERVICE_SCHEME + METHOD_SCHEME;

	public static final String  SIMPLIFIED_SERVICE_PROPAGATED_METHOD_SCHEME =
		SIMPLIFIED_SERVICE_SCHEME + METHOD_SCHEME;

	public static final String RESOURCE_PROPAGATED_METHOD_SCHEME = 
		RESOURCE_SCHEME + METHOD_SCHEME;

	public static final String  SIMPLIFIED_RESOURCE_PROPAGATED_METHOD_SCHEME =
		SIMPLIFIED_RESOURCE_SCHEME + METHOD_SCHEME;
	

    //**************************************************************************
	//**************************************************************************

	private static final Pattern GENERIC_METHOD_SCHEME_PATTERN = 
		Pattern.compile(GENERIC_METHOD_SCHEME);
	
	private static final Pattern PROVIDERS_PATTERN = 
		Pattern.compile(PROVIDERS_SCHEME);

	private static final Pattern PROVIDER_PATTERN = 
		Pattern.compile(PROVIDER_SCHEME);
	
	private static final Pattern SIMPLIFIED_PROVIDER_PATTERN = 
		Pattern.compile(SIMPLIFIED_PROVIDER_SCHEME);
	
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
	
	private static final Pattern RESOURCE_PROPAGATED_METHOD_SCHEME_PATTERN = 
		Pattern.compile(RESOURCE_PROPAGATED_METHOD_SCHEME);
	
	private static final Pattern SIMPLIFIED_RESOURCE_PROPAGATED_METHOD_SCHEME_PATTERN = 
		Pattern.compile(SIMPLIFIED_RESOURCE_PROPAGATED_METHOD_SCHEME);
	
	private static final Pattern ROOT_PROPAGATED_METHOD_SCHEME_PATTERN =
		Pattern.compile(ROOT_PROPAGATED_METHOD_SCHEME);

	private static final Pattern PROVIDERS_PROPAGATED_METHOD_SCHEME_PATTERN =
		Pattern.compile(PROVIDERS_PROPAGATED_METHOD_SCHEME);
	
	private static final Pattern PROVIDER_PROPAGATED_METHOD_SCHEME_PATTERN =
		Pattern.compile(PROVIDER_PROPAGATED_METHOD_SCHEME);
	
	private static final Pattern SIMPLIFIED_PROVIDER_PROPAGATED_METHOD_SCHEME_PATTERN =
		Pattern.compile(SIMPLIFIED_PROVIDER_PROPAGATED_METHOD_SCHEME);

	private static final Pattern SERVICE_PROPAGATED_METHOD_SCHEME_PATTERN =
		Pattern.compile(SERVICE_PROPAGATED_METHOD_SCHEME);

	private static final Pattern SIMPLIFIED_SERVICE_PROPAGATED_METHOD_SCHEME_PATTERN =
		Pattern.compile(SIMPLIFIED_SERVICE_PROPAGATED_METHOD_SCHEME);
		
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
	private boolean multi = false;
	
	protected AccessMethod.Type method = null;
	private Map<String, List<String>> query;
	
	protected NorthboundEndpoint endpoint;
	private NorthboundAccessWrapper request;
	
	/**
	 * Constructor
	 */
	public NorthboundAccess()
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
		this.multi = false;
		this.match = false;
		
		Matcher matcher = GENERIC_METHOD_SCHEME_PATTERN.matcher(path);
		if(matcher.matches())
		{
			matcher = ROOT_PROPAGATED_METHOD_SCHEME_PATTERN.matcher(path);
			if(matcher.matches())
			{		
				this.method = AccessMethod.Type.valueOf(matcher.group(1));
				this.match = true;
				this.multi = true;
				return;
			}
			matcher = PROVIDERS_PROPAGATED_METHOD_SCHEME_PATTERN.matcher(path);
			if(matcher.matches())
			{		
				this.method = AccessMethod.Type.valueOf(matcher.group(1));
				this.match = true;
				this.multi = true;
				return;
			}	
			matcher = PROVIDER_PROPAGATED_METHOD_SCHEME_PATTERN.matcher(path);
			if(matcher.matches())
			{		
				this.serviceProvider = matcher.group(1);
				this.method = AccessMethod.Type.valueOf(matcher.group(2));
				this.match = true;
				this.multi = true;
				return;
			}	
			matcher = SIMPLIFIED_PROVIDER_PROPAGATED_METHOD_SCHEME_PATTERN.matcher(path);
			if(matcher.matches())
			{		
				this.serviceProvider = matcher.group(1);
				this.method = AccessMethod.Type.valueOf(matcher.group(3));
				this.match = true;
				this.multi = true;
				return;
			}
			matcher = SERVICE_PROPAGATED_METHOD_SCHEME_PATTERN.matcher(path);
			if(matcher.matches())
			{		
				this.serviceProvider = matcher.group(1);
				this.service = matcher.group(2);
				this.method = AccessMethod.Type.valueOf(matcher.group(3));
				this.match = true;
				this.multi = true;
				return;
			}	
			matcher = SIMPLIFIED_SERVICE_PROPAGATED_METHOD_SCHEME_PATTERN.matcher(path);
			if(matcher.matches())
			{		
				this.serviceProvider = matcher.group(1);
				this.service = matcher.group(3);
				this.method = AccessMethod.Type.valueOf(matcher.group(4));
				this.match = true;
				this.multi = true;
				return;
			}
			matcher = RESOURCE_PROPAGATED_METHOD_SCHEME_PATTERN.matcher(path);
			if(matcher.matches())
			{
				this.serviceProvider = matcher.group(1);
				this.service = matcher.group(2);
				this.resource = matcher.group(3);			
				this.method = AccessMethod.Type.valueOf(matcher.group(4));
				this.match = true;
				this.multi = false;
				return;
			}	   
			matcher = SIMPLIFIED_RESOURCE_PROPAGATED_METHOD_SCHEME_PATTERN.matcher(path);
			if(matcher.matches())
			{
				this.serviceProvider = matcher.group(1);
				this.service = matcher.group(3);
				this.resource = matcher.group(4);			
				this.method = AccessMethod.Type.valueOf(matcher.group(5));
				this.match = true;
				this.multi = false;
				return;
			}	
		}
	    this.method = AccessMethod.Type.valueOf(AccessMethod.DESCRIBE);		    
		matcher = RESOURCE_PATTERN.matcher(path);
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);
			this.service = matcher.group(2);
			this.resource = matcher.group(3);		
			this.match = true;
			this.multi = false;
			return;
		}
		matcher = SIMPLIFIED_RESOURCE_PATTERN.matcher(path);
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);
			this.service = matcher.group(3);
			this.resource = matcher.group(4);		
			this.match = true;
			this.multi = false;
			return;
		}		
		matcher = RESOURCES_PATTERN.matcher(path);		
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);
			this.service = matcher.group(2);		
			this.isElementList = true;
			this.match = true;
			this.multi = true;
			return;
		}		
		matcher = SERVICE_PATTERN.matcher(path);
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);
			this.service = matcher.group(2);
			this.match = true;
			this.multi = true;
			return;
		}		
		matcher = SIMPLIFIED_SERVICE_PATTERN.matcher(path);	
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);
			this.service = matcher.group(3);
			this.match = true;
			this.multi = true;
			return;
		}		
		matcher = SERVICES_PATTERN.matcher(path);
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);
			this.isElementList = true;		
			this.match = true;
			this.multi = true;
			return;
		}
		matcher = PROVIDER_PATTERN.matcher(path);
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);		
			this.match = true;
			this.multi = true;
			return;
		}
		matcher = SIMPLIFIED_PROVIDER_PATTERN.matcher(path);
		if(matcher.matches())
		{
			this.serviceProvider = matcher.group(1);		
			this.match = true;
			this.multi = true;
			return;
		}		
		matcher = PROVIDERS_PATTERN.matcher(path);
		if(matcher.matches())
		{
			this.match = true;		
			this.isElementList = true;
			this.multi = true;
		}	
	}
	
	/**
	 * @param content
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	private Parameter[] processParameters(String content)
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
    	
        List<Parameter> parametersList = new ArrayList<Parameter>();
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
			parametersList.add(parameter);
        }
        Iterator<Map.Entry<String,List<String>>> iterator = 
        		this.query.entrySet().iterator();
        
        while(iterator.hasNext())
        {
        	Map.Entry<String,List<String>> entry = iterator.next();
        	Parameter parameter = null;
            try
			{
            	parameter = new Parameter(mediator, entry.getKey(), 
					entry.getValue().size()>1?JSONArray.class:String.class,
					entry.getValue().size()==0?"true"
						:(entry.getValue().size()==1?entry.getValue().get(0)
							:new JSONArray(entry.getValue())));
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
			parametersList.add(parameter);
        }
        return parametersList.toArray(new Parameter[0]);
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
	public boolean init(NorthboundAccessWrapper request) throws IOException
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
			parameters = processParameters(request.getContent());
			
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
     * 
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
		
		if(!this.multi && 
		   !this.method.name().equals(AccessMethod.ACT) && 
		   !this.method.name().equals(AccessMethod.DESCRIBE))
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
				int length = parameters==null?0:parameters.length;
				
				Object[] arguments = length==0?null:new Object[length];
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
				NorthboundRecipient recipient = 
				this.request.createRecipient(parameters);
				if(recipient == null)
				{	
					sendError(400, "Unable to create the appropriate recipient");
					return false;
				} 
				index = 0;
				length = parameters==null?0:parameters.length;
				
				String sender = null;
				boolean isPattern = false;
				boolean isComplement = false;
				SnaMessage.Type[] types = null;
				JSONArray conditions = null;
				
				SnaFilter filter = null;
				
				for(;index < length; index++)
				{
					String name = parameters[index].getName();
					switch(name)
					{
						case "conditions":
							conditions = CastUtils.cast(mediator.getClassLoader(),
								JSONArray.class, parameters[index].getValue());
						break;
						case "sender":
							sender = CastUtils.cast(mediator.getClassLoader(),
								String.class, parameters[index].getValue());
						break;
						case "pattern":
							isPattern = CastUtils.cast(mediator.getClassLoader(),
								boolean.class, parameters[index].getValue());
						break;
						case "complement":
							isComplement = CastUtils.cast(mediator.getClassLoader(),
								boolean.class, parameters[index].getValue());
						break;
						case "types":
							types = CastUtils.castArray(mediator.getClassLoader(),
							    SnaMessage.Type[].class, parameters[index].getValue());
						default:;
					}
				}
				if(sender == null)
				{
					sender = "/[^/]+(/[^/]+)*";
				}
				if(types == null)
				{
					types = SnaMessage.Type.values();
				}
				if(conditions == null)
				{
					conditions = new JSONArray();
				}
				filter = new SnaFilter(mediator, sender, isPattern,
						isComplement, conditions);
				filter.addHandledType(types);
				
			    builder.withArgument(new Object[] {recipient, filter});
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
