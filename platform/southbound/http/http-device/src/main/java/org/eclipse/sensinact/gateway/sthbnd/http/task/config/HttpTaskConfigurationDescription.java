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

import java.util.Collections;
import java.util.List;

import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTaskConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HttpTaskConfigurationDescription {
	
	public static  HttpTaskConfigurationDescription toDescription(HttpTaskConfiguration httpTaskConfiguration) {
    	HttpTaskConfigurationDescription description = new HttpTaskConfigurationDescription();
    	description.setAcceptType(httpTaskConfiguration.acceptType());
    	description.setClientSSLCertificate(httpTaskConfiguration.clientSSLCertificate());
    	description.setClientSSLCertificatePassword(httpTaskConfiguration.clientSSLCertificatePassword());
    	description.setContent(httpTaskConfiguration.content());
    	description.setContentType(httpTaskConfiguration.contentType());
    	description.setDirect(httpTaskConfiguration.direct());
    	description.setHeaders(KeyValuePairDescription.toDescription(httpTaskConfiguration.headers()));
    	description.setHost(httpTaskConfiguration.host());
    	description.setHttpMethod(httpTaskConfiguration.httpMethod());
    	description.setPacket(httpTaskConfiguration.packet());
    	description.setPath(httpTaskConfiguration.path());
    	description.setPort(httpTaskConfiguration.port());
    	description.setQuery(KeyValuePairDescription.toDescription(httpTaskConfiguration.query()));
    	description.setScheme(httpTaskConfiguration.scheme());
    	description.setServerSSLCertificate(httpTaskConfiguration.serverSSLCertificate());
    	description.setConnectTimeout(httpTaskConfiguration.connectTimeout());
    	description.setReadTimeout(httpTaskConfiguration.readTimeout());
    	return description;
    }
	
	public static final String NO_CERTIFICATE = "#NO_CERTIFICATE#";
    public static final String DEFAULT_CLIENT_SSL_CERTIFICATE = NO_CERTIFICATE;
    public static final String DEFAULT_CLIENT_SSL_CERTIFICATE_PASSWORD = NO_CERTIFICATE;
    public static final String DEFAULT_SERVER_SSL_CERTIFICATE = NO_CERTIFICATE;
    public static final String DEFAULT_ACCEPT_TYPE = "text/plain";
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";
    public static final String DEFAULT_SCHEME = "http";
    public static final String DEFAULT_HTTP_METHOD = "GET";
    public static final String DEFAULT_PORT = "80";
    public static final String DEFAULT_PATH = "/";
    public static final int DEFAULT_CONNECTION_TIMEOUT = 300000;
    public static final int DEFAULT_READ_TIMEOUT = 300000;
    public static final NestedMappingDescription[] DEFAULT_NESTED_MAPPING = new NestedMappingDescription[0];
    public static final RootMappingDescription[] DEFAULT_ROOT_MAPPING = new RootMappingDescription[0];

	@JsonProperty(value="acceptType")
	protected String acceptType;

	@JsonProperty(value="contentType")
	protected String contentType;

	@JsonProperty(value="httpMethod")
	protected String httpMethod;

	@JsonProperty(value="scheme")
	protected String scheme;

	@JsonProperty(value="host",required=true)
	protected String host;

	@JsonProperty(value="port")
	protected String port;

	@JsonProperty(value="path")
	protected String path;

	@JsonProperty(value="direct")
	protected boolean direct;

	@JsonProperty(value="clientSSLCertificate")
	protected String clientSSLCertificate;

	@JsonProperty(value="clientSSLCertificatePassword")
	protected String clientSSLCertificatePassword;

	@JsonProperty(value="serverSSLCertificate")
	protected String serverSSLCertificate;

	@JsonProperty(value="query")
	protected List<KeyValuePairDescription> query;

	@JsonProperty(value="headers")
	protected List<KeyValuePairDescription> headers;

	@JsonProperty(value="content")
	protected Class<? extends HttpTaskConfigurator> content;

	@JsonProperty(value="packet")
	protected Class<? extends HttpPacket> packet;

	@JsonProperty(value="readTimeout")
	protected int readTimeout;

	@JsonProperty(value="connectTimeout")
    protected int connectTimeout;

	@JsonProperty(value="nestedMapping")
	protected NestedMappingDescription[] nestedMapping;

	@JsonProperty(value="rootMapping")
	protected RootMappingDescription[] rootMapping;
	
	public HttpTaskConfigurationDescription() {}
	
	public HttpTaskConfigurationDescription(String acceptType, String contentType, String httpMethod, 
		String scheme, String host, String port, String path, boolean direct, String clientSSLCertificate, 
		String clientSSLCertificatePassword, String serverSSLCertificate, 
		List<KeyValuePairDescription> query, List<KeyValuePairDescription> headers, 
		Class<? extends HttpTaskConfigurator> content, Class<? extends HttpPacket> packet,
		int readTimeout, int connectTimeout, RootMappingDescription[] rootMapping, 
		NestedMappingDescription[] nestedMapping) {
		
		this.acceptType=acceptType;
		this.contentType=contentType; 
		this.httpMethod=httpMethod; 
		this.scheme=scheme;
		this.host=host;
		this.port=port;
		this.path=path; 
		this.direct=direct; 
		this.clientSSLCertificate=clientSSLCertificate; 
		this.clientSSLCertificatePassword=clientSSLCertificatePassword;
		this.serverSSLCertificate=serverSSLCertificate; 
		if(query!=null)
			this.query = Collections.unmodifiableList(query); 
		if(headers != null)
			this.headers = Collections.unmodifiableList(headers); 
		this.content = content;
		this.packet = packet;
		this.readTimeout = readTimeout;
		this.connectTimeout = connectTimeout;
		this.nestedMapping = nestedMapping;
		this.rootMapping = rootMapping;
	}

	/**
	 * @return the acceptType
	 */
	public String getAcceptType() {
		if(acceptType == null)
			return DEFAULT_ACCEPT_TYPE;
		return acceptType;
	}

	/**
	 * @param acceptType the acceptType to set
	 */
	public void setAcceptType(String acceptType) {
		this.acceptType = acceptType;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		if(contentType == null)
			return DEFAULT_CONTENT_TYPE;
		return contentType;
	}

	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @return the httpMethod
	 */
	public String getHttpMethod() {
		if(httpMethod == null)
			return DEFAULT_HTTP_METHOD;
		return httpMethod;
	}

	/**
	 * @param httpMethod the httpMethod to set
	 */
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	/**
	 * @return the scheme
	 */
	public String getScheme() {
		if(scheme == null)
			return DEFAULT_SCHEME;
		return scheme;
	}

	/**
	 * @param scheme the scheme to set
	 */
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		if(port == null)
			return DEFAULT_PORT;
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		if(path == null)
			return DEFAULT_PATH;
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the direct
	 */
	public boolean isDirect() {
		return direct;
	}

	/**
	 * @param direct the direct to set
	 */
	public void setDirect(boolean direct) {
		this.direct = direct;
	}

	/**
	 * @return the clientSSLCertificate
	 */
	public String getClientSSLCertificate() {
		if(clientSSLCertificate == null)
			return DEFAULT_CLIENT_SSL_CERTIFICATE;
		return clientSSLCertificate;
	}

	/**
	 * @param clientSSLCertificate the clientSSLCertificate to set
	 */
	public void setClientSSLCertificate(String clientSSLCertificate) {
		this.clientSSLCertificate = clientSSLCertificate;
	}

	/**
	 * @return the clientSSLCertificatePassword
	 */
	public String getClientSSLCertificatePassword() {
		if(clientSSLCertificatePassword == null)
			return DEFAULT_CLIENT_SSL_CERTIFICATE_PASSWORD;
		return clientSSLCertificatePassword;
	}

	/**
	 * @param clientSSLCertificatePassword the clientSSLCertificatePassword to set
	 */
	public void setClientSSLCertificatePassword(String clientSSLCertificatePassword) {
		this.clientSSLCertificatePassword = clientSSLCertificatePassword;
	}

	/**
	 * @return the serverSSLCertificate
	 */
	public String getServerSSLCertificate() {
		if(serverSSLCertificate == null)
			return DEFAULT_SERVER_SSL_CERTIFICATE;
		return serverSSLCertificate;
	}

	/**
	 * @param serverSSLCertificate the serverSSLCertificate to set
	 */
	public void setServerSSLCertificate(String serverSSLCertificate) {
		this.serverSSLCertificate = serverSSLCertificate;
	}

	/**
	 * @return the query
	 */
	public List<KeyValuePairDescription> getQuery() {
		if(this.query == null)
			return Collections.<KeyValuePairDescription>emptyList();
		return query;
	}

	/**
	 * @param query the query to set
	 */
	public void setQuery(List<KeyValuePairDescription> query) {
		if(query != null)
			this.query = Collections.unmodifiableList(query);
	}

	/**
	 * @return the headers
	 */
	public List<KeyValuePairDescription> getHeaders() {
		if(this.headers == null)
			return Collections.<KeyValuePairDescription>emptyList();
		return headers;
	}

	/**
	 * @param headers the headers to set
	 */
	public void setHeaders(List<KeyValuePairDescription> headers) {
		if(headers != null)
			this.headers = Collections.unmodifiableList(headers);
	}

	/**
	 * @return the content
	 */
	public Class<? extends HttpTaskConfigurator> getContent() {
		if(this.content == null)
			return HttpTaskConfigurator.class;
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(Class<? extends HttpTaskConfigurator> content) {
		this.content = content;
	}

	/**
	 * @return the packet
	 */
	public Class<? extends HttpPacket> getPacket() {
		if(packet == null)
			return HttpPacket.class;
		return packet;
	}

	/**
	 * @param packet the packet to set
	 */
	public void setPacket(Class<? extends HttpPacket> packet) {
		this.packet = packet;
	}

	/**
	 * @return the readTimeout
	 */
	public int getReadTimeout() {
		if(this.readTimeout == 0 )
			return HttpTaskConfigurationDescription.DEFAULT_READ_TIMEOUT;
		return readTimeout;
	}

	/**
	 * @param readTimeout the readTimeout to set
	 */
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	/**
	 * @return the connectTimeout
	 */
	public int getConnectTimeout() {
		if(this.connectTimeout == 0 )
			return HttpTaskConfigurationDescription.DEFAULT_CONNECTION_TIMEOUT;
		return connectTimeout;
	}

	/**
	 * @param connectTimeout the connectTimeout to set
	 */
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	/**
	 * @return the nestedMapping
	 */
	public NestedMappingDescription[] getNestedMapping() {
		if(this.nestedMapping == null)
			return DEFAULT_NESTED_MAPPING;
		return this.nestedMapping;
	}

	/**
	 * @param nestedMapping the {@link NestedMappingDescription} to set
	 */
	public void setNestedMapping(NestedMappingDescription[] nestedMapping) {
		this.nestedMapping = nestedMapping;
	}

	/**
	 * @return the rootMapping
	 */
	public RootMappingDescription[] getRootMapping() {
		if(this.rootMapping == null)
			return DEFAULT_ROOT_MAPPING;
		return this.rootMapping;
	}

	/**
	 * @param rootMapping the {@link RootMappingDescription} to set
	 */
	public void setMapping(RootMappingDescription[] rootMapping) {
		this.rootMapping = rootMapping;
	}
}
