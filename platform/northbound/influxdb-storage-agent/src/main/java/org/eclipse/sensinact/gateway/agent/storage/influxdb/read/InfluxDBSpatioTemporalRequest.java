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

import java.util.Collections;
import java.util.List;

import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricSpatioTemporalRequest;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.SpatioTemporalDTO;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbConnector;


public class InfluxDBSpatioTemporalRequest extends InfluxDBRequest<SpatioTemporalDTO> implements HistoricSpatioTemporalRequest{

	protected String region;
	protected String function;
	protected long temporalWindow;
	
	public InfluxDBSpatioTemporalRequest(InfluxDbConnector influxDbConnector) {
		super(influxDbConnector);
	}

	@Override
	public List<SpatioTemporalDTO> execute() {
		return Collections.emptyList();
	}

	@Override
	public void setFunction(String function) {
		this.function = function;
	}

	@Override
	public void setTemporalWindow(long temporalWindow) {
		this.temporalWindow = temporalWindow;
	}

	@Override
	public void setRegion(String region) {
		this.region = region;
	}
}
