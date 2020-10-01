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
package org.eclipse.sensinact.gateway.tools.connector.influxdb;

import java.io.IOException;
import java.util.Arrays;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Pong;

/**
 * InfluxDB database connector
 */
public class InfluxDbConnector  {

    private InfluxDB influxDB;
    
    /**
     * Constructor
     * 
     * @throws IOException if an error occurs when connecting to the InfluxDB instance
     */
    public InfluxDbConnector() throws IOException {
    	this(new InfluxDbConnectorConfiguration.Builder().build());
    }
    
    /**
     * Constructor
     * 
     * @param configuration {@link InfluxDbConnectorConfiguration} allowing to configure the connection
     * to the InfluxDB instance
     * 
     * @throws IOException if an error occurs when connecting to the InfluxDB instance
     */
    public InfluxDbConnector(InfluxDbConnectorConfiguration configuration) throws IOException {
    	boolean connected = false;
    	
    	if(configuration.getUserName()!=null && configuration.getPassword()!=null)
    		connected = this.connect(configuration.getUri(),configuration.getUserName(),configuration.getPassword());
    	else
    		connected = this.connect(configuration.getUri());
    	if(connected) {
            influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
            influxDB.enableBatch();
    	}
    	else
    		throw new IOException("Unable to connect");
    }

    
    /**
     * Creates a connection and returns true if it has been done properly.
     * Returns false otherwise 
     *  
     * @param uri
     * @param database
     * 
     * @return <ul>
     * 			<li>true if the connection has been properly done</li>
     * 			<li>false otherwise</li>
     * 		   </ul>
     */
    private boolean connect(String uri) {
        influxDB = InfluxDBFactory.connect(uri);
        if(!checkVersion())
        	return false; 
        return true;
    }
    

    /**
     * Creates a connection and returns true if it has been done properly.
     * Returns false otherwise  
     *  
     * @param uri
     * @param username
     * @param password
     * @param database
     * 
     * @return <ul>
     * 			<li>true if the connection has been properly done</li>
     * 			<li>false otherwise</li>
     * 		   </ul>
     */
    private boolean connect(String uri, String username, String password) {
        influxDB = InfluxDBFactory.connect(uri, username, password);
        if(!checkVersion())
        	return false;
        return true;
    }
    
    private boolean checkVersion() {
    	Pong response = this.influxDB.ping();
        if (response.getVersion().equalsIgnoreCase("unknown")) {
            System.out.println("Error pinging server.");
            influxDB.close();
            influxDB = null;
            return false;
        }   
        return true;
    }

    /**
     * Returns true if the database whose name is passed as parameter exists in 
     * the InfluxDB instance this InfluxDbConnector is connected to ; otherwise 
     * returns false
     *  
     * @param databaseName the name of the database
     * 
     * @return
     * 	<ul>
     * 		<li>true if the database with the specified name exists</li>
     * 		<li>false otherwise</li>
     * </ul>
     */
    @SuppressWarnings("deprecation")
	public boolean exists(String databaseName) {    	
    	return this.influxDB.databaseExists(databaseName);
    }    
    
    /**
     * Returns the database with the name passed as parameter if it exists, otherwise 
     * it is created and returned
     *  
     * @param databaseName the name of the database
     * 
     * @return the newly created database or the one previously existing with the 
     * specified name
     */
    @SuppressWarnings("deprecation")
	public InfluxDbDatabase createIfNotExists(String databaseName) {    	
    	if(!exists(databaseName)) 
    		this.influxDB.createDatabase(databaseName);
    	return new InfluxDbDatabase(this.influxDB,databaseName);
    }

    /**
     * Returns the database with the name passed as parameter if it exists, otherwise 
     * returns null
     *  
     * @param databaseName the name of the database
     * 
     * @return
     * 	<ul>
     * 		<li>the database with the specified name if it exists</li>
     * 		<li>null otherwise</li>
     * </ul>
     */
    public InfluxDbDatabase getIfExists(String databaseName) {
    	if(!exists(databaseName))
    		return null;
    	return new InfluxDbDatabase(this.influxDB,databaseName);
    }
    
    /**
     * Close the connection this InfluxDbConnector initiated
     * with an InfluxDB instance
     */
    public void close() {
    	this.influxDB.close();
    }
}
