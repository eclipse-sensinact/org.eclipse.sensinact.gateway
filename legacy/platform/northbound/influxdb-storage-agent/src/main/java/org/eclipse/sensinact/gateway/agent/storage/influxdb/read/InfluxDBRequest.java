/*********************************************************************
* Copyright (c) 2021 Kentyou
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.agent.storage.influxdb.read;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricRequest;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.TemporalDTO;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDBTagDTO;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbConnector;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbDatabase;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class InfluxDBRequest<T> implements HistoricRequest<T>{
	
	private static final Logger LOG = LoggerFactory.getLogger(InfluxDBRequest.class);
	
	private static final List<String> REPLACEMENTS = Arrays.asList(":",".","-","[","]","(",")","{","}","+");	
	
	protected String database;
	protected String measurement;
	
	protected List<ResourceInfo> resources = new ArrayList<>();
	
	public static class ResourceInfo {
		public final String provider;
		public final String service;
		public final String resource;
		
		public ResourceInfo(String provider, String service, String resource) {
			this.provider = provider;
			this.service = service;
			this.resource = resource;
		}

		public String getPath() {
			return String.format("%s/%s/%s", provider, service, resource);
		}
	}

	protected InfluxDbConnector influxDbConnector;

	public InfluxDBRequest(InfluxDbConnector influxDbConnector) {
		this.influxDbConnector = influxDbConnector;
	}
	
	public void setMeasurement(String measurement) {
		this.measurement = measurement;
	}
	
	public void setDatabase(String database) {
		this.database = database;
	}

	@Override
	public void addTargetResource(String provider, String service, String resource) {
		resources.add(new ResourceInfo(provider, service, resource));
	}

	protected InfluxDBTagDTO getDataSourcePath(ResourceInfo ri) {
	    InfluxDBTagDTO historicAttributeDTO = new InfluxDBTagDTO();
		historicAttributeDTO.name="path";		
		String datasource = null;
		if(ri.resource == null) {
			
			if(ri.provider == null)
				return null;
			
			datasource = ri.provider;			
			for(String replacement:REPLACEMENTS)
				datasource = datasource.replace(replacement,"\\".concat(replacement));
			
			historicAttributeDTO.value=new StringBuilder(
				).append("/("
				).append(datasource
				).append("\\/([^\\/]+\\/?)+)/"
				).toString();			
			historicAttributeDTO.pattern=true;			
			return historicAttributeDTO;			
		}			
		if(ri.provider == null){
			if(ri.service == null)
				return null;
			
			datasource = ri.service;				
			for(String replacement:REPLACEMENTS)
				datasource = datasource.replace(replacement,"\\".concat(replacement));
			
			datasource = new StringBuilder(
					).append("/([^\\/]+\\/"
					).append(datasource
					).append("\\/[^\\/]+)/"
					).toString();
			historicAttributeDTO.value=datasource;
			historicAttributeDTO.pattern=true;
			return historicAttributeDTO;
			
		}
		historicAttributeDTO.value=new StringBuilder(
		    ).append("/"
			).append(ri.provider
			).append("/"
			).append(ri.service
			).append("/"
			).append(ri.resource
			).append("/"
			).append(DataResource.VALUE
			).toString();			
		historicAttributeDTO.pattern=false;			
		return historicAttributeDTO;	
	}

	protected InfluxDBTagDTO getResource(ResourceInfo ri) {
		if(ri.resource == null) 
			return null;
	    InfluxDBTagDTO historicAttributeDTO = new InfluxDBTagDTO();
		historicAttributeDTO.name="resource";	
		historicAttributeDTO.value=ri.resource;
		historicAttributeDTO.pattern=false;
		return historicAttributeDTO;
	}
	
	private List<TemporalDTO> buildTemporalDTOList(QueryResult result){
		List<List<Object>> serie = null;
		try {
		   	serie = result.getResults().get(0).getSeries().get(0).getValues();
		} catch(NullPointerException e ) {
			return Collections.<TemporalDTO>emptyList();
		}
		List<TemporalDTO> list = new ArrayList<>();
	   	for(int i=0;i<serie.size();i++){ 
	   		TemporalDTO dto = null;
	   		try {
				dto = new TemporalDTO();
				dto.tagID = i;
				dto.timestamp = Instant.parse(String.valueOf(serie.get(i).get(0))).toEpochMilli();
				dto.value = String.valueOf(serie.get(i).get(1));
				list.add(dto);
	   		} catch(Exception e) {
	   			LOG.error(e.getMessage(),e);
	   			if(dto!=null) {
	   				dto.error=e.getMessage();
	   				list.add(dto);
	   			}
	   		}
		}
		return list;
	}
	
	/**
    * Returns the String formated values for the columns List passed as parameter, of the 
    * records from the InfluxDB measurement whose name is also passed as parameter, and 
    * compliant with tags dictionary argument.
    * 
    * @param measurement the name of the InfluxDB measurement in which searching the points
    * @param tags the dictionary of tags allowing to parameterize the research
    * @param columns the Strings List defining the fields to be provided
    * @return the JSON formated String result of the research
    */
   protected List<TemporalDTO> get(InfluxDbDatabase db, String measurement, List<InfluxDBTagDTO> tags) {
   	   QueryResult result = db.getResult(measurement, tags, Arrays.asList("time","value"));
       List<TemporalDTO> list = buildTemporalDTOList(result);
   	   return list;
   }

   /**
    * Returns the String formated aggregated values for the column passed as parameter, of the records from 
    * the InfluxDB measurement whose name is also passed as parameter, and compliant with tags dictionary 
    * argument.
    * 
    * @param measurement the name of the InfluxDB measurement in which searching the points
    * @param tags the dictionary of tags allowing to parameterize the research
    * @param function the String name of the aggregation function applying
    * @param timeWindow the time window of the specified aggregation function
    * @param column the String name of the column on which  the specified aggregation function applies
    * @return the JSON formated String result of the research
    */
   protected List<TemporalDTO> get(InfluxDbDatabase db, String measurement, List<InfluxDBTagDTO> tags, String function, long timeWindow) {
   	   QueryResult result = db.getResult(measurement, tags, "value", function, timeWindow);
       List<TemporalDTO> list = buildTemporalDTOList(result);
   	   return list;
   }

   /**
    * Returns the String formated values for the columns List passed as parameter, of the records from 
    * the InfluxDB measurement whose name is also passed as parameter, compliant with tags dictionary 
    * argument and starting from the specified String formated start datetime. 
    * 
    * @param measurement the name of the InfluxDB measurement in which searching the points
    * @param tags the dictionary of tags allowing to parameterize the research
    * @param columns the Strings List defining the fields to be provided
    * @param start the LocalDateTime defining the chronological beginning of records in which to search 
    * @return the JSON formated String result of the research
    */
   protected List<TemporalDTO> get(InfluxDbDatabase db, String measurement, List<InfluxDBTagDTO> tags, ZonedDateTime start) {
   	   QueryResult result = db.getResult(measurement, tags, Arrays.asList("time","value"), start);
       List<TemporalDTO> list = buildTemporalDTOList(result);
   	   return list;
   }	

   /**
    * Returns the String formated aggregated values for the column passed as parameter, of the records from 
    * the InfluxDB measurement whose name is also passed as parameter, and compliant with tags dictionary 
    * argument.
    * 
    * @param measurement the name of the InfluxDB measurement in which searching the points
    * @param tags the dictionary of tags allowing to parameterize the research
    * @param column the String name of the column on which  the specified aggregation function applies
    * @param function the String name of the aggregation function applying
    * @param timeWindow the time window of the specified aggregation function
    * @param start the LocalDateTime defining the chronological beginning of records in which to search 
    * 
    * @return the JSON formated String result of the research
    */
   protected List<TemporalDTO> get(InfluxDbDatabase db, String measurement,  List<InfluxDBTagDTO> tags, String function, long timeWindow, ZonedDateTime start) {
   	   QueryResult result = db.getResult(measurement, tags, "value", function, timeWindow, start);
       List<TemporalDTO> list = buildTemporalDTOList(result);
   	   return list;
   }
   
   /**
    * Returns the String formated values for the columns List passed as parameter, of the records from 
    * the InfluxDB measurement whose name is also passed as parameter, compliant with tags dictionary 
    * argument, between both start and end datetimes. 
    * 
    * @param measurement the name of the InfluxDB measurement in which searching the points
    * @param columns the Strings List defining the fields to be provided
    * @param tags the dictionary of tags allowing to parameterize the research
    * @param start the LocalDateTime defining the chronological beginning of records in which to search 
    * @param end the LocalDateTime defining the chronological ending of records in which to search 
    * 
    * @return the JSON formated String result of the research
    */
   protected List<TemporalDTO> get(InfluxDbDatabase db, String measurement, List<InfluxDBTagDTO> tags, ZonedDateTime start, ZonedDateTime end) { 
   	   QueryResult result = db.getResult(measurement, tags, Arrays.asList("time","value"), start, end);
       List<TemporalDTO> list = buildTemporalDTOList(result);
   	   return list;
   }	
   
	/**
	 * Returns the String formated aggregated values for the column passed as parameter, of the records from 
	 * the InfluxDB measurement whose name is also passed as parameter, and compliant with tags dictionary 
	 * argument.
	 * 
	 * @param measurement the name of the InfluxDB measurement in which searching the points
	 * @param tags the dictionary of tags allowing to parameterize the research
	 * @param column the String name of the column on which  the specified aggregation function applies
	 * @param function the String name of the aggregation function applying
	 * @param timeWindow the time window of the specified aggregation function
	 * @param start the LocalDateTime defining the chronological beginning of records in which to search 
	 * @param end the LocalDateTime defining the chronological ending of records in which to search 
	 * 
	 * @return the JSON formated String result of the research
	*/
	protected List<TemporalDTO> get(InfluxDbDatabase db, String measurement,  List<InfluxDBTagDTO> tags, String function, long timeWindow, ZonedDateTime start, ZonedDateTime end) {
		QueryResult result = db.getResult(measurement, tags, "value", function, timeWindow, start, end);
	    List<TemporalDTO> list = buildTemporalDTOList(result);
	 	return list;
	}    

}
