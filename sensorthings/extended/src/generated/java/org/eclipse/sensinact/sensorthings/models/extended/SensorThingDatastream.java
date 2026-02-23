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
 * A representation of the model object '<em><b>Sensor Thing Datastream</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingDatastream#getDatastream <em>Datastream</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorThingDatastream()
 * @model
 * @generated
 */
@ProviderType
public interface SensorThingDatastream extends DynamicProvider {
	/**
	 * Returns the value of the '<em><b>Datastream</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Datastream</em>' containment reference.
	 * @see #setDatastream(DataStreamService)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorThingDatastream_Datastream()
	 * @model containment="true"
	 * @generated
	 */
	DataStreamService getDatastream();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingDatastream#getDatastream <em>Datastream</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Datastream</em>' containment reference.
	 * @see #getDatastream()
	 * @generated
	 */
	void setDatastream(DataStreamService value);

} // SensorThingDatastream
