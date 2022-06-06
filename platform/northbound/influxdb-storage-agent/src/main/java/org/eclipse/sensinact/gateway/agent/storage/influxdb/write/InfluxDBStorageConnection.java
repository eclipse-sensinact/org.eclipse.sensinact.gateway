/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.agent.storage.influxdb.write;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.historic.storage.agent.generic.StorageConnection;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbDatabase;
import org.eclipse.sensinact.gateway.util.json.JSONObjectStatement;
import org.eclipse.sensinact.gateway.util.json.JSONTokenerStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 * Extended {@link SorageConnection} dedicated to InfluxDB data store
 */
public class InfluxDBStorageConnection extends StorageConnection {
	
	Logger LOGGER=LoggerFactory.getLogger(InfluxDBStorageConnection.class);
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
	 * @param database the {@link InfluxDbDatabase} in which data will be stored
	 * @param measurement the String name of the measurement in which data will be stored
	 * @param props the Dictionary holding the properties applying on the InfluxDbStorageConnection to 
	 * be instantiated
	 * 
	 * @throws IOException 
	 */
	public InfluxDBStorageConnection(InfluxDbDatabase database, String measurement, Dictionary<String,?> props){
		super();
		this.database = database;
		this.measurement = measurement;		
		this.enableDefault = true;
		this.enableGeoJSON = false;
		
		Object fieldsProperty = props.get(STORAGE_AGENT_INFLUXDB_FIELDS);		
		Object tagsProperty = props.get(STORAGE_AGENT_INFLUXDB_TAGS);
		Object defaultProperty = props.get(STORAGE_AGENT_INFLUXDB_ENABLE_DEFAULT);
		Object geojsonProperty = props.get(STORAGE_AGENT_INFLUXDB_ENABLE_GEOJSON);

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
	public void store(JsonObject obj)  {
		String measurement = null;
		final Dictionary<String,Object> fs = new Hashtable<>();		
		final Dictionary<String,String> ts = new Hashtable<>();		
		for(String key : obj.keySet()) {
			if(this.fields.contains(key)) {
				fs.put(key,obj.get(key));
				continue;
			}
			if(this.tags.contains(key))
				ts.put(key,String.valueOf(obj.get(key)));
		}
		this.extractLocation(fs, obj.get("location"));
		JsonValue o = obj.get(DataResource.VALUE);
		Object value = null;
		if(o != null) {			
			if(o instanceof JsonString) {
				value = ((JsonString)o).getString();
			} else if (o instanceof JsonNumber) {
				value = ((JsonNumber)o).doubleValue();
			} else { 
				value = String.valueOf(o);
			}
		}
		if(value == null)
			measurement = this.measurement;
		else if(value.getClass()==String.class)
			measurement=this.measurement.concat("_str");
		else
			measurement=this.measurement.concat("_num");
		long tm  = obj.getJsonNumber("timestamp").longValueExact();
		long timestamp = 0;
		if(tm>0)
			timestamp = tm;
		else
			timestamp = System.currentTimeMillis();
		//At least one tag for storing
		if(!ts.isEmpty())
			this.database.add(measurement, ts, fs, value,timestamp);	
	}

	private void extractLocation(Dictionary<String,Object> fields, JsonValue location)  {		
		if(location == null || location.getValueType() == ValueType.NULL)
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
				LOGGER.error(e.getMessage(),e);
			}
		} else
			geolocation = lc;

		if(this.enableGeoJSON && geolocation!=null)
			fields.put("geolocation", geolocation);
	}
}
