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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricTemporalRequest;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.TemporalDTO;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbConnector;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbDatabase;


public class InfluxDBTemporalRequest extends AbstractInfluxDBTemporalRequest<List<TemporalDTO>> implements HistoricTemporalRequest {

	public InfluxDBTemporalRequest(InfluxDbConnector influxDbConnector) {
		super(influxDbConnector);
	}

	@Override
	public Map<String,List<TemporalDTO>> execute() {
		InfluxDbDatabase db = influxDbConnector.getIfExists(super.database);
		if(db == null || resources.isEmpty())
			return Collections.emptyMap();
		ResourceInfo ri = resources.get(0);
		List<TemporalDTO> s;
		if(this.function == null) {
			s = super.get(db,
					super.measurement.concat("_num"),
					Arrays.asList(super.getDataSourcePath(ri), super.getResource(ri)),
					start,
					end);
	     } else {
			s = super.get(db,
					super.measurement.concat("_num"),
					Arrays.asList(super.getDataSourcePath(ri), super.getResource(ri)),
					this.function, 
					this.temporalWindow <=0?10000:temporalWindow,
					start,
					end);
		}
		return Collections.singletonMap(ri.getPath(), s);
	}

}
