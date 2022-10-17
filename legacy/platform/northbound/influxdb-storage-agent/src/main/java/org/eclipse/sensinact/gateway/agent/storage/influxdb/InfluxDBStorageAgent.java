/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.agent.storage.influxdb;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.sensinact.gateway.agent.storage.influxdb.write.InfluxDBStorageConnection;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.historic.storage.agent.generic.StorageAgent;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricProvider;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbConnector;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbConnectorConfiguration;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbDatabase;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfluxDBStorageAgent extends StorageAgent {
	
	private static final Logger LOG = LoggerFactory.getLogger(InfluxDBStorageAgent.class);
		
	static final String INFLUX_AGENT_URI_PROPS              = "org.eclipse.sensinact.gateway.history.influx.uri";
	static final String INFLUX_AGENT_SCHEME_PROPS      = "org.eclipse.sensinact.gateway.history.influx.scheme";
	static final String INFLUX_AGENT_HOST_PROPS           = "org.eclipse.sensinact.gateway.history.influx.host";
	static final String INFLUX_AGENT_PORT_PROPS           = "org.eclipse.sensinact.gateway.history.influx.port";
	static final String INFLUX_AGENT_PATH_PROPS            = "org.eclipse.sensinact.gateway.history.influx.path";
	
	static final String INFLUX_AGENT_LOGIN_PROPS          = "org.eclipse.sensinact.gateway.history.influx.login";
	static final String INFLUX_AGENT_PASSWORD_PROPS = "org.eclipse.sensinact.gateway.history.influx.password";
	
	static final String INFLUX_AGENT_DB_PROPS    	        = "org.eclipse.sensinact.gateway.history.influx.database";

	static final String INFLUX_AGENT_MEASUREMENT_PROPS   = "org.eclipse.sensinact.gateway.history.influx.measurement";
	static final String INFLUX_AGENT_DEFAULT_MEASUREMENT = "test";
	
	static final String DEFAULT_DATABASE    	   		 = "sensinact";
	static final String STORAGE_HISTORY_PROVIDER    = "sensinact.history.provider";

		
	private Mediator mediator;
	private InfluxDbConnector connector;
	private String measurement;
	private InfluxDbDatabase database;

	public InfluxDBStorageAgent(BundleContext bc, Dictionary<String,?> props) {
		super(bc);
		this.mediator = new Mediator(bc);
		
		String db = (String) props.get(INFLUX_AGENT_DB_PROPS);
		if(db == null)
			db = DEFAULT_DATABASE;
		
		String username = (String) props.get(INFLUX_AGENT_LOGIN_PROPS);
		String password = (String) props.get(INFLUX_AGENT_PASSWORD_PROPS);

		InfluxDbConnectorConfiguration configuration;
		
		InfluxDbConnectorConfiguration.Builder builder = new InfluxDbConnectorConfiguration.Builder();
		
		String uri = (String) props.get(INFLUX_AGENT_URI_PROPS);
		
		if(uri == null) {
			String scheme =  (String) props.get(INFLUX_AGENT_SCHEME_PROPS);
			if(scheme == null)
				scheme = InfluxDbConnectorConfiguration.DEFAULT_SCHEME;
			
			String host = (String) props.get(INFLUX_AGENT_HOST_PROPS);
			if(host == null)
				host = InfluxDbConnectorConfiguration.DEFAULT_HOST;
			
			int port = -1;
			String portStr =  (String) props.get(INFLUX_AGENT_PORT_PROPS);
			if(portStr == null)
				port = InfluxDbConnectorConfiguration.DEFAULT_PORT;
			else 
				port = Integer.parseInt(portStr);
			
			String path = (String) props.get(INFLUX_AGENT_PATH_PROPS);
			if(path == null)
				path = InfluxDbConnectorConfiguration.DEFAULT_PATH;

			configuration = builder.withScheme(scheme
			).withHost(host
			).withPort(port
			).withPath(path
			).withUsername(username
			).withPassword(password
			).build();
		} else {
			configuration = builder.withUri(uri
            ).withUsername(username
            ).withPassword(password
            ).build();
		}
		try {
			this.connector = new InfluxDbConnector(configuration);
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
			throw new RuntimeException(e);
		}
		this.measurement = (String) props.get(INFLUX_AGENT_MEASUREMENT_PROPS);
		if(this.measurement == null)
			this.measurement = INFLUX_AGENT_DEFAULT_MEASUREMENT;
				
		this.database = this.connector.createIfNotExists(db);
		
		InfluxDBHistoricProvider provider = new InfluxDBHistoricProvider(connector, db, this.measurement);
		this.mediator.register(new Hashtable<String, Object>(){
			private static final long serialVersionUID = 1L;
			{
			this.put(STORAGE_HISTORY_PROVIDER, measurement);
		}}, provider,new Class[] { HistoricProvider.class});
		
		super.setStorageKeys((String) props.get(STORAGE_AGENT_KEYS_PROPS));
		super.setStorageConnection(new InfluxDBStorageConnection(database, this.measurement, props));
	}

	@Override
	protected String[] getKeyProcessorProviderIdentifiers() {
		return new String[] {this.measurement};
	}
}
