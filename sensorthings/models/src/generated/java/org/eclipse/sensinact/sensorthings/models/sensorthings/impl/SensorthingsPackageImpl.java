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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.sensinact.model.core.provider.ProviderPackage;

import org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService;
import org.eclipse.sensinact.sensorthings.models.sensorthings.SensorThingsDevice;
import org.eclipse.sensinact.sensorthings.models.sensorthings.SensorThingsService;
import org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsFactory;
import org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class SensorthingsPackageImpl extends EPackageImpl implements SensorthingsPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sensorThingsDeviceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sensorThingsServiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dataStreamServiceEClass = null;

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
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private SensorthingsPackageImpl() {
		super(eNS_URI, SensorthingsFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link SensorthingsPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static SensorthingsPackage init() {
		if (isInited) return (SensorthingsPackage)EPackage.Registry.INSTANCE.getEPackage(SensorthingsPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredSensorthingsPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		SensorthingsPackageImpl theSensorthingsPackage = registeredSensorthingsPackage instanceof SensorthingsPackageImpl ? (SensorthingsPackageImpl)registeredSensorthingsPackage : new SensorthingsPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		ProviderPackage.eINSTANCE.eClass();
		EcorePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theSensorthingsPackage.createPackageContents();

		// Initialize created meta-data
		theSensorthingsPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theSensorthingsPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(SensorthingsPackage.eNS_URI, theSensorthingsPackage);
		return theSensorthingsPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSensorThingsDevice() {
		return sensorThingsDeviceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSensorThingsDevice_Thing() {
		return (EReference)sensorThingsDeviceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSensorThingsService() {
		return sensorThingsServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSensorThingsService_Id() {
		return (EAttribute)sensorThingsServiceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSensorThingsService_Description() {
		return (EAttribute)sensorThingsServiceEClass.getEStructuralFeatures().get(1);
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
	public EAttribute getDataStreamService_SensorThingsId() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_Name() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_Description() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_LatestObservation() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_Unit() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_Sensor() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataStreamService_ObservedProperty() {
		return (EAttribute)dataStreamServiceEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SensorthingsFactory getSensorthingsFactory() {
		return (SensorthingsFactory)getEFactoryInstance();
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
		sensorThingsDeviceEClass = createEClass(SENSOR_THINGS_DEVICE);
		createEReference(sensorThingsDeviceEClass, SENSOR_THINGS_DEVICE__THING);

		sensorThingsServiceEClass = createEClass(SENSOR_THINGS_SERVICE);
		createEAttribute(sensorThingsServiceEClass, SENSOR_THINGS_SERVICE__ID);
		createEAttribute(sensorThingsServiceEClass, SENSOR_THINGS_SERVICE__DESCRIPTION);

		dataStreamServiceEClass = createEClass(DATA_STREAM_SERVICE);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__SENSOR_THINGS_ID);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__NAME);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__DESCRIPTION);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__LATEST_OBSERVATION);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__UNIT);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__SENSOR);
		createEAttribute(dataStreamServiceEClass, DATA_STREAM_SERVICE__OBSERVED_PROPERTY);
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
		sensorThingsDeviceEClass.getESuperTypes().add(theProviderPackage.getDynamicProvider());
		sensorThingsServiceEClass.getESuperTypes().add(theProviderPackage.getService());
		dataStreamServiceEClass.getESuperTypes().add(theProviderPackage.getService());

		// Initialize classes, features, and operations; add parameters
		initEClass(sensorThingsDeviceEClass, SensorThingsDevice.class, "SensorThingsDevice", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSensorThingsDevice_Thing(), this.getSensorThingsService(), null, "thing", null, 0, 1, SensorThingsDevice.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(sensorThingsServiceEClass, SensorThingsService.class, "SensorThingsService", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSensorThingsService_Id(), ecorePackage.getEJavaObject(), "id", null, 0, 1, SensorThingsService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSensorThingsService_Description(), ecorePackage.getEString(), "description", null, 0, 1, SensorThingsService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(dataStreamServiceEClass, DataStreamService.class, "DataStreamService", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDataStreamService_SensorThingsId(), ecorePackage.getEJavaObject(), "sensorThingsId", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_Name(), ecorePackage.getEString(), "name", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_Description(), ecorePackage.getEString(), "description", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_LatestObservation(), ecorePackage.getEJavaObject(), "latestObservation", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_Unit(), ecorePackage.getEString(), "unit", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_Sensor(), ecorePackage.getEString(), "sensor", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataStreamService_ObservedProperty(), ecorePackage.getEString(), "observedProperty", null, 0, 1, DataStreamService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

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

} //SensorthingsPackageImpl
