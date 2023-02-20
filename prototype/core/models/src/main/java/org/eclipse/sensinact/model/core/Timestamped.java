/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Data In Motion - initial API and implementation 
 */
package org.eclipse.sensinact.model.core;

import java.time.Instant;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Timestamped</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.model.core.Timestamped#getTimestamp <em>Timestamp</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getTimestamped()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface Timestamped extends EObject {
	/**
	 * Returns the value of the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Timestamp</em>' attribute.
	 * @see #setTimestamp(Instant)
	 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getTimestamped_Timestamp()
	 * @model dataType="org.eclipse.sensinact.model.core.EInstant"
	 * @generated
	 */
	Instant getTimestamp();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.model.core.Timestamped#getTimestamp <em>Timestamp</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Timestamp</em>' attribute.
	 * @see #getTimestamp()
	 * @generated
	 */
	void setTimestamp(Instant value);

} // Timestamped
