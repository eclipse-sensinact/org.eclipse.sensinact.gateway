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
