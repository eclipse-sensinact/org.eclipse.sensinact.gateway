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

import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricValueRequest;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.TemporalDTO;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbConnector;


public class InfluxDBValueRequest extends AbstractInfluxDBValueRequest<TemporalDTO> implements HistoricValueRequest{

	protected String region;
	
	public InfluxDBValueRequest(InfluxDbConnector influxDbConnector) {
		super(influxDbConnector);
	}

	@Override
	public TemporalDTO execute() {
		return null;
	}
}
