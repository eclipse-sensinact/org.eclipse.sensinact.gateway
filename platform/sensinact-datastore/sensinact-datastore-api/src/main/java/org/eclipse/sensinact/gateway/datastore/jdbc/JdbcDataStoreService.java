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
package org.eclipse.sensinact.gateway.datastore.jdbc;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreConnectionProvider;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;
import org.eclipse.sensinact.gateway.datastore.api.UnableToFindDataStoreException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * {@link DataStoreService} service implementation
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
//insert method must be implemented by the extended JdbcDataStoreService
//because retrieving the last inserted row identifier depends on the SGBD
//in use
public abstract class JdbcDataStoreService implements DataStoreService {
	
    /**
	 * Retrieves and returns the long identifier of the last 
	 * inserted record
	 * 
	 * @param statement the {@link Statement} used for executing a 
	 * static SQL statement and returning the results it produces
	 * 
	 * @return the last inserted recortd's long identifier
     * @throws SQLException 
	 */
	protected abstract long getLastInsertedId(Statement statement)
			throws SQLException;
	
    /**
     * Returns the {@link DataStoreConnectionProvider} providing
     * JDBC {@link Connection}s
     *
     * @return the JDBC {@link Connection} provider
     */
    protected abstract DataStoreConnectionProvider<Connection> getDataBaseConnectionProvider();

    /**
     * Stops this JdbcDataStoreService
     */
    protected abstract void stop();

    /**
     * Lock use for synchronization
     */
    protected final Object lock = new Object();
    
    /**
     * The {@link Mediator}
     */
    protected Mediator mediator;

    /**
     * Constructor
     *
     * @throws UnableToLoadDriverClassException
     * @throws UnableToFindDataStoreException
     */
    public JdbcDataStoreService(Mediator mediator) throws UnableToFindDataStoreException {
        this.mediator = mediator;
    }

    /**
     * Converts the {@link ResultSet} passed as parameter into
     * a JSONObject
     *
     * @param resultSet the {@link ResultSet} object to convert
     * @return the result set as a JSONObject
     * @throws SQLException
     * @throws JSONException
     */
    protected JSONArray resultSetToJSon(ResultSet resultSet) throws SQLException, JSONException {
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnCount = rsmd.getColumnCount();

        JSONArray result = new JSONArray();

        while (resultSet.next()) {
            JSONObject obj = new JSONObject();
            for (int index = 1; index <= columnCount; index++) {
                String column = rsmd.getColumnName(index);
                Object value = resultSet.getObject(column);
                if(value == null) {
                	continue;
                }
                String[] columnElements = column.split("#");
            	JSONObject parent = obj;                
            	Object subObj = null;
            	int n=0;
            	for(;n < columnElements.length-1;n++) {
            		subObj = parent.opt(columnElements[n]);
                	if(subObj==null) {
                		subObj=new JSONObject();
                		parent.put(columnElements[n], subObj);
                	}
                	parent=(JSONObject)subObj;
            	}
            	parent.put(columnElements[n], value);
            }
            result.put(obj);
        }
        return result;
    }

    /**
     * Converts the {@link ResultSet} passed as parameter into
     * a JSON formated String
     *
     * @param resultSet the {@link ResultSet} object to be converted
     * 
     * @return the result set as a JSON formated String
     * 
     * @throws SQLException
     */
    protected String resultSetToJSonString(ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnCount = rsmd.getColumnCount();

        StringBuilder builder = new StringBuilder();
        int line = 0;
        builder.append("[");

        while (resultSet.next()) {
            JSONObject obj = new JSONObject();
            for (int index = 1; index <= columnCount; index++) {
                String column = rsmd.getColumnName(index);
                Object value = resultSet.getObject(column);
                if(value == null) {
                	continue;
                }
                String[] columnElements = column.split("#");
            	JSONObject parent = obj;                
            	Object subObj = null;
            	int n=0;
            	for(;n < columnElements.length-1;n++) {
            		subObj = parent.opt(columnElements[n]);
                	if(subObj==null) {
                		subObj=new JSONObject();
                		parent.put(columnElements[n], subObj);
                	}
                	parent=(JSONObject)subObj;
            	}
            	parent.put(columnElements[n], value);                
            }
            if(line > 0) {
                builder.append(',');
            }
            builder.append(obj.toString());
            line+=1;
        }
        builder.append("]");
        return builder.toString();
    }

    
    /**
     * @param executor
     * @return
     */
    protected <R> R executeStatement(Executable<Statement, R> executor) {
        Statement statement = null;
        DataStoreConnectionProvider<Connection> provider = this.getDataBaseConnectionProvider();

        R result = null;

        if (provider == null) {
            return result;
        }
        try {
            Connection connection = this.getDataBaseConnectionProvider().openConnection();

            synchronized (lock) {
                statement = connection.createStatement();
                statement.setQueryTimeout(30);
            }
            result = executor.execute(statement);

        } catch (Exception e) {
        	e.printStackTrace();
            mediator.error(e);

        } finally {
            if (statement != null) {
                try {
                    statement.close();

                } catch (SQLException e) {
                    mediator.error("Error while closing statement", e);
                }
            }
            if (provider != null) {
                provider.closeConnection();
            }
        }
        return result;
    }
    
    /**
     * @inheritDoc
     * @see DataStoreService#select(java.lang.String)
     */
    public String selectAsString(final String query) {
        return this.<String>executeStatement(new Executable<Statement, String>() {
            @Override
            public String execute(Statement statement) throws Exception {
                try {
                    ResultSet rs = null;
                    synchronized (lock) {
                        rs = statement.executeQuery(query);
                    }
                    String result = resultSetToJSonString(rs);
                    return result;

                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        });
    }
    
    /**
     * @inheritDoc
     * @see DataStoreService#select(java.lang.String)
     */
    public JSONArray select(final String query) {
        return this.<JSONArray>executeStatement(new Executable<Statement, JSONArray>() {
            @Override
            public JSONArray execute(Statement statement) throws Exception {
                try {
                    ResultSet rs = null;
                    synchronized (lock) {
                        rs = statement.executeQuery(query);
                    }
                    JSONArray result = resultSetToJSon(rs);
                    return result;

                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        });
    }

	/**
	 * @inheritDoc
	 *
	 * @see DataStoreService# insert(java.lang.String)
	 */
	@Override
	public long insert(final String query) {
		return this.<Long>executeStatement(new Executable<Statement, Long>() {
			@Override
			public Long execute(Statement statement) throws Exception {
				
				long lastID = -1;

				synchronized (lock) {
					try {
						statement.addBatch(query);
						statement.executeBatch();
						lastID = JdbcDataStoreService.this.getLastInsertedId(statement);
						Connection connection = statement.getConnection();
						connection.commit();

					} catch (Exception e) {
						synchronized (lock) {
							try {
								statement.getConnection().rollback();

							} catch (SQLException ex) {
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
    /**
     * @inheritDoc
     * @see DataStoreService#delete(java.lang.String)
     */
    public int delete(String query) {
        return doQuery(query);
    }

    /**
     * @inheritDoc
     * @see DataStoreService#update(java.lang.String)
     */
    public int update(String query) {
        return doQuery(query);
    }

    /**
     * @param query
     * @return
     * @throws SQLException
     */
    protected Integer doQuery(final String query) {
        return this.<Integer>executeStatement(new Executable<Statement, Integer>() {
            @Override
            public Integer execute(Statement statement) throws Exception {
                int[] updateCounts = new int[]{-1};
                synchronized (lock) {
                    try {
                        statement.addBatch(query);
                        updateCounts = statement.executeBatch();
                        Connection connection = statement.getConnection();
                        connection.commit();

                    } catch (Exception e) {
                        if (statement != null) {
                            try {
                                statement.getConnection().rollback();

                            } catch (SQLException ex) {
                                throw new DataStoreException(ex);
                            }
                        }
                        throw e;
                    }
                }
                return updateCounts[0];
            }
        });
    }
}
