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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricTemporalRequest;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.TemporalDTO;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbConnector;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbDatabase;


public class InfluxDBTemporalRequest extends AbstractInfluxDBTemporalRequest<List<TemporalDTO>> implements HistoricTemporalRequest {

	public InfluxDBTemporalRequest(InfluxDbConnector influxDbConnector) {
		super(influxDbConnector);
	}

	@Override
	public List<TemporalDTO> execute() {
		InfluxDbDatabase db = influxDbConnector.getIfExists(super.database);
		if(db == null)
			return Collections.emptyList();
		List<TemporalDTO> s;
		if(this.function == null) {
			s = super.get(db,
					super.measurement.concat("_num"),
					Arrays.asList(super.getDataSourcePath(), super.getResource()),
					start,
					end);
	     } else {
			s = super.get(db,
					super.measurement.concat("_num"),
					Arrays.asList(super.getDataSourcePath(), super.getResource()),
					this.function, 
					this.temporalWindow <=0?10000:temporalWindow,
					start,
					end);
		}
		return s;
	}

}
