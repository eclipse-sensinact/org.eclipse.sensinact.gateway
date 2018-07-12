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
package org.eclipse.sensinact.gateway.core.security.dao;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
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
import org.eclipse.sensinact.gateway.datastore.api.StatementExecutor;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Generic and abstract Data Access Object to interact with the datastore
 * where secured access information of sensiNact are stored
 */
abstract class AbstractSnaDAO<E extends SnaEntity> implements SnaDAO<E> {
    //********************************************************************//
    //						NESTED DECLARATIONS						  	  //
    //********************************************************************//

    /**
     * Allows to refer to complex select statement defined in
     * a separated file
     *
     * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
     */
    protected class UserDefinedSelectStatement {
        private Mediator mediator;
        private URL definition;
        private String statement;

        /**
         * Constructor
         *
         * @param mediator   the {@link Mediator} allowing
         *                   to interact with the OSGi host environment
         * @param definition the string path of the file where is
         *                   defined the select statement to load
         */
        protected UserDefinedSelectStatement(Mediator mediator, String definition) {
            this.mediator = mediator;
            this.statement = null;
            this.definition = mediator.getContext().getBundle().getResource(definition);
        }

        /**
         * Returns the SQL statement of this
         * UserDefinedSelectStatement
         *
         * @return this UserDefinedSelectStatement's
         * SQL statement
         */
        protected String getStatement() {
            if (this.statement == null) {
                try {
                    byte[] statementBytes = IOUtils.read(definition.openStream(), true);

                    this.statement = new String(statementBytes);

                } catch (Exception e) {
                    this.statement = SelectDirective.getSelectDirective(mediator, AbstractSnaDAO.this.entityType).toString();
                }
            }
            return this.statement;
        }
    }

    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//

    /**
     * Completes the creation of the given {@link SnaEntity} in the datastore.
     *
     * @param entity     the created {@link SnaEntity} in the datastore.
     * @param identifier the long identifier of the new created
     *                   {@link SnaEntity}
     */
    abstract void created(E entity, long identifier);

    /**
     * Completes the update of an {@link SnaEntity} in the datastore.
     *
     * @param records the number of updated {@link SnaEntity}s in the datastore
     */
    abstract void updated(int records);

    /**
     * Completes the deletion of an {@link SnaEntity} in the datastore.
     *
     * @param records the number of deleted {@link SnaEntity}s in the datastore
     */
    abstract void deleted(int records);

    //********************************************************************//
    //						STATIC DECLARATIONS  						  //
    //********************************************************************//
    //Variable reference
    public static final String REQUEST_VAR = "#VAR#";

    /**
     * Formats and executes the create statement passed as parameter
     * and returned the long identifier of the created record
     *
     * @param query     the SQL create statement to format and execute
     * @param variables the variable number of argument values to format
     *                  the SQL create statement
     * @return the long identifier of the created record in the
     * targeted data store
     */
    public static Long create(Mediator mediator, final String query, final String... variables) {
        return AbstractSnaDAO.execute(mediator, new StatementExecutor<Long>() {
            @Override
            public Long execute(DataStoreService service) throws DataStoreException {
                return service.insert(formatQuery(query, variables));
            }
        });
    }

    /**
     * Formats and executes the update statement passed as parameter
     * and returned the number of updated records
     *
     * @param query     the SQL update statement to format and execute
     * @param variables the variable number of argument values to format
     *                  the SQL update statement
     * @return the number of updated records in the targeted
     * data store
     */
    public static Integer update(Mediator mediator, final String query, final String... variables) {
        return AbstractSnaDAO.execute(mediator, new StatementExecutor<Integer>() {
            @Override
            public Integer execute(DataStoreService service) throws DataStoreException {
                return service.update(formatQuery(query, variables));
            }
        });
    }

    /**
     * Formats and executes the delete statement passed as parameter and
     * returned the number of deleted records
     *
     * @param query     the SQL delete statement to format and execute
     * @param variables the variable number of argument values to format the SQL
     *                  delete statement
     * @return the number of deleted records in the targeted data store
     */
    public static Integer delete(Mediator mediator, final String query, final String... variables) {
        return AbstractSnaDAO.execute(mediator, new StatementExecutor<Integer>() {
            @Override
            public Integer execute(DataStoreService service) throws DataStoreException {
                return service.delete(formatQuery(query, variables));
            }
        });
    }

    /**
     * Formats and executes the select statement passed as parameter and
     * returned the array of JSON formated selected records
     *
     * @param query     the SQL select statement to format and execute
     * @param variables the variable number of argument values to format the SQL
     *                  select statement
     * @return the array of JSON formated selected records
     */
    public static JSONArray select(Mediator mediator, final String query, final String... variables) {
        return AbstractSnaDAO.execute(mediator, new StatementExecutor<JSONArray>() {
            @Override
            public JSONArray execute(DataStoreService service) throws DataStoreException {
                return service.select(formatQuery(query, variables));
            }
        });
    }

    /**
     * Executes the {@link StatementExecutor} passed as parameter on the
     * registered {@link DataStoreService} if it exists
     *
     * @param executor the {@link StatementExecutor} to call
     * @return the call result object whose type depends on the returned type of
     * the called {@link StatementExecutor}
     */
    public static <T> T execute(Mediator mediator, StatementExecutor<T> executor) {
        T result = mediator.callService(DataStoreService.class, "(&(data.store.provider=jdbc)(data.store.sgbd=sqlite))", executor);
        return result;
    }

    /**
     * Formats the SQL query passed as parameter by replacing REQUEST_VAR
     * markers by the variable argument values in order
     *
     * @param query     the SQL query to format
     * @param variables the set of variable values to substitute to REQUEST_VAR
     *                  markers in the query
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
     * Builds and returns the list of registered entities whose
     * type is passed as parameter
     *
     * @param mediator     the {@link Mediator} allowing to
     *                     interact with the OSGi host environment
     * @param entityType2  the type of the registered SnaEntities to build
     *                     the list of
     * @param keyDirective
     * @return the list of registered entities  of the specified type
     */
    protected static <E extends SnaEntity> E getEntity(Mediator mediator, Class<E> entityType, KeyDirective keyDirective) {
        E entity = null;
        List<E> entities = getEntities(mediator, entityType, keyDirective);
        if (!entities.isEmpty()) {
            entity = entities.get(0);
        }
        return entity;
    }

    /**
     * Builds and returns the list of registered entities whose
     * type is passed as parameter
     *
     * @param mediator    the {@link Mediator} allowing to
     *                    interact with the OSGi host environment
     * @param entityType2 the type of the registered SnaEntities to build
     *                    the list of
     * @return the list of registered entities  of the specified type
     */
    protected static <E extends SnaEntity> List<E> getEntities(Mediator mediator, Class<E> entityType) {
        return getEntities(mediator, entityType, null);
    }

    /**
     * Builds and returns the list of registered entities whose
     * type is passed as parameter
     *
     * @param mediator     the {@link Mediator} allowing to
     *                     interact with the OSGi host environment
     * @param entityType   the type of the registered SnaEntities to build
     *                     the list of
     * @param keyDirective
     * @return the list of registered entities  of the specified type
     */
    public static <E extends SnaEntity> List<E> getEntities(Mediator mediator, Class<E> entityType, KeyDirective keyDirective) {
        SelectDirective selectDirective = SelectDirective.getSelectDirective(mediator, entityType);

        selectDirective.join(keyDirective);
        JSONArray array = AbstractSnaDAO.select(mediator, selectDirective.toString());

        int index = 0;
        int length = array == null ? 0 : array.length();
        List<E> entitiesList = new ArrayList<E>(length);
        Constructor<E> constructor = null;
        if (length > 0) {
            try {
                constructor = entityType.getConstructor(Mediator.class, JSONObject.class);

                for (; index < length; index++) {
                    JSONObject jsonObject = array.getJSONObject(index);
                    entitiesList.add(constructor.newInstance(mediator, jsonObject));
                }
            } catch (Exception e) {
                mediator.error(e.getMessage(), e);
                return Collections.<E>emptyList();
            }
        }
        return entitiesList;
    }

    /**
     * Returns true if the {@link Immutable} annotation instance
     * passed as parameter contains the {@link Immutable.Operation}
     * also passed as parameter; Otherwise returns false.
     *
     * @param immutable the {@link Immutable} annotation instance
     * @param op        the {@link Immutable.Operation} to check whether it is
     *                  allowed or not
     * @return <ul>
     * <li>true if the specified {@link Immutable} annotation instance
     * contains the specified {@link Immutable.Operation}
     * </li>
     * <li>false otherwise</li>
     * </ul>
     */
    protected static boolean isImmutable(Immutable immutable, Immutable.Operation op) {
        for (Operation operation : immutable.operation()) {
            if (operation.equals(op)) {
                return true;
            }
        }
        return false;
    }

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    protected final Mediator mediator;
    protected final Class<E> entityType;
    protected final Map<String, UserDefinedSelectStatement> userDefinedSelectStatements;

    /**
     * Constructor
     *
     * @param mediator
     * @param entityType
     */
    AbstractSnaDAO(Mediator mediator, Class<E> entityType) {
        this.mediator = mediator;
        this.entityType = entityType;
        this.userDefinedSelectStatements = new HashMap<String, UserDefinedSelectStatement>();
    }

    /**
     * @param name
     * @param statement
     */
    protected void registerUserDefinedSelectStatement(String name, UserDefinedSelectStatement statement) {
        this.userDefinedSelectStatements.put(name, statement);
    }

    /**
     * @inheritDoc
     * @see SnaDAO#select(java.lang.String)
     */
    @Override
    public List<E> select(String name, String... variables) throws DAOException {
        UserDefinedSelectStatement userDefinedSelectStatement = this.userDefinedSelectStatements.get(name);

        if (userDefinedSelectStatement == null) {
            return select();
        }
        String query = userDefinedSelectStatement.getStatement();
        JSONArray array = AbstractSnaDAO.select(mediator, query, variables);

        int index = 0;
        int length = array == null ? 0 : array.length();
        List<E> entitiesList = new ArrayList<E>(length);

        for (; index < length; index++) {
            try {
                JSONObject jsonObject = array.getJSONObject(index);
                entitiesList.add(entityType.getConstructor(Mediator.class, JSONObject.class).newInstance(mediator, jsonObject));

            } catch (Exception e) {
                mediator.error(e.getMessage(), e);
                return Collections.<E>emptyList();
            }
        }
        return entitiesList;
    }

    /**
     * @inheritDoc
     * @see SnaDAO#
     * select(java.util.Map)
     */
    @Override
    public List<E> select(Map<String, Object> whereDirectives) throws DAOException {
        if (whereDirectives == null) {
            return select();
        }
        SelectDirective selectDirective = SelectDirective.getSelectDirective(this.mediator, entityType);

        Iterator<Map.Entry<String, Object>> iterator = whereDirectives.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            selectDirective.where(entry.getKey(), entry.getValue());
        }
        JSONArray array = AbstractSnaDAO.select(mediator, selectDirective.toString());

        int index = 0;
        int length = array == null ? 0 : array.length();
        List<E> entitiesList = new ArrayList<E>(length);

        for (; index < length; index++) {
            try {
                JSONObject jsonObject = array.getJSONObject(index);
                entitiesList.add(entityType.getConstructor(Mediator.class, JSONObject.class).newInstance(mediator, jsonObject));

            } catch (Exception e) {
                e.printStackTrace();
                mediator.error(e.getMessage(), e);
                return Collections.<E>emptyList();
            }
        }
        return entitiesList;
    }

    /**
     * @inheritDoc
     * @see SnaDAO#select(java.util.Map)
     */
    @Override
    public E select(List<SnaEntity.Key> keyMap) throws DAOException {
        KeyDirective keyDirective = KeyDirective.createKeyDirective(mediator, entityType);
        keyDirective.assign(keyMap);

        E entity = AbstractSnaDAO.getEntity(this.mediator, this.entityType, keyDirective);
        return entity;
    }

    /**
     * @inheritDoc
     * @see SnaDAO#select()
     */
    @Override
    public List<E> select() throws DAOException {
        List<E> entities = AbstractSnaDAO.getEntities(this.mediator, this.entityType);
        return entities;
    }

    /**
     * @inheritDoc
     * @see SnaDAO#
     * create(SnaEntity)
     */
    @Override
    public void create(E entity) throws DAOException {
        CreateDirective createDirective = CreateDirective.getCreateDirective(this.mediator, entity);

        this.created(entity, AbstractSnaDAO.create(mediator, createDirective.toString()));
    }

    /**
     * @inheritDoc
     * @see SnaDAO#
     * update(SnaEntity)
     */
    @Override
    public void update(E entity) throws DAOException {
        UpdateDirective updateDirective = UpdateDirective.getUpdateDirective(this.mediator, entity);
        this.updated(AbstractSnaDAO.update(mediator, updateDirective.toString()));
    }

    /**
     * @inheritDoc
     * @see SnaDAO#
     * delete(SnaEntity)
     */
    @Override
    public void delete(E entity) throws DAOException {
        DeleteDirective deleteDirective = DeleteDirective.getDeleteDirective(this.mediator, entity);
        this.deleted(AbstractSnaDAO.delete(this.mediator, deleteDirective.toString()));
    }

}
