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
import org.eclipse.sensinact.gateway.common.primitive.Name;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.core.security.dao.SnaDAO;
import org.eclipse.sensinact.gateway.core.security.entity.SnaEntity;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Column;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.ForeignKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.PrimaryKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * DAO TABLE Primary Key directive
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class KeyDirective extends Directive {
    //********************************************************************//
    //						NESTED DECLARATIONS						  	  //
    //********************************************************************//

    /**
     * References a Column of the Table
     * of this KeyDirective
     */
    public class KeyEntry implements Nameable {
        private String column;
        private Object value = null;

        /**
         * @return the column
         */
        public String getColumn() {
            return column;
        }

        /**
         * @param column the column to set
         */
        public void setColumn(String column) {
            this.column = column;
        }

        /**
         * @return the value
         */
        public Object getValue() {
            return value;
        }

        /**
         * @param value the value to set
         */
        public void setValue(Object value) {
            this.value = value;
        }

        /**
         * @inheritDoc
         * @see Nameable#getName()
         */
        @Override
        public String getName() {
            return KeyDirective.this.getColumnName(this.column);
        }
    }

    /**
     * References Foreign Column and Table
     * for a composed primary key definition
     */
    class ForeignKeyEntry extends KeyEntry {
        private String foreignTable;
        private String foreignColumn;

        /**
         * @return the foreignTable
         */
        public String getForeignTable() {
            return foreignTable;
        }

        /**
         * @param foreignTable the foreignTable to set
         */
        public void setForeignTable(String foreignTable) {
            this.foreignTable = foreignTable;
        }

        /**
         * @return the foreignColumn
         */
        public String getForeignColumn() {
            return foreignColumn;
        }

        /**
         * @param foreignColumn the foreignColumn to set
         */
        public void setForeignColumn(String foreignColumn) {
            this.foreignColumn = foreignColumn;
        }

        /**
         * @inheritDoc
         * @see Nameable#getName()
         */
        @Override
        public String getName() {
            return new StringBuilder().append(this.foreignTable).append(SnaDAO.DOT).append(this.foreignColumn).toString();
        }
    }
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS  						  //
    //********************************************************************//

    /**
     * Creates and returns a {@link KeyDirective} object
     * for the extended {@link SnaEntity} type passed as
     * parameter
     *
     * @return a new {@link KeyDirective} instance for the
     * specified {@link SnaEntity} type
     */
    public static <E extends SnaEntity> KeyDirective createKeyDirective(Mediator mediator, Class<E> entityType) {
        return createKeyDirective(mediator, entityType.getAnnotation(Table.class), entityType.getAnnotation(PrimaryKey.class), SnaEntity.getFields(entityType));
    }

    /**
     * Creates and returns a {@link KeyDirective} object
     * for the extended {@link SnaEntity} type passed as
     * parameter
     *
     * @return a new {@link KeyDirective} instance for the
     * specified {@link SnaEntity} type
     */
    public static <E extends SnaEntity> KeyDirective createKeyDirective(Mediator mediator, Table table, PrimaryKey primaryKey, Map<Field, Column> fields) {
        KeyDirective keyDirective = new KeyDirective(mediator, table.value());
        String[] keys = primaryKey == null ? null : primaryKey.value();
        int length = keys == null ? 0 : keys.length;

        if (length > 0) {
            Iterator<Map.Entry<Field, Column>> iterator = fields.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<Field, Column> entry = iterator.next();
                String fieldAnnotation = entry.getValue().value();

                int index = 0;
                for (; index < length; index++) {
                    if (!keys[index].equals(fieldAnnotation)) {
                        continue;
                    }
                    ForeignKey foreignAnnotation = entry.getKey().getAnnotation(ForeignKey.class);

                    if (foreignAnnotation == null) {
                        keyDirective.addKeyEntry(keys[index]);

                    } else {
                        keyDirective.addForeignKeyEntry(keys[index], foreignAnnotation.table(), foreignAnnotation.refer());
                    }
                }

            }
        }
        return keyDirective;
    }

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    private List<KeyEntry> keyEntries;

    /**
     * Constructor
     */
    public KeyDirective(Mediator mediator, String table) {
        super(mediator, table);
        this.keyEntries = new ArrayList<KeyEntry>();
    }

    /**
     * @param keyDirective
     */
    protected void join(KeyDirective keyDirective) {
        this.keyEntries.addAll(keyDirective.keyEntries);
    }

    /**
     * @param entity
     */
    public void assign(SnaEntity entity) {
        this.assign(entity.getKeys());
    }

    /**
     * @param entity
     */
    public void assign(List<SnaEntity.Key> keyList) {
        Iterator<SnaEntity.Key> iterator = keyList.iterator();
        while (iterator.hasNext()) {
            SnaEntity.Key entry = iterator.next();
            this.assign(entry.getName(), entry.value());
        }
    }

    /**
     * @param entity
     */
    public void assign(String name, Object value) {
        int index = this.keyEntries.indexOf(new Name<KeyEntry>(name));
        if (index == -1) {
            return;
        }
        this.keyEntries.get(index).setValue(value);
    }

    /**
     * Defines the value of all KeyEntries of this KeyDirective
     * to -1
     */
    protected void reset() {
        Iterator<KeyEntry> iterator = this.keyEntries.iterator();
        while (iterator.hasNext()) {
            KeyEntry entry = iterator.next();
            entry.setValue(-1);
        }
    }

    /**
     * @param table
     * @param column
     */
    public KeyEntry addKeyEntry(String column) {
        KeyEntry keyEntry = new KeyEntry();
        keyEntry.setColumn(column);
        this.keyEntries.add(keyEntry);
        return keyEntry;
    }

    /**
     * @param table
     * @param column
     * @param foreignTable
     * @param foreignColumn
     */
    public ForeignKeyEntry addForeignKeyEntry(String column, String foreignTable, String foreignColumn) {
        ForeignKeyEntry keyEntry = new ForeignKeyEntry();
        keyEntry.setColumn(column);
        keyEntry.setForeignTable(foreignTable);
        keyEntry.setForeignColumn(foreignColumn);
        this.keyEntries.add(keyEntry);
        return keyEntry;
    }

    /**
     * @return
     */
    public String getValueDirective() {
        StringBuilder builder = new StringBuilder();
        Iterator<KeyEntry> iterator = this.keyEntries.iterator();

        while (iterator.hasNext()) {
            KeyEntry entry = iterator.next();
            if (entry.getValue() != null) {
                if (builder.length() > 0) {
                    builder.append(SnaDAO.SPACE).append(SnaDAO.AND).append(SnaDAO.SPACE);
                }
                super.buildEqualityDirective(builder, entry.getColumn(), entry.getValue());
            }
        }
        return builder.toString();
    }

    /**
     * @inheritDoc
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Iterator<KeyEntry> iterator = this.keyEntries.iterator();

        while (iterator.hasNext()) {
            KeyEntry entry = iterator.next();
            if (ForeignKeyEntry.class.isAssignableFrom(entry.getClass())) {
                if (builder.length() > 0) {
                    builder.append(SnaDAO.SPACE).append(SnaDAO.AND).append(SnaDAO.SPACE);
                }
                builder.append(((ForeignKeyEntry) entry).getForeignTable()).append(SnaDAO.DOT).append(((ForeignKeyEntry) entry).getForeignColumn()).append(SnaDAO.EQUALS_OPERATOR).append(super.getColumnName(entry.getColumn()));
            }
        }
        return builder.toString();
    }
}
