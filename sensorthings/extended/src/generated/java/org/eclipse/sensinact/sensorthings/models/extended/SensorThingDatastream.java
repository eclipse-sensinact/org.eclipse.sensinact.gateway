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

import org.eclipse.emf.common.util.EList;

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
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingDatastream#getDataStreams <em>Data Streams</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorThingDatastream()
 * @model
 * @generated
 */
@ProviderType
public interface SensorThingDatastream extends DynamicProvider {
	/**
	 * Returns the value of the '<em><b>Data Streams</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data Streams</em>' containment reference list.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorThingDatastream_DataStreams()
	 * @model containment="true"
	 * @generated
	 */
	EList<DataStreamService> getDataStreams();

} // SensorThingDatastream
