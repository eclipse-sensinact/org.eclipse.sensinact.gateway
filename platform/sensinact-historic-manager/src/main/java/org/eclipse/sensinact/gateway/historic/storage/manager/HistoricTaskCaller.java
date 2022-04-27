/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.historic.storage.manager;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.annotation.TaskCommand;
import org.eclipse.sensinact.gateway.generic.annotation.TaskExecution;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricProvider;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricSpatialRequest;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricSpatioTemporalRequest;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricTemporalRequest;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.TemporalDTO;
import org.osgi.dto.DTO;

/**
 *
 */
@TaskExecution
public class HistoricTaskCaller {

    private enum AggregationType { 
    	COUNT, 
    	MEAN, 
    	SUM, 
    	SUM_SQUARE, 
    	MIN, 
    	MAX, 
    	MEDIAN, 
    	DISTINCT 
    };

	private static class HistoricKey {
		
		String sensinactId, konceptId, kapabilityId, function, region; 
		Instant fromTime, toTime;
		long timeWindow;
		
		HistoricKey(String sensinactId, String konceptId, String kapabilityId, Instant fromTime, Instant toTime,
			String function, long timeWindow, String region){
			this.sensinactId = sensinactId;
			this.konceptId = konceptId; 
			this.kapabilityId = kapabilityId;
			this.fromTime = fromTime;
			this.toTime = toTime;
			this.function = function;
			this.timeWindow = timeWindow;
			this.region = region;
		}

		@Override
		public int hashCode() {
			return Objects.hash(fromTime, function, kapabilityId, konceptId, region, sensinactId, timeWindow, toTime);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			HistoricKey other = (HistoricKey) obj;
			return Objects.equals(fromTime, other.fromTime) && Objects.equals(function, other.function)
					&& Objects.equals(kapabilityId, other.kapabilityId) && Objects.equals(konceptId, other.konceptId)
					&& Objects.equals(region, other.region) && Objects.equals(sensinactId, other.sensinactId)
					&& timeWindow == other.timeWindow && Objects.equals(toTime, other.toTime);
		}
		
		                                  
	}

	private Mediator mediator;
	
	private WeakHashMap<HistoricKey, DTO[]> cache;
	
	public HistoricTaskCaller(Mediator mediator){
		this.mediator = mediator;
		this.cache = new WeakHashMap<>();
	}
	
	private ZonedDateTime parseTime(String time) {
		try {
			long longTime = Long.parseLong(time);
			return Instant.ofEpochMilli(longTime).atOffset(UTC).toZonedDateTime();
		} catch (NumberFormatException nfe) {
			return ZonedDateTime.parse(time);
		}
	}
	
	@TaskCommand(method = Task.CommandType.GET, target = "/historicManager/history/requester")
	public String get(String uri, String attributeName, String provider, String service, String resource, String from, String to, 
		String function, String window, String region) {
		try {
			String result = null;
			long w = Long.parseLong(window);
			
			ZonedDateTime dtf = parseTime(from);
			ZonedDateTime dtt = parseTime(to);
			
			if(function!=null && !"#NONE#".equals(function) && w > 0) {
				AggregationType aggregation = null;		
				try {
					aggregation = AggregationType.valueOf(function);			
				}catch(Exception e) {
					aggregation  = AggregationType.MEAN;
				}
				result = getAggregatedTemporalHistory(provider,service,resource, dtf, dtt, aggregation, w);
			} else
				result = getTemporalHistory(provider,service,resource, dtf, dtt);
				
			//JSONObject obj = new JSONObject();
			//obj.put("history", result);
			return result;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String getTemporalHistory(String provider, String service, String resource, ZonedDateTime from, ZonedDateTime to) {
		HistoricTemporalRequest request = createTemporalRequest(provider, service, resource, from, to);
		if(request == null)
			return "[]";		
		HistoricKey historic = new HistoricKey(provider, service, resource, from.toInstant(), to.toInstant(), null, 0, null);
		DTO[] data = this.cache.get(historic);
		if(data == null)
			data = request.execute().get(String.format("%s/%s/%s", provider, service, resource))
				.toArray(new TemporalDTO[] {});
		this.cache.put(historic,data);
		final AtomicBoolean first = new AtomicBoolean(true);
		return Arrays.stream(data).<StringBuilder>collect(
			()->{return new StringBuilder();},
			(sb,t)->{
				if(first.get()) {
					sb.append("[");
					first.set(false);
				}else
					sb.append(",");
				sb.append("{");
				sb.append("\"tagID\":" );
				sb.append(((TemporalDTO)t).tagID);
				sb.append(",\"timestamp\":" );
				sb.append(((TemporalDTO)t).timestamp);
				sb.append(",\"value\":\"" );
				sb.append(((TemporalDTO)t).value);
				sb.append("\"}");
			},
			(sb1,sb2)->{sb1.append(sb2.toString());}
		).append("]").toString();
	}	
	
	private String getAggregatedTemporalHistory(String provider, String service, String resource, ZonedDateTime from, ZonedDateTime to,
			AggregationType method, long period) {		
		
		HistoricTemporalRequest request = createTemporalRequest(provider, service, resource, from, to);
		if(request == null)
			return "[]";
		
		request.setFunction(method.name().toLowerCase());
		request.setTemporalWindow(period);
		
		HistoricKey historic = new HistoricKey(provider, service, resource, from.toInstant(), to.toInstant(), method.name(), period, null);
		DTO[] data = this.cache.get(historic);
		if(data == null)
			data = request.execute().get(String.format("%s/%s/%s", provider, service, resource))
				.toArray(new TemporalDTO[] {});
		this.cache.put(historic,data);
		final AtomicBoolean first = new AtomicBoolean(true);
		return Arrays.stream(data).<StringBuilder>collect(
			()->{return new StringBuilder();},
			(sb,t)->{
				if(first.get()) {
					sb.append("[");
					first.set(false);
				} else
					sb.append(",");
				sb.append("{");
				sb.append("\"tagID\":" );
				sb.append(((TemporalDTO)t).tagID);
				sb.append(",\"timestamp\":" );
				sb.append(((TemporalDTO)t).timestamp);
				sb.append(",\"value\":\"" );
				sb.append(((TemporalDTO)t).value);
				sb.append("\"}");
			},
			(sb1,sb2)->{sb1.append(sb2.toString());}
		).append("]").toString();
	}
	
	private HistoricTemporalRequest createTemporalRequest(String provider, String service, String resource, 
			ZonedDateTime fromTime, ZonedDateTime toTime){		
		
		HistoricTemporalRequest request = this.mediator.callService(HistoricProvider.class, 
			new Executable<HistoricProvider,HistoricTemporalRequest>(){
				@Override
				public HistoricTemporalRequest execute(HistoricProvider provider) throws Exception {
					return provider.newTemporalRequest();
				}
		});		
		if(request == null)
			return null;
		request.addTargetResource(provider, service, resource);
		request.setHistoricStartTime(fromTime);
		request.setHistoricEndTime(toTime);
		
		return request;		
	}
	
	private HistoricSpatialRequest createSpatialRequest(String provider, String service, String resource, 
		String region, ZonedDateTime time){
		
		HistoricSpatialRequest request = this.mediator.callService(HistoricProvider.class, 
			new Executable<HistoricProvider,HistoricSpatialRequest>(){
				@Override
				public HistoricSpatialRequest execute(HistoricProvider provider) throws Exception {
					return provider.newSpatialRequest();
				}
		});		
		if(request == null)
			return null;
		request.addTargetResource(provider, service, resource);
		request.setHistoricTime(time);
		request.setRegion(region);
		
		return request;		
	}

	private HistoricSpatioTemporalRequest createSpatioTemporalRequest(String provider, String service, String resource, 
		String region, ZonedDateTime fromTime, ZonedDateTime toTime){
		
		HistoricSpatioTemporalRequest request = this.mediator.callService(HistoricProvider.class, 
			new Executable<HistoricProvider,HistoricSpatioTemporalRequest>(){
				@Override
				public HistoricSpatioTemporalRequest execute(HistoricProvider provider) throws Exception {
					return provider.newSpatioTemporalRequest();
				}
		});		
		if(request == null)
			return null;
		request.addTargetResource(provider, service, resource);
		request.setHistoricStartTime(fromTime);
		request.setHistoricEndTime(toTime);
		request.setRegion(region);
		
		return request;		
	}
}
