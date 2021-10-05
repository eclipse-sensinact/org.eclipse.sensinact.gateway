/* 
 * Copyright 2021 Kentyou 
 * Proprietary and confidential
 * 
 * All Rights Reserved. 
 * Unauthorized copying of this file is strictly prohibited
 */
package org.eclipse.sensinact.gateway.tools.connector.influxdb;

import java.net.URI;

/**
 * InfluxDbConnectorConfiguration helps at configuring a InfluxDb client 
 * to be instantiated
 */
public class InfluxDbConnectorConfiguration {
    
    /**
     * {@link InfluxDbConnectorConfiguration} Builder
     */
    public static class Builder {

    	String username;
    	String password;
    	String scheme;
    	String host;
    	int port;
    	String path;
    	
    	/**
    	 * Constructor
    	 */
    	public Builder(){
    	}
    	
    	/**
    	 * Defines the String name of the user to be connected
    	 * 
    	 * @param username the name of the user
    	 * @return this Builder
    	 */
    	public Builder withUsername(String username) {
    		if(username != null) {
    			String user = username.trim();
    			if(user.length()>0)
    				this.username = user;
    		}
    		return this;
    	}

    	/**
    	 * Defines the String password of the user to be connected
    	 * 
    	 * @param password the password of the user
    	 * @return this Builder
    	 */
    	public Builder withPassword(String password) {
    		if(password != null) {
    			String pass = password.trim();
    			if(pass.length()>0)
    				this.password = pass;
    		}
    		return this;
    	}

    	/**
    	 * Defines the String uri of the InfluxDB instance to connect to
    	 *  
    	 * @param host the String uri of the InfluxDB instance
    	 * @return this Builder
    	 */
    	public Builder withUri(String uri) {
    		if(uri!=null) {
    			URI u = URI.create(uri);
    			this.scheme = u.getScheme();
    			this.host = u.getHost();
    			this.port = u.getPort();
    			this.path = u.getPath();
    		}
    		return this;
    	}
    	
    	/**
    	 * Defines the String host of the InfluxDB instance to connect to
    	 *  
    	 * @param host the String host of the InfluxDB instance
    	 * @return this Builder
    	 */
    	public Builder withHost(String host) {
    		if(host!=null) {
    			String h = host.trim();
    			if(h.length() > 0)
    				this.host = h;
    		}
    		return this;
    	}
    	
    	/**
    	 * Defines the integer port number of the InfluxDB instance to connect to
    	 *  
    	 * @param port the integer port number of the InfluxDB instance
    	 * @return this Builder
    	 */
    	public Builder withPort(int port) {
    		if(port > 0)
    			this.port = port;
    		return this;
    	}

    	/**
    	 * Defines the String path of the URI of the database to connect to
    	 *  
    	 * @param path the String path of the URI of the database
    	 * @return this Builder
    	 */
    	public Builder withPath(String path) {
    		if(path!=null) {
    			String p = path.trim();
    			if(p.length() > 0)
    				this.path = p;
    		}
    		return this;
    	}
    	
    	private String getPath() {
    		if(this.path == null)
    			return DEFAULT_PATH;
    		return this.path;
    	}

    	/**
    	 * Defines the String scheme of the URI to connect to the database
    	 *  
    	 * @param path the String scheme of the URI of the database
    	 * @return this Builder
    	 */
    	public Builder withScheme(String scheme) {
    		if(scheme!=null) {
    			String s = scheme.trim();
    			if(s.length() > 0)
    				this.scheme = s;
    		}
    		return this;
    	}

    	/**
    	 * Builds the {@link InfluxDbConnectorConfiguration}
    	 * 
    	 * @return the {@link InfluxDbConnectorConfiguration} built by
    	 * this Builder
    	 */
    	public InfluxDbConnectorConfiguration build() {
    		final InfluxDbConnectorConfiguration config;
    		if(host == null && port==0 && (username==null || password==null)) {
    			config = new InfluxDbConnectorConfiguration();
    			config.setPath(getPath());
    		} else {
	    		if(scheme == null)
	    			this.scheme = DEFAULT_SCHEME; 
	    		if(host == null)
	    			this.host = DEFAULT_HOST; 
	    		if(port == 0)
	    			this.port = DEFAULT_PORT;
	    		if(this.username!=null && this.password!=null) 
	    			config = new InfluxDbConnectorConfiguration(username, password, scheme, host, port, getPath());	
	    		else	
	    			config = new InfluxDbConnectorConfiguration(scheme, host, port, getPath());
    		}
    		return config;
    	}
    }

	public static final String DEFAULT_SCHEME = "http";
	public static final String DEFAULT_HOST = "localhost";
	public static final String DEFAULT_PATH = "";
	public static final int    DEFAULT_PORT = 8086;
	
	private String username;
	private String password;
	
	private String scheme;
	private String host;
	private int port;
	private String path;
	
    /**
     * Constructor
     */
    private InfluxDbConnectorConfiguration(){
    	this(DEFAULT_SCHEME, DEFAULT_HOST, DEFAULT_PORT, DEFAULT_PATH);
    }
    
    /**
     * Constructor
     * 
     * @param scheme the String host of the InfluxDB instance to connect to
     * @param host the String host of the InfluxDB instance to connect to
     * @param port the integer port number of the InfluxDB instance to connect to
     * @param path the String path of the URI to connect to the InfluxDB instance
     */
    private InfluxDbConnectorConfiguration(String scheme, String host, int port, String path){
    	this.scheme = scheme;
    	this.port = port;
    	this.host = host;
    	this.path = path;
    }

    /**
     * Constructor
     * 
     * @param username the String name of the user to be connected
     * @param password the String password of the user to be connected
     * @param scheme the String scheme of the URI to connect to the InfluxDB instance
     * @param host the String host of the InfluxDB instance to connect to
     * @param port the integer port number of the InfluxDB instance to connect to
     * @param path the String path of the URI to connect to the InfluxDB instance
     */
    private InfluxDbConnectorConfiguration(String username, String password, String scheme, String host, int port, String path){
    	this(scheme, host, port, path);
    	this.username = username;
    	this.password = password;
    }

    /**
     * Defines the String path of the URI to connect to the database
     * 
     * @param path the String path of the URI
     */
    private void setPath(String path) {
    	this.path = path;
    }
    
    /**
     * Returns the String name of the user to be connected to 
     * the database  
     * 
     * @return the String name of the user
     */
    public String getUserName() {
    	return this.username;
    }

    /**
     * Returns the String password of the user to be connected 
     * to the database  
     * 
     * @return the String password of the user
     */
    public String getPassword() {
    	return this.password;
    }
    
    /**
     * Returns the String URI to connect to the InluxDB instance
     * 
     * @return the connection String URI 
     */
    public String getUri() {
    	String port = null;
    	if(this.port <= 0)
    		port="";
    	else 
    		port=":".concat(String.valueOf(this.port));
    	
    	String uri = String.format("%s://%s%s%s",scheme,host,port,path);
    	return uri;
    }
    
}
