/*
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
package org.eclipse.sensinact.model.core;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

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
 * @see org.eclipse.sensinact.model.core.SensiNactFactory
 * @model kind="package"
 *        annotation="http://www.eclipse.org/OCL/Import ecore='http://www.eclipse.org/emf/2002/Ecore'"
 * @generated
 */
@ProviderType
public interface SensiNactPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "core";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://eclipse.org/sensinact/core/1.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "sensinactCore";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	SensiNactPackage eINSTANCE = org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.model.core.impl.ProviderImpl <em>Provider</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.model.core.impl.ProviderImpl
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getProvider()
	 * @generated
	 */
	int PROVIDER = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROVIDER__ID = 0;

	/**
	 * The feature id for the '<em><b>Admin</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROVIDER__ADMIN = 1;

	/**
	 * The feature id for the '<em><b>Linked Providers</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROVIDER__LINKED_PROVIDERS = 2;

	/**
	 * The number of structural features of the '<em>Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROVIDER_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROVIDER_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.model.core.impl.ServiceImpl <em>Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.model.core.impl.ServiceImpl
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getService()
	 * @generated
	 */
	int SERVICE = 2;

	/**
	 * The feature id for the '<em><b>Metadata</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE__METADATA = 0;

	/**
	 * The number of structural features of the '<em>Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.model.core.impl.AdminImpl <em>Admin</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.model.core.impl.AdminImpl
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getAdmin()
	 * @generated
	 */
	int ADMIN = 1;

	/**
	 * The feature id for the '<em><b>Metadata</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ADMIN__METADATA = SERVICE__METADATA;

	/**
	 * The feature id for the '<em><b>Friendly Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ADMIN__FRIENDLY_NAME = SERVICE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Location</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ADMIN__LOCATION = SERVICE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Admin</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ADMIN_FEATURE_COUNT = SERVICE_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>Admin</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ADMIN_OPERATION_COUNT = SERVICE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.model.core.impl.MetadataImpl <em>Metadata</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.model.core.impl.MetadataImpl
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getMetadata()
	 * @generated
	 */
	int METADATA = 3;

	/**
	 * The feature id for the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METADATA__FEATURE = 0;

	/**
	 * The feature id for the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METADATA__TIMESTAMP = 1;

	/**
	 * The feature id for the '<em><b>Source</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METADATA__SOURCE = 2;

	/**
	 * The feature id for the '<em><b>Extra</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METADATA__EXTRA = 3;

	/**
	 * The number of structural features of the '<em>Metadata</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METADATA_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>Metadata</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METADATA_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.model.core.impl.FeatureMetadataImpl <em>Feature Metadata</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.model.core.impl.FeatureMetadataImpl
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getFeatureMetadata()
	 * @generated
	 */
	int FEATURE_METADATA = 4;

	/**
	 * The feature id for the '<em><b>Key</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_METADATA__KEY = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_METADATA__VALUE = 1;

	/**
	 * The number of structural features of the '<em>Feature Metadata</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_METADATA_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Feature Metadata</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_METADATA_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.model.core.impl.ModelMetadataImpl <em>Model Metadata</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.model.core.impl.ModelMetadataImpl
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getModelMetadata()
	 * @generated
	 */
	int MODEL_METADATA = 5;

	/**
	 * The feature id for the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_METADATA__FEATURE = METADATA__FEATURE;

	/**
	 * The feature id for the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_METADATA__TIMESTAMP = METADATA__TIMESTAMP;

	/**
	 * The feature id for the '<em><b>Source</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_METADATA__SOURCE = METADATA__SOURCE;

	/**
	 * The feature id for the '<em><b>Extra</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_METADATA__EXTRA = METADATA__EXTRA;

	/**
	 * The feature id for the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_METADATA__VERSION = METADATA_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Model Metadata</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_METADATA_FEATURE_COUNT = METADATA_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Model Metadata</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_METADATA_OPERATION_COUNT = METADATA_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.model.core.impl.FeatureCustomMetadataImpl <em>Feature Custom Metadata</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.model.core.impl.FeatureCustomMetadataImpl
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getFeatureCustomMetadata()
	 * @generated
	 */
	int FEATURE_CUSTOM_METADATA = 6;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_CUSTOM_METADATA__NAME = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_CUSTOM_METADATA__VALUE = 1;

	/**
	 * The feature id for the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_CUSTOM_METADATA__TIMESTAMP = 2;

	/**
	 * The number of structural features of the '<em>Feature Custom Metadata</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_CUSTOM_METADATA_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Feature Custom Metadata</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_CUSTOM_METADATA_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '<em>EGeo Json Object</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.gateway.geojson.GeoJsonObject
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getEGeoJsonObject()
	 * @generated
	 */
	int EGEO_JSON_OBJECT = 7;

	/**
	 * The meta object id for the '<em>EInstant</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.time.Instant
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getEInstant()
	 * @generated
	 */
	int EINSTANT = 8;


	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.model.core.Provider <em>Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Provider</em>'.
	 * @see org.eclipse.sensinact.model.core.Provider
	 * @generated
	 */
	EClass getProvider();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.model.core.Provider#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see org.eclipse.sensinact.model.core.Provider#getId()
	 * @see #getProvider()
	 * @generated
	 */
	EAttribute getProvider_Id();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.sensinact.model.core.Provider#getAdmin <em>Admin</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Admin</em>'.
	 * @see org.eclipse.sensinact.model.core.Provider#getAdmin()
	 * @see #getProvider()
	 * @generated
	 */
	EReference getProvider_Admin();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.sensinact.model.core.Provider#getLinkedProviders <em>Linked Providers</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Linked Providers</em>'.
	 * @see org.eclipse.sensinact.model.core.Provider#getLinkedProviders()
	 * @see #getProvider()
	 * @generated
	 */
	EReference getProvider_LinkedProviders();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.model.core.Admin <em>Admin</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Admin</em>'.
	 * @see org.eclipse.sensinact.model.core.Admin
	 * @generated
	 */
	EClass getAdmin();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.model.core.Admin#getFriendlyName <em>Friendly Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Friendly Name</em>'.
	 * @see org.eclipse.sensinact.model.core.Admin#getFriendlyName()
	 * @see #getAdmin()
	 * @generated
	 */
	EAttribute getAdmin_FriendlyName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.model.core.Admin#getLocation <em>Location</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Location</em>'.
	 * @see org.eclipse.sensinact.model.core.Admin#getLocation()
	 * @see #getAdmin()
	 * @generated
	 */
	EAttribute getAdmin_Location();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.model.core.Service <em>Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Service</em>'.
	 * @see org.eclipse.sensinact.model.core.Service
	 * @generated
	 */
	EClass getService();

	/**
	 * Returns the meta object for the map '{@link org.eclipse.sensinact.model.core.Service#getMetadata <em>Metadata</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>Metadata</em>'.
	 * @see org.eclipse.sensinact.model.core.Service#getMetadata()
	 * @see #getService()
	 * @generated
	 */
	EReference getService_Metadata();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.model.core.Metadata <em>Metadata</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Metadata</em>'.
	 * @see org.eclipse.sensinact.model.core.Metadata
	 * @generated
	 */
	EClass getMetadata();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.sensinact.model.core.Metadata#getFeature <em>Feature</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Feature</em>'.
	 * @see org.eclipse.sensinact.model.core.Metadata#getFeature()
	 * @see #getMetadata()
	 * @generated
	 */
	EReference getMetadata_Feature();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.model.core.Metadata#getTimestamp <em>Timestamp</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Timestamp</em>'.
	 * @see org.eclipse.sensinact.model.core.Metadata#getTimestamp()
	 * @see #getMetadata()
	 * @generated
	 */
	EAttribute getMetadata_Timestamp();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.sensinact.model.core.Metadata#getSource <em>Source</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Source</em>'.
	 * @see org.eclipse.sensinact.model.core.Metadata#getSource()
	 * @see #getMetadata()
	 * @generated
	 */
	EReference getMetadata_Source();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.sensinact.model.core.Metadata#getExtra <em>Extra</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Extra</em>'.
	 * @see org.eclipse.sensinact.model.core.Metadata#getExtra()
	 * @see #getMetadata()
	 * @generated
	 */
	EReference getMetadata_Extra();

	/**
	 * Returns the meta object for class '{@link java.util.Map.Entry <em>Feature Metadata</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Feature Metadata</em>'.
	 * @see java.util.Map.Entry
	 * @model keyType="org.eclipse.emf.ecore.EStructuralFeature"
	 *        valueType="org.eclipse.sensinact.model.core.Metadata" valueContainment="true"
	 * @generated
	 */
	EClass getFeatureMetadata();

	/**
	 * Returns the meta object for the reference '{@link java.util.Map.Entry <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Key</em>'.
	 * @see java.util.Map.Entry
	 * @see #getFeatureMetadata()
	 * @generated
	 */
	EReference getFeatureMetadata_Key();

	/**
	 * Returns the meta object for the containment reference '{@link java.util.Map.Entry <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Value</em>'.
	 * @see java.util.Map.Entry
	 * @see #getFeatureMetadata()
	 * @generated
	 */
	EReference getFeatureMetadata_Value();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.model.core.ModelMetadata <em>Model Metadata</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Model Metadata</em>'.
	 * @see org.eclipse.sensinact.model.core.ModelMetadata
	 * @generated
	 */
	EClass getModelMetadata();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.model.core.ModelMetadata#getVersion <em>Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Version</em>'.
	 * @see org.eclipse.sensinact.model.core.ModelMetadata#getVersion()
	 * @see #getModelMetadata()
	 * @generated
	 */
	EAttribute getModelMetadata_Version();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.model.core.FeatureCustomMetadata <em>Feature Custom Metadata</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Feature Custom Metadata</em>'.
	 * @see org.eclipse.sensinact.model.core.FeatureCustomMetadata
	 * @generated
	 */
	EClass getFeatureCustomMetadata();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.model.core.FeatureCustomMetadata#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.sensinact.model.core.FeatureCustomMetadata#getName()
	 * @see #getFeatureCustomMetadata()
	 * @generated
	 */
	EAttribute getFeatureCustomMetadata_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.model.core.FeatureCustomMetadata#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see org.eclipse.sensinact.model.core.FeatureCustomMetadata#getValue()
	 * @see #getFeatureCustomMetadata()
	 * @generated
	 */
	EAttribute getFeatureCustomMetadata_Value();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.model.core.FeatureCustomMetadata#getTimestamp <em>Timestamp</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Timestamp</em>'.
	 * @see org.eclipse.sensinact.model.core.FeatureCustomMetadata#getTimestamp()
	 * @see #getFeatureCustomMetadata()
	 * @generated
	 */
	EAttribute getFeatureCustomMetadata_Timestamp();

	/**
	 * Returns the meta object for data type '{@link org.eclipse.sensinact.gateway.geojson.GeoJsonObject <em>EGeo Json Object</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>EGeo Json Object</em>'.
	 * @see org.eclipse.sensinact.gateway.geojson.GeoJsonObject
	 * @model instanceClass="org.eclipse.sensinact.gateway.geojson.GeoJsonObject"
	 * @generated
	 */
	EDataType getEGeoJsonObject();

	/**
	 * Returns the meta object for data type '{@link java.time.Instant <em>EInstant</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>EInstant</em>'.
	 * @see java.time.Instant
	 * @model instanceClass="java.time.Instant"
	 * @generated
	 */
	EDataType getEInstant();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	SensiNactFactory getSensiNactFactory();

} //SensiNactPackage
