/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.datastore.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreConnectionProvider;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.spi.JsonProvider;

/**
 * {@link DataStoreService} service implementation
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
//insert method must be implemented by the extended JdbcDataStoreService
//because retrieving the last inserted row identifier depends on the SGBD
//in use
public abstract class JdbcDataStoreService implements DataStoreService {
	
	private static final Logger LOG = LoggerFactory.getLogger(JdbcDataStoreService.class);
	
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
     * Converts the {@link ResultSet} passed as parameter into
     * a JSONObject
     *
     * @param resultSet the {@link ResultSet} object to convert
     * @return the result set as a JSONObject
     * @throws SQLException
     * @throws JSONException
     */
    @SuppressWarnings("unchecked")
	protected JsonArray resultSetToJSon(ResultSet resultSet) throws SQLException, JsonException {
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnCount = rsmd.getColumnCount();

        JsonProvider provider = JsonProviderFactory.getProvider();
		JsonArrayBuilder result = provider.createArrayBuilder();

        while (resultSet.next()) {
            Map<String,Object> children = new HashMap<>();
            for (int index = 1; index <= columnCount; index++) {
                String column = rsmd.getColumnName(index);
                Object value = resultSet.getObject(column);
                if(value == null) {
                	continue;
                }
                String[] columnElements = column.split("#");
                Map<String, Object> addTo = children;
                for(int n = 0; n < columnElements.length - 1; n++) {
                	addTo = (Map<String, Object>) addTo.computeIfAbsent(columnElements[n], k -> new HashMap<>());
                }
                addTo.put(columnElements[columnElements.length - 1], value);
            }
            
            result.add(fromMap(children));
        }
        return result.build();
    }

    @SuppressWarnings("unchecked")
	private JsonObjectBuilder fromMap(Map<String, Object> map) {
    	JsonProvider provider = JsonProviderFactory.getProvider();
		JsonObjectBuilder job = provider.createObjectBuilder();
    	for (Entry<String, Object> e : map.entrySet()) {
			Object value = e.getValue();
			if(value instanceof Map) {
				job.add(e.getKey(), fromMap((Map<String, Object>)value));
			} else if(value instanceof Number) {
				job.add(e.getKey(), provider.createValue((Number) value));
			} else {
				job.add(e.getKey(), value.toString());
			}
		}
    	return job;
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
        return resultSetToJSon(resultSet).toString();
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
            LOG.error(e.getMessage(), e);

        } finally {
            if (statement != null) {
                try {
                    statement.close();

                } catch (SQLException e) {
                    LOG.error("Error while closing statement", e);
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
    public JsonArray select(final String query) {
        return this.<JsonArray>executeStatement(new Executable<Statement, JsonArray>() {
            @Override
            public JsonArray execute(Statement statement) throws Exception {
                try {
                    ResultSet rs = null;
                    synchronized (lock) {
                        rs = statement.executeQuery(query);
                    }
                    JsonArray result = resultSetToJSon(rs);
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
