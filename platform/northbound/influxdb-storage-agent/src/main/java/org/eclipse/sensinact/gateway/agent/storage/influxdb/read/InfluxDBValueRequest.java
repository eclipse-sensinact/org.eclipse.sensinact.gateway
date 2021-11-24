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
package org.eclipse.sensinact.gateway.agent.storage.influxdb.read;

import static java.util.stream.Collectors.toList;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricValueRequest;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.TemporalDTO;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDBTagDTO;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbConnector;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbDatabase;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;


public class InfluxDBValueRequest extends AbstractInfluxDBValueRequest<TemporalDTO> implements HistoricValueRequest{

	public InfluxDBValueRequest(InfluxDbConnector influxDbConnector) {
		super(influxDbConnector);
	}

	@Override
	public Map<String,TemporalDTO> execute() {
		InfluxDbDatabase db = influxDbConnector.getIfExists(super.database);
		if(db == null || resources.isEmpty())
			return Collections.emptyMap();
		
		List<String> measures = Stream.of("_num", "_str")
				.map(measurement::concat)
				.collect(toList());
		
		List<List<InfluxDBTagDTO>> tags = resources.stream()
				.map(this::getDataSourcePath)
				.map(Collections::singletonList)
				.collect(toList());
		
		QueryResult results = db.getPointInTimeResults(measures,tags,time);
		
		
		Map<String, TemporalDTO> map = new HashMap<>();
		
		for(int i = 0; i < resources.size(); i++) {
			String key = resources.get(i).getPath();
			
			Result r = results.getResults().get(i);
			if(r.getSeries() == null) {
				map.put(key, null);
			} else {
				TemporalDTO dto = new TemporalDTO();
				dto.tagID = 0;
				Series series = r.getSeries().get(0);
				for (int j = 0; j < series.getColumns().size(); j++) {
					String column = series.getColumns().get(j);
					Object value = series.getValues().get(0).get(j);
					
					switch(column) {
						case "time":
							dto.timestamp = value == null ? 0 : 
								OffsetDateTime.parse(value.toString()).toInstant().toEpochMilli();
							break;
						case "value":
							dto.value = value == null ? null : value.toString();
							break;
						default:
							break;
					}
				}
				map.put(key, dto);
			}
		}
		
		return map;
	}
}
