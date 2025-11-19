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
package org.eclipse.sensinact.sensorthings.models.sensorthings;

import org.eclipse.emf.ecore.EFactory;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage
 * @generated
 */
@ProviderType
public interface SensorthingsFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	SensorthingsFactory eINSTANCE = org.eclipse.sensinact.sensorthings.models.sensorthings.impl.SensorthingsFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Sensor Things Device</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Sensor Things Device</em>'.
	 * @generated
	 */
	SensorThingsDevice createSensorThingsDevice();

	/**
	 * Returns a new object of class '<em>Sensor Things Service</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Sensor Things Service</em>'.
	 * @generated
	 */
	SensorThingsService createSensorThingsService();

	/**
	 * Returns a new object of class '<em>Data Stream Service</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Data Stream Service</em>'.
	 * @generated
	 */
	DataStreamService createDataStreamService();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	SensorthingsPackage getSensorthingsPackage();

} //SensorthingsFactory
