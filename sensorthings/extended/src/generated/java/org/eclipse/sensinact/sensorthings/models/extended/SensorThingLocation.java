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
package org.eclipse.sensinact.sensorthings.models.extended;

import org.eclipse.sensinact.model.core.provider.DynamicProvider;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sensor Thing Location</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingLocation#getLocation <em>Location</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorThingLocation()
 * @model
 * @generated
 */
@ProviderType
public interface SensorThingLocation extends DynamicProvider {
	/**
	 * Returns the value of the '<em><b>Location</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Location</em>' reference.
	 * @see #setLocation(SensorThingLocationService)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorThingLocation_Location()
	 * @model
	 * @generated
	 */
	SensorThingLocationService getLocation();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingLocation#getLocation <em>Location</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Location</em>' reference.
	 * @see #getLocation()
	 * @generated
	 */
	void setLocation(SensorThingLocationService value);

} // SensorThingLocation
