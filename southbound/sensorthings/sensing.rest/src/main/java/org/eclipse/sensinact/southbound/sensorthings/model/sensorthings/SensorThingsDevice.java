/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Kentyou - initial API and implementation 
 */
package org.eclipse.sensinact.southbound.sensorthings.model.sensorthings;

import org.eclipse.sensinact.model.core.provider.DynamicProvider;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sensor Things Device</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.southbound.sensorthings.model.sensorthings.SensorThingsDevice#getThing <em>Thing</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.southbound.sensorthings.model.sensorthings.SensorthingsPackage#getSensorThingsDevice()
 * @model
 * @generated
 */
@ProviderType
public interface SensorThingsDevice extends DynamicProvider {
	/**
	 * Returns the value of the '<em><b>Thing</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Thing</em>' reference.
	 * @see #setThing(SensorThingsService)
	 * @see org.eclipse.sensinact.southbound.sensorthings.model.sensorthings.SensorthingsPackage#getSensorThingsDevice_Thing()
	 * @model
	 * @generated
	 */
	SensorThingsService getThing();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.southbound.sensorthings.model.sensorthings.SensorThingsDevice#getThing <em>Thing</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Thing</em>' reference.
	 * @see #getThing()
	 * @generated
	 */
	void setThing(SensorThingsService value);

} // SensorThingsDevice
