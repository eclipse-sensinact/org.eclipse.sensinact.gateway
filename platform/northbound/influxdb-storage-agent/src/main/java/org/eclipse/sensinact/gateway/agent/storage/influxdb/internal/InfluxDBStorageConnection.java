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
package org.eclipse.sensinact.gateway.agent.storage.influxdb.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.sensinact.gateway.agent.storage.generic.StorageConnection;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbDatabase;
import org.eclipse.sensinact.gateway.util.json.JSONObjectStatement;
import org.eclipse.sensinact.gateway.util.json.JSONTokenerStatement;
import org.json.JSONObject;

/**
 * Extended {@link SorageConnection} dedicated to InfluxDB data store
 */
public class InfluxDBStorageConnection extends StorageConnection {
	

	private static final String STORAGE_AGENT_INFLUXDB_FIELDS         = "org.eclipse.sensinact.gateway.history.influxdb.fields";
	private static final String STORAGE_AGENT_INFLUXDB_TAGS           = "org.eclipse.sensinact.gateway.history.influxdb.tags";
	private static final String STORAGE_AGENT_INFLUXDB_ENABLE_DEFAULT = "org.eclipse.sensinact.gateway.history.influxdb.default";
	private static final String STORAGE_AGENT_INFLUXDB_ENABLE_GEOJSON = "org.eclipse.sensinact.gateway.history.influxdb.geojson";
	
	private static final JSONObjectStatement STATEMENT = 
    		new JSONObjectStatement(new JSONTokenerStatement(
			    "{" + 
			    " \"type\": \"Feature\"," + 
			    " \"properties\": {}," + 
			    "  \"geometry\": {" + 
			    "     \"type\": \"Point\"," + 
			    "     \"coordinates\": [ $(longitude),$(latitude)] " + 
			    "  }" + 
			    "}"));
	
	private String measurement;
	private InfluxDbDatabase database;
	
	private boolean enableGeoJSON;
	private boolean enableDefault;
	private Set<String> fields;
	private Set<String> tags;
	
	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing the InfluxDbAgentCallback to be instantiated
	 * to interact with the OSGi host environment
	 * @param database the {@link InfluxDbDatabase} in which data will be stored
	 * @param measurement the String name of the measurement in which data will be stored
	 * 
	 * @throws IOException 
	 */
	public InfluxDBStorageConnection(Mediator mediator, InfluxDbDatabase database, String measurement){
		super(mediator);
		this.database = database;
		this.measurement = measurement;		
		this.enableDefault = true;
		this.enableGeoJSON = false;
		
		Object fieldsProperty = mediator.getProperty(STORAGE_AGENT_INFLUXDB_FIELDS);		
		Object tagsProperty = mediator.getProperty(STORAGE_AGENT_INFLUXDB_TAGS);
		Object defaultProperty = mediator.getProperty(STORAGE_AGENT_INFLUXDB_ENABLE_DEFAULT);
		Object geojsonProperty = mediator.getProperty(STORAGE_AGENT_INFLUXDB_ENABLE_GEOJSON);

		this.fields = new HashSet<>();
		this.tags = new HashSet<>();
		
		if(geojsonProperty != null)
			enableGeoJSON = Boolean.parseBoolean(String.valueOf(geojsonProperty));
		
		if(defaultProperty != null)
			enableDefault = Boolean.parseBoolean(String.valueOf(defaultProperty));
		
		if(enableDefault) {
			this.fields.add("latitude");
			this.fields.add("longitude");
			this.tags.add("path");
			this.tags.add("resource");
		}		
		if(enableGeoJSON)
			this.fields.add("geojson");
		
		if(fieldsProperty != null) {
			String fieldsStr = String.valueOf(fieldsProperty);
			String[] fieldsArr = fieldsStr.split(",");
			for(String field : fieldsArr) {
				String fd = field.trim();
				if(fd.length() > 0)
					this.fields.add(fd);
			}
		}		
		if(tagsProperty != null) {
			String tagsStr = String.valueOf(tagsProperty);
			String[] tagsArr = tagsStr.split(",");
			for(String tag : tagsArr) {
				String tg = tag.trim();
				if(tg.length() > 0)
					this.tags.add(tg);
			}
		}
	}
	
	@Override
	public void store(JSONObject obj)  {	
		final Dictionary<String,Object> fs = new Hashtable<>();		
		final Dictionary<String,String> ts = new Hashtable<>();		
		for(Iterator<String> it = obj.keys();it.hasNext();) {
			String key = it.next();
			if(this.fields.contains(key)) {
				fs.put(key,obj.get(key));
				continue;
			}
			if(this.tags.contains(key))
				ts.put(key,String.valueOf(obj.get(key)));
		}
		this.extractLocation(fs, obj.opt("location"));		
		this.database.add(measurement, ts, fs, obj.opt(DataResource.VALUE));	
	}

	private void extractLocation(Dictionary<String,Object> fields, Object location)  {		
		if(location == null)
			return;		
		double latitude = -1;
		double longitude = -1;
		String geolocation = null;
		String lc = String.valueOf(location);
		
		String separator;
		String[] separators = new String[] {":",",",".","-"," "};
		int ind = 0;
		for(;ind < separators.length;ind++) {
			if(lc.indexOf(separators[ind]) > -1)
				break;
		}
		if(ind <  separators.length)
			separator=separators[ind];
		else
			return;
		String[] locationElements = lc.split(separator);
		if(locationElements.length == 2) {
			try {
				latitude = Double.parseDouble(locationElements[0]);
				longitude = Double.parseDouble(locationElements[1]);			    
				if(this.enableDefault) {
					fields.put("latitude", latitude);
					fields.put("longitude", longitude);
				}
				STATEMENT.apply("latitude", latitude);
			    STATEMENT.apply("longitude", longitude);		    
			    geolocation = STATEMENT.toString();
			    
			} catch(IllegalArgumentException e) {
				mediator.error(e);
			}
		} else
			geolocation = lc;

		if(this.enableGeoJSON && geolocation!=null)
			fields.put("geolocation", geolocation);
	}
		
	// Constants.EARTH_SPHERICAL_MODEL_RADIUS * 1000D 2* atan2(sqrt(sqrt(sin(((lat2 - lat1) * Constants.DEGREES_TO_RADIUS_COEF) / 2))+sqrt(sin(((lng2 - lng1) * Constants.DEGREES_TO_RADIUS_COEF) / 2))*cos(lat1 * Constants.DEGREES_TO_RADIUS_COEF)*cos(lat2 * Constants.DEGREES_TO_RADIUS_COEF))
    // sqrt(1-(sqrt(sin(((lat2 - lat1) * Constants.DEGREES_TO_RADIUS_COEF) / 2))+sqrt(sin(((lng2 - lng1) * Constants.DEGREES_TO_RADIUS_COEF) / 2))*cos(lat1 * Constants.DEGREES_TO_RADIUS_COEF)*cos(lat2 * Constants.DEGREES_TO_RADIUS_COEF))))

}
