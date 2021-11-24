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

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.annotation.TaskCommand;
import org.eclipse.sensinact.gateway.generic.annotation.TaskExecution;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricProvider;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricValueRequest;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.TemporalDTO;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 */
@TaskExecution
public class HistoricValueQuery {

	private Mediator mediator;
	
	public HistoricValueQuery(Mediator mediator){
		this.mediator = mediator;
	}
	
	private ZonedDateTime parseTime(String time) {
		try {
			long longTime = Long.parseLong(time);
			return Instant.ofEpochMilli(longTime).atOffset(UTC).toZonedDateTime();
		} catch (NumberFormatException nfe) {
			return ZonedDateTime.parse(time);
		}
	}
	
	@TaskCommand(method = Task.CommandType.GET, target = "/historicManager/history/single")
	public String get(String path, String[] paths, String time) {
		try {
			ZonedDateTime t = parseTime(time);
			
			HistoricValueRequest request = this.mediator.callService(HistoricProvider.class, 
					HistoricProvider::newValueRequest);
			
			if(request == null)
				return null;
				
			request.setHistoricTime(t);
			Arrays.stream(paths)
				.map(s -> s.split("/"))
				.forEach(s -> request.addTargetResource(s[0], s[1], s[2]));
			
			Map<String, TemporalDTO> data = request.execute();
			
			JSONArray array = new JSONArray();
			
			for(Entry<String, TemporalDTO> e : data.entrySet()) {
				JSONObject container = new JSONObject();
				JSONObject value = new JSONObject();
				
				container.put("path", e.getKey());
				container.put("historicValue", value);
				
				TemporalDTO dto = e.getValue();
				
				value.put("tagId", dto.tagID);
				value.put("timestamp", dto.timestamp);
				value.put("value", dto.value);

				array.put(container);
			}
			
			return array.toString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
