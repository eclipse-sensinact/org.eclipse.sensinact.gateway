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
package org.eclipse.sensinact.sensorthings.models.extended.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.sensinact.sensorthings.models.extended.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ExtendedFactoryImpl extends EFactoryImpl implements ExtendedFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ExtendedFactory init() {
		try {
			ExtendedFactory theExtendedFactory = (ExtendedFactory)EPackage.Registry.INSTANCE.getEFactory(ExtendedPackage.eNS_URI);
			if (theExtendedFactory != null) {
				return theExtendedFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new ExtendedFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ExtendedFactoryImpl() {
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
			case ExtendedPackage.SENSOR_THING_DEVICE: return createSensorThingDevice();
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE: return createSensorThingLocationService();
			case ExtendedPackage.DATA_STREAM_SERVICE: return createDataStreamService();
			case ExtendedPackage.SENSOR_THING_SERVICE: return createSensorThingService();
			case ExtendedPackage.SENSOR_THING_LOCATION: return createSensorThingLocation();
			case ExtendedPackage.SENSOR_THING_DATASTREAM: return createSensorThingDatastream();
			case ExtendedPackage.SENSOR_THING_OBSERVED_PROPERTY: return createSensorThingObservedProperty();
			case ExtendedPackage.SENSOR_THING_SENSOR: return createSensorThingSensor();
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE: return createObservedPropertyService();
			case ExtendedPackage.SENSOR_SERVICE: return createSensorService();
			case ExtendedPackage.SENSOR_THING_FOI: return createSensorThingFoi();
			case ExtendedPackage.FEATURE_THING_SERVICE: return createFeatureThingService();
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
	public SensorThingDevice createSensorThingDevice() {
		SensorThingDeviceImpl sensorThingDevice = new SensorThingDeviceImpl();
		return sensorThingDevice;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SensorThingLocationService createSensorThingLocationService() {
		SensorThingLocationServiceImpl sensorThingLocationService = new SensorThingLocationServiceImpl();
		return sensorThingLocationService;
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
	public SensorThingService createSensorThingService() {
		SensorThingServiceImpl sensorThingService = new SensorThingServiceImpl();
		return sensorThingService;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SensorThingLocation createSensorThingLocation() {
		SensorThingLocationImpl sensorThingLocation = new SensorThingLocationImpl();
		return sensorThingLocation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SensorThingDatastream createSensorThingDatastream() {
		SensorThingDatastreamImpl sensorThingDatastream = new SensorThingDatastreamImpl();
		return sensorThingDatastream;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SensorThingObservedProperty createSensorThingObservedProperty() {
		SensorThingObservedPropertyImpl sensorThingObservedProperty = new SensorThingObservedPropertyImpl();
		return sensorThingObservedProperty;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SensorThingSensor createSensorThingSensor() {
		SensorThingSensorImpl sensorThingSensor = new SensorThingSensorImpl();
		return sensorThingSensor;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ObservedPropertyService createObservedPropertyService() {
		ObservedPropertyServiceImpl observedPropertyService = new ObservedPropertyServiceImpl();
		return observedPropertyService;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SensorService createSensorService() {
		SensorServiceImpl sensorService = new SensorServiceImpl();
		return sensorService;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SensorThingFoi createSensorThingFoi() {
		SensorThingFoiImpl sensorThingFoi = new SensorThingFoiImpl();
		return sensorThingFoi;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FeatureThingService createFeatureThingService() {
		FeatureThingServiceImpl featureThingService = new FeatureThingServiceImpl();
		return featureThingService;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ExtendedPackage getExtendedPackage() {
		return (ExtendedPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static ExtendedPackage getPackage() {
		return ExtendedPackage.eINSTANCE;
	}

} //ExtendedFactoryImpl
