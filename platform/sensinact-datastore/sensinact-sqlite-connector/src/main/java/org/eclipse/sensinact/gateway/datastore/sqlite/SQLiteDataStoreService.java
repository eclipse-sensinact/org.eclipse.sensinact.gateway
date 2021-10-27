/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.datastore.sqlite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.sensinact.gateway.core.security.SecurityDataStoreService;
import org.eclipse.sensinact.gateway.datastore.api.DataStore;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreConnectionProvider;
import org.eclipse.sensinact.gateway.datastore.api.UnableToConnectToDataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.UnableToFindDataStoreException;
import org.eclipse.sensinact.gateway.datastore.jdbc.JdbcDataStoreService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@DataStore(provider = "jdbc",sgbd = "sqlite")
@Component(name = SQLiteDataStoreService.PID,service = SecurityDataStoreService.class,configurationPolicy = ConfigurationPolicy.REQUIRE)
public class SQLiteDataStoreService extends JdbcDataStoreService 
implements SecurityDataStoreService{
	
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	public static final String PID = "SQLiteDataStoreService"; 
	
	@ObjectClassDefinition(pid = PID)
	public static @interface SQLiteConfig{
		
		@AttributeDefinition(required = true)
		String database();
	}
	
	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	private SQLiteConnectionProvider provider;

	
	@Activate
	public void start(SQLiteConfig config) throws UnableToConnectToDataStoreException, UnableToFindDataStoreException {
		this.provider = new SQLiteConnectionProvider(config.database());
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see JdbcDataStoreService# getDataBaseConnectionProvider(java.lang.String)
	 */
	@Override
	protected DataStoreConnectionProvider<Connection> getDataBaseConnectionProvider() {
		return this.provider;
	}

	/**
	 * @inheritDoc
	 *
	 * @see JdbcDataStoreService#stop()
	 */
	@Deactivate
	public void stop() {
		if (provider != null) {
			provider.stop();
			while (provider.getCount() > 0) {
				try {
					Thread.sleep(10);

				} catch (InterruptedException ex) {
					Thread.interrupted();
					ex.printStackTrace();
				}
			}
		}
		provider = null;
	}


	@Override
	public long getLastInsertedId(Statement statement) throws SQLException {
		ResultSet rs = null;
		long lastID = -1;
		rs = statement.executeQuery("SELECT last_insert_rowid() AS LASTID;");
		if (rs.next()) {
			lastID = rs.getLong(1);
		}
		return lastID;
	}
}
