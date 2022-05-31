/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security.entity;

import org.eclipse.sensinact.gateway.core.security.entity.annotation.Immutable;

import jakarta.json.JsonObject;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Immutable
public abstract class ImmutableSnaEntity extends SnaEntity {

	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// INSTANCE DECLARATIONS //

	// ********************************************************************//

	/**
	 * @param mediator
	 */
	public ImmutableSnaEntity() {
		super();
	}

	/**
	 * @param mediator
	 * @param row
	 */
	public ImmutableSnaEntity(JsonObject row) {
		super(row);
	}
}
