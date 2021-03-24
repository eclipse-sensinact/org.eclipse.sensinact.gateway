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

import org.eclipse.sensinact.gateway.util.LocationUtils;
import org.eclipse.sensinact.gateway.util.location.Segment;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A InfluxDbDatabase provide CRUD methods to an InfluxDB database 
 */
public class InfluxDbDatabase {

	private static final long MILLISECOND = 1;
	
	private static final long SECOND = MILLISECOND * 1000;

	private static final long MINUTE = SECOND * 60;

	private static final long HOUR = MINUTE * 60;

	private static final long DAY = HOUR * 24;
	
	private static final long WEEK = DAY * 7;
	
	
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
    	this.add(measurement, tags, null, value, System.currentTimeMillis());
    }     
    
    /**
     * Adds the Object value passed as parameter to the InfluxDB measurement whose name is also 
     * passed as parameter, and tagged using the tags dictionary argument 
     *  
     * @param measurement the name of the InfluxB measurement to which adding the value 
     * @param tags the dictionary of tags to be used to tag the value to be added
     * @param fields the dictionary of fields to be stored with the value
     * @param value the Object value to add
     */
    public void add(String measurement, Dictionary<String,String> tags, Dictionary<String,Object> fields, Object value) {
    	this.add(measurement, tags, fields, value, System.currentTimeMillis());
    }         

    /**
     * Adds the Object value passed as parameter to the InfluxDB measurement whose name is also 
     * passed as parameter, and tagged using the tags dictionary argument, with the defined timestamp 
     *  
     * @param measurement the name of the InfluxB measurement to which adding the value 
     * @param tags the dictionary of tags to be used to tag the value to be added
     * @param fields the dictionary of fields to be stored with the value
     * @param value the Object value to add
     * @param timestamp the millisecond unix epoch timestamp 
     */
    public void add(String measurement, Dictionary<String,String> tags, Dictionary<String,Object> fields, Object value, long timestamp) {
    	Point point = null;
        Builder builder = Point.measurement(measurement);
        if(tags != null && !tags.isEmpty()) {		         	       		 
	    	for(Iterator<String> it = Collections.list(tags.keys()).iterator();it.hasNext();) {
	    		String key = it.next();
	    		builder.tag(key, tags.get(key));
	    	}
    	}
    	builder.time(timestamp, TimeUnit.MILLISECONDS);

        if(fields != null && !fields.isEmpty()) {		         	       		 
	    	for(Iterator<String> it = Collections.list(fields.keys()).iterator();it.hasNext();) {
	    		String key = it.next();
	    		addField(builder, key, fields.get(key));
	    	}
    	}
        addField(builder,"value",value);
        point = builder.build();
        
        influxDB.write(point);
        influxDB.flush();
    }
    
    /*
     * (non-javadoc)
     * Add a field with the specified name to the Point Builder passed as parameter after the cast of the value 
     */    
    private void addField(Builder builder, String key, Object value) {
    	if(value == null)
    		return;
    	if(value.getClass().isPrimitive()) {
    		switch(value.getClass().getName()) {
	    		case "byte":
	    			builder.addField(key, Byte.valueOf((byte)value));
    				break;
	    		case "short":
	    			builder.addField(key, Short.valueOf((short)value));
    				break;
	    		case "int":
	    			builder.addField(key, Integer.valueOf((int)value));
    				break;
	    		case "long":
	    			builder.addField(key, Long.valueOf((long)value));
    				break;
	    		case "float":
	    			builder.addField(key, Float.valueOf((float)value));
	    			break;
	    		case "double":
	    			builder.addField(key, Double.valueOf((double)value));
    				break;
	    		case "char":
	    			builder.addField(key, new String(new char[] {(char)value}));
    				break;
	    		case "boolean":
	    			builder.addField(key, Boolean.valueOf((boolean)value));
    				break;
    		}
    	} else if (value instanceof String) {
             builder.addField(key, (String) value);
        } else if (value instanceof Number) {
        	switch(value.getClass().getName()) {
	    		case "java.lang.Byte":
	    			builder.addField(key, (Byte) value);
					break;
	    		case "java.lang.Short":
	    			builder.addField(key, (Short) value);
					break;
	    		case "java.lang.Integer":
	    			builder.addField(key, (Integer) value);
					break;
	    		case "java.lang.Long":
	    			builder.addField(key, (Long) value);
					break;
	    		case "java.lang.Float":
	    			builder.addField(key, (Float) value);
	    			break;
	    		case "java.lang.Double":
	    			builder.addField(key, (Double) value);
					break;
	    		case "java.lang.Character":
	    			builder.addField(key, new String(new char[] {((Character)value).charValue()}));
					break;
	    		case "java.lang.Boolean":
	    			builder.addField(key, new String(new char[] {(char)value}));
					break;
        	}			
		} else if(value instanceof Enum){
			builder.addField(key, ((Enum)value).name());
		} else {
			builder.addField(key, String.valueOf(value));			
		}
    }

    /*
     * (non-javadoc)
     */
    private String getTimeWindow(long timeWindow) {
    	if(timeWindow <= MILLISECOND)
    		return "";
    	long[] measures = new long[]{WEEK,DAY,HOUR,MINUTE,SECOND,MILLISECOND};
    	int pos = 0;
    	long d = 0;
    	while(pos < measures.length) {
    		long measure = measures[pos];
    		if(timeWindow > measure) {
    			d = timeWindow / measure;
    			if(d*measure == timeWindow)
    				break;
    		}
    		d = 0;
    		pos+=1;
    	}
    	switch(pos) {
    	case 0:
    		return String.format(" group by time(%sw)",d);
    	case 1:
    		return String.format(" group by time(%sd)",d);
    	case 2:
    		return String.format(" group by time(%sh)",d);
    	case 3:
    		return String.format(" group by time(%sm)",d);
    	case 4:
    		return String.format(" group by time(%ss)",d);
    	case 5:
    		return String.format(" group by time(%sms)",d);
    	default: 
    		return "";
    	}    	
    }
    
    /*
     * (non-javadoc)
     * 
     */
    private String getSelectFunction(String column, String function) {
    	String select = null;
    	if(column==null)
    		return null;
    	if(function == null)
    		return column;
    	
		switch(function) {
    		case "avg":
    			select = String.format("MEAN(%s)",column);
    			break;
    		case "count":
    			select = String.format("COUNT(%s)",column);
    			break;
    		case "countDistinct":
    			select = String.format("COUNT(DISTINCT(%s))",column);
    			break;
    		case "distinct":
    			select = String.format("DISTINCT(%s)",column);
    			break;
    		case "max":
    			select = String.format("MAX(%s)",column);
    			break;
    		case "median":
    			select = String.format("MEDIAN(%s)",column);
    			break;
    		case "min":
    			select = String.format("MIN(%s)",column);
    			break;
    		case "sqsum":
    			select = String.format("SQRT(SUM(%s))",column,column);
    			break;
    		case "sum":
    			select = String.format("SUM(%s)",column);
    			break;
    		default:
    			select = column;   
    	}
		return select;
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
     * the InfluxDB measurement whose name is also passed as parameter, and compliant with tags dictionary 
     * argument.
     * 
     * @param measurement the name of the InfluxDB measurement in which searching the points
     * @param tags the dictionary of tags allowing to parameterize the research
     * @param column the String name of the column on which  the specified aggregation function applies
     * @param function the String name of the aggregation function applying
     * @param timeWindow the time window of the specified aggregation function
     * 
     * @return the JSON formated String result of the research
     */
    public String get(String measurement, Dictionary<String,String> tags, String column, String function, long timeWindow) {
    	String select = getSelectFunction(column, function);
    	if(select == null)
    		return null;
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
    	String window = getTimeWindow(timeWindow);
    	Query query = new Query(String.format("SELECT %s%s%s%s", select, from, where, window),database);
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
     * the InfluxDB measurement whose name is also passed as parameter, and compliant with tags dictionary 
     * argument.
     * 
     * @param measurement the name of the InfluxDB measurement in which searching the points
     * @param tags the dictionary of tags allowing to parameterize the research
     * @param column the String name of the column on which  the specified aggregation function applies
     * @param function the String name of the aggregation function applying
     * @param timeWindow the time window of the specified aggregation function
     * @param start the String formated date defining the chronological beginning of records in which to search 
     * 
     * @return the JSON formated String result of the research
     */
    public String get(String measurement, Dictionary<String,String> tags, String column, String function, long timeWindow, String start) {
    	if(start == null || !isValidDate(start))
    		return get(measurement, tags, column, function, timeWindow);
    	String select = getSelectFunction(column, function);
    	if(select == null)
    		return null;
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
    	String window = getTimeWindow(timeWindow);
    	Query query = new Query(String.format("SELECT %s%s%s%s", select, from, where, window),database);
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
     * Returns the String formated values for the columns List passed as parameter, of the records from 
     * the InfluxDB measurement whose name is also passed as parameter, and compliant with tags dictionary 
     * argument.
     * 
     * @param measurement the name of the InfluxDB measurement in which searching the points
     * @param tags the dictionary of tags allowing to parameterize the research
     * @param column the String name of the column on which  the specified aggregation function applies
     * @param function the String name of the aggregation function applying
     * @param timeWindow the time window of the specified aggregation function
     * @param start the String formated date defining the chronological beginning of records in which to search 
     * @param end the String formated date defining the chronological ending of records in which to search 
     * 
     * @return the JSON formated String result of the research
     */
    public String get(String measurement, Dictionary<String,String> tags, String column, String function, long timeWindow, String start, String end) {
    	if(end == null || !isValidDate(end))
    		return get(measurement, tags, column, function, timeWindow, start);
    	String select = getSelectFunction(column, function);
    	if(select == null)
    		return null;
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
    	String window = getTimeWindow(timeWindow);
    	Query query = new Query(String.format("SELECT %s%s%s%s", select, from, where, window),database);
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
