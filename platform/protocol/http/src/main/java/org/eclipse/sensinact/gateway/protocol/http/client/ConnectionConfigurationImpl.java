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
package org.eclipse.sensinact.gateway.protocol.http.client;

import org.eclipse.sensinact.gateway.protocol.http.HeadersCollection;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A Request configuration data structure
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ConnectionConfigurationImpl<RESPONSE extends Response, REQUEST extends Request<RESPONSE>> extends HeadersCollection implements ConnectionConfiguration<RESPONSE, REQUEST> {

    protected Object content;
    protected int readTimeout;
    protected int connectTimeout;
    protected String contentType;
    protected String acceptType;
    protected String uri;
    protected String httpMethod;
    protected ProxyConfiguration proxyConfiguration;
    protected Map<String, String> parameters;
	private String serverCertificate;
	private String clientCertificate;

    /**
     * Constructor
     */
    public ConnectionConfigurationImpl() {
        super();
        this.readTimeout = -1;
        this.connectTimeout = -1;
        this.parameters = new HashMap<String, String>();
        this.proxyConfiguration = new ProxyConfiguration();
    }

    /**
     * Constructor
     *
     * @param configuration JSON formated string describing the
     *                      ConnectionConfiguration to instantiate
     */
    public ConnectionConfigurationImpl(String configuration) {
        this();
        JSONObject jsonConfiguration = new JSONObject(configuration);

        this.uri = (String) jsonConfiguration.opt("uri");
        this.httpMethod = (String) jsonConfiguration.opt("httpMethod");

        this.content = jsonConfiguration.opt("content");

        this.acceptType = (String) jsonConfiguration.opt("acceptType");
        this.contentType = (String) jsonConfiguration.opt("contentType");

        Integer timeout = (Integer) jsonConfiguration.opt("connectTimeout");
        this.connectTimeout = timeout != null ? timeout.intValue() : -1;

        timeout = (Integer) jsonConfiguration.opt("readTimeout");
        this.readTimeout = timeout != null ? timeout.intValue() : -1;

        JSONArray params = jsonConfiguration.optJSONArray("parameters");
        int index = 0;
        int length = params == null ? 0 : params.length();

        for (; index < length; index++) {
            JSONObject object = params.optJSONObject(index);
            if (!JSONObject.NULL.equals(object)) {
                queryParameter(object.optString("key"), object.optString("value"));
            }
        }
    }

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#queryParameter(java.lang.String, java.lang.String)
     */
    @Override
    public ConnectionConfigurationImpl<RESPONSE, REQUEST> queryParameter(String key, String value) {
        if (key != null && key.length() > 0 && value != null) {
            this.parameters.put(key, value);
        }
        return this;
    }

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#setUri(java.lang.String)
     */
    @Override
    public ConnectionConfigurationImpl<RESPONSE, REQUEST> setUri(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#getUri()
     */
    @Override
    public String getUri() {
        if (!this.parameters.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder.append(this.uri);

            Iterator<Map.Entry<String, String>> iterator = this.parameters.entrySet().iterator();
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

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#setContent(java.lang.Object)
     */
    @Override
    public ConnectionConfigurationImpl<RESPONSE, REQUEST> setContent(Object content) {
        this.content = content;
        return this;
    }

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#getContent()
     */
    @Override
    public Object getContent() {
        return this.content;
    }

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#setAccept(java.lang.String)
     */
    @Override
    public ConnectionConfigurationImpl<RESPONSE, REQUEST> setAccept(String acceptType) {
        this.acceptType = acceptType;
        return this;
    }

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#getAccept()
     */
    @Override
    public String getAccept() {
        return this.acceptType;
    }

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#setContentType(java.lang.String)
     */
    @Override
    public ConnectionConfigurationImpl<RESPONSE, REQUEST> setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#getContentType()
     */
    @Override
    public String getContentType() {
        return this.contentType;
    }

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#setHttpMethod(java.lang.String)
     */
    @Override
    public ConnectionConfigurationImpl<RESPONSE, REQUEST> setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#getHttpMethod()
     */
    @Override
    public String getHttpMethod() {
        if (this.httpMethod == null) {
            return ConnectionConfiguration.DEFAULT_HTTP_METHOD;
        }
        return this.httpMethod;
    }

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#setConnectTimeout(int)
     */
    @Override
    public ConnectionConfigurationImpl<RESPONSE, REQUEST> setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#getConnectTimeout()
     */
    @Override
    public int getConnectTimeout() {
        if (this.connectTimeout <= 0) {
            return ConnectionConfiguration.DEFAULT_CONNECTION_TIMEOUT;
        }
        return this.connectTimeout;
    }

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#setReadTimeout(int)
     */
    @Override
    public ConnectionConfigurationImpl<RESPONSE, REQUEST> setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#getReadTimeout()
     */
    @Override
    public int getReadTimeout() {
        if (this.readTimeout <= 0) {
            return ConnectionConfiguration.DEFAULT_READ_TIMEOUT;
        }
        return this.readTimeout;
    }

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#setProxyHost(java.lang.String)
     */
    @Override
    public ConnectionConfigurationImpl<RESPONSE, REQUEST> setProxyHost(String proxyHost) {
        this.proxyConfiguration.setProxyHost(proxyHost);
        return this;
    }

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#setProxyPort(int)
     */
    @Override
    public ConnectionConfigurationImpl<RESPONSE, REQUEST> setProxyPort(int proxyPort) {
        this.proxyConfiguration.setProxyPort(proxyPort);
        return this;
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


    /**
     * Returns a new {@link Proxy} instance using
     * the retrieved proxy's host and port
     * configuration
     *
     * @return a new {@link Proxy} instance
     */
    @Override
    public Proxy getProxy() {
        return this.proxyConfiguration.getProxy();
    }

    /**
     * @inheritDoc
     * @see ConnectionConfiguration#connect()
     */
    @Override
    public HttpURLConnection connect() throws IOException {
        return ConnectionConfiguration.HttpURLConnectionBuilder.build(this);
    }

    /**
     * @inheritDoc
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HTTP ");
        builder.append(this.getHttpMethod());
        builder.append(" Request [");
        builder.append(this.getUri());
        builder.append("]");
        builder.append("\n\tContent-Type:");
        builder.append(this.getContentType());
        builder.append("\n\tAccept:");
        builder.append(this.getAccept());
        builder.append("\n\tConnection Timeout:");
        builder.append(this.getConnectTimeout());
        builder.append("\n\tRead Timeout:");
        builder.append(this.getReadTimeout());
        builder.append(super.toString());
        return builder.toString();
    }
}
