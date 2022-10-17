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
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.annotation.TaskCommand;
import org.eclipse.sensinact.gateway.generic.annotation.TaskExecution;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricProvider;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricValueRequest;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.TemporalDTO;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.spi.JsonProvider;

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
			JsonProvider jp = JsonProviderFactory.getProvider();
			JsonArrayBuilder array = jp.createArrayBuilder();
			
			for(Entry<String, TemporalDTO> e : data.entrySet()) {
				JsonObjectBuilder value = jp.createObjectBuilder();
				TemporalDTO dto = e.getValue();
				
				value.add("tagId", dto.tagID);
				value.add("timestamp", dto.timestamp);
				value.add("value", dto.value);

				JsonObjectBuilder container = jp.createObjectBuilder();
				container.add("path", e.getKey());
				container.add("historicValue", value);

				array.add(container);
			}
			
			return array.toString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
