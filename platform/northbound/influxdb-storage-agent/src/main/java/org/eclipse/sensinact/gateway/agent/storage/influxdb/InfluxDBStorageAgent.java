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
package org.eclipse.sensinact.gateway.agent.storage.influxdb;

import java.util.Hashtable;

import org.eclipse.sensinact.gateway.agent.storage.influxdb.write.InfluxDBStorageConnection;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.AgentRelay;
import org.eclipse.sensinact.gateway.historic.storage.agent.generic.StorageAgent;
import org.eclipse.sensinact.gateway.historic.storage.reader.api.HistoricProvider;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbConnector;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbConnectorConfiguration;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbDatabase;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(immediate=true, service = {AgentRelay.class})
public class InfluxDBStorageAgent extends StorageAgent {
		
	private static final String INFLUX_AGENT_URI_PROPS           = "org.eclipse.sensinact.gateway.history.influx.uri";
	private static final String INFLUX_AGENT_SCHEME_PROPS        = "org.eclipse.sensinact.gateway.history.influx.scheme";
	private static final String INFLUX_AGENT_HOST_PROPS          = "org.eclipse.sensinact.gateway.history.influx.host";
	private static final String INFLUX_AGENT_PORT_PROPS          = "org.eclipse.sensinact.gateway.history.influx.port";
	private static final String INFLUX_AGENT_PATH_PROPS          = "org.eclipse.sensinact.gateway.history.influx.path";
	
	private static final String INFLUX_AGENT_LOGIN_PROPS         = "org.eclipse.sensinact.gateway.history.influx.login";
	private static final String INFLUX_AGENT_PASSWORD_PROPS      = "org.eclipse.sensinact.gateway.history.influx.password";
	
	private static final String INFLUX_AGENT_DB_PROPS    	     = "org.eclipse.sensinact.gateway.history.influx.database";

	private static final String INFLUX_AGENT_MEASUREMENT_PROPS   = "org.eclipse.sensinact.gateway.history.influx.measurement";
	private static final String INFLUX_AGENT_DEFAULT_MEASUREMENT = "test";
	
	private static final String DEFAULT_DATABASE    	   		 = "sensinact";

		
	private Mediator mediator;
	private InfluxDbConnector connector;
	private String measurement;
	private InfluxDbDatabase database;

	@Activate
	public void activate(ComponentContext context) {
		BundleContext bc = context.getBundleContext();
		this.mediator = new Mediator(bc);
		
		String db = (String) mediator.getProperty(INFLUX_AGENT_DB_PROPS);
		if(db == null)
			db = DEFAULT_DATABASE;
		
		String username = (String) mediator.getProperty(INFLUX_AGENT_LOGIN_PROPS);
		String password = (String) mediator.getProperty(INFLUX_AGENT_PASSWORD_PROPS);

		InfluxDbConnectorConfiguration configuration;
		
		InfluxDbConnectorConfiguration.Builder builder = new InfluxDbConnectorConfiguration.Builder();
		
		String uri = (String) mediator.getProperty(INFLUX_AGENT_URI_PROPS);
		
		if(uri == null) {
			String scheme =  (String) mediator.getProperty(INFLUX_AGENT_SCHEME_PROPS);
			if(scheme == null)
				scheme = InfluxDbConnectorConfiguration.DEFAULT_SCHEME;
			
			String host = (String) mediator.getProperty(INFLUX_AGENT_HOST_PROPS);
			if(host == null)
				host = InfluxDbConnectorConfiguration.DEFAULT_HOST;
			
			int port = -1;
			String portStr =  (String) mediator.getProperty(INFLUX_AGENT_PORT_PROPS);
			if(portStr == null)
				port = InfluxDbConnectorConfiguration.DEFAULT_PORT;
			else 
				port = Integer.parseInt(portStr);
			
			String path = (String) mediator.getProperty(INFLUX_AGENT_PATH_PROPS);
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
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		this.measurement = (String) mediator.getProperty(INFLUX_AGENT_MEASUREMENT_PROPS);
		if(this.measurement == null)
			this.measurement = INFLUX_AGENT_DEFAULT_MEASUREMENT;
		
		this.database = this.connector.createIfNotExists(db);
		
		InfluxDBHistoricProvider provider = new InfluxDBHistoricProvider(connector, db, this.measurement);
		this.mediator.register(new Hashtable<String, Object>(), provider,new Class[] { HistoricProvider.class});
		
		super.setStorageKeys((String) mediator.getProperty(STORAGE_AGENT_KEYS_PROPS));
		super.setStorageConnection(new InfluxDBStorageConnection(mediator, database, this.measurement));
		
	}

	@Deactivate
	public void deactivate() {
		super.stop();
	}
}
