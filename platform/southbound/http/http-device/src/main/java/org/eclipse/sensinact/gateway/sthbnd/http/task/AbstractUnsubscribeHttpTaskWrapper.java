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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.AbstractUnsubscribeTaskWrapper;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.UnsubscribeTaskWrapper;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfiguration;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;
import org.eclipse.sensinact.gateway.util.ReflectUtils;

/**
 *
 */
public abstract class AbstractUnsubscribeHttpTaskWrapper<RESPONSE extends HttpResponse, REQUEST extends Request<RESPONSE>> 
extends AbstractUnsubscribeTaskWrapper implements HttpTask<RESPONSE,REQUEST>, UnsubscribeTaskWrapper {
	
	/**
	 * Constructor
	 * 
	 * @param task the {@link Task} subscribe access method
	 */
	protected AbstractUnsubscribeHttpTaskWrapper(Mediator mediator, HttpTask<RESPONSE,REQUEST> task, ProtocolStackEndpoint<?> endpoint) {
		super(mediator, task, endpoint);
	}

	@Override
    public HttpTask<RESPONSE, REQUEST> setDirect(boolean direct) {
		((HttpTask<?,?>)task).setDirect(direct);
        return this;
    }
	@Override
	 public HttpTask<RESPONSE, REQUEST> setPacketType(Class<? extends HttpPacket> packetType){
		 ((HttpTask<?,?>)task).setPacketType(packetType);
		 return this;
	 }
	
	@Override
	public HttpTask<RESPONSE, REQUEST> queryParameter(String key, String value) {
		((HttpTask<?,?>)task).queryParameter(key, value) ;
		return this;
	}

	@Override
	public String getServerSSLCertificate() {
		return ((HttpTask<?,?>)task).getServerSSLCertificate();
	}

	@Override
	public void setServerSSLCertificate(String serverCertificate) {
		((HttpTask<?,?>)task).setServerSSLCertificate(serverCertificate);
	}

	@Override
	public String getClientSSLCertificate() {
		return ((HttpTask<?,?>)task).getClientSSLCertificate();
	}

	@Override
	public void setClientSSLCertificate(String clientCertificate) {
		((HttpTask<?,?>)task).setClientSSLCertificate(clientCertificate);
	}

	@Override
	public HttpTask<RESPONSE, REQUEST> setUri(String uri) {
		((HttpTask<?,?>)task).setUri(uri);
		return this;
	}

	@Override
	public String getUri() {
		return ((HttpTask<?,?>)task).getUri();
	}

	@Override
	public HttpTask<RESPONSE, REQUEST> setContent(Object content) {
		((HttpTask<?,?>)task).setContent(content);
		return this;
	}

	@Override
	public Object getContent() {
		return ((HttpTask<?,?>)task).getContent();
	}

	@Override
	public HttpTask<RESPONSE, REQUEST> setAccept(String acceptType) {
		((HttpTask<?,?>)task).setAccept(acceptType);
		return this;
	}

	@Override
	public String getAccept() {
		return ((HttpTask<?,?>)task).getAccept();
	}

	@Override
	public HttpTask<RESPONSE, REQUEST> setContentType(String contentType) {
		((HttpTask<?,?>)task).setContentType(contentType);
		return this;
	}

	@Override
	public String getContentType() {
		return ((HttpTask<?,?>)task).getContentType();
	}

	@Override
	public HttpTask<RESPONSE, REQUEST> setHttpMethod(String httpMethod) {
		((HttpTask<?,?>)task).setHttpMethod(httpMethod);
		return this;
	}

	@Override
	public String getHttpMethod() {
		return ((HttpTask<?,?>)task).getHttpMethod();
	}

	@Override
	public HttpTask<RESPONSE, REQUEST> setConnectTimeout(int connectTimeout) {
		((HttpTask<?,?>)task).setConnectTimeout(connectTimeout);
		return this;
	}

	@Override
	public int getConnectTimeout() {
		return ((HttpTask<?,?>)task).getConnectTimeout();
	}

	@Override
	public HttpTask<RESPONSE, REQUEST> setReadTimeout(int readTimeout) {
		((HttpTask<?,?>)task).setReadTimeout(readTimeout);
		return this;
	}

	@Override
	public int getReadTimeout() {
		return ((HttpTask<?,?>)task).getReadTimeout();
	}

	@Override
	public HttpTask<RESPONSE, REQUEST> setProxyHost(String proxyHost) {
		((HttpTask<?,?>)task).setProxyHost(proxyHost);
		return this;
	}

	@Override
	public HttpTask<RESPONSE, REQUEST> setProxyPort(int proxyPort) {
		((HttpTask<?,?>)task).setProxyPort(proxyPort);
		return this;
	}

	@Override
	public Proxy getProxy() {
		return ((HttpTask<?,?>)task).getProxy();
	}

    @Override
    public HttpURLConnection connect() throws IOException {
        return ConnectionConfiguration.HttpURLConnectionBuilder.build(this);
    }

	@Override
	public void addHeaders(Map<String, List<String>> headers) {
		((HttpTask<?,?>)task).addHeaders(headers);
	}

	@Override
	public void addHeader(String header, String value) {
		((HttpTask<?,?>)task).addHeader(header, value);
	}

	@Override
	public void addHeader(String header, List<String> values) {
		((HttpTask<?,?>)task).addHeader(header, values);
	}

	@Override
	public List<String> getHeader(String header) {
		return ((HttpTask<?,?>)task).getHeader(header);
	}

	@Override
	public String getHeaderAsString(String header) {
		return ((HttpTask<?,?>)task).getHeaderAsString(header);
	}

	@Override
	public Map<String, List<String>> getHeaders() {
		return ((HttpTask<?,?>)task).getHeaders();
	}

	@Override
	public Iterator<String> iterator() {
		return ((HttpTask<?,?>)task).iterator();
	}

	@Override
	public Map<String, List<String>> getOptions() {
		return ((HttpTask<?,?>)task).getOptions();
	}

	@Override
	public Class<? extends HttpPacket> getPacketType() {
		return ((HttpTask<?,?>)task).getPacketType();
	}

	@Override
    public REQUEST build() {
	    return ReflectUtils.getInstance(Request.class,((HttpTaskImpl<RESPONSE,REQUEST>)task
	    		).requestType, new Object[]{super.mediator, this});
    }
}
