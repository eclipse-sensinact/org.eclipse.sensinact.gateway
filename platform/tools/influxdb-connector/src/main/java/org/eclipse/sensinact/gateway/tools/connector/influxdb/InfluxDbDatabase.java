/* 
 * Copyright 2021 Kentyou 
 * Proprietary and confidential
 * 
 * All Rights Reserved. 
 * Unauthorized copying of this file is strictly prohibited
 */
package org.eclipse.sensinact.gateway.tools.connector.influxdb;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;


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

    private InfluxDB influxDB;
	private String database;
	private final ZoneOffset offset;

	/**
	 * Constructor
	 * 
	 * @param influxDB the {@link InfluxDB} wrapped by the InfluxDbDatabase to be instantiated
	 * @param database the String database name
	 */
	public InfluxDbDatabase(InfluxDB influxDB, String database) {
		this(influxDB,database,"autogen");
	}
	
	/**
	 * Constructor
	 * 
	 * @param influxDB the {@link InfluxDB} wrapped by the InfluxDbDatabase to be instantiated
	 * @param database the String database name
	 * @param retention the String retention policy applying
	 */
	public InfluxDbDatabase(InfluxDB influxDB, String database, String retention) {
		this.influxDB = influxDB;
		this.database = database;
		this.offset = ZoneOffset.systemDefault().getRules().getOffset(Instant.now());

        influxDB.setDatabase(database);
        influxDB.setRetentionPolicy(retention);
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
    
    private String getSelectFunction(String column, String function) {
    	String select = null;
    	if(column ==null || column.trim().length()==0)
    		return null;
    	if(function == null) 
    		return column;   	
    	
		switch(function) {
		   case "avg":
		   case "mean":
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
    		case "sum_square":
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
    
    private String buildWhereClause(List<InfluxDBTagDTO> tags) {
    	String where=null;
    	if(tags == null || tags.isEmpty()) {
    		where="";
    		return where;
    	}
    	StringBuilder builder = new StringBuilder();
   		builder.append(" WHERE ");
   		for(int i=0;i<tags.size();i++) {  
   			InfluxDBTagDTO t = tags.get(i); 
			if(i > 0)
				builder.append(" AND ");
		    builder.append(t.name);
		    builder.append(t.pattern?"=~":"=");
		    builder.append(t.pattern?" ":"'");
		    builder.append(t.value);
			builder.append(t.pattern?" ":"'");
   		}
		where = builder.toString();
		return where;
    }
   
    public QueryResult getResult(String measurement, List<InfluxDBTagDTO> tags,  List<String> columns) {
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
    	String where = buildWhereClause(tags);
    	Query query = new Query(String.format("SELECT %s%s%s", select, from, where),database);
    	QueryResult result = this.influxDB.query(query);
    	return result;
    }

    public QueryResult getResult(String measurement, List<InfluxDBTagDTO> tags, String column, String function, long timeWindow) {
    	String select = getSelectFunction(column, function);
    	if(select == null)
    		return null;
    	String from = String.format(" FROM %s " , measurement);
    	
    	String where = buildWhereClause(tags);
    	String window = getTimeWindow(timeWindow);
    	Query query = new Query(String.format("SELECT %s%s%s%s", select, from, where, window),database);
    	QueryResult result = this.influxDB.query(query);
    	return result;
    }

    public QueryResult getResult(String measurement, List<InfluxDBTagDTO> tags, List<String> columns, LocalDateTime start) {
    	if(start == null)
    		return getResult(measurement, tags, columns);
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
    	
    	String where =  buildWhereClause(tags);

    	SimpleDateFormatProvider formatProvider = THREAD_LOCAL_FORMATS.get();
    	
    	SimpleDateFormat df = formatProvider.iterator().next();
    	
    	String startDate = df.format(new Date(start.toInstant(offset).toEpochMilli()));
    	
    	THREAD_LOCAL_FORMATS.remove();
    	if(where.length() == 0)
    		where = String.format(" WHERE time > '%s'", startDate);
    	else {
    		StringBuilder builder = new StringBuilder();
    		builder.append(where);
    		builder.append(String.format(" AND time > '%s'", startDate));
    		where = builder.toString();
    	}
    	Query query = new Query(String.format("SELECT %s%s%s", select, from, where),database);
    	QueryResult result = this.influxDB.query(query);
    	return result;
    }	

    public QueryResult getResult(String measurement,  List<InfluxDBTagDTO> tags, String column, String function, long timeWindow, LocalDateTime start) {
    	if(start == null)
    		return getResult(measurement, tags, column, function, timeWindow);
    	String select = getSelectFunction(column, function);
    	if(select == null)
    		return null;
    	String from = String.format(" FROM %s " , measurement);
    	String where =  buildWhereClause(tags);
    	SimpleDateFormatProvider formatProvider = THREAD_LOCAL_FORMATS.get();
    	
    	SimpleDateFormat df = formatProvider.iterator().next();    	
    	String startDate = df.format(new Date(start.toInstant(offset).toEpochMilli()));
    	
    	THREAD_LOCAL_FORMATS.remove();
    	if(where.length() == 0)
    		where = String.format(" WHERE time > '%s'", startDate);
    	else {
    		StringBuilder builder = new StringBuilder();
    		builder.append(where);
    		builder.append(String.format(" AND time > '%s'", startDate));
    		where = builder.toString();
    	} 
    	String window = getTimeWindow(timeWindow);
    	Query query = new Query(String.format("SELECT %s%s%s%s", select, from, where, window),database);
    	QueryResult result = this.influxDB.query(query);
    	return result;
    }

    public QueryResult getResult(String measurement, List<InfluxDBTagDTO> tags, List<String> columns,  LocalDateTime start, LocalDateTime end) {    	
    	if(end == null)
    		return this.getResult(measurement, tags,  columns, start);
    	String select = null;
    	if(columns==null || columns.isEmpty())
    		select = "* ";
    	else
    		select = columns.stream().collect(StringBuilder::new,(b,s)-> { 
    			b.append(s); 
    			b.append(",");
    		}, (h,t)->h.append(t.toString())).toString();
    	select =select.substring(0,select.length()-1);
    	    	
    	SimpleDateFormatProvider formatProvider = THREAD_LOCAL_FORMATS.get();
    	SimpleDateFormat df = formatProvider.iterator().next();
    	
    	String startDate=null;
    	String endDate=null;
    	
    	startDate = df.format(new Date(start.toInstant(offset).toEpochMilli()));
    	endDate = df.format(new Date(end.toInstant(offset).toEpochMilli()));
    	
    	THREAD_LOCAL_FORMATS.remove();
    	
    	String from = String.format(" FROM %s " , measurement);
    	String where =  buildWhereClause(tags);
    	if(where.length() == 0)
    		where = String.format(" WHERE time > '%s' AND time < '%s'", startDate, endDate);
    	else {
    		StringBuilder builder = new StringBuilder();
    		builder.append(where);
    		builder.append(String.format(" AND time > '%s' AND time < '%s'", startDate, endDate));
    		where = builder.toString();
    	}
    	Query query = new Query(String.format("SELECT %s%s%s", select, from, where),database);
	    QueryResult result = this.influxDB.query(query);
    	return result;
    }	
	
    public QueryResult getResult(String measurement,  List<InfluxDBTagDTO> tags, String column, String function, long timeWindow, LocalDateTime start, LocalDateTime end) {
    	if(end == null)
    		return getResult(measurement,  tags, column, function, timeWindow, start);

    	String select = getSelectFunction(column, function);
    	if(select == null)
    		return null;
    	String from = String.format(" FROM %s " , measurement);

    	SimpleDateFormatProvider formatProvider = THREAD_LOCAL_FORMATS.get();    	
    	SimpleDateFormat df = formatProvider.iterator().next();
    	
    	String startDate = df.format(new Date(start.toInstant(offset).toEpochMilli()));
    	String endDate = df.format(new Date(end.toInstant(offset).toEpochMilli()));
    	
    	THREAD_LOCAL_FORMATS.remove();
    	String where =  buildWhereClause(tags);
    	if(where.length() == 0)
    		where = String.format(" WHERE time > '%s' AND time < '%s'", startDate, endDate);
    	else {
    		StringBuilder builder = new StringBuilder();
    		builder.append(where);
    		builder.append(String.format(" AND time > '%s' AND time < '%s'", startDate, endDate));
    		where = builder.toString();
    	} 
    	String window = getTimeWindow(timeWindow);
    	Query query = new Query(String.format("SELECT %s%s%s%s", select, from, where, window),database);
    	QueryResult result = this.influxDB.query(query);
    	return result;
    }  
}
