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
package org.eclipse.sensinact.gateway.tools.connector.mongodb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.ConnectionString;

/**
 * MongoDbConnectorConfiguration helps at creating a {@link ConnectionString} of a 
 * MongoDb client to be instantiated
 */
public class MongoDbConnectorConfiguration {

    /**
     * Key-Value Pair data structure
     */
    private static final class Option {
    	public final String key;
    	public final String value;
    	/**
    	 * Constructor
    	 * 
    	 * @param key the String key of the Option to be instantiated
    	 * @param value the String value of the Option to be instantiated
    	 */
    	Option(String key,String value){
    		this.key = key;
    		this.value = value;
    	}
    }
    
    /**
     * {@link MongoDbConnectorConfiguration} Builder
     */
    public static class Builder {
    	List<Option> options;
    	String database;
    	String username;
    	String password;
    	String host;
    	int port;
    	
    	/**
    	 * Constructor
    	 */
    	public Builder(){
    		this.options = new ArrayList<>();
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
    	 * Defines the String host of the MongoDB instance to connect to
    	 *  
    	 * @param host the String host of the MongoDB instance
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
    	 * Defines the integer port number of the MongoDB instance to connect to
    	 *  
    	 * @param port the integer port number of the MongoDB instance
    	 * @return this Builder
    	 */
    	public Builder withPort(int port) {
    		if(port > 0)
    			this.port = port;
    		return this;
    	}

    	/**
    	 * Defines the String name of the database to connect to
    	 *  
    	 * @param database the String name of the database
    	 * @return this Builder
    	 */
    	public Builder withDatabase(String database) {
    		if(database != null) {
    			String dbc = database.trim();
    			if(dbc.length() > 0)
    				this.database = dbc;
    		}
    		return this;
    	}

    	/**
    	 * Defines an new {@link Option} to be used to parameterize the connection
    	 * String
    	 *  
    	 * @param key the String key of the Option to be created
    	 * @param value the String value of the Option to be created
    	 * @return this Builder
    	 */
    	public Builder withOption(String key,String value) {
    		if(key!=null && value!=null) {
    			String k = key.trim();
    			String v = value.trim();
    			if(k.length()>0 && v.length()>0)
    				this.options.add(new Option(k,v));
    		}
    		return this;
    	}
    	
    	/**
    	 * Builds the {@link MongoDbConnectorConfiguration}
    	 * 
    	 * @return the {@link MongoDbConnectorConfiguration} built by
    	 * this Builder
    	 */
    	public MongoDbConnectorConfiguration build() {
    		final MongoDbConnectorConfiguration config;
    		if(host == null && port==0 && (username==null || password==null))
    			config = new MongoDbConnectorConfiguration();
    		else {
	    		if(host == null)
	    			this.host = DEFAULT_HOST; 
	    		if(this.username!=null && this.password!=null) {
	    			config = new MongoDbConnectorConfiguration(username, password, host, port);
	    			if(this.database != null)
	    				config.setDatabase(database);
	    		}
	    		else	
	    			config = new MongoDbConnectorConfiguration(host, port);
    		}
    		if(!this.options.isEmpty())
    			this.options.stream().forEach(o -> config.addOption(o.key, o.value));
    		return config;
    	}
    }
    
	private static final String DEFAULT_HOST = "localhost";
	private static final int    DEFAULT_PORT = 27017;

	private Map<String,List<String>> options;
	private String database;
	
	private String username;
	private String password;
	private String host;
	private int port;
	
    /**
     * Constructor
     */
    private MongoDbConnectorConfiguration(){
    	this(DEFAULT_HOST,DEFAULT_PORT);
    }
    
    /**
     * Constructor
     * 
     * @param host the String host of the MongoDB instance to connect to
     * @param port the integer port number of the MongoDB instance to connect to
     */
    private MongoDbConnectorConfiguration(String host, int port){
    	this.port = port;
    	this.host = host;
    	this.options = new HashMap<>();
    }

    /**
     * Constructor
     * 
     * @param username the String name of the user to be connected
     * @param password the String password of the user to be connected
     * @param host the String host of the MongoDB instance to connect to
     * @param port the integer port number of the MongoDB instance to connect to
     */
    private MongoDbConnectorConfiguration(String username, String password, String host, int port){
    	this(host, port);
    	this.username = username;
    	this.password = password;
    }
    
    /**
     * Defines the String name of the database to be connected to
     * 
     * @param database the String name of the database
     */
    private void setDatabase(String database) {
    	this.database = database;
    }

    /**
	 * Defines an new configuration option to be used to parameterize the connection
	 * String
	 *  
	 * @param key the String key of the configuration option
	 * @param value the String value of the configuration option
	 */
    private void addOption(String key,String value) {
    	List<String> list = this.options.get(key);
    	if(list == null) {
    		list = new ArrayList<>();
    		this.options.put(key,list);
    	}
    	list.add(value);	
    }
    
    /**
     * Returns {@link ConnectionString} configured by this MongoDbConnectorConfiguration
     * 
     * @return the {@link ConnectionString}
     */
    public ConnectionString connectionString() {
    	String database = null;
    	String authentication = null;
    	//if no credentials are provided the 'admin' database is used by default
    	if(username != null && password != null) {
    		authentication = String.format("username:password@", username,password);
    		if(this.database != null)
        		database = String.format("/%s",this.database);
        	else
        		database = "";
    	}
    	else {
    		authentication = "";
    		database = "";
    	}    	
    	String options = null;
    	if(!this.options.isEmpty()) {
    		options = String.format("?%s", this.options.entrySet().stream().<StringBuilder>collect(StringBuilder::new,
    		(s,e)->s.append(e.getValue().stream().<StringBuilder>collect(StringBuilder::new,(l,m)->l.append(e.getKey()).append("=").append(m).append("&"),(l,m)->l.append(m.toString())).toString()),
    		(t,s)->t.append(s.toString())).toString());
    		options = options.substring(0,options.length()-1);
    	} else
    		options = "";
    	String port = null;
    	if(this.port == 0)
    		port="";
    	else 
    		port=":".concat(String.valueOf(this.port));
    	String cs = String.format("mongodb://%s%s%s%s%s",authentication,host,port,database,options);
    	ConnectionString connectionString = new ConnectionString(cs);
    	return connectionString;
    }
    
}
