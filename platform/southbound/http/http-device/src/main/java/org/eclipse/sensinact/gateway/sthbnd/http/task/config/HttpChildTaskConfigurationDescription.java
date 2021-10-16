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
/**
 *
 */
package org.eclipse.sensinact.gateway.sthbnd.http.task.config;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpChildTaskConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HttpChildTaskConfigurationDescription extends HttpTaskConfigurationDescription{

    public static List<HttpChildTaskConfigurationDescription> toDescription(HttpChildTaskConfiguration[] httpChildTaskConfigurations) {
    	List<HttpChildTaskConfigurationDescription> description = new ArrayList<>();
    	if(httpChildTaskConfigurations == null ||httpChildTaskConfigurations.length == 0)
    		return description;
    	for(HttpChildTaskConfiguration config : httpChildTaskConfigurations)
    		description.add(toDescription(config));
    	return description;
    }

    public static HttpChildTaskConfigurationDescription toDescription(HttpChildTaskConfiguration httpChildTaskConfiguration) {
    	HttpChildTaskConfigurationDescription description = new HttpChildTaskConfigurationDescription();
    	description.setAcceptType(httpChildTaskConfiguration.acceptType());
    	description.setClientSSLCertificate(httpChildTaskConfiguration.clientSSLCertificate());
    	description.setClientSSLCertificatePassword(httpChildTaskConfiguration.clientSSLCertificatePassword());
    	description.setContent(httpChildTaskConfiguration.content());
    	description.setContentType(httpChildTaskConfiguration.contentType());
    	description.setDirect(httpChildTaskConfiguration.direct());
    	description.setHeaders(KeyValuePairDescription.toDescription(httpChildTaskConfiguration.headers()));
    	description.setHost(httpChildTaskConfiguration.host());
    	description.setHttpMethod(httpChildTaskConfiguration.httpMethod());
    	description.setPacket(httpChildTaskConfiguration.packet());
    	description.setPath(httpChildTaskConfiguration.path());
    	description.setPort(httpChildTaskConfiguration.port());
    	description.setQuery(KeyValuePairDescription.toDescription(httpChildTaskConfiguration.query()));
    	description.setScheme(httpChildTaskConfiguration.scheme());
    	description.setServerSSLCertificate(httpChildTaskConfiguration.serverSSLCertificate());
    	description.setIdentifier(httpChildTaskConfiguration.identifier());
    	description.setConnectTimeout(httpChildTaskConfiguration.connectTimeout());
    	description.setReadTimeout(httpChildTaskConfiguration.readTimeout());
    	return description;
    }
    
    public static final String DEFAULT_ACCEPT_TYPE = "#EMPTY#";
    public static final String DEFAULT_CONTENT_TYPE = "#EMPTY#";
    public static final String DEFAULT_SCHEME = "#EMPTY#";
    public static final String DEFAULT_HTTP_METHOD = "#EMPTY#";
    public static final String DEFAULT_PORT = "#EMPTY#";
    public static final String DEFAULT_PATH = "#EMPTY#";
    public static final String DEFAULT_HOST = "#EMPTY#";
    public static final int DEFAULT_CONNECTION_TIMEOUT = -1;
    public static final int DEFAULT_READ_TIMEOUT = -1;
    public static final NestedMappingDescription[] DEFAULT_NESTED_MAPPING = new NestedMappingDescription[0];
    public static final RootMappingDescription[] DEFAULT_ROOT_MAPPING = new RootMappingDescription[0];

	@JsonProperty(value="identifier")
    private String identifier;
    
    public HttpChildTaskConfigurationDescription() {
    	super();
    }
    
    public HttpChildTaskConfigurationDescription(String acceptType, String contentType, String httpMethod, 
		String scheme, String host, String  port, String path, boolean direct, String clientSSLCertificate, 
		String clientSSLCertificatePassword, String serverSSLCertificate, 
		List<KeyValuePairDescription> query, List<KeyValuePairDescription> headers, 
		Class<? extends HttpTaskConfigurator> content,Class<? extends HttpPacket> packet,
		int readTimeout, int connectTimeout, String identifier, RootMappingDescription[] rootMapping, 
		NestedMappingDescription[] nestedMapping) {
    	
    	super(acceptType,contentType, httpMethod, scheme, host, port, path, direct, clientSSLCertificate, 
		clientSSLCertificatePassword, serverSSLCertificate, query, headers, content, packet, readTimeout,
		connectTimeout, rootMapping, nestedMapping);
    	this.identifier = identifier;
    }

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getAcceptType() {
		if(acceptType == null)
			return DEFAULT_ACCEPT_TYPE;
		return acceptType;
	}

	@Override
	public String getContentType() {
		if(contentType == null)
			return DEFAULT_CONTENT_TYPE;
		return contentType;
	}


	@Override
	public String getHttpMethod() {
		if(httpMethod == null)
			return DEFAULT_HTTP_METHOD;
		return httpMethod;
	}

	@Override
	public String getScheme() {
		if(scheme == null)
			return DEFAULT_SCHEME;
		return scheme;
	}

	
	@Override
	public String getHost() {
		if(host == null)
			return DEFAULT_HOST;
		return host;
	}
	
	@Override
	public String getPort() {
		if(port == null)
			return DEFAULT_PORT;
		return port;
	}

	@Override
	public String getPath() {
		if(path == null)
			return DEFAULT_PATH;
		return path;
	}

	@Override
	public int getReadTimeout() {
		if(readTimeout == 0)
			return DEFAULT_READ_TIMEOUT;
		return readTimeout;
	}

    @Override
	public int getConnectTimeout() {
		if(connectTimeout == 0)
			return DEFAULT_CONNECTION_TIMEOUT;
		return connectTimeout;
	}

	@Override
	public NestedMappingDescription[] getNestedMapping() {
		if(nestedMapping == null)
			return DEFAULT_NESTED_MAPPING;
		return nestedMapping;
	}

	@Override
	public RootMappingDescription[] getRootMapping() {
		if(rootMapping == null)
			return DEFAULT_ROOT_MAPPING;
		return rootMapping;
	}
}
