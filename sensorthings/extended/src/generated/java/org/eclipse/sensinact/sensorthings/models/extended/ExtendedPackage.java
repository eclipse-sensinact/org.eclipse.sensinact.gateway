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
 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedFactory
 * @model kind="package"
 *        annotation="http://www.eclipse.org/OCL/Import ecore='http://www.eclipse.org/emf/2002/Ecore'"
 * @generated
 */
@ProviderType
@EPackage(uri = ExtendedPackage.eNS_URI, genModel = "/model/sensorthings.extended.genmodel", genModelSourceLocations = {"src/main/resources/model/sensorthings.extended.genmodel","extended/src/main/resources/model/sensorthings.extended.genmodel"}, ecore="/model/sensorthings.extended.ecore", ecoreSourceLocations="/src/main/resources/model/sensorthings.extended.ecore")
public interface ExtendedPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "extended";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://org.eclipse.sensinact/sensorthings/extended";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "ste";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ExtendedPackage eINSTANCE = org.eclipse.sensinact.sensorthings.models.extended.impl.ExtendedPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingDeviceImpl <em>Sensor Thing Device</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingDeviceImpl
	 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.ExtendedPackageImpl#getSensorThingDevice()
	 * @generated
	 */
	int SENSOR_THING_DEVICE = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DEVICE__ID = ProviderPackage.DYNAMIC_PROVIDER__ID;

	/**
	 * The feature id for the '<em><b>Admin</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DEVICE__ADMIN = ProviderPackage.DYNAMIC_PROVIDER__ADMIN;

	/**
	 * The feature id for the '<em><b>Linked Providers</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DEVICE__LINKED_PROVIDERS = ProviderPackage.DYNAMIC_PROVIDER__LINKED_PROVIDERS;

	/**
	 * The feature id for the '<em><b>Services</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DEVICE__SERVICES = ProviderPackage.DYNAMIC_PROVIDER__SERVICES;

	/**
	 * The feature id for the '<em><b>Thing</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DEVICE__THING = ProviderPackage.DYNAMIC_PROVIDER_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Sensor Thing Device</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DEVICE_FEATURE_COUNT = ProviderPackage.DYNAMIC_PROVIDER_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Service</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DEVICE___GET_SERVICE__STRING = ProviderPackage.DYNAMIC_PROVIDER___GET_SERVICE__STRING;

	/**
	 * The operation id for the '<em>Get Service EClass</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DEVICE___GET_SERVICE_ECLASS__STRING = ProviderPackage.DYNAMIC_PROVIDER___GET_SERVICE_ECLASS__STRING;

	/**
	 * The number of operations of the '<em>Sensor Thing Device</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DEVICE_OPERATION_COUNT = ProviderPackage.DYNAMIC_PROVIDER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingLocationServiceImpl <em>Sensor Thing Location Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingLocationServiceImpl
	 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.ExtendedPackageImpl#getSensorThingLocationService()
	 * @generated
	 */
	int SENSOR_THING_LOCATION_SERVICE = 1;

	/**
	 * The feature id for the '<em><b>Metadata</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_LOCATION_SERVICE__METADATA = ProviderPackage.SERVICE__METADATA;

	/**
	 * The feature id for the '<em><b>Encoding Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_LOCATION_SERVICE__ENCODING_TYPE = ProviderPackage.SERVICE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_LOCATION_SERVICE__ID = ProviderPackage.SERVICE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Sensor Thing Location Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_LOCATION_SERVICE_FEATURE_COUNT = ProviderPackage.SERVICE_FEATURE_COUNT + 2;

	/**
	 * The operation id for the '<em>EIs Set</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_LOCATION_SERVICE___EIS_SET__ESTRUCTURALFEATURE = ProviderPackage.SERVICE___EIS_SET__ESTRUCTURALFEATURE;

	/**
	 * The number of operations of the '<em>Sensor Thing Location Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_LOCATION_SERVICE_OPERATION_COUNT = ProviderPackage.SERVICE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl <em>Data Stream Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl
	 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.ExtendedPackageImpl#getDataStreamService()
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
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__ID = ProviderPackage.SERVICE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__TIMESTAMP = ProviderPackage.SERVICE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Sensor Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__SENSOR_NAME = ProviderPackage.SERVICE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Sensor Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__SENSOR_DESCRIPTION = ProviderPackage.SERVICE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Sensor Encoding Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__SENSOR_ENCODING_TYPE = ProviderPackage.SERVICE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Unit Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__UNIT_NAME = ProviderPackage.SERVICE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Unit Symbol</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__UNIT_SYMBOL = ProviderPackage.SERVICE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Unit Definition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__UNIT_DEFINITION = ProviderPackage.SERVICE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Observed Property Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__OBSERVED_PROPERTY_NAME = ProviderPackage.SERVICE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Observed Property Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__OBSERVED_PROPERTY_ID = ProviderPackage.SERVICE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Observed Property Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__OBSERVED_PROPERTY_DESCRIPTION = ProviderPackage.SERVICE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Observed Property Definition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__OBSERVED_PROPERTY_DEFINITION = ProviderPackage.SERVICE_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Sensor Metadata</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__SENSOR_METADATA = ProviderPackage.SERVICE_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Sensor Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__SENSOR_ID = ProviderPackage.SERVICE_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Sensor Properties</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__SENSOR_PROPERTIES = ProviderPackage.SERVICE_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Observed Property Properties</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__OBSERVED_PROPERTY_PROPERTIES = ProviderPackage.SERVICE_FEATURE_COUNT + 15;

	/**
	 * The feature id for the '<em><b>Last Observation</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__LAST_OBSERVATION = ProviderPackage.SERVICE_FEATURE_COUNT + 16;

	/**
	 * The feature id for the '<em><b>Thing Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE__THING_ID = ProviderPackage.SERVICE_FEATURE_COUNT + 17;

	/**
	 * The number of structural features of the '<em>Data Stream Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_STREAM_SERVICE_FEATURE_COUNT = ProviderPackage.SERVICE_FEATURE_COUNT + 18;

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
	 * The meta object id for the '{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingServiceImpl <em>Sensor Thing Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingServiceImpl
	 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.ExtendedPackageImpl#getSensorThingService()
	 * @generated
	 */
	int SENSOR_THING_SERVICE = 3;

	/**
	 * The feature id for the '<em><b>Metadata</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_SERVICE__METADATA = ProviderPackage.SERVICE__METADATA;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_SERVICE__PROPERTIES = ProviderPackage.SERVICE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_SERVICE__ID = ProviderPackage.SERVICE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Location Ids</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_SERVICE__LOCATION_IDS = ProviderPackage.SERVICE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Datastream Ids</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_SERVICE__DATASTREAM_IDS = ProviderPackage.SERVICE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Sensor Thing Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_SERVICE_FEATURE_COUNT = ProviderPackage.SERVICE_FEATURE_COUNT + 4;

	/**
	 * The operation id for the '<em>EIs Set</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_SERVICE___EIS_SET__ESTRUCTURALFEATURE = ProviderPackage.SERVICE___EIS_SET__ESTRUCTURALFEATURE;

	/**
	 * The number of operations of the '<em>Sensor Thing Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_SERVICE_OPERATION_COUNT = ProviderPackage.SERVICE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingLocationImpl <em>Sensor Thing Location</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingLocationImpl
	 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.ExtendedPackageImpl#getSensorThingLocation()
	 * @generated
	 */
	int SENSOR_THING_LOCATION = 4;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_LOCATION__ID = ProviderPackage.DYNAMIC_PROVIDER__ID;

	/**
	 * The feature id for the '<em><b>Admin</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_LOCATION__ADMIN = ProviderPackage.DYNAMIC_PROVIDER__ADMIN;

	/**
	 * The feature id for the '<em><b>Linked Providers</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_LOCATION__LINKED_PROVIDERS = ProviderPackage.DYNAMIC_PROVIDER__LINKED_PROVIDERS;

	/**
	 * The feature id for the '<em><b>Services</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_LOCATION__SERVICES = ProviderPackage.DYNAMIC_PROVIDER__SERVICES;

	/**
	 * The feature id for the '<em><b>Location</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_LOCATION__LOCATION = ProviderPackage.DYNAMIC_PROVIDER_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Sensor Thing Location</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_LOCATION_FEATURE_COUNT = ProviderPackage.DYNAMIC_PROVIDER_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Service</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_LOCATION___GET_SERVICE__STRING = ProviderPackage.DYNAMIC_PROVIDER___GET_SERVICE__STRING;

	/**
	 * The operation id for the '<em>Get Service EClass</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_LOCATION___GET_SERVICE_ECLASS__STRING = ProviderPackage.DYNAMIC_PROVIDER___GET_SERVICE_ECLASS__STRING;

	/**
	 * The number of operations of the '<em>Sensor Thing Location</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_LOCATION_OPERATION_COUNT = ProviderPackage.DYNAMIC_PROVIDER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingDatastreamImpl <em>Sensor Thing Datastream</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingDatastreamImpl
	 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.ExtendedPackageImpl#getSensorThingDatastream()
	 * @generated
	 */
	int SENSOR_THING_DATASTREAM = 5;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DATASTREAM__ID = ProviderPackage.DYNAMIC_PROVIDER__ID;

	/**
	 * The feature id for the '<em><b>Admin</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DATASTREAM__ADMIN = ProviderPackage.DYNAMIC_PROVIDER__ADMIN;

	/**
	 * The feature id for the '<em><b>Linked Providers</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DATASTREAM__LINKED_PROVIDERS = ProviderPackage.DYNAMIC_PROVIDER__LINKED_PROVIDERS;

	/**
	 * The feature id for the '<em><b>Services</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DATASTREAM__SERVICES = ProviderPackage.DYNAMIC_PROVIDER__SERVICES;

	/**
	 * The feature id for the '<em><b>Datastream</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DATASTREAM__DATASTREAM = ProviderPackage.DYNAMIC_PROVIDER_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Sensor Thing Datastream</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DATASTREAM_FEATURE_COUNT = ProviderPackage.DYNAMIC_PROVIDER_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Service</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DATASTREAM___GET_SERVICE__STRING = ProviderPackage.DYNAMIC_PROVIDER___GET_SERVICE__STRING;

	/**
	 * The operation id for the '<em>Get Service EClass</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DATASTREAM___GET_SERVICE_ECLASS__STRING = ProviderPackage.DYNAMIC_PROVIDER___GET_SERVICE_ECLASS__STRING;

	/**
	 * The number of operations of the '<em>Sensor Thing Datastream</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SENSOR_THING_DATASTREAM_OPERATION_COUNT = ProviderPackage.DYNAMIC_PROVIDER_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingDevice <em>Sensor Thing Device</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Sensor Thing Device</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.SensorThingDevice
	 * @generated
	 */
	EClass getSensorThingDevice();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingDevice#getThing <em>Thing</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Thing</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.SensorThingDevice#getThing()
	 * @see #getSensorThingDevice()
	 * @generated
	 */
	EReference getSensorThingDevice_Thing();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingLocationService <em>Sensor Thing Location Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Sensor Thing Location Service</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.SensorThingLocationService
	 * @generated
	 */
	EClass getSensorThingLocationService();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingLocationService#getEncodingType <em>Encoding Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Encoding Type</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.SensorThingLocationService#getEncodingType()
	 * @see #getSensorThingLocationService()
	 * @generated
	 */
	EAttribute getSensorThingLocationService_EncodingType();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingLocationService#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.SensorThingLocationService#getId()
	 * @see #getSensorThingLocationService()
	 * @generated
	 */
	EAttribute getSensorThingLocationService_Id();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService <em>Data Stream Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Data Stream Service</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService
	 * @generated
	 */
	EClass getDataStreamService();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getId()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_Id();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getTimestamp <em>Timestamp</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Timestamp</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getTimestamp()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_Timestamp();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorName <em>Sensor Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sensor Name</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorName()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_SensorName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorDescription <em>Sensor Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sensor Description</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorDescription()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_SensorDescription();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorEncodingType <em>Sensor Encoding Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sensor Encoding Type</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorEncodingType()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_SensorEncodingType();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getUnitName <em>Unit Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Unit Name</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getUnitName()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_UnitName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getUnitSymbol <em>Unit Symbol</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Unit Symbol</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getUnitSymbol()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_UnitSymbol();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getUnitDefinition <em>Unit Definition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Unit Definition</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getUnitDefinition()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_UnitDefinition();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyName <em>Observed Property Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Observed Property Name</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyName()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_ObservedPropertyName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyId <em>Observed Property Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Observed Property Id</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyId()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_ObservedPropertyId();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyDescription <em>Observed Property Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Observed Property Description</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyDescription()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_ObservedPropertyDescription();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyDefinition <em>Observed Property Definition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Observed Property Definition</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyDefinition()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_ObservedPropertyDefinition();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorMetadata <em>Sensor Metadata</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sensor Metadata</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorMetadata()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_SensorMetadata();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorId <em>Sensor Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sensor Id</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorId()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_SensorId();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorProperties <em>Sensor Properties</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sensor Properties</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorProperties()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_SensorProperties();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyProperties <em>Observed Property Properties</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Observed Property Properties</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyProperties()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_ObservedPropertyProperties();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getLastObservation <em>Last Observation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Last Observation</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getLastObservation()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_LastObservation();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getThingId <em>Thing Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Thing Id</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getThingId()
	 * @see #getDataStreamService()
	 * @generated
	 */
	EAttribute getDataStreamService_ThingId();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingService <em>Sensor Thing Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Sensor Thing Service</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.SensorThingService
	 * @generated
	 */
	EClass getSensorThingService();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getProperties <em>Properties</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Properties</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getProperties()
	 * @see #getSensorThingService()
	 * @generated
	 */
	EAttribute getSensorThingService_Properties();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getId()
	 * @see #getSensorThingService()
	 * @generated
	 */
	EAttribute getSensorThingService_Id();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getLocationIds <em>Location Ids</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Location Ids</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getLocationIds()
	 * @see #getSensorThingService()
	 * @generated
	 */
	EAttribute getSensorThingService_LocationIds();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getDatastreamIds <em>Datastream Ids</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Datastream Ids</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getDatastreamIds()
	 * @see #getSensorThingService()
	 * @generated
	 */
	EAttribute getSensorThingService_DatastreamIds();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingLocation <em>Sensor Thing Location</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Sensor Thing Location</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.SensorThingLocation
	 * @generated
	 */
	EClass getSensorThingLocation();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingLocation#getLocation <em>Location</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Location</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.SensorThingLocation#getLocation()
	 * @see #getSensorThingLocation()
	 * @generated
	 */
	EReference getSensorThingLocation_Location();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingDatastream <em>Sensor Thing Datastream</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Sensor Thing Datastream</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.SensorThingDatastream
	 * @generated
	 */
	EClass getSensorThingDatastream();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingDatastream#getDatastream <em>Datastream</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Datastream</em>'.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.SensorThingDatastream#getDatastream()
	 * @see #getSensorThingDatastream()
	 * @generated
	 */
	EReference getSensorThingDatastream_Datastream();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ExtendedFactory getExtendedFactory();

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
		 * The meta object literal for the '{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingDeviceImpl <em>Sensor Thing Device</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingDeviceImpl
		 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.ExtendedPackageImpl#getSensorThingDevice()
		 * @generated
		 */
		EClass SENSOR_THING_DEVICE = eINSTANCE.getSensorThingDevice();

		/**
		 * The meta object literal for the '<em><b>Thing</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SENSOR_THING_DEVICE__THING = eINSTANCE.getSensorThingDevice_Thing();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingLocationServiceImpl <em>Sensor Thing Location Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingLocationServiceImpl
		 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.ExtendedPackageImpl#getSensorThingLocationService()
		 * @generated
		 */
		EClass SENSOR_THING_LOCATION_SERVICE = eINSTANCE.getSensorThingLocationService();

		/**
		 * The meta object literal for the '<em><b>Encoding Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SENSOR_THING_LOCATION_SERVICE__ENCODING_TYPE = eINSTANCE.getSensorThingLocationService_EncodingType();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SENSOR_THING_LOCATION_SERVICE__ID = eINSTANCE.getSensorThingLocationService_Id();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl <em>Data Stream Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl
		 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.ExtendedPackageImpl#getDataStreamService()
		 * @generated
		 */
		EClass DATA_STREAM_SERVICE = eINSTANCE.getDataStreamService();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__ID = eINSTANCE.getDataStreamService_Id();

		/**
		 * The meta object literal for the '<em><b>Timestamp</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__TIMESTAMP = eINSTANCE.getDataStreamService_Timestamp();

		/**
		 * The meta object literal for the '<em><b>Sensor Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__SENSOR_NAME = eINSTANCE.getDataStreamService_SensorName();

		/**
		 * The meta object literal for the '<em><b>Sensor Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__SENSOR_DESCRIPTION = eINSTANCE.getDataStreamService_SensorDescription();

		/**
		 * The meta object literal for the '<em><b>Sensor Encoding Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__SENSOR_ENCODING_TYPE = eINSTANCE.getDataStreamService_SensorEncodingType();

		/**
		 * The meta object literal for the '<em><b>Unit Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__UNIT_NAME = eINSTANCE.getDataStreamService_UnitName();

		/**
		 * The meta object literal for the '<em><b>Unit Symbol</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__UNIT_SYMBOL = eINSTANCE.getDataStreamService_UnitSymbol();

		/**
		 * The meta object literal for the '<em><b>Unit Definition</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__UNIT_DEFINITION = eINSTANCE.getDataStreamService_UnitDefinition();

		/**
		 * The meta object literal for the '<em><b>Observed Property Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__OBSERVED_PROPERTY_NAME = eINSTANCE.getDataStreamService_ObservedPropertyName();

		/**
		 * The meta object literal for the '<em><b>Observed Property Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__OBSERVED_PROPERTY_ID = eINSTANCE.getDataStreamService_ObservedPropertyId();

		/**
		 * The meta object literal for the '<em><b>Observed Property Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__OBSERVED_PROPERTY_DESCRIPTION = eINSTANCE.getDataStreamService_ObservedPropertyDescription();

		/**
		 * The meta object literal for the '<em><b>Observed Property Definition</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__OBSERVED_PROPERTY_DEFINITION = eINSTANCE.getDataStreamService_ObservedPropertyDefinition();

		/**
		 * The meta object literal for the '<em><b>Sensor Metadata</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__SENSOR_METADATA = eINSTANCE.getDataStreamService_SensorMetadata();

		/**
		 * The meta object literal for the '<em><b>Sensor Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__SENSOR_ID = eINSTANCE.getDataStreamService_SensorId();

		/**
		 * The meta object literal for the '<em><b>Sensor Properties</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__SENSOR_PROPERTIES = eINSTANCE.getDataStreamService_SensorProperties();

		/**
		 * The meta object literal for the '<em><b>Observed Property Properties</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__OBSERVED_PROPERTY_PROPERTIES = eINSTANCE.getDataStreamService_ObservedPropertyProperties();

		/**
		 * The meta object literal for the '<em><b>Last Observation</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__LAST_OBSERVATION = eINSTANCE.getDataStreamService_LastObservation();

		/**
		 * The meta object literal for the '<em><b>Thing Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_STREAM_SERVICE__THING_ID = eINSTANCE.getDataStreamService_ThingId();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingServiceImpl <em>Sensor Thing Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingServiceImpl
		 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.ExtendedPackageImpl#getSensorThingService()
		 * @generated
		 */
		EClass SENSOR_THING_SERVICE = eINSTANCE.getSensorThingService();

		/**
		 * The meta object literal for the '<em><b>Properties</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SENSOR_THING_SERVICE__PROPERTIES = eINSTANCE.getSensorThingService_Properties();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SENSOR_THING_SERVICE__ID = eINSTANCE.getSensorThingService_Id();

		/**
		 * The meta object literal for the '<em><b>Location Ids</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SENSOR_THING_SERVICE__LOCATION_IDS = eINSTANCE.getSensorThingService_LocationIds();

		/**
		 * The meta object literal for the '<em><b>Datastream Ids</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SENSOR_THING_SERVICE__DATASTREAM_IDS = eINSTANCE.getSensorThingService_DatastreamIds();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingLocationImpl <em>Sensor Thing Location</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingLocationImpl
		 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.ExtendedPackageImpl#getSensorThingLocation()
		 * @generated
		 */
		EClass SENSOR_THING_LOCATION = eINSTANCE.getSensorThingLocation();

		/**
		 * The meta object literal for the '<em><b>Location</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SENSOR_THING_LOCATION__LOCATION = eINSTANCE.getSensorThingLocation_Location();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingDatastreamImpl <em>Sensor Thing Datastream</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingDatastreamImpl
		 * @see org.eclipse.sensinact.sensorthings.models.extended.impl.ExtendedPackageImpl#getSensorThingDatastream()
		 * @generated
		 */
		EClass SENSOR_THING_DATASTREAM = eINSTANCE.getSensorThingDatastream();

		/**
		 * The meta object literal for the '<em><b>Datastream</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SENSOR_THING_DATASTREAM__DATASTREAM = eINSTANCE.getSensorThingDatastream_Datastream();

	}

} //ExtendedPackage
