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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.sensinact.model.core.provider.ProviderPackage;

import org.eclipse.sensinact.sensorthings.models.extended.DataStreamService;
import org.eclipse.sensinact.sensorthings.models.extended.ExtendedFactory;
import org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage;
import org.eclipse.sensinact.sensorthings.models.extended.SensorThingDatastream;
import org.eclipse.sensinact.sensorthings.models.extended.SensorThingDevice;
import org.eclipse.sensinact.sensorthings.models.extended.SensorThingLocation;
import org.eclipse.sensinact.sensorthings.models.extended.SensorThingLocationService;
import org.eclipse.sensinact.sensorthings.models.extended.SensorThingService;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ExtendedPackageImpl extends EPackageImpl implements ExtendedPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sensorThingDeviceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sensorThingLocationServiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dataStreamServiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sensorThingServiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sensorThingLocationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sensorThingDatastreamEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ExtendedPackageImpl() {
		super(eNS_URI, ExtendedFactory.eINSTANCE);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link ExtendedPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ExtendedPackage init() {
		if (isInited) return (ExtendedPackage)EPackage.Registry.INSTANCE.getEPackage(ExtendedPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredExtendedPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		ExtendedPackageImpl theExtendedPackage = registeredExtendedPackage instanceof ExtendedPackageImpl ? (ExtendedPackageImpl)registeredExtendedPackage : new ExtendedPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		ProviderPackage.eINSTANCE.eClass();
		EcorePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theExtendedPackage.createPackageContents();

		// Initialize created meta-data
		theExtendedPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theExtendedPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ExtendedPackage.eNS_URI, theExtendedPackage);
		return theExtendedPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSensorThingDevice() {
		return sensorThingDeviceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSensorThingDevice_Thing() {
		return (EReference)sensorThingDeviceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSensorThingLocationService() {
		return sensorThingLocationServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSensorThingLocationService_EncodingType() {
		return (EAttribute)sensorThingLocationServiceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSensorThingLocationService_Id() {
		return (EAttribute)sensorThingLocationServiceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDataStreamService() {
		return dataStreamServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_Id() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_Timestamp() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_SensorName() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_SensorDescription() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_SensorEncodingType() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_UnitName() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_UnitSymbol() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_UnitDefinition() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_ObservedPropertyName() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_ObservedPropertyId() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_ObservedPropertyDescription() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_ObservedPropertyDefinition() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_SensorMetadata() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_SensorId() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_SensorProperties() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_ObservedPropertyProperties() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_LastObservation() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(16);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_ObservationType() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(17);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_ThingId() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(18);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSensorThingService() {
		return sensorThingServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSensorThingService_Properties() {
		return (EAttribute)sensorThingServiceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSensorThingService_Id() {
		return (EAttribute)sensorThingServiceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSensorThingService_LocationIds() {
		return (EAttribute)sensorThingServiceEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSensorThingService_DatastreamIds() {
		return (EAttribute)sensorThingServiceEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSensorThingLocation() {
		return sensorThingLocationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSensorThingLocation_Location() {
		return (EReference)sensorThingLocationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSensorThingDatastream() {
		return sensorThingDatastreamEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSensorThingDatastream_Datastream() {
		return (EReference)sensorThingDatastreamEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ExtendedFactory getExtendedFactory() {
		return (ExtendedFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		sensorThingDeviceEClass = createEClass(SENSOR_THING_DEVICE);
		createEReference(sensorThingDeviceEClass, SENSOR_THING_DEVICE__THING);

		sensorThingLocationServiceEClass = createEClass(SENSOR_THING_LOCATION_SERVICE);
		createEAttribute(sensorThingLocationServiceEClass, SENSOR_THING_LOCATION_SERVICE__ENCODING_TYPE);
		createEAttribute(sensorThingLocationServiceEClass, SENSOR_THING_LOCATION_SERVICE__ID);

		dataStreamServiceEClass = createEClass(DATA_STREAM_SERVICE);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__ID);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__TIMESTAMP);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__SENSOR_NAME);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__SENSOR_DESCRIPTION);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__SENSOR_ENCODING_TYPE);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__UNIT_NAME);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__UNIT_SYMBOL);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__UNIT_DEFINITION);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__OBSERVED_PROPERTY_NAME);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__OBSERVED_PROPERTY_ID);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__OBSERVED_PROPERTY_DESCRIPTION);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__OBSERVED_PROPERTY_DEFINITION);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__SENSOR_METADATA);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__SENSOR_ID);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__SENSOR_PROPERTIES);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__OBSERVED_PROPERTY_PROPERTIES);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__LAST_OBSERVATION);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__OBSERVATION_TYPE);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__THING_ID);

		sensorThingServiceEClass = createEClass(SENSOR_THING_SERVICE);
		createEAttribute(sensorThingServiceEClass, SENSOR_THING_SERVICE__PROPERTIES);
		createEAttribute(sensorThingServiceEClass, SENSOR_THING_SERVICE__ID);
		createEAttribute(sensorThingServiceEClass, SENSOR_THING_SERVICE__LOCATION_IDS);
		createEAttribute(sensorThingServiceEClass, SENSOR_THING_SERVICE__DATASTREAM_IDS);

		sensorThingLocationEClass = createEClass(SENSOR_THING_LOCATION);
		createEReference(sensorThingLocationEClass, SENSOR_THING_LOCATION__LOCATION);

		sensorThingDatastreamEClass = createEClass(SENSOR_THING_DATASTREAM);
		createEReference(sensorThingDatastreamEClass, SENSOR_THING_DATASTREAM__DATASTREAM);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		ProviderPackage theProviderPackage = (ProviderPackage)EPackage.Registry.INSTANCE.getEPackage(ProviderPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		sensorThingDeviceEClass.getESuperTypes().add(theProviderPackage.getDynamicProvider());
		sensorThingLocationServiceEClass.getESuperTypes().add(theProviderPackage.getService());
		dataStreamServiceEClass.getESuperTypes().add(theProviderPackage.getService());
		sensorThingServiceEClass.getESuperTypes().add(theProviderPackage.getService());
		sensorThingLocationEClass.getESuperTypes().add(theProviderPackage.getDynamicProvider());
		sensorThingDatastreamEClass.getESuperTypes().add(theProviderPackage.getDynamicProvider());

		// Initialize classes, features, and operations; add parameters
		initEClass(sensorThingDeviceEClass, SensorThingDevice.class, "SensorThingDevice", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSensorThingDevice_Thing(), this.getSensorThingService(), null, "thing", null, 0, 1, SensorThingDevice.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(sensorThingLocationServiceEClass, SensorThingLocationService.class, "SensorThingLocationService", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSensorThingLocationService_EncodingType(), ecorePackage.getEString(), "encodingType", null, 0, 1, SensorThingLocationService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSensorThingLocationService_Id(), ecorePackage.getEString(), "id", null, 0, 1, SensorThingLocationService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(dataStreamServiceEClass, DataStreamService.class, "DataStreamService", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDataStreamService_Id(), ecorePackage.getEString(), "id", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_Timestamp(), theProviderPackage.getEInstant(), "timestamp", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_SensorName(), ecorePackage.getEString(), "sensorName", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_SensorDescription(), ecorePackage.getEString(), "sensorDescription", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_SensorEncodingType(), ecorePackage.getEString(), "sensorEncodingType", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_UnitName(), ecorePackage.getEString(), "unitName", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_UnitSymbol(), ecorePackage.getEString(), "unitSymbol", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_UnitDefinition(), ecorePackage.getEString(), "unitDefinition", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_ObservedPropertyName(), ecorePackage.getEString(), "observedPropertyName", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_ObservedPropertyId(), ecorePackage.getEString(), "observedPropertyId", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_ObservedPropertyDescription(), ecorePackage.getEString(), "observedPropertyDescription", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_ObservedPropertyDefinition(), ecorePackage.getEString(), "observedPropertyDefinition", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_SensorMetadata(), ecorePackage.getEJavaObject(), "sensorMetadata", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_SensorId(), ecorePackage.getEString(), "sensorId", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		EGenericType g1 = createEGenericType(ecorePackage.getEMap());
		EGenericType g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		initEAttribute(getDataStreamService_SensorProperties(), g1, "sensorProperties", null, 0, 1, DataStreamService.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		g1 = createEGenericType(ecorePackage.getEMap());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		initEAttribute(getDataStreamService_ObservedPropertyProperties(), g1, "observedPropertyProperties", null, 0, 1, DataStreamService.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_LastObservation(), ecorePackage.getEString(), "lastObservation", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_ObservationType(), ecorePackage.getEString(), "observationType", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_ThingId(), ecorePackage.getEString(), "thingId", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(sensorThingServiceEClass, SensorThingService.class, "SensorThingService", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSensorThingService_Properties(), ecorePackage.getEJavaObject(), "properties", null, 0, 1, SensorThingService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSensorThingService_Id(), ecorePackage.getEString(), "id", null, 0, 1, SensorThingService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSensorThingService_LocationIds(), ecorePackage.getEString(), "locationIds", null, 0, -1, SensorThingService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSensorThingService_DatastreamIds(), ecorePackage.getEString(), "datastreamIds", null, 0, -1, SensorThingService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(sensorThingLocationEClass, SensorThingLocation.class, "SensorThingLocation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSensorThingLocation_Location(), this.getSensorThingLocationService(), null, "location", null, 0, 1, SensorThingLocation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(sensorThingDatastreamEClass, SensorThingDatastream.class, "SensorThingDatastream", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSensorThingDatastream_Datastream(), this.getDataStreamService(), null, "datastream", null, 0, 1, SensorThingDatastream.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/OCL/Import
		createImportAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/OCL/Import</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createImportAnnotations() {
		String source = "http://www.eclipse.org/OCL/Import";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "ecore", "http://www.eclipse.org/emf/2002/Ecore"
		   });
	}

} //ExtendedPackageImpl
