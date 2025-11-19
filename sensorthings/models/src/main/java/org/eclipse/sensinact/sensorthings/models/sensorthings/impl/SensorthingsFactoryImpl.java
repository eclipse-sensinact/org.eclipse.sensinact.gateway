/**
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
package org.eclipse.sensinact.sensorthings.models.sensorthings.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.sensinact.sensorthings.models.sensorthings.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class SensorthingsFactoryImpl extends EFactoryImpl implements SensorthingsFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static SensorthingsFactory init() {
		try {
			SensorthingsFactory theSensorthingsFactory = (SensorthingsFactory)EPackage.Registry.INSTANCE.getEFactory(SensorthingsPackage.eNS_URI);
			if (theSensorthingsFactory != null) {
				return theSensorthingsFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new SensorthingsFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SensorthingsFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case SensorthingsPackage.SENSOR_THINGS_DEVICE: return createSensorThingsDevice();
			case SensorthingsPackage.SENSOR_THINGS_SERVICE: return createSensorThingsService();
			case SensorthingsPackage.DATA_STREAM_SERVICE: return createDataStreamService();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SensorThingsDevice createSensorThingsDevice() {
		SensorThingsDeviceImpl sensorThingsDevice = new SensorThingsDeviceImpl();
		return sensorThingsDevice;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SensorThingsService createSensorThingsService() {
		SensorThingsServiceImpl sensorThingsService = new SensorThingsServiceImpl();
		return sensorThingsService;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DataStreamService createDataStreamService() {
		DataStreamServiceImpl dataStreamService = new DataStreamServiceImpl();
		return dataStreamService;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SensorthingsPackage getSensorthingsPackage() {
		return (SensorthingsPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static SensorthingsPackage getPackage() {
		return SensorthingsPackage.eINSTANCE;
	}

} //SensorthingsFactoryImpl
