/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.http.task;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.generic.TaskImpl;
import org.eclipse.sensinact.gateway.generic.TaskTranslator;
import org.eclipse.sensinact.gateway.protocol.http.HeadersCollection;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfiguration;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.ProxyConfiguration;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.MappingDescription;
import org.eclipse.sensinact.gateway.util.ReflectUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@link HttpTask} implementation 
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class HttpTaskImpl<RESPONSE extends HttpResponse, REQUEST extends Request<RESPONSE>> 
extends TaskImpl implements HttpTask<RESPONSE, REQUEST> {
	
    protected Object content;
    protected int readTimeout;
    protected int connectTimeout;
    protected String contentType;
    protected String acceptType;
    protected String uri;
    protected String httpMethod;
	protected String serverCertificate;
	protected String clientCertificate;
	protected String clientCertificatePassword;
    protected ProxyConfiguration proxyConfiguration;
    protected Map<String, String> queries;
    protected HeadersCollection headers;
    protected Class<? extends HttpPacket> packetType;
    protected Class<REQUEST> requestType;
	protected MappingDescription[] mappings;
    protected boolean direct;

    /**
     * Constructor
     *
     * @param mediator       the associated {@link Mediator}
     * @param command        the {@link CommandType} of the task to be
     *                       instantiated
     * @param transmitter    the {@link TaskTranslator} in charge of sending
     *                       the request created by the task to be instantiated
     * @param requestType    the type of {@link Request} to be created by the
     *                       task to be instantiated
     * @param path           String path of the sensiNact model source element of the
     *                       task to be instantiated
     * @param profileId      the string profile identifier of the requirer {@link
     *                       ModelInstance}
     * @param resourceConfig the {@link ResourceConfig} mapped to the resource
     *                       which created the task to be instantiated if it applies
     * @param parameters     the objects array parameterizing the task execution
     */
    public HttpTaskImpl(Mediator mediator, CommandType command, TaskTranslator transmitter, 
    	Class<REQUEST> requestType, String path, String profileId, ResourceConfig resourceConfig, 
    	Object[] parameters) {
    	
        super(mediator, command, transmitter, path, profileId, resourceConfig, 
        		parameters);

        this.requestType = requestType;
        this.queries = new HashMap<String, String>();
        this.headers = new HeadersCollection();
        this.proxyConfiguration = new ProxyConfiguration();
    }

    @Override
    public HttpTaskImpl<RESPONSE, REQUEST> queryParameter(String key, String value) {
        if (key != null && key.length() > 0 && value != null && value.length() > 0) {
            this.queries.put(key, value);
        }
        return this;
    }
    
    @Override
    public HttpTaskImpl<RESPONSE, REQUEST> setPacketType(Class<? extends HttpPacket> packetType) {
        this.packetType = packetType;
        return this;
    }

    @Override
    public Class<? extends HttpPacket> getPacketType() {
        return this.packetType;
    }

	@Override
	public HttpTask<RESPONSE, REQUEST> setMapping(MappingDescription[] mappings) {
		this.mappings = mappings;
		return this;
	}

	@Override
	public MappingDescription[] getMapping() {
		return this.mappings;
	}
	
    @Override
    public HttpTaskImpl<RESPONSE, REQUEST> setUri(String uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public String getUri() {
        if (!this.queries.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder.append(this.uri);

            Iterator<Map.Entry<String, String>> iterator = this.queries.entrySet().iterator();
            int index = 0;
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                builder.append(index == 0 ? "?" : "&");
                builder.append(entry.getKey());
                builder.append("=");
                builder.append(entry.getValue());
                index++;
            }
            return builder.toString();
        }
        return this.uri;
    }

    @Override
    public HttpTaskImpl<RESPONSE, REQUEST> setContent(Object content) {
        this.content = content;
        return this;
    }

    @Override
    public Object getContent() {
        return this.content;
    }

    @Override
    public HttpTaskImpl<RESPONSE, REQUEST> setAccept(String acceptType) {
        this.acceptType = acceptType;
        return this;
    }

    @Override
    public String getAccept() {
        return this.acceptType;
    }

    @Override
    public HttpTaskImpl<RESPONSE, REQUEST> setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public HttpTaskImpl<RESPONSE, REQUEST> setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    @Override
    public String getHttpMethod() {
        if (this.httpMethod == null) {
            return ConnectionConfigurationImpl.DEFAULT_HTTP_METHOD;
        }
        return this.httpMethod;
    }

    @Override
    public HttpTaskImpl<RESPONSE, REQUEST> setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    @Override
    public int getConnectTimeout() {
        if (this.connectTimeout <= 0) {
            return ConnectionConfigurationImpl.DEFAULT_CONNECTION_TIMEOUT;
        }
        return this.connectTimeout;
    }

    @Override
    public HttpTaskImpl<RESPONSE, REQUEST> setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    @Override
    public int getReadTimeout() {
        if (this.readTimeout <= 0) {
            return ConnectionConfigurationImpl.DEFAULT_READ_TIMEOUT;
        }
        return this.readTimeout;
    }

    @Override
    public HttpTaskImpl<RESPONSE, REQUEST> setProxyHost(String proxyHost) {
        this.proxyConfiguration.setProxyHost(proxyHost);
        return this;
    }

    @Override
    public HttpTaskImpl<RESPONSE, REQUEST> setProxyPort(int proxyPort) {
        this.proxyConfiguration.setProxyPort(proxyPort);
        return this;
    }

    @Override
    public Proxy getProxy() {
        return this.proxyConfiguration.getProxy();
    }

    @Override
	public void setServerSSLCertificate(String serverCertificate) {
		this.serverCertificate = serverCertificate;
	}

	@Override
	public String getServerSSLCertificate() {
		return this.serverCertificate;
	}

	@Override
	public void setClientSSLCertificate(String clientCertificate) {
		this.clientCertificate = clientCertificate;
	}
	
	@Override
	public String getClientSSLCertificate() {
		return this.clientCertificate;
	}

	@Override
	public String getClientSSLCertificatePassword() {
		return this.clientCertificatePassword;
	}

	@Override
	public void setClientSSLCertificatePassword(String clientCertificatePassword) {
		this.clientCertificatePassword = clientCertificatePassword;
		
	}
	
    @Override
    public HttpURLConnection connect() throws IOException {
        return ConnectionConfiguration.HttpURLConnectionBuilder.build(this);
    }

    @Override
    public void addHeaders(Map<String, List<String>> headers) {
        this.headers.addHeaders(headers);
    }

    @Override
    public void addHeader(String header, String value) {
        this.headers.addHeader(header, value);
    }

    @Override
    public void addHeader(String header, List<String> values) {
        this.headers.addHeader(header, values);
    }

    @Override
    public List<String> getHeader(String header) {
        return this.headers.getHeader(header);
    }

    @Override
    public String getHeaderAsString(String header) {
        return this.headers.getHeaderAsString(header);
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return this.headers.getHeaders();
    }

    @Override
    public Iterator<String> iterator() {
        return this.headers.iterator();
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.URI;
    }
    
    @Override
    public Map<String, List<String>> getOptions() {
        return this.getHeaders();
    }
    
    @Override
    public HttpTaskImpl<RESPONSE, REQUEST> setDirect(boolean direct) {
        this.direct = direct;
        return this;
    }

    @Override
    public boolean isDirect() {
        return this.direct;
    }

    @Override
    public REQUEST build() {
        return ReflectUtils.getInstance(Request.class, this.requestType, 
        		new Object[]{super.mediator, this});
    }    
}
