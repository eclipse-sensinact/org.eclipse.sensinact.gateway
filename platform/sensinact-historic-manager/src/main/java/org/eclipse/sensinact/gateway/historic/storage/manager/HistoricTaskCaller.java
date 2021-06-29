/*
 * Copyright (c) 2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.historic.storage.manager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
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
import org.json.JSONObject;
import org.osgi.dto.DTO;

/**
 *
 */
@TaskExecution
public class HistoricTaskCaller {

	private static final ZoneOffset OFFSET = ZoneOffset.systemDefault().getRules().getOffset(Instant.now());
	
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

	private class HistoricKey {
		
		String sensinactId, konceptId, kapabilityId, function, region; 
		LocalDateTime fromTime, toTime;
		long timeWindow;
		
		HistoricKey(String sensinactId, String konceptId, String kapabilityId, LocalDateTime fromTime, LocalDateTime toTime,
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
			int hash = this.sensinactId.hashCode();
			hash += this.konceptId.hashCode();
			hash += this.kapabilityId.hashCode();
			hash += this.fromTime.hashCode();
			hash += this.toTime.hashCode();
			hash += this.function!=null?this.function.hashCode():0;
			hash += this.function!=null?this.timeWindow:0;
			hash += this.region!=null?this.region.hashCode():0;
			return hash;
		}                                     
	}

	private Mediator mediator;
	
	private WeakHashMap<HistoricKey, DTO[]> cache;
	
	public HistoricTaskCaller(Mediator mediator){
		this.mediator = mediator;
		this.cache = new WeakHashMap<>();
	}
	
	@TaskCommand(method = Task.CommandType.GET, target = "/historicManager/history/requester")
	public String get(String uri, String attributeName, String provider, String service, String resource, String from, String to, 
		String function, String window, String region) {
		try {
			String result = null;
			long f = Long.parseLong(from);
			long t = Long.parseLong(to);
			long w = Long.parseLong(window);
			
			LocalDateTime dtf = LocalDateTime.ofEpochSecond(f/1000l, (int) (f - ((f/1000) * 1000))*1000, OFFSET);
			LocalDateTime dtt = LocalDateTime.ofEpochSecond(t/1000l, (int) (t - ((t/1000) * 1000))*1000, OFFSET);
			
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
	
	private String getTemporalHistory(String provider, String service, String resource, LocalDateTime from, LocalDateTime to) {
		HistoricTemporalRequest request = createTemporalRequest(provider, service, resource, from, to);
		if(request == null)
			return "[]";		
		HistoricKey historic = new HistoricKey(provider, service, resource, from, to, null, 0, null);
		DTO[] data = this.cache.get(historic);
		if(data == null)
			data = request.execute().toArray(new TemporalDTO[] {});
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
	
	private String getAggregatedTemporalHistory(String provider, String service, String resource, LocalDateTime from, LocalDateTime to,
			AggregationType method, long period) {		
		
		HistoricTemporalRequest request = createTemporalRequest(provider, service, resource, from, to);
		if(request == null)
			return "[]";
		
		request.setFunction(method.name().toLowerCase());
		request.setTemporalWindow(period);
		
		HistoricKey historic = new HistoricKey(provider, service, resource, from, to, method.name(), period, null);
		DTO[] data = this.cache.get(historic);
		if(data == null)
			data = request.execute().toArray(new TemporalDTO[] {});
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
			LocalDateTime fromTime, LocalDateTime toTime){		
		
		HistoricTemporalRequest request = this.mediator.callService(HistoricProvider.class, 
			new Executable<HistoricProvider,HistoricTemporalRequest>(){
				@Override
				public HistoricTemporalRequest execute(HistoricProvider provider) throws Exception {
					return provider.newTemporalRequest();
				}
		});		
		if(request == null)
			return null;
		request.setServiceProviderIdentifier(provider);
		request.setServiceIdentifier(service);
		request.setResourceIdentifier(resource);
		request.setHistoricStartTime(fromTime);
		request.setHistoricEndTime(toTime);
		
		return request;		
	}
	
	private HistoricSpatialRequest createSpatialRequest(String provider, String service, String resource, 
		String region, LocalDateTime fromTime, LocalDateTime toTime){
		
		HistoricSpatialRequest request = this.mediator.callService(HistoricProvider.class, 
			new Executable<HistoricProvider,HistoricSpatialRequest>(){
				@Override
				public HistoricSpatialRequest execute(HistoricProvider provider) throws Exception {
					return provider.newSpatialRequest();
				}
		});		
		if(request == null)
			return null;
		request.setServiceProviderIdentifier(provider);
		request.setServiceIdentifier(service);
		request.setResourceIdentifier(resource);
		request.setHistoricStartTime(fromTime);
		request.setHistoricEndTime(toTime);
		request.setRegion(region);
		
		return request;		
	}

	private HistoricSpatioTemporalRequest createSpatioTemporalRequest(String provider, String service, String resource, 
		String region, LocalDateTime fromTime, LocalDateTime toTime){
		
		HistoricSpatioTemporalRequest request = this.mediator.callService(HistoricProvider.class, 
			new Executable<HistoricProvider,HistoricSpatioTemporalRequest>(){
				@Override
				public HistoricSpatioTemporalRequest execute(HistoricProvider provider) throws Exception {
					return provider.newSpatioTemporalRequest();
				}
		});		
		if(request == null)
			return null;
		request.setServiceProviderIdentifier(provider);
		request.setServiceIdentifier(service);
		request.setResourceIdentifier(resource);
		request.setHistoricStartTime(fromTime);
		request.setHistoricEndTime(toTime);
		request.setRegion(region);
		
		return request;		
	}
}
