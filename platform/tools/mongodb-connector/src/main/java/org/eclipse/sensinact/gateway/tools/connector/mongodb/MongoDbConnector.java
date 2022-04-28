/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.tools.connector.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
 * MongoDB database connector
 */
public class MongoDbConnector {

	private MongoClient mongoClient;

    /**
     * Constructor
     */
    public MongoDbConnector(){   
    	this(new MongoDbConnectorConfiguration.Builder().build());
    }
    
    /**
     * Constructor
     * 
     * @param configuration {@link MongoDbConnectorConfiguration} allowing to configure
     * the connection to MongoDB
     */
    public MongoDbConnector(MongoDbConnectorConfiguration configuration){   
    	this.mongoClient = MongoClients.create(configuration.connectionString());
    }
    
    /**
     * Returns true if the database whose name is passed as parameter in the 
     * MongoDB instance this MongoDbConnector is connected to ; otherwise returns
     * false
     *  
     * @param databaseName the name of the database
     * 
     * @return
     * 	<ul>
     * 		<li>true if the database with the specified name exists</li>
     * 		<li>false otherwise</li>
     * </ul>
     */
    public boolean exists(String databaseName) {
    	MongoCursor<String> cursor = this.mongoClient.listDatabaseNames().iterator();
    	while(cursor.hasNext()) {
    		if(cursor.next().equals(databaseName))
    			return true;
    	}
    	return false;
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
    public MongoDbDatabase createIfNotExists(String databaseName) {    	
    	MongoDatabase base = this.mongoClient.getDatabase(databaseName);
    	return new MongoDbDatabase(base);
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
    public MongoDbDatabase getIfExists(String databaseName) {
    	if(!exists(databaseName))
    		return null;
    	MongoDatabase base = this.mongoClient.getDatabase(databaseName);
    	return new MongoDbDatabase(base);
    }
    
    /**
     * Close the connection this MongoDbConnector initiated
     * with an MongoDB instance
     */
    public void close() {
    	this.mongoClient.close();
    }
    

}
