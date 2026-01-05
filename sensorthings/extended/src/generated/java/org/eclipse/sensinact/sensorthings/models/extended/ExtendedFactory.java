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

import org.eclipse.emf.ecore.EFactory;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage
 * @generated
 */
@ProviderType
public interface ExtendedFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ExtendedFactory eINSTANCE = org.eclipse.sensinact.sensorthings.models.extended.impl.ExtendedFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Sensor Thing Device</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Sensor Thing Device</em>'.
	 * @generated
	 */
	SensorThingDevice createSensorThingDevice();

	/**
	 * Returns a new object of class '<em>Sensor Thing Location Service</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Sensor Thing Location Service</em>'.
	 * @generated
	 */
	SensorThingLocationService createSensorThingLocationService();

	/**
	 * Returns a new object of class '<em>Data Stream Service</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Data Stream Service</em>'.
	 * @generated
	 */
	DataStreamService createDataStreamService();

	/**
	 * Returns a new object of class '<em>Sensor Thing Service</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Sensor Thing Service</em>'.
	 * @generated
	 */
	SensorThingService createSensorThingService();

	/**
	 * Returns a new object of class '<em>Sensor Thing Location</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Sensor Thing Location</em>'.
	 * @generated
	 */
	SensorThingLocation createSensorThingLocation();

	/**
	 * Returns a new object of class '<em>Sensor Thing Datastream</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Sensor Thing Datastream</em>'.
	 * @generated
	 */
	SensorThingDatastream createSensorThingDatastream();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	ExtendedPackage getExtendedPackage();

} //ExtendedFactory
