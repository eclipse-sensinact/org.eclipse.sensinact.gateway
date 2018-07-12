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
package org.eclipse.sensinact.gateway.core.security.dao.directive;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.dao.SnaDAO;
import org.eclipse.sensinact.gateway.core.security.entity.SnaEntity;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Column;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.PrimaryKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Table;
import org.eclipse.sensinact.gateway.util.ReflectUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SelectDirective extends Directive {
    //********************************************************************//
    //						NESTED DECLARATIONS		    				  //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//

    //********************************************************************//
    //						STATIC DECLARATIONS		      				  //
    //********************************************************************//

    /**
     * @param mediator
     * @param entityType
     * @param fields
     * @return
     */
    static <E extends SnaEntity> SelectDirective getSelectDirective(Mediator mediator, Class<E> entityType, Map<Field, Column> fields) {
        Table table = entityType.getAnnotation(Table.class);

        KeyDirective keyDirective = KeyDirective.createKeyDirective(mediator, table, entityType.getAnnotation(PrimaryKey.class), fields);

        SelectDirective selectDirective = new SelectDirective(mediator, table.value(), keyDirective);

        Iterator<Map.Entry<Field, Column>> iterator = fields.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Field, Column> entry = iterator.next();
//			Field field = entry.getKey();

//			ForeignKey foreignKey = field.getAnnotation(ForeignKey.class);

//			if(foreignKey == null)
//			{				
            selectDirective.select(table.value(), entry.getValue().value());

//			} else if(SnaEntity.class.isAssignableFrom(field.getType()))
//			{
//				selectDirective.join(getSelectDirective(
//				mediator, (Class<? extends SnaEntity>)field.getType()));
//			}
        }
        selectDirective.join(keyDirective);
        return selectDirective;
    }

    /**
     * @param mediator
     * @param entityType
     * @return
     */
    public static <E extends SnaEntity> SelectDirective getSelectDirective(Mediator mediator, Class<E> entityType) {
        return SelectDirective.getSelectDirective(mediator, entityType, ReflectUtils.getAnnotatedFields(entityType, Column.class));
    }
    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    protected Map<String, List<String>> select;
    protected Map<String, Object> where;
    protected KeyDirective keyDirective;

    /**
     * Constructor
     */
    public SelectDirective(Mediator mediator, String table, KeyDirective keyDirective) {
        super(mediator, table);
        this.keyDirective = keyDirective;
        this.select = new HashMap<String, List<String>>();
        this.where = new HashMap<String, Object>();
    }

    /**
     * @param column
     */
    public void select(String table, String column) {
        if (column == null || column.length() == 0) {
            return;
        }
        String targetedTable = table;
        if (table == null || table.length() == 0) {
            targetedTable = this.table;
        }
        List<String> list = this.select.get(targetedTable);
        if (list == null) {
            list = new ArrayList<String>();
            this.select.put(targetedTable, list);
        }
        list.add(column);
    }

    /**
     * @param condition
     */
    public void where(String column, Object value) {
        if (column == null || column.length() == 0) {
            return;
        }
        this.where.put(column, value);
    }

    /**
     * Joins the SelectDirective passed as parameter
     * to this one
     *
     * @param selectDirective the SelectDirective to join to this one
     */
    public void join(SelectDirective selectDirective) {
        if (selectDirective == null) {
            return;
        }
        this.select.putAll(selectDirective.select);
        this.where.putAll(selectDirective.where);
        this.keyDirective.join(selectDirective.keyDirective);
    }

    /**
     * Joins the KeyDirective passed as parameter
     * to this SelectDirective
     *
     * @param keyDirective the KeyDirective to join to this
     *                     SelectDirective
     */
    public void join(KeyDirective keyDirective) {
        if (keyDirective != null) {
            this.keyDirective.join(keyDirective);
        }
    }

    /**
     * @param object
     * @return
     */
    protected String getColumnName(String table, String column) {
        if (table == null || table.equals(this.table)) {
            return super.getColumnName(column);
        }
        String[] columnElements = column.split(".");
        if (columnElements.length == 2 && table.equals(columnElements[0])) {
            return column;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(table);
        builder.append(SnaDAO.DOT);
        builder.append(column);
        return builder.toString();
    }

    /**
     * @inheritDoc
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (this.select.isEmpty()) {
            return null;
        }
        StringBuilder selectBuilder = new StringBuilder();
        selectBuilder.append(SnaDAO.SELECT_DIRECTIVE).append(SnaDAO.SPACE);
        StringBuilder fromBuilder = new StringBuilder();
        fromBuilder.append(SnaDAO.SPACE).append(SnaDAO.FROM_DIRECTIVE).append(SnaDAO.SPACE);
        Iterator<Map.Entry<String, List<String>>> iterator = this.select.entrySet().iterator();

        Map.Entry<String, List<String>> entry = null;
        int fromIndex = 0;

        while (iterator.hasNext()) {
            entry = iterator.next();
            List<String> list = entry.getValue();

            int selectIndex = 0;
            int length = list == null ? 0 : list.size();

            for (; selectIndex < length; selectIndex++) {
                if (selectIndex > 0) {
                    selectBuilder.append(SnaDAO.COMMA);
                }
                selectBuilder.append(this.getColumnName(entry.getKey(), list.get(selectIndex)));
            }
            if (fromIndex++ > 0) {
                fromBuilder.append(SnaDAO.COMMA);
            }
            fromBuilder.append(entry.getKey());
        }
        List<String> directives = new ArrayList<String>();

        Iterator<Map.Entry<String, Object>> conditionIterator = this.where.entrySet().iterator();
        while (conditionIterator.hasNext()) {
            StringBuilder builder = new StringBuilder();
            super.buildEqualityDirective(builder, conditionIterator.next());
            directives.add(builder.toString());
        }
        String keyDirectiveStr = null;
        String keyDirectiveValue = null;

        if (keyDirective != null
                //&& (keyDirectiveStr = keyDirective.toString())!= null
                && (keyDirectiveValue = keyDirective.getValueDirective()) != null
                //&& keyDirectiveStr.length() > 0
                && keyDirectiveValue.length() > 0) {
            //directives.add(keyDirectiveStr);
            directives.add(keyDirectiveValue);
        }

        StringBuilder whereBuilder = new StringBuilder();
        if (!directives.isEmpty()) {
            whereBuilder.append(SnaDAO.SPACE).append(SnaDAO.WHERE_DIRECTIVE).append(SnaDAO.SPACE);

            int index = 0;
            int length = directives.size();

            for (; index < length; index++) {
                if (index > 0) {
                    whereBuilder.append(SnaDAO.SPACE).append(SnaDAO.AND).append(SnaDAO.SPACE);
                }
                whereBuilder.append(directives.get(index));
            }
        }
        String selectStatement = selectBuilder.append(fromBuilder.toString()).append(whereBuilder.toString()).toString();
        return selectStatement;
    }
}
