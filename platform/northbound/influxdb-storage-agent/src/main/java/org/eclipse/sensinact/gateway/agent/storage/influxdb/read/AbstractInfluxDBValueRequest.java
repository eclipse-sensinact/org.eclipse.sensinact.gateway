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

import java.time.ZonedDateTime;

import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbConnector;


public abstract class AbstractInfluxDBValueRequest<T> extends InfluxDBRequest<T> {

	protected ZonedDateTime time;
	
	public AbstractInfluxDBValueRequest(InfluxDbConnector influxDbConnector) {
		super(influxDbConnector);
	}
	
	public void setHistoricTime(ZonedDateTime time) {
		this.time = time;
	}

}
