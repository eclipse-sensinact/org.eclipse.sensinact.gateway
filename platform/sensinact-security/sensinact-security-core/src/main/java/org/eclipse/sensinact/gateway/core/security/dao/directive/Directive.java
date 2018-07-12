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
/**
 *
 */
package org.eclipse.sensinact.gateway.core.security.dao.directive;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.dao.SnaDAO;
import org.eclipse.sensinact.gateway.util.CastUtils;

import java.util.Map;

/**
 *
 */
public abstract class Directive {
    protected Mediator mediator;
    protected String table;

    /**
     * @param mediator
     * @param table
     */
    protected Directive(Mediator mediator, String table) {
        this.mediator = mediator;
        this.table = table;
    }

    /**
     * @param builder
     * @param entry
     */
    protected void buildEqualityDirective(StringBuilder builder, Map.Entry<String, Object> entry) {
        this.buildEqualityDirective(builder, entry.getKey(), entry.getValue());
    }

    /**
     * @param builder
     * @param column
     * @param value
     */
    protected void buildEqualityDirective(StringBuilder builder, String column, Object value) {
        builder.append(this.getColumnName(column));
        builder.append(SnaDAO.EQUALS_OPERATOR);
        builder.append(this.getStringValue(value));
    }

    /**
     * @param object
     * @return
     */
    protected String getColumnName(String column) {
        String[] columnElements = column.split(".");
        if (columnElements.length == 2 && this.table.equals(columnElements[0])) {
            return column;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(this.table);
        builder.append(SnaDAO.DOT);
        builder.append(column);
        return builder.toString();
    }

    /**
     * @param object
     * @return
     */
    protected String getStringValue(Object object) {
        StringBuilder builder = new StringBuilder();
        if (object != null && object.getClass() == String.class) {
            builder.append(SnaDAO.QUOTE);
            builder.append((String) object);
            builder.append(SnaDAO.QUOTE);

        } else {
            builder.append(CastUtils.cast(mediator.getClassLoader(), String.class, object));
        }
        return builder.toString();
    }
}
