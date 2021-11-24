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


public abstract class AbstractInfluxDBTemporalRequest<T> extends InfluxDBRequest<T> {

	protected String function;
	protected long temporalWindow;
	protected ZonedDateTime start;
	protected ZonedDateTime end;
	
	public AbstractInfluxDBTemporalRequest(InfluxDbConnector influxDbConnector) {
		super(influxDbConnector);
	}
	
	public void setHistoricStartTime(ZonedDateTime fromTime) {
		this.start = fromTime;
	}

	public void setHistoricEndTime(ZonedDateTime toTime) {
		this.end = toTime;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public void setTemporalWindow(long temporalWindow) {
		this.temporalWindow = temporalWindow;
	}

}
