/**
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Data In Motion - initial API and implementation 
 */
package org.eclipse.sensinact.model.core.impl;

import java.time.Instant;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.model.core.Action;
import org.eclipse.sensinact.model.core.ActionParameter;
import org.eclipse.sensinact.model.core.Admin;
import org.eclipse.sensinact.model.core.AnnotationMetadata;
import org.eclipse.sensinact.model.core.FeatureCustomMetadata;
import org.eclipse.sensinact.model.core.Metadata;
import org.eclipse.sensinact.model.core.Provider;
import org.eclipse.sensinact.model.core.ResourceAttribute;
import org.eclipse.sensinact.model.core.ResourceMetadata;
import org.eclipse.sensinact.model.core.ResourceType;
import org.eclipse.sensinact.model.core.SensiNactFactory;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.model.core.Service;
import org.eclipse.sensinact.model.core.ServiceReference;
import org.eclipse.sensinact.model.core.Timestamped;
import org.eclipse.sensinact.model.core.ValueType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class SensiNactPackageImpl extends EPackageImpl implements SensiNactPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass providerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass adminEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass serviceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass metadataEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass featureMetadataEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass annotationMetadataEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass featureCustomMetadataEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass resourceAttributeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass serviceReferenceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass actionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass actionParameterEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass timestampedEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass resourceMetadataEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum resourceTypeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum valueTypeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType eGeoJsonObjectEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType eInstantEDataType = null;

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
	 * @see org.eclipse.sensinact.model.core.SensiNactPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private SensiNactPackageImpl() {
		super(eNS_URI, SensiNactFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link SensiNactPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static SensiNactPackage init() {
		if (isInited) return (SensiNactPackage)EPackage.Registry.INSTANCE.getEPackage(SensiNactPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredSensiNactPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		SensiNactPackageImpl theSensiNactPackage = registeredSensiNactPackage instanceof SensiNactPackageImpl ? (SensiNactPackageImpl)registeredSensiNactPackage : new SensiNactPackageImpl();

		isInited = true;

		// Create package meta-data objects
		theSensiNactPackage.createPackageContents();

		// Initialize created meta-data
		theSensiNactPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theSensiNactPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(SensiNactPackage.eNS_URI, theSensiNactPackage);
		return theSensiNactPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getProvider() {
		return providerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getProvider_Id() {
		return (EAttribute)providerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getProvider_Admin() {
		return (EReference)providerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getProvider_LinkedProviders() {
		return (EReference)providerEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAdmin() {
		return adminEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAdmin_FriendlyName() {
		return (EAttribute)adminEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAdmin_Location() {
		return (EAttribute)adminEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getService() {
		return serviceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getService_Metadata() {
		return (EReference)serviceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMetadata() {
		return metadataEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getMetadata_Extra() {
		return (EReference)metadataEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMetadata_Locked() {
		return (EAttribute)metadataEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMetadata_OriginalName() {
		return (EAttribute)metadataEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getFeatureMetadata() {
		return featureMetadataEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getFeatureMetadata_Key() {
		return (EReference)featureMetadataEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getFeatureMetadata_Value() {
		return (EReference)featureMetadataEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAnnotationMetadata() {
		return annotationMetadataEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getFeatureCustomMetadata() {
		return featureCustomMetadataEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFeatureCustomMetadata_Name() {
		return (EAttribute)featureCustomMetadataEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFeatureCustomMetadata_Value() {
		return (EAttribute)featureCustomMetadataEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFeatureCustomMetadata_Timestamp() {
		return (EAttribute)featureCustomMetadataEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getResourceAttribute() {
		return resourceAttributeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getResourceAttribute_ResourceType() {
		return (EAttribute)resourceAttributeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getResourceAttribute_ValueType() {
		return (EAttribute)resourceAttributeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getResourceAttribute_ExternalGet() {
		return (EAttribute)resourceAttributeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getResourceAttribute_ExternalSet() {
		return (EAttribute)resourceAttributeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getResourceAttribute_Stale() {
		return (EAttribute)resourceAttributeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getServiceReference() {
		return serviceReferenceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAction() {
		return actionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getActionParameter() {
		return actionParameterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTimestamped() {
		return timestampedEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTimestamped_Timestamp() {
		return (EAttribute)timestampedEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getResourceMetadata() {
		return resourceMetadataEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getResourceType() {
		return resourceTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getValueType() {
		return valueTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getEGeoJsonObject() {
		return eGeoJsonObjectEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getEInstant() {
		return eInstantEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SensiNactFactory getSensiNactFactory() {
		return (SensiNactFactory)getEFactoryInstance();
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
		providerEClass = createEClass(PROVIDER);
		createEAttribute(providerEClass, PROVIDER__ID);
		createEReference(providerEClass, PROVIDER__ADMIN);
		createEReference(providerEClass, PROVIDER__LINKED_PROVIDERS);

		adminEClass = createEClass(ADMIN);
		createEAttribute(adminEClass, ADMIN__FRIENDLY_NAME);
		createEAttribute(adminEClass, ADMIN__LOCATION);

		serviceEClass = createEClass(SERVICE);
		createEReference(serviceEClass, SERVICE__METADATA);

		metadataEClass = createEClass(METADATA);
		createEReference(metadataEClass, METADATA__EXTRA);
		createEAttribute(metadataEClass, METADATA__LOCKED);
		createEAttribute(metadataEClass, METADATA__ORIGINAL_NAME);

		featureMetadataEClass = createEClass(FEATURE_METADATA);
		createEReference(featureMetadataEClass, FEATURE_METADATA__KEY);
		createEReference(featureMetadataEClass, FEATURE_METADATA__VALUE);

		annotationMetadataEClass = createEClass(ANNOTATION_METADATA);

		featureCustomMetadataEClass = createEClass(FEATURE_CUSTOM_METADATA);
		createEAttribute(featureCustomMetadataEClass, FEATURE_CUSTOM_METADATA__NAME);
		createEAttribute(featureCustomMetadataEClass, FEATURE_CUSTOM_METADATA__VALUE);
		createEAttribute(featureCustomMetadataEClass, FEATURE_CUSTOM_METADATA__TIMESTAMP);

		resourceAttributeEClass = createEClass(RESOURCE_ATTRIBUTE);
		createEAttribute(resourceAttributeEClass, RESOURCE_ATTRIBUTE__RESOURCE_TYPE);
		createEAttribute(resourceAttributeEClass, RESOURCE_ATTRIBUTE__VALUE_TYPE);
		createEAttribute(resourceAttributeEClass, RESOURCE_ATTRIBUTE__EXTERNAL_GET);
		createEAttribute(resourceAttributeEClass, RESOURCE_ATTRIBUTE__EXTERNAL_SET);
		createEAttribute(resourceAttributeEClass, RESOURCE_ATTRIBUTE__STALE);

		serviceReferenceEClass = createEClass(SERVICE_REFERENCE);

		actionEClass = createEClass(ACTION);

		actionParameterEClass = createEClass(ACTION_PARAMETER);

		timestampedEClass = createEClass(TIMESTAMPED);
		createEAttribute(timestampedEClass, TIMESTAMPED__TIMESTAMP);

		resourceMetadataEClass = createEClass(RESOURCE_METADATA);

		// Create enums
		resourceTypeEEnum = createEEnum(RESOURCE_TYPE);
		valueTypeEEnum = createEEnum(VALUE_TYPE);

		// Create data types
		eGeoJsonObjectEDataType = createEDataType(EGEO_JSON_OBJECT);
		eInstantEDataType = createEDataType(EINSTANT);
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

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		adminEClass.getESuperTypes().add(this.getService());
		metadataEClass.getESuperTypes().add(this.getTimestamped());
		annotationMetadataEClass.getESuperTypes().add(this.getMetadata());
		resourceAttributeEClass.getESuperTypes().add(ecorePackage.getEAttribute());
		resourceAttributeEClass.getESuperTypes().add(this.getMetadata());
		serviceReferenceEClass.getESuperTypes().add(ecorePackage.getEReference());
		serviceReferenceEClass.getESuperTypes().add(this.getMetadata());
		actionEClass.getESuperTypes().add(ecorePackage.getEOperation());
		actionEClass.getESuperTypes().add(this.getMetadata());
		actionParameterEClass.getESuperTypes().add(ecorePackage.getEParameter());
		actionParameterEClass.getESuperTypes().add(this.getTimestamped());
		resourceMetadataEClass.getESuperTypes().add(this.getMetadata());

		// Initialize classes, features, and operations; add parameters
		initEClass(providerEClass, Provider.class, "Provider", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getProvider_Id(), ecorePackage.getEString(), "id", null, 1, 1, Provider.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getProvider_Admin(), this.getAdmin(), null, "admin", null, 0, 1, Provider.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getProvider_LinkedProviders(), this.getProvider(), null, "linkedProviders", null, 0, -1, Provider.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(adminEClass, Admin.class, "Admin", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAdmin_FriendlyName(), ecorePackage.getEString(), "friendlyName", null, 0, 1, Admin.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAdmin_Location(), this.getEGeoJsonObject(), "location", null, 0, 1, Admin.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(serviceEClass, Service.class, "Service", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getService_Metadata(), this.getFeatureMetadata(), null, "metadata", null, 0, -1, Service.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(metadataEClass, Metadata.class, "Metadata", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getMetadata_Extra(), this.getFeatureCustomMetadata(), null, "extra", null, 0, -1, Metadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMetadata_Locked(), ecorePackage.getEBoolean(), "locked", "false", 0, 1, Metadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMetadata_OriginalName(), ecorePackage.getEString(), "originalName", null, 1, 1, Metadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(featureMetadataEClass, Map.Entry.class, "FeatureMetadata", !IS_ABSTRACT, !IS_INTERFACE, !IS_GENERATED_INSTANCE_CLASS);
		initEReference(getFeatureMetadata_Key(), ecorePackage.getETypedElement(), null, "key", null, 0, 1, Map.Entry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getFeatureMetadata_Value(), this.getResourceMetadata(), null, "value", null, 0, 1, Map.Entry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(annotationMetadataEClass, AnnotationMetadata.class, "AnnotationMetadata", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(featureCustomMetadataEClass, FeatureCustomMetadata.class, "FeatureCustomMetadata", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getFeatureCustomMetadata_Name(), ecorePackage.getEString(), "name", null, 0, 1, FeatureCustomMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFeatureCustomMetadata_Value(), ecorePackage.getEJavaObject(), "value", null, 0, 1, FeatureCustomMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFeatureCustomMetadata_Timestamp(), this.getEInstant(), "timestamp", null, 0, 1, FeatureCustomMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(resourceAttributeEClass, ResourceAttribute.class, "ResourceAttribute", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getResourceAttribute_ResourceType(), this.getResourceType(), "resourceType", "SENSOR", 0, 1, ResourceAttribute.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getResourceAttribute_ValueType(), this.getValueType(), "valueType", "MODIFIABLE", 0, 1, ResourceAttribute.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getResourceAttribute_ExternalGet(), ecorePackage.getEBoolean(), "externalGet", null, 0, 1, ResourceAttribute.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getResourceAttribute_ExternalSet(), ecorePackage.getEBoolean(), "externalSet", null, 0, 1, ResourceAttribute.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getResourceAttribute_Stale(), ecorePackage.getEInt(), "stale", null, 0, 1, ResourceAttribute.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(serviceReferenceEClass, ServiceReference.class, "ServiceReference", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(actionEClass, Action.class, "Action", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(actionParameterEClass, ActionParameter.class, "ActionParameter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(timestampedEClass, Timestamped.class, "Timestamped", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTimestamped_Timestamp(), this.getEInstant(), "timestamp", null, 0, 1, Timestamped.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(resourceMetadataEClass, ResourceMetadata.class, "ResourceMetadata", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		// Initialize enums and add enum literals
		initEEnum(resourceTypeEEnum, ResourceType.class, "ResourceType");
		addEEnumLiteral(resourceTypeEEnum, ResourceType.ACTION);
		addEEnumLiteral(resourceTypeEEnum, ResourceType.PROPERTY);
		addEEnumLiteral(resourceTypeEEnum, ResourceType.SENSOR);
		addEEnumLiteral(resourceTypeEEnum, ResourceType.STATE_VARIABLE);

		initEEnum(valueTypeEEnum, ValueType.class, "ValueType");
		addEEnumLiteral(valueTypeEEnum, ValueType.FIXED);
		addEEnumLiteral(valueTypeEEnum, ValueType.OBSERVABLE);
		addEEnumLiteral(valueTypeEEnum, ValueType.MODIFIABLE);

		// Initialize data types
		initEDataType(eGeoJsonObjectEDataType, GeoJsonObject.class, "EGeoJsonObject", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(eInstantEDataType, Instant.class, "EInstant", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/OCL/Import
		createImportAnnotations();
		// http://www.eclipse.org/emf/2002/GenModel
		createGenModelAnnotations();
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

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/emf/2002/GenModel</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createGenModelAnnotations() {
		String source = "http://www.eclipse.org/emf/2002/GenModel";
		addAnnotation
		  (getResourceAttribute_Stale(),
		   source,
		   new String[] {
			   "documentation", "Indicator when an external get needs to be triggered and the internal data chache becomes stale. Negative values are never stale, 0 is always stale and postive values indicate the number milliseconds till the last get."
		   });
	}

} //SensiNactPackageImpl
