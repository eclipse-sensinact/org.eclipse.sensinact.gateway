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
package org.eclipse.sensinact.gateway.core.security.dao;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.security.dao.directive.CreateDirective;
import org.eclipse.sensinact.gateway.core.security.dao.directive.DeleteDirective;
import org.eclipse.sensinact.gateway.core.security.dao.directive.KeyDirective;
import org.eclipse.sensinact.gateway.core.security.dao.directive.SelectDirective;
import org.eclipse.sensinact.gateway.core.security.dao.directive.UpdateDirective;
import org.eclipse.sensinact.gateway.core.security.entity.SnaEntity;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Immutable;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Immutable.Operation;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic and abstract Data Access Object to interact with the datastore where
 * secured access information of sensiNact are stored
 */
abstract class AbstractSnaDAO<E extends SnaEntity> implements SnaDAO<E> {
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractSnaDAO.class);

	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	/**
	 * Allows to refer to complex select statement defined in a separated file
	 * 
	 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
	 */
	protected class UserDefinedSelectStatement {
		private URL definition;
		private String statement;

		/**
		 * Constructor
		 * 
		 * @param definition
		 *            the url of the file where is defined the select statement
		 *            to load
		 */
		protected UserDefinedSelectStatement(URL definition) {
			this.statement = null;
			this.definition = definition;
		}

		/**
		 * Returns the SQL statement of this UserDefinedSelectStatement
		 * 
		 * @return this UserDefinedSelectStatement's SQL statement
		 */
		protected String getStatement() {
			if (this.statement == null) {
				try {
					byte[] statementBytes = IOUtils.read(definition.openStream(), true);

					this.statement = new String(statementBytes);

				} catch (Exception e) {
					this.statement = SelectDirective.getSelectDirective( AbstractSnaDAO.this.entityType)
							.toString();
				}
			}
			return this.statement;
		}
	}

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	/**
	 * Completes the creation of the given {@link SnaEntity} in the datastore.
	 * 
	 * @param entity
	 *            the created {@link SnaEntity} in the datastore.
	 * @param identifier
	 *            the long identifier of the new created {@link SnaEntity}
	 */
	abstract void created(E entity, long identifier);

	/**
	 * Completes the update of an {@link SnaEntity} in the datastore.
	 * 
	 * @param records
	 *            the number of updated {@link SnaEntity}s in the datastore
	 */
	abstract void updated(int records);

	/**
	 * Completes the deletion of an {@link SnaEntity} in the datastore.
	 * 
	 * @param records
	 *            the number of deleted {@link SnaEntity}s in the datastore
	 */
	abstract void deleted(int records);

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	// Variable reference
	public static final String REQUEST_VAR = "#VAR#";

	/**
	 * Formats the SQL query passed as parameter by replacing REQUEST_VAR markers by
	 * the variable argument values in order
	 * 
	 * @param query
	 *            the SQL query to format
	 * @param variables
	 *            the set of variable values to substitute to REQUEST_VAR markers in
	 *            the query
	 * @return the formated SQL query
	 */
	private static String formatQuery(String query, String... variables) {
		String formatedQuery = new String(query);

		if (variables != null) {
			for (String variable : variables) {
				formatedQuery = formatedQuery.replaceFirst(REQUEST_VAR, variable);
			}
		}
		formatedQuery = formatedQuery.replace('\n', ' ');
		return formatedQuery;
	}

	/**
	 * Returns true if the {@link Immutable} annotation instance passed as parameter
	 * contains the {@link Immutable.Operation} also passed as parameter; Otherwise
	 * returns false.
	 * 
	 * @param immutable
	 *            the {@link Immutable} annotation instance
	 * @param op
	 *            the {@link Immutable.Operation} to check whether it is allowed or
	 *            not
	 * 
	 * @return
	 *         <ul>
	 *         <li>true if the specified {@link Immutable} annotation instance
	 *         contains the specified {@link Immutable.Operation}</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 * 
	 */
	protected static boolean isImmutable(Immutable immutable, Immutable.Operation op) {
		for (Operation operation : immutable.operation()) {
			if (operation.equals(op)) {
				return true;
			}
		}
		return false;
	}

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	protected final Class<E> entityType;
	protected final DataStoreService dataStoreService;
	protected final Map<String, UserDefinedSelectStatement> userDefinedSelectStatements;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param entityType
	 */
	AbstractSnaDAO(Class<E> entityType, DataStoreService dataStoreService) {
		this.dataStoreService = dataStoreService;
		this.entityType = entityType;
		this.userDefinedSelectStatements = new HashMap<String, UserDefinedSelectStatement>();
	}

	/**
	 * Formats and executes the create statement passed as parameter and returned
	 * the long identifier of the created record
	 * 
	 * @param query
	 *            the SQL create statement to format and execute
	 * @param variables
	 *            the variable number of argument values to format the SQL create
	 *            statement
	 * @return the long identifier of the created record in the targeted data store
	 * @throws DataStoreException
	 */
	public Long create(String query, String... variables) throws DataStoreException {
		return dataStoreService.insert(formatQuery(query, variables));
	}

	/**
	 * Formats and executes the update statement passed as parameter and returned
	 * the number of updated records
	 * 
	 * @param query
	 *            the SQL update statement to format and execute
	 * @param variables
	 *            the variable number of argument values to format the SQL update
	 *            statement
	 * @return the number of updated records in the targeted data store
	 * 
	 * @throws DataStoreException
	 */
	public Integer update(String query, String... variables) throws DataStoreException {
		return dataStoreService.update(formatQuery(query, variables));
	}

	/**
	 * Formats and executes the delete statement passed as parameter and returned
	 * the number of deleted records
	 * 
	 * @param query
	 *            the SQL delete statement to format and execute
	 * @param variables
	 *            the variable number of argument values to format the SQL delete
	 *            statement
	 * @return the number of deleted records in the targeted data store
	 * @throws DataStoreException
	 */
	public Integer delete(String query, String... variables) throws DataStoreException {
		return dataStoreService.delete(formatQuery(query, variables));
	}

	/**
	 * @throws DataStoreException
	 * @inheritDoc
	 *
	 * @see SnaDAO#select(java.lang.String)
	 */
	@Override
	public List<E> select(String name, String... variables) throws DAOException, DataStoreException {
		UserDefinedSelectStatement userDefinedSelectStatement = this.userDefinedSelectStatements.get(name);

		if (userDefinedSelectStatement == null) {
			return select();
		}
		String query = userDefinedSelectStatement.getStatement();
		JSONArray array = this.selectStatement(query, variables);

		int index = 0;
		int length = array == null ? 0 : array.length();
		List<E> entitiesList = new ArrayList<E>(length);

		for (; index < length; index++) {
			try {
				JSONObject jsonObject = array.getJSONObject(index);
				entitiesList.add(
						entityType.getConstructor(JSONObject.class).newInstance(jsonObject));

			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				return Collections.<E>emptyList();
			}
		}
		return entitiesList;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SnaDAO#select(java.util.Map)
	 */
	@Override
	public List<E> select(Map<String, Object> whereDirectives) throws DAOException, DataStoreException {
		if (whereDirectives == null) {
			return select();
		}
		SelectDirective selectDirective = SelectDirective.getSelectDirective(entityType);

		Iterator<Map.Entry<String, Object>> iterator = whereDirectives.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, Object> entry = iterator.next();
			selectDirective.where(entry.getKey(), entry.getValue());
		}
		JSONArray array = this.selectStatement(selectDirective.toString());

		int index = 0;
		int length = array == null ? 0 : array.length();
		List<E> entitiesList = new ArrayList<E>(length);

		for (; index < length; index++) {
			try {
				JSONObject jsonObject = array.getJSONObject(index);
				entitiesList.add(
						entityType.getConstructor(JSONObject.class).newInstance(jsonObject));

			} catch (Exception e) {
				e.printStackTrace();
				LOG.error(e.getMessage(), e);
				return Collections.<E>emptyList();
			}
		}
		return entitiesList;
	}

	@Override
	public E select(List<SnaEntity.Key> keyMap) throws DAOException, DataStoreException {
		KeyDirective keyDirective = KeyDirective.createKeyDirective(entityType);
		keyDirective.assign(keyMap);

		E entity = this.getEntity(this.entityType, keyDirective);
		return entity;
	}

	/**
	 * Formats and executes the select statement passed as parameter and returned
	 * the array of JSON formated selected records
	 * 
	 * @param query
	 *            the SQL select statement to format and execute
	 * @param variables
	 *            the variable number of argument values to format the SQL select
	 *            statement
	 * @return the array of JSON formated selected records
	 * @throws DataStoreException
	 */
	private JSONArray selectStatement(String query, String... variables) throws DataStoreException {
		return this.dataStoreService.select(formatQuery(query, variables));
	}

	/**
	 * @param name
	 * @param statement
	 */
	protected void registerUserDefinedSelectStatement(String name, UserDefinedSelectStatement statement) {
		this.userDefinedSelectStatements.put(name, statement);
	}

	/**
	 * Builds and returns the list of registered entities whose type is passed as
	 * parameter
	 * 
	 * @param entityType
	 *            the type of the registered SnaEntities to build the list of
	 * @param keyDirective
	 * @return the list of registered entities of the specified type
	 * 
	 * @throws DataStoreException
	 */
	protected E getEntity(Class<E> entityType, KeyDirective keyDirective) throws DataStoreException {
		E entity = null;
		List<E> entities = getEntities(entityType, keyDirective);
		if (!entities.isEmpty()) {
			entity = entities.get(0);
		}
		return entity;
	}

	/**
	 * Builds and returns the list of registered entities whose type is passed as
	 * parameter
	 * 
	 * @param entityType
	 *            the type of the registered SnaEntities to build the list of
	 * @return the list of registered entities of the specified type
	 * 
	 * @throws DataStoreException
	 */
	protected <E extends SnaEntity> List<E> getEntities(Class<E> entityType) throws DataStoreException {
		return getEntities(entityType, null);
	}

	/**
	 * Builds and returns the list of registered entities whose type is passed as
	 * parameter
	 * 
	 * @param entityType
	 *            the type of the registered SnaEntities to build the list of
	 * @param keyDirective
	 * @return the list of registered entities of the specified type
	 * @throws DataStoreException
	 */
	public <E extends SnaEntity> List<E> getEntities(Class<E> entityType, KeyDirective keyDirective)
			throws DataStoreException {
		SelectDirective selectDirective = SelectDirective.getSelectDirective(entityType);

		selectDirective.join(keyDirective);

		JSONArray array = this.selectStatement(selectDirective.toString());

		int index = 0;
		int length = array == null ? 0 : array.length();
		List<E> entitiesList = new ArrayList<E>(length);
		Constructor<E> constructor = null;
		if (length > 0) {
			try {
				constructor = entityType.getConstructor(JSONObject.class);

				for (; index < length; index++) {
					JSONObject jsonObject = array.getJSONObject(index);
					entitiesList.add(constructor.newInstance(jsonObject));
				}
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				return Collections.<E>emptyList();
			}
		}
		return entitiesList;
	}

	@Override
	public List<E> select() throws DAOException, DataStoreException {
		List<E> entities = this.getEntities(this.entityType);
		return entities;
	}

	@Override
	public void create(E entity) throws DAOException, DataStoreException {
		CreateDirective createDirective = CreateDirective.getCreateDirective(entity);
		this.created(entity, this.create(createDirective.toString()));
	}

	@Override
	public void update(E entity) throws DAOException, DataStoreException {
		UpdateDirective updateDirective = UpdateDirective.getUpdateDirective(entity);
		this.updated(this.update(updateDirective.toString()));
	}

	@Override
	public void delete(E entity) throws DAOException, DataStoreException {
		DeleteDirective deleteDirective = DeleteDirective.getDeleteDirective(entity);
		this.deleted(this.delete(deleteDirective.toString()));
	}

}
