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

package org.eclipse.sensinact.gateway.sthbnd.http.task;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.TaskImpl;
import org.eclipse.sensinact.gateway.generic.TaskTranslator;
import org.eclipse.sensinact.gateway.generic.uri.URITask;
import org.eclipse.sensinact.gateway.protocol.http.HeadersCollection;
import org.eclipse.sensinact.gateway.protocol.http.Headers;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfiguration;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.ProxyConfiguration;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpConnectionConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;
import org.eclipse.sensinact.gateway.util.ReflectUtils;

/**
 * Extended {@link URITask} dedicated to HTTP communication
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class HttpTask<RESPONSE extends HttpResponse,
REQUEST extends Request<RESPONSE>> extends TaskImpl 
implements HttpConnectionConfiguration<RESPONSE, REQUEST>, URITask
{			
	protected Object content;
	protected int readTimeout;
	protected int connectTimeout;
	protected String contentType;
	protected String acceptType;
	protected String uri;
	protected String httpMethod;
	protected ProxyConfiguration proxyConfiguration;
	protected Map<String,String> queries;
	protected HeadersCollection headers;
	protected Class<? extends HttpPacket> packetType;
	protected Class<REQUEST> requestType;
	protected boolean direct;
    

    /**
     * Constructor
     * 
     * @param mediator the associated {@link Mediator}
     * @param command the {@link CommandType} of the task to be
     * instantiated
     * @param transmitter the {@link TaskTranslator} in charge of sending 
     * the request created by the task to be instantiated
     * @param requestType the type of {@link Request} to be created by the 
     * task to be instantiated
     * @param path String path of the sensiNact model source element of the
     * task to be instantiated
     * @param profileId the string profile identifier of the requirer {@link
     * ModelInstance}
     * @param resourceConfig the {@link ResourceConfig} mapped to the resource
     * which created the task to be instantiated if it applies
     * @param parameters the objects array parameterizing the task execution
     */
	public HttpTask(Mediator mediator, CommandType command, TaskTranslator 
			transmitter, Class<REQUEST> requestType, String path, String profileId, 
			ResourceConfig resourceConfig, Object[] parameters) 
	{
		super(mediator, command, transmitter, path, profileId,
				resourceConfig, parameters);
		
        this.requestType = requestType; 
        this.queries = new HashMap<String,String>();
        this.headers = new HeadersCollection();
        this.proxyConfiguration = new ProxyConfiguration();
	}

	
	/** 
	 * @inheritDoc
	 * 
	 * @see ConnectionConfiguration#
	 * queryParameter(java.lang.String, java.lang.String)
	 */
	@Override
	public HttpTask<RESPONSE,REQUEST> queryParameter(String key, String value)
	{
		if(key!=null && key.length()>0 
				&& value!=null && value.length()>0)
		{
			this.queries.put(key, value);
		}	
		return this;
	}

	/**
	 * @param packetType
	 * 
	 * @return
	 */
	public HttpTask<RESPONSE,REQUEST> setPacketType(Class<? extends HttpPacket> packetType)
	{
		this.packetType = packetType;
		return this;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see HttpConnectionConfiguration#getPacketType()
	 */
	public Class<? extends HttpPacket> getPacketType()
	{
		return this.packetType;
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see ConnectionConfiguration#setUri(java.lang.String)
	 */
	@Override
	public HttpTask<RESPONSE,REQUEST> setUri(String uri)
	{
		this.uri = uri;
		return this;
	}	

	/** 
	 * @inheritDoc
	 * 
	 * @see ConnectionConfiguration#getUri()
	 */
	@Override
	public String getUri()
	{
		if(!this.queries.isEmpty())
		{
			StringBuilder builder = new StringBuilder();
			builder.append(this.uri);
			
			Iterator<Map.Entry<String,String>> iterator = 
					this.queries.entrySet().iterator();
			int index = 0;
			while(iterator.hasNext())
			{
				Map.Entry<String,String> entry = iterator.next();
				builder.append(index==0?"?":"&");
				builder.append(entry.getKey());
				builder.append("=");
				builder.append(entry.getValue());
				index++;
			}
			return builder.toString();
		}
		return this.uri;
	}	

	/** 
	 * @inheritDoc
	 * 
	 * @see ConnectionConfiguration#setContent(java.lang.Object)
	 */
	@Override
	public HttpTask<RESPONSE,REQUEST> setContent(Object content)
	{
		this.content = content;
		return this;
	}

	/** 
	 * @inheritDoc
	 * 
     * @see ConnectionConfiguration#getContent()
     */
	@Override
    public Object getContent()
    {
	    return this.content;
    }
	
	/** 
	 * @inheritDoc
	 * 
     * @see ConnectionConfiguration#setAccept(java.lang.String)
     */
	@Override
    public HttpTask<RESPONSE,REQUEST> setAccept(String acceptType)
    {
    	this.acceptType = acceptType;
	    return this;
    }
	
	/** 
	 * @inheritDoc
	 * 
     * @see ConnectionConfiguration#getAccept()
     */
	@Override
    public String getAccept()
    {
    	return this.acceptType;
    }

	/** 
	 * @inheritDoc
	 * 
     * @see ConnectionConfiguration#setContentType(java.lang.String)
     */
	@Override
    public HttpTask<RESPONSE,REQUEST> setContentType(String contentType)
    {
    	this.contentType = contentType;
	    return this;
    }

    /**
     * @inheritDoc
     * 
     * @see ConnectionConfiguration#getContentType()
     */
	@Override
    public String getContentType()
    {
    	return this.contentType;
    }   

    /**
     * @inheritDoc
     * 
   	 * @see ConnectionConfiguration#setHttpMethod(java.lang.String)
   	 */
    @Override
   	public HttpTask<RESPONSE,REQUEST> setHttpMethod(String httpMethod)
   	{
   		this.httpMethod = httpMethod;
   		return this;
   	}

    /**
     * @inheritDoc
     * 
   	 * @see ConnectionConfiguration#getHttpMethod()
   	 */
   	@Override
   	public String getHttpMethod()
   	{
   		if(this.httpMethod == null)
   		{
   			return ConnectionConfigurationImpl.DEFAULT_HTTP_METHOD;
   		}
   		return this.httpMethod;
   	}

    /**
     * @inheritDoc
     * 
     * @see ConnectionConfiguration#setConnectTimeout(int)
     */
   	@Override
    public HttpTask<RESPONSE,REQUEST> setConnectTimeout(int connectTimeout)
    {
	    this.connectTimeout = connectTimeout;
	    return this;
    }

    /**
     * @inheritDoc
     * 
     * @see ConnectionConfiguration#getConnectTimeout()
     */
    @Override
    public int getConnectTimeout()
    {
   		if(this.connectTimeout <= 0)
   		{
   			return ConnectionConfigurationImpl.DEFAULT_CONNECTION_TIMEOUT;
   		}
	    return this.connectTimeout;
    }

    /**
     * @inheritDoc
     * 
     * @see ConnectionConfiguration#setReadTimeout(int)
     */
    @Override
    public HttpTask<RESPONSE,REQUEST> setReadTimeout(int readTimeout)
    {
	    this.readTimeout = readTimeout;
	    return this;
    }

    /**
     * @inheritDoc
     * 
     * @see ConnectionConfiguration#getReadTimeout()
     */
    @Override
    public int getReadTimeout()
    {
   		if(this.readTimeout <= 0)
   		{
   			return ConnectionConfigurationImpl.DEFAULT_READ_TIMEOUT;
   		}
	    return this.readTimeout;
    }  
    
    /**
     * @inheritDoc
     * 
	 * @see ConnectionConfiguration#setProxyHost(java.lang.String)
	 */
    @Override
	public HttpTask<RESPONSE,REQUEST> setProxyHost(String proxyHost)
	{
		this.proxyConfiguration.setProxyHost(proxyHost);
		return this;
	}

    /**
     * @inheritDoc
     * 
	 * @see ConnectionConfiguration#setProxyPort(int)
	 */
	@Override
	public HttpTask<RESPONSE,REQUEST> setProxyPort(int proxyPort)
	{
		this.proxyConfiguration.setProxyPort(proxyPort);
		return this;
	}
	
	/**
	 * Returns a new {@link Proxy} instance using
	 * the retrieved proxy's host and port 
	 * configuration
	 * 
	 * @return
	 * 		a new {@link Proxy} instance 
	 */
	@Override
	public Proxy getProxy()
	{
		return this.proxyConfiguration.getProxy();
	}

	/** 
	 * @inheritDoc
	 *
	 * @see ConnectionConfiguration#connect()
	 */
    @Override
    public HttpURLConnection connect() throws IOException
    {
    	return ConnectionConfiguration.HttpURLConnectionBuilder.build(this);
    }

	/**
	 * @inheritDoc
	 *
	 * @see Headers#addHeaders(java.util.Map)
	 */
	@Override
	public void addHeaders(Map<String, List<String>> headers)
	{
		this.headers.addHeaders(headers);
	}

	/**
	 * @inheritDoc
	 *
	 * @see Headers#addHeader(java.lang.String, java.lang.String)
	 */
	@Override
	public void addHeader(String header, String value)
	{
		this.headers.addHeader(header, value);
	}

	/**
	 * @inheritDoc
	 *
	 * @see Headers#addHeader(java.lang.String, java.util.List)
	 */
	@Override
	public void addHeader(String header, List<String> values)
	{
		this.headers.addHeader(header, values);
	}

	/**
	 * @inheritDoc
	 *
	 * @see Headers#getHeader(java.lang.String)
	 */
	@Override
	public List<String> getHeader(String header)
	{
		return this.headers.getHeader(header);
	}

	/**
	 * @inheritDoc
	 *
	 * @see Headers#getHeaderAsString(java.lang.String)
	 */
	@Override
	public String getHeaderAsString(String header)
	{
		return this.headers.getHeaderAsString(header);
	}

	/**
	 * @inheritDoc
	 *
	 * @see Headers#getHeaders()
	 */
	@Override
	public Map<String, List<String>> getHeaders() 
	{		
		return this.headers.getHeaders();
	}

	/**
	 * @inheritDoc
	 *
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<String> iterator() {
		
		return this.headers.iterator();
	}

	/**
	 * @inheritDoc
	 *
     * @see Task#getRequestType()
     */
    @Override
    public RequestType getRequestType()
    {
	    return RequestType.URI;
    }
    
    /**
     * @return
     */
    public REQUEST build()
    {
    	return ReflectUtils.getInstance(Request.class, 
    		this.requestType, new Object[]{super.mediator, 
    				this});
    }

	/**
	 * @inheritDoc
	 *
	 * @see URITask#getOptions()
	 */
	@Override
	public Map<String, List<String>> getOptions() 
	{
		return this.getHeaders();
	}

	/**
	 * 
	 * @param direct
	 * @return
	 */
	public HttpTask<RESPONSE,REQUEST> setDirect(boolean direct)
	{
		this.direct = direct;
		return this;
	}
	
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.generic.TaskImpl#isDirect()
	 */
	@Override
	public boolean isDirect()
	{
		return this.direct;
	}
}
