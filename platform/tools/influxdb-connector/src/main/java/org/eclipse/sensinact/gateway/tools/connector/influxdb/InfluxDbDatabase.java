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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A InfluxDbDatabase provide CRUD methods to an InfluxDB database 
 */
public class InfluxDbDatabase {

	private interface SimpleDateFormatProvider {
		Iterator<SimpleDateFormat> iterator();
	}
	
	private static final ThreadLocal<SimpleDateFormatProvider> THREAD_LOCAL_FORMATS = new ThreadLocal<SimpleDateFormatProvider>() {
		
		protected SimpleDateFormatProvider initialValue() {
			return new SimpleDateFormatProvider(){

				@Override
				public Iterator<SimpleDateFormat> iterator() {
					return Arrays.asList(
						new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
						new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
					).iterator();
				}
			};
		}
	};
	
	private static final Logger LOG = LoggerFactory.getLogger(InfluxDbDatabase.class);

    private InfluxDB influxDB;
	private String database;

	/**
	 * Constructor
	 * 
	 * @param influxDB the {@link InfluxDB} wrapped by the InfluxDbDatabase to be instantiated
	 */
	public InfluxDbDatabase(InfluxDB influxDB, String database) {
		this.influxDB = influxDB;
		this.database = database;

        influxDB.setDatabase(database);
        influxDB.setRetentionPolicy("autogen");
	}
	
	private boolean isValidDate(String datetime) {
		SimpleDateFormatProvider formatProvider = THREAD_LOCAL_FORMATS.get();
		boolean valid = false;
		for(Iterator<SimpleDateFormat> it = formatProvider.iterator();it.hasNext();) {
			SimpleDateFormat format = it.next();
			try {			
				format.parse(datetime);
				valid = true;
				break;
			}catch(ParseException e) {
				LOG.error(e.getMessage(),e);
			};			
		}
		THREAD_LOCAL_FORMATS.remove();
		return valid;
	}

    /**
     * Adds the Object value passed as parameter to the InfluxDB measurement whose name is also 
     * passed as parameter, and tagged using the tags dictionary argument 
     *  
     * @param measurement the name of the InfluxB measurement to which adding the value 
     * @param tags the dictionary of tags to be used to tag the value to be added
     * @param value the Object value to add
     */
    public void add(String measurement, Dictionary<String,String> tags, Object value) {
    	this.add(measurement, tags, value, System.currentTimeMillis());
    }         

    /**
     * Adds the Object value passed as parameter to the InfluxDB measurement whose name is also 
     * passed as parameter, and tagged using the tags dictionary argument, with the defined timestamp 
     *  
     * @param measurement the name of the InfluxB measurement to which adding the value 
     * @param tags the dictionary of tags to be used to tag the value to be added
     * @param value the Object value to add
     * @param timestamp the millisecond unix epoch timestamp 
     */
    public void add(String measurement, Dictionary<String,String> tags, Object value, long timestamp) {
    	Point point = null;
        Builder builder = Point.measurement(measurement);
        if(tags != null && !tags.isEmpty()) {		         	       		 
	    	for(Iterator<String> it = Collections.list(tags.keys()).iterator();it.hasNext();) {
	    		String key = it.next();
	    		builder.tag(key, String.valueOf(tags.get(key)));
	    	}
    	}
    	builder.time(timestamp, TimeUnit.MILLISECONDS);
    	if(value.getClass().isPrimitive()) {
    		switch(value.getClass().getName()) {
	    		case "byte":
	    			point = builder.addField("value", Byte.valueOf((byte)value)).build();
    				break;
	    		case "short":
	    			point = builder.addField("value", Short.valueOf((short)value)).build();
    				break;
	    		case "int":
	    			point = builder.addField("value", Integer.valueOf((int)value)).build();
    				break;
	    		case "long":
	    			point = builder.addField("value", Long.valueOf((long)value)).build();
    				break;
	    		case "float":
	    			point = builder.addField("value", Float.valueOf((float)value)).build();
	    			break;
	    		case "double":
	    			point = builder.addField("value", Double.valueOf((double)value)).build();
    				break;
	    		case "char":
	    			point = builder.addField("value", new String(new char[] {(char)value})).build();
    				break;
	    		case "boolean":
	    			point = builder.addField("value", Boolean.valueOf((boolean)value)).build();
    				break;
    		}
    	} else if (value instanceof String) {
             point = builder.addField("value", (String) value).build();
        } else if (value instanceof Number) {
        	switch(value.getClass().getName()) {
    		case "java.lang.Byte":
    			point = builder.addField("value", (Byte) value).build();
				break;
    		case "java.lang.Short":
    			point = builder.addField("value", (Short) value).build();
				break;
    		case "java.lang.Integer":
    			point = builder.addField("value", (Integer) value).build();
				break;
    		case "java.lang.Long":
    			point = builder.addField("value", (Long) value).build();
				break;
    		case "java.lang.Float":
    			point = builder.addField("value", (Float) value).build();
    			break;
    		case "java.lang.Double":
    			point = builder.addField("value", (Double) value).build();
				break;
    		case "java.lang.Character":
    			point = builder.addField("value", new String(new char[] {((Character)value).charValue()})).build();
				break;
    		case "java.lang.Boolean":
    			point = builder.addField("value", new String(new char[] {(char)value})).build();
				break;
        	}			
		} else if(value instanceof Enum){
			point = builder.addField("value", ((Enum)value).name()).build();
		} else {
			point = builder.addField("value", String.valueOf(value)).build();			
		}
        influxDB.write(point);
        influxDB.flush();
    }	

    /**
     * Returns the String formated values for the columns List passed as parameter, of the records from 
     * the InfluxDB measurement whose name is also passed as parameter, and compliant with tags dictionary 
     * argument.
     * 
     * @param measurement the name of the InfluxDB measurement in which searching the points
     * @param columns the Strings List defining the fields to be provided
     * @param tags the dictionary of tags allowing to parameterize the research
     * 
     * @return the JSON formated String result of the research
     */
    public String get(String measurement, List<String> columns, Dictionary<String,String> tags) {
    	String select = null;
    	if(columns==null || columns.isEmpty())
    		select = "* ";
    	else
    		select = columns.stream().collect(StringBuilder::new,(b,s)-> { 
    			b.append(s); 
    			b.append(",");
    		}, (h,t)->h.append(t.toString())).toString();
    	select =select.substring(0,select.length()-1);

    	String from = String.format(" FROM %s " , measurement);
    	
    	String where = null;
    	if(tags != null && !tags.isEmpty()) {
	   		StringBuilder builder = new StringBuilder();
	   		builder.append(" WHERE ");
			for(Iterator<String> it = Collections.list(tags.keys()).iterator();it.hasNext();) {
				String key = it.next();
				builder.append(key);
				builder.append("='");
				builder.append(tags.get(key));
				builder.append("'");
				if(it.hasNext())
					builder.append(" AND ");
			}
			where = builder.toString();
    	} else 
    		where="";
    	
    	Query query = new Query(String.format("SELECT %s%s%s", select, from, where),database);
    	QueryResult result = this.influxDB.query(query);
    	return result.toString();
    }

    /**
     * Returns the String formated values for the columns List passed as parameter, of the records from 
     * the InfluxDB measurement whose name is also passed as parameter, compliant with tags dictionary 
     * argument and starting from the specified String formated start datetime. 
     * 
     * @param measurement the name of the InfluxDB measurement in which searching the points
     * @param columns the Strings List defining the fields to be provided
     * @param tags the dictionary of tags allowing to parameterize the research
     * @param start the String formated date defining the chronological beginning of records in which to search 
     * 
     * @return the JSON formated String result of the research
     */
    public String get(String measurement, List<String> columns, Dictionary<String,String> tags, String start) {
    	if(start == null || !isValidDate(start))
    		return get(measurement, columns, tags);
    	String select = null;
    	if(columns==null || columns.isEmpty())
    		select = "* ";
    	else
    		select = columns.stream().collect(StringBuilder::new,(b,s)-> { 
    			b.append(s); 
    			b.append(",");
    		}, (h,t)->h.append(t.toString())).toString();
    	select =select.substring(0,select.length()-1);

    	String from = String.format(" FROM %s " , measurement);
    	
    	String where = null;
    	if(tags != null && !tags.isEmpty()) {    		
	   		StringBuilder builder = new StringBuilder();
	   		builder.append(" WHERE ");
			for(Iterator<String> it = Collections.list(tags.keys()).iterator();it.hasNext();) {
				String key = it.next();
				builder.append(key);
				builder.append("='");
				builder.append(tags.get(key));
				builder.append("'");
				if(it.hasNext())
					builder.append(" AND ");
			}
			builder.append(String.format(" AND time > '%s'", start));
			where = builder.toString();
    	} else 
			where = String.format(" WHERE time > '%s'", start);
    	
    	Query query = new Query(String.format("SELECT %s%s%s", select, from, where),database);
    	QueryResult result = this.influxDB.query(query);
    	return result.toString();
    }	

    /**
     * Returns the String formated values for the columns List passed as parameter, of the records from 
     * the InfluxDB measurement whose name is also passed as parameter, compliant with tags dictionary 
     * argument, between both String formated start and end datetimes. 
     * 
     * @param measurement the name of the InfluxDB measurement in which searching the points
     * @param columns the Strings List defining the fields to be provided
     * @param tags the dictionary of tags allowing to parameterize the research
     * @param start the String formated date defining the chronological beginning of records in which to search 
     * @param end the String formated date defining the chronological ending of records in which to search 
     * 
     * @return the JSON formated String result of the research
     */
    public String get(String measurement, List<String> columns, Dictionary<String,String> tags, String start, String end) {
    	if(end == null || !isValidDate(end))
    		return get(measurement, columns, tags, start);
    	String select = null;
    	if(columns==null || columns.isEmpty())
    		select = "* ";
    	else
    		select = columns.stream().collect(StringBuilder::new,(b,s)-> { 
    			b.append(s); 
    			b.append(",");
    		}, (h,t)->h.append(t.toString())).toString();
    	select =select.substring(0,select.length()-1);

    	String from = String.format(" FROM %s " , measurement);
    	if(start == null || !isValidDate(start))   
			start = "1970-01-01T00:00:00.001Z";
		
    	String where = null;
    	if(tags != null && !tags.isEmpty()) {
	   		StringBuilder builder = new StringBuilder();
	   		builder.append(" WHERE ");
			for(Iterator<String> it = Collections.list(tags.keys()).iterator();it.hasNext();) {
				String key = it.next();
				builder.append(key);
				builder.append("='");
				builder.append(tags.get(key));
				builder.append("'");
				if(it.hasNext())
					builder.append(" AND ");
			}
		    builder.append(String.format(" AND time > '%s' AND time < '%s'", start, end));
			where = builder.toString();
    	} else 
			where = String.format(" WHERE time > '%s' AND time < '%s'", start, end);
    	Query query = new Query(String.format("SELECT %s%s%s", select, from, where),database);
    	QueryResult result = this.influxDB.query(query);
    	return result.toString();
    }	

    /**
     * Returns the List of records from the InfluxDB measurement whose name is passed as parameter, 
     * mapped to the specified result type, and compliant with tags dictionary also passed as parameter
     * 
     * @param <T> result unit type  
     * 
     * @param resultType the type to which found points (records) are mapped 
     * @param measurement the name of the InfluxDB measurement in which searching the points
     * @param columns the Strings List defining the fields to be provided
     * @param tags the dictionary of tags allowing to parameterize the research 
     * 
     * @return the List of resultType typed instances resulting of the search
     */
    public <T> List<T> get(Class<T> resultType, String measurement, List<String> columns, Dictionary<String,String> tags) {
    	String select = null;
    	if(columns.isEmpty())
    		select = "* ";
    	else
    		select = columns.stream().collect(StringBuilder::new,(b,s)-> { 
    			b.append(s); 
    			b.append(",");
    		}, (h,t)->h.append(t.toString())).toString();
    	select =select.substring(0,select.length()-1);
    	
    	String from = String.format(" FROM %s " , measurement);
    	
    	String where = null;
    	if(tags != null && !tags.isEmpty()) {
	   		StringBuilder builder = new StringBuilder();
	   		builder.append(" WHERE ");
			for(Iterator<String> it = Collections.list(tags.keys()).iterator();it.hasNext();) {
				String key = it.next();
				builder.append(key);
				builder.append("='");
				builder.append(tags.get(key));
				builder.append("'");
				if(it.hasNext())
					builder.append(" AND ");
			}
			where = builder.toString();
    	} else 
    		where="";
    	
    	Query query = new Query(String.format("SELECT %s%s%s", select, from, where),database);
    	QueryResult result = this.influxDB.query(query);
    	InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
    	return resultMapper.toPOJO(result, resultType);
    }	

    /**
     * Returns the List of records from the InfluxDB measurement whose name is passed as parameter, 
     * mapped to the specified result type, compliant with tags dictionary also passed as parameter, 
     * and starting from the specified String formated start datetime.. 
     * 
     * @param <T> result unit type  
     * 
     * @param resultType the type to which found points (records) are mapped 
     * @param measurement the name of the InfluxDB measurement in which searching the points
     * @param columns the Strings List defining the fields to be provided
     * @param tags the dictionary of tags allowing to parameterize the research
     * @param start the String formated date defining the chronological beginning of records in which to search
     * 
     * @return the List of resultType typed instances resulting of the search
     */
    public <T> List<T> get(Class<T> resultType, String measurement, List<String> columns, Dictionary<String,String> tags, String start) {
    	if(start == null || !isValidDate(start))
    		return get(resultType, measurement, columns, tags);
    	String select = null;
    	if(columns.isEmpty())
    		select = "* ";
    	else
    		select = columns.stream().collect(StringBuilder::new,(b,s)-> { 
    			b.append(s); 
    			b.append(",");
    		}, (h,t)->h.append(t.toString())).toString();
    	select =select.substring(0,select.length()-1);
    	
    	String from = String.format(" FROM %s " , measurement);
		
    	String where = null;
    	if(tags != null && !tags.isEmpty()) {
	   		StringBuilder builder = new StringBuilder();
	   		builder.append(" WHERE ");
			for(Iterator<String> it = Collections.list(tags.keys()).iterator();it.hasNext();) {
				String key = it.next();
				builder.append(key);
				builder.append("='");
				builder.append(tags.get(key));
				builder.append("'");
				if(it.hasNext())
					builder.append(" AND ");
			}
			builder.append(String.format(" AND time > '%s'", start));
			where = builder.toString();
    	} else
    		where = String.format(" WHERE time > '%s'", start);
    	Query query = new Query(String.format("SELECT %s%s%s", select, from, where),database);
    	QueryResult result = this.influxDB.query(query);
    	InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
    	return resultMapper.toPOJO(result, resultType);
    }	

    /**
     * Returns the List of records from the InfluxDB measurement whose name is passed as parameter, 
     * mapped to the specified result type, compliant with tags dictionary also passed as parameter, 
     * and between both String formated start and end datetimes. 
     * 
     * @param <T> result unit type  
     * 
     * @param resultType the type to which found points (records) are mapped 
     * @param measurement the name of the InfluxDB measurement in which searching the points
     * @param columns the Strings List defining the fields to be provided
     * @param tags the dictionary of tags allowing to parameterize the research
     * @param start the String formated date defining the chronological beginning of records in which to search 
     * @param end the String formated date defining the chronological ending of records in which to search 
     * 
     * @return the List of resultType typed instances resulting of the search
     */
    public <T> List<T> get(Class<T> resultType, String measurement, List<String> columns, Dictionary<String,String> tags, String start, String end) {
    	if(end == null || !isValidDate(end))
    		return get(resultType, measurement, columns, tags, start);
    	String select = null;
    	if(columns.isEmpty())
    		select = "* ";
    	else
    		select = columns.stream().collect(StringBuilder::new,(b,s)-> { 
    			b.append(s); 
    			b.append(",");
    		}, (h,t)->h.append(t.toString())).toString();
    	select =select.substring(0,select.length()-1);
    	
    	String from = String.format(" FROM %s " , measurement);
    	if(start == null || !isValidDate(start))   
			start = "1970-01-01T00:00:00.001Z";
		
    	String where = null;
    	if(tags != null && !tags.isEmpty()) {
	   		StringBuilder builder = new StringBuilder();
	   		builder.append(" WHERE ");
			for(Iterator<String> it = Collections.list(tags.keys()).iterator();it.hasNext();) {
				String key = it.next();
				builder.append(key);
				builder.append("='");
				builder.append(tags.get(key));
				builder.append("'");
				if(it.hasNext())
					builder.append(" AND ");
			}
			builder.append(String.format(" AND time > '%s' AND time < '%s'", start, end));
			where = builder.toString();
    	} else
    		where = String.format(" WHERE time > '%s' AND time < '%s'", start, end);
    	
    	Query query = new Query(String.format("SELECT %s%s%s", select, from, where),database);
    	QueryResult result = this.influxDB.query(query);
    	InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
    	return resultMapper.toPOJO(result, resultType);
    }	
}
