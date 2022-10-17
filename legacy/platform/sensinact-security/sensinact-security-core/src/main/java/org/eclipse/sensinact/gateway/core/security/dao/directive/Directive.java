/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
/**
 * 
 */
package org.eclipse.sensinact.gateway.core.security.dao.directive;

import java.util.Map;

import org.eclipse.sensinact.gateway.core.security.dao.SnaDAO;
import org.eclipse.sensinact.gateway.util.CastUtils;

/**
 * 
 */
public abstract class Directive {
	protected String table;

	/**
	 * @param table
	 */
	protected Directive( String table) {
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
			builder.append(CastUtils.cast(String.class, object));
		}
		return builder.toString();
	}
}
