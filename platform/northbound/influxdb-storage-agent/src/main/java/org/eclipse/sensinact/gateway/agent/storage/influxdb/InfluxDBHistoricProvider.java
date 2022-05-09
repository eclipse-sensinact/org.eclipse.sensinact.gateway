/*********************************************************************
* Copyright (c) 2021 Kentyou
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.agent.storage.influxdb;

import org.eclipse.sensinact.gateway.agent.storage.influxdb.read.InfluxDBSpatialRequest;
import org.eclipse.sensinact.gateway.agent.storage.influxdb.read.InfluxDBSpatioTemporalRequest;
import org.eclipse.sensinact.gateway.agent.storage.influxdb.read.InfluxDBTemporalRequest;
import org.eclipse.sensinact.gateway.agent.storage.influxdb.read.InfluxDBValueRequest;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricProvider;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricSpatialRequest;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricSpatioTemporalRequest;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricTemporalRequest;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricValueRequest;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbConnector;

/**
 * InfluxDB {@link HistoricProvider}
 */
public class InfluxDBHistoricProvider implements HistoricProvider {

	private InfluxDbConnector connector;
	private String measurement;
	private String database;
	
	public InfluxDBHistoricProvider(InfluxDbConnector connector, String database, String measurement){
		this.connector = connector;
		this.database = database;
		this.measurement = measurement;
	}	

	/**
	 * Creates and returns a new {@link HistoricTemporalRequest}
	 * 
	 * @return a newly created {@link HistoricTemporalRequest}
	 */
	public HistoricTemporalRequest newTemporalRequest() {
		InfluxDBTemporalRequest request =  new InfluxDBTemporalRequest(this.connector);
		request.setDatabase(this.database);
		request.setMeasurement(this.measurement);
		return request;
	}

	/**
	 * Creates and returns a new {@link HistoricSpatialRequest}
	 * 
	 * @return a newly created {@link HistoricSpatialRequest}
	 */
	public HistoricSpatialRequest newSpatialRequest() {
		InfluxDBSpatialRequest request =  new InfluxDBSpatialRequest(this.connector);
		request.setDatabase(this.database);
		request.setMeasurement(this.measurement);
		return request;
	}

	/**
	 * Creates and returns a new {@link HistoricSpatioTemporalRequest}
	 * 
	 * @return a newly created {@link HistoricSpatioTemporalRequest}
	 */
	public HistoricSpatioTemporalRequest newSpatioTemporalRequest() {
		InfluxDBSpatioTemporalRequest request = new InfluxDBSpatioTemporalRequest(this.connector);
		request.setDatabase(this.database);
		request.setMeasurement(this.measurement);
		return request;
	}

	@Override
	public HistoricValueRequest newValueRequest() {
		InfluxDBValueRequest request = new InfluxDBValueRequest(connector);
		request.setDatabase(database);
		request.setMeasurement(measurement);
		return request;
	}


}
