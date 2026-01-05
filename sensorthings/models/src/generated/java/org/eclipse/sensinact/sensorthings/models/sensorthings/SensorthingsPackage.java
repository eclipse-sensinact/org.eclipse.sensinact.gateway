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


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.sensinact.model.core.provider.ProviderPackage;

import org.gecko.emf.osgi.annotation.provide.EPackage;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsFactory
 * @model kind="package"
 *        annotation="http://www.eclipse.org/OCL/Import ecore='http://www.eclipse.org/emf/2002/Ecore'"
 * @generated
 */
@ProviderType
@EPackage(uri = SensorthingsPackage.eNS_URI, genModel = "/model/sensorthings.genmodel", genModelSourceLocations = {"src/main/resources/model/sensorthings.genmodel","models/src/main/resources/model/sensorthings.genmodel"}, ecore="/model/sensorthings.ecore", ecoreSourceLocations="/src/main/resources/model/sensorthings.ecore")
public interface SensorthingsPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "sensorthings";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://eclipse.org/sensinact/sensorthings/import/1.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "sensorthings";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	SensorthingsPackage eINSTANCE = org.eclipse.sensinact.sensorthings.models.sensorthings.impl.SensorthingsPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.impl.SensorThingsDeviceImpl <em>Sensor Things Device</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.impl.SensorThingsDeviceImpl
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.impl.SensorthingsPackageImpl#getSensorThingsDevice()
	 * @generated
	 */
	int SENSOR_THINGS_DEVICE = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THINGS_DEVICE__ID = ProviderPackage.DYNAMIC_PROVIDER__ID;

	/**
	 * The feature id for the '<em><b>Admin</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THINGS_DEVICE__ADMIN = ProviderPackage.DYNAMIC_PROVIDER__ADMIN;

	/**
	 * The feature id for the '<em><b>Linked Providers</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THINGS_DEVICE__LINKED_PROVIDERS = ProviderPackage.DYNAMIC_PROVIDER__LINKED_PROVIDERS;

	/**
	 * The feature id for the '<em><b>Services</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THINGS_DEVICE__SERVICES = ProviderPackage.DYNAMIC_PROVIDER__SERVICES;

	/**
	 * The feature id for the '<em><b>Thing</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THINGS_DEVICE__THING = ProviderPackage.DYNAMIC_PROVIDER_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Sensor Things Device</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THINGS_DEVICE_FEATURE_COUNT = ProviderPackage.DYNAMIC_PROVIDER_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Service</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THINGS_DEVICE___GET_SERVICE__STRING = ProviderPackage.DYNAMIC_PROVIDER___GET_SERVICE__STRING;

	/**
	 * The operation id for the '<em>Get Service EClass</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THINGS_DEVICE___GET_SERVICE_ECLASS__STRING = ProviderPackage.DYNAMIC_PROVIDER___GET_SERVICE_ECLASS__STRING;

	/**
	 * The number of operations of the '<em>Sensor Things Device</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THINGS_DEVICE_OPERATION_COUNT = ProviderPackage.DYNAMIC_PROVIDER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.impl.SensorThingsServiceImpl <em>Sensor Things Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.impl.SensorThingsServiceImpl
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.impl.SensorthingsPackageImpl#getSensorThingsService()
	 * @generated
	 */
	int SENSOR_THINGS_SERVICE = 1;

	/**
	 * The feature id for the '<em><b>Metadata</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THINGS_SERVICE__METADATA = ProviderPackage.SERVICE__METADATA;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THINGS_SERVICE__ID = ProviderPackage.SERVICE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THINGS_SERVICE__DESCRIPTION = ProviderPackage.SERVICE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Sensor Things Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THINGS_SERVICE_FEATURE_COUNT = ProviderPackage.SERVICE_FEATURE_COUNT + 2;

	/**
	 * The operation id for the '<em>EIs Set</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THINGS_SERVICE___EIS_SET__ESTRUCTURALFEATURE = ProviderPackage.SERVICE___EIS_SET__ESTRUCTURALFEATURE;

	/**
	 * The number of operations of the '<em>Sensor Things Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THINGS_SERVICE_OPERATION_COUNT = ProviderPackage.SERVICE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.impl.DataStreamServiceImpl <em>Data Stream Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.impl.DataStreamServiceImpl
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.impl.SensorthingsPackageImpl#getDataStreamService()
	 * @generated
	 */
	int DATA_STREAM_SERVICE = 2;

	/**
	 * The feature id for the '<em><b>Metadata</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__METADATA = ProviderPackage.SERVICE__METADATA;

	/**
	 * The feature id for the '<em><b>Sensor Things Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__SENSOR_THINGS_ID = ProviderPackage.SERVICE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__NAME = ProviderPackage.SERVICE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__DESCRIPTION = ProviderPackage.SERVICE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Latest Observation</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__LATEST_OBSERVATION = ProviderPackage.SERVICE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__UNIT = ProviderPackage.SERVICE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Sensor</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__SENSOR = ProviderPackage.SERVICE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Observed Property</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__OBSERVED_PROPERTY = ProviderPackage.SERVICE_FEATURE_COUNT + 6;

	/**
	 * The number of structural features of the '<em>Data Stream Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE_FEATURE_COUNT = ProviderPackage.SERVICE_FEATURE_COUNT + 7;

	/**
	 * The operation id for the '<em>EIs Set</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE___EIS_SET__ESTRUCTURALFEATURE = ProviderPackage.SERVICE___EIS_SET__ESTRUCTURALFEATURE;

	/**
	 * The number of operations of the '<em>Data Stream Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE_OPERATION_COUNT = ProviderPackage.SERVICE_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.SensorThingsDevice <em>Sensor Things Device</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Sensor Things Device</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorThingsDevice
	 * @generated
	 */
	EClass getSensorThingsDevice();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.SensorThingsDevice#getThing <em>Thing</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Thing</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorThingsDevice#getThing()
	 * @see #getSensorThingsDevice()
	 * @generated
	 */
	EReference getSensorThingsDevice_Thing();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.SensorThingsService <em>Sensor Things Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Sensor Things Service</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorThingsService
	 * @generated
	 */
	EClass getSensorThingsService();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.SensorThingsService#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorThingsService#getId()
	 * @see #getSensorThingsService()
	 * @generated
	 */
	EAttribute getSensorThingsService_Id();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.SensorThingsService#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorThingsService#getDescription()
	 * @see #getSensorThingsService()
	 * @generated
	 */
	EAttribute getSensorThingsService_Description();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService <em>Data Stream Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Data Stream Service</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService
	 * @generated
	 */
	EClass getDataStreamService();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getSensorThingsId <em>Sensor Things Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sensor Things Id</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getSensorThingsId()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_SensorThingsId();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getName()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getDescription()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_Description();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getLatestObservation <em>Latest Observation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Latest Observation</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getLatestObservation()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_LatestObservation();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getUnit <em>Unit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Unit</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getUnit()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_Unit();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getSensor <em>Sensor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sensor</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getSensor()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_Sensor();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getObservedProperty <em>Observed Property</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Observed Property</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getObservedProperty()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_ObservedProperty();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	SensorthingsFactory getSensorthingsFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.impl.SensorThingsDeviceImpl <em>Sensor Things Device</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.impl.SensorThingsDeviceImpl
		 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.impl.SensorthingsPackageImpl#getSensorThingsDevice()
		 * @generated
		 */
		EClass SENSOR_THINGS_DEVICE = eINSTANCE.getSensorThingsDevice();

		/**
		 * The meta object literal for the '<em><b>Thing</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SENSOR_THINGS_DEVICE__THING = eINSTANCE.getSensorThingsDevice_Thing();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.impl.SensorThingsServiceImpl <em>Sensor Things Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.impl.SensorThingsServiceImpl
		 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.impl.SensorthingsPackageImpl#getSensorThingsService()
		 * @generated
		 */
		EClass SENSOR_THINGS_SERVICE = eINSTANCE.getSensorThingsService();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SENSOR_THINGS_SERVICE__ID = eINSTANCE.getSensorThingsService_Id();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SENSOR_THINGS_SERVICE__DESCRIPTION = eINSTANCE.getSensorThingsService_Description();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.impl.DataStreamServiceImpl <em>Data Stream Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.impl.DataStreamServiceImpl
		 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.impl.SensorthingsPackageImpl#getDataStreamService()
		 * @generated
		 */
		EClass DATA_STREAM_SERVICE = eINSTANCE.getDataStreamService();

		/**
		 * The meta object literal for the '<em><b>Sensor Things Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__SENSOR_THINGS_ID = eINSTANCE.getDataStreamService_SensorThingsId();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__NAME = eINSTANCE.getDataStreamService_Name();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__DESCRIPTION = eINSTANCE.getDataStreamService_Description();

		/**
		 * The meta object literal for the '<em><b>Latest Observation</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__LATEST_OBSERVATION = eINSTANCE.getDataStreamService_LatestObservation();

		/**
		 * The meta object literal for the '<em><b>Unit</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__UNIT = eINSTANCE.getDataStreamService_Unit();

		/**
		 * The meta object literal for the '<em><b>Sensor</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__SENSOR = eINSTANCE.getDataStreamService_Sensor();

		/**
		 * The meta object literal for the '<em><b>Observed Property</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__OBSERVED_PROPERTY = eINSTANCE.getDataStreamService_ObservedProperty();

	}

} //SensorthingsPackage
