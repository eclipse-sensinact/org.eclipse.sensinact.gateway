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
package org.eclipse.sensinact.gateway.core.security.dao.directive;

import java.lang.reflect.Field;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.security.dao.SnaDAO;
import org.eclipse.sensinact.gateway.core.security.entity.SnaEntity;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Column;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.PrimaryKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Table;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class DeleteDirective extends Directive {

	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	/**
	 * @param mediator
	 * @param entityType
	 * @param fields
	 * 
	 * @return
	 */
	public static <E extends SnaEntity> DeleteDirective getDeleteDirective(E entity) {
		Class<? extends SnaEntity> entityType = entity.getClass();

		Table table = entityType.getAnnotation(Table.class);
		Map<Field, Column> fields = SnaEntity.getFields(entityType);

		KeyDirective keyDirective = KeyDirective.createKeyDirective(table,
				entityType.getAnnotation(PrimaryKey.class), fields);
		keyDirective.assign(entity);
		DeleteDirective deleteDirective = new DeleteDirective(table.value(), keyDirective);
		return deleteDirective;
	}

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	protected KeyDirective keyDirective;

	/**
	 * Constructor
	 */
	public DeleteDirective(String table, KeyDirective keyDirective) {
		super(table);
		this.keyDirective = keyDirective;
	}

	/**
	 * @inheritDoc
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String deleteStatement = new StringBuilder().append(SnaDAO.DELETE_DIRECTIVE).append(SnaDAO.SPACE).append(table)
				.append(SnaDAO.SPACE).append(SnaDAO.WHERE_DIRECTIVE).append(SnaDAO.SPACE)
				.append(this.keyDirective.getValueDirective()).toString();
		// System.out.println("**************************************");
		// System.out.println(deleteStatement);
		// System.out.println("**************************************");
		return deleteStatement;
	}

}
