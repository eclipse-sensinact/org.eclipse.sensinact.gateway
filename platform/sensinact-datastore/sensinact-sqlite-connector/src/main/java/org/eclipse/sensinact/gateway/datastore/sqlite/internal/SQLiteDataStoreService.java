/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.datastore.sqlite.internal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreConnectionProvider;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.UnableToConnectToDataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.UnableToFindDataStoreException;
import org.eclipse.sensinact.gateway.datastore.jdbc.JdbcDataStoreService;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SQLiteDataStoreService extends JdbcDataStoreService
{
	//********************************************************************//
	//						NESTED DECLARATIONS			  			      //
	//********************************************************************//

	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//

	//********************************************************************//
	//						STATIC DECLARATIONS							  //
	//********************************************************************//

	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//

	private SQLiteConnectionProvider provider;

	/**
	 * @param mediator
	 * @param dbName
	 * @throws UnableToFindDataStoreException
	 * @throws UnableToConnectToDataStoreException 
	 */
	public SQLiteDataStoreService(Mediator mediator, String dbName)
	        throws UnableToFindDataStoreException, 
	        UnableToConnectToDataStoreException
	{
		super(mediator);
	    this.provider = new SQLiteConnectionProvider(mediator, dbName);    
	}

	/**
	 * @inheritDoc
	 *
	 * @see JdbcDataStoreService#
	 * getDataBaseConnectionProvider(java.lang.String)
	 */
	@Override
	protected DataStoreConnectionProvider<Connection> getDataBaseConnectionProvider()
	{
		return this.provider;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see JdbcDataStoreService#stop()
	 */
	public void stop()
	{	
		if(provider != null)
		{
			provider.stop();
			while(provider.getCount()>0)
			{
				try
				{
					Thread.sleep(10);
				
				} catch (InterruptedException ex)
				{
					Thread.interrupted();
					ex.printStackTrace();
				}
			}
		}	
		provider = null;
	}

	/**
	 * @inheritDoc
	 *
	 * @see DataStoreService#
	 * insert(java.lang.String)
	 */
	@Override
	public long insert(final String query)
	{
		return super.<Long>executeStatement(
			new Executable<Statement, Long>()
			{
				@Override
				public Long execute(Statement statement) 
						throws Exception
				{	
					ResultSet rs = null;
					long lastID = -1;
					
					synchronized (lock) 
					{
						try
						{
							statement.addBatch(query);
							statement.executeBatch();
							rs = statement.executeQuery(
									"SELECT last_insert_rowid() AS LASTID;");
							if(rs.next())
							{
								lastID = rs.getLong(1);
							}
							Connection connection = statement.getConnection();
							connection.commit();

						} catch (Exception e) 
						{
							synchronized (lock) 
							{
								try 
								{
									statement.getConnection().rollback();

								} catch (SQLException ex)
								{
									throw new DataStoreException(ex);

								} catch (NullPointerException ex) {
									// do nothing
								}
							}
							throw new DataStoreException(e);
						}
					}
					return lastID;				
				}
			});
	}
}
