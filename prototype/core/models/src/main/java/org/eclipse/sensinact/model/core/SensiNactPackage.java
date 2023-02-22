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
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;

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
	 * The meta object id for the '{@link org.eclipse.sensinact.model.core.Timestamped <em>Timestamped</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.model.core.Timestamped
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getTimestamped()
	 * @generated
	 */
	int TIMESTAMPED = 11;

	/**
	 * The feature id for the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIMESTAMPED__TIMESTAMP = 0;

	/**
	 * The number of structural features of the '<em>Timestamped</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIMESTAMPED_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Timestamped</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIMESTAMPED_OPERATION_COUNT = 0;

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
	 * The feature id for the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METADATA__TIMESTAMP = TIMESTAMPED__TIMESTAMP;

	/**
	 * The feature id for the '<em><b>Extra</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METADATA__EXTRA = TIMESTAMPED_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Locked</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METADATA__LOCKED = TIMESTAMPED_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Original Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METADATA__ORIGINAL_NAME = TIMESTAMPED_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Metadata</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METADATA_FEATURE_COUNT = TIMESTAMPED_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Metadata</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METADATA_OPERATION_COUNT = TIMESTAMPED_OPERATION_COUNT + 0;

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
	 * The meta object id for the '{@link org.eclipse.sensinact.model.core.impl.AnnotationMetadataImpl <em>Annotation Metadata</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.model.core.impl.AnnotationMetadataImpl
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getAnnotationMetadata()
	 * @generated
	 */
	int ANNOTATION_METADATA = 5;

	/**
	 * The feature id for the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANNOTATION_METADATA__TIMESTAMP = METADATA__TIMESTAMP;

	/**
	 * The feature id for the '<em><b>Extra</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANNOTATION_METADATA__EXTRA = METADATA__EXTRA;

	/**
	 * The feature id for the '<em><b>Locked</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANNOTATION_METADATA__LOCKED = METADATA__LOCKED;

	/**
	 * The feature id for the '<em><b>Original Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANNOTATION_METADATA__ORIGINAL_NAME = METADATA__ORIGINAL_NAME;

	/**
	 * The number of structural features of the '<em>Annotation Metadata</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANNOTATION_METADATA_FEATURE_COUNT = METADATA_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>Annotation Metadata</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANNOTATION_METADATA_OPERATION_COUNT = METADATA_OPERATION_COUNT + 0;

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
	 * The meta object id for the '{@link org.eclipse.sensinact.model.core.impl.ResourceAttributeImpl <em>Resource Attribute</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.model.core.impl.ResourceAttributeImpl
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getResourceAttribute()
	 * @generated
	 */
	int RESOURCE_ATTRIBUTE = 7;

	/**
	 * The feature id for the '<em><b>EAnnotations</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__EANNOTATIONS = EcorePackage.EATTRIBUTE__EANNOTATIONS;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__NAME = EcorePackage.EATTRIBUTE__NAME;

	/**
	 * The feature id for the '<em><b>Ordered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__ORDERED = EcorePackage.EATTRIBUTE__ORDERED;

	/**
	 * The feature id for the '<em><b>Unique</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__UNIQUE = EcorePackage.EATTRIBUTE__UNIQUE;

	/**
	 * The feature id for the '<em><b>Lower Bound</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__LOWER_BOUND = EcorePackage.EATTRIBUTE__LOWER_BOUND;

	/**
	 * The feature id for the '<em><b>Upper Bound</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__UPPER_BOUND = EcorePackage.EATTRIBUTE__UPPER_BOUND;

	/**
	 * The feature id for the '<em><b>Many</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__MANY = EcorePackage.EATTRIBUTE__MANY;

	/**
	 * The feature id for the '<em><b>Required</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__REQUIRED = EcorePackage.EATTRIBUTE__REQUIRED;

	/**
	 * The feature id for the '<em><b>EType</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__ETYPE = EcorePackage.EATTRIBUTE__ETYPE;

	/**
	 * The feature id for the '<em><b>EGeneric Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__EGENERIC_TYPE = EcorePackage.EATTRIBUTE__EGENERIC_TYPE;

	/**
	 * The feature id for the '<em><b>Changeable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__CHANGEABLE = EcorePackage.EATTRIBUTE__CHANGEABLE;

	/**
	 * The feature id for the '<em><b>Volatile</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__VOLATILE = EcorePackage.EATTRIBUTE__VOLATILE;

	/**
	 * The feature id for the '<em><b>Transient</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__TRANSIENT = EcorePackage.EATTRIBUTE__TRANSIENT;

	/**
	 * The feature id for the '<em><b>Default Value Literal</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__DEFAULT_VALUE_LITERAL = EcorePackage.EATTRIBUTE__DEFAULT_VALUE_LITERAL;

	/**
	 * The feature id for the '<em><b>Default Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__DEFAULT_VALUE = EcorePackage.EATTRIBUTE__DEFAULT_VALUE;

	/**
	 * The feature id for the '<em><b>Unsettable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__UNSETTABLE = EcorePackage.EATTRIBUTE__UNSETTABLE;

	/**
	 * The feature id for the '<em><b>Derived</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__DERIVED = EcorePackage.EATTRIBUTE__DERIVED;

	/**
	 * The feature id for the '<em><b>EContaining Class</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__ECONTAINING_CLASS = EcorePackage.EATTRIBUTE__ECONTAINING_CLASS;

	/**
	 * The feature id for the '<em><b>ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__ID = EcorePackage.EATTRIBUTE__ID;

	/**
	 * The feature id for the '<em><b>EAttribute Type</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__EATTRIBUTE_TYPE = EcorePackage.EATTRIBUTE__EATTRIBUTE_TYPE;

	/**
	 * The feature id for the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__TIMESTAMP = EcorePackage.EATTRIBUTE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Extra</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__EXTRA = EcorePackage.EATTRIBUTE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Locked</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__LOCKED = EcorePackage.EATTRIBUTE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Original Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__ORIGINAL_NAME = EcorePackage.EATTRIBUTE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Resource Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__RESOURCE_TYPE = EcorePackage.EATTRIBUTE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Value Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__VALUE_TYPE = EcorePackage.EATTRIBUTE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>External Get</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__EXTERNAL_GET = EcorePackage.EATTRIBUTE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>External Set</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__EXTERNAL_SET = EcorePackage.EATTRIBUTE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Stale</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE__STALE = EcorePackage.EATTRIBUTE_FEATURE_COUNT + 8;

	/**
	 * The number of structural features of the '<em>Resource Attribute</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE_FEATURE_COUNT = EcorePackage.EATTRIBUTE_FEATURE_COUNT + 9;

	/**
	 * The operation id for the '<em>Get EAnnotation</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE___GET_EANNOTATION__STRING = EcorePackage.EATTRIBUTE___GET_EANNOTATION__STRING;

	/**
	 * The operation id for the '<em>Get Feature ID</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE___GET_FEATURE_ID = EcorePackage.EATTRIBUTE___GET_FEATURE_ID;

	/**
	 * The operation id for the '<em>Get Container Class</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE___GET_CONTAINER_CLASS = EcorePackage.EATTRIBUTE___GET_CONTAINER_CLASS;

	/**
	 * The number of operations of the '<em>Resource Attribute</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_ATTRIBUTE_OPERATION_COUNT = EcorePackage.EATTRIBUTE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.model.core.impl.ServiceReferenceImpl <em>Service Reference</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.model.core.impl.ServiceReferenceImpl
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getServiceReference()
	 * @generated
	 */
	int SERVICE_REFERENCE = 8;

	/**
	 * The feature id for the '<em><b>EAnnotations</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__EANNOTATIONS = EcorePackage.EREFERENCE__EANNOTATIONS;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__NAME = EcorePackage.EREFERENCE__NAME;

	/**
	 * The feature id for the '<em><b>Ordered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__ORDERED = EcorePackage.EREFERENCE__ORDERED;

	/**
	 * The feature id for the '<em><b>Unique</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__UNIQUE = EcorePackage.EREFERENCE__UNIQUE;

	/**
	 * The feature id for the '<em><b>Lower Bound</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__LOWER_BOUND = EcorePackage.EREFERENCE__LOWER_BOUND;

	/**
	 * The feature id for the '<em><b>Upper Bound</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__UPPER_BOUND = EcorePackage.EREFERENCE__UPPER_BOUND;

	/**
	 * The feature id for the '<em><b>Many</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__MANY = EcorePackage.EREFERENCE__MANY;

	/**
	 * The feature id for the '<em><b>Required</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__REQUIRED = EcorePackage.EREFERENCE__REQUIRED;

	/**
	 * The feature id for the '<em><b>EType</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__ETYPE = EcorePackage.EREFERENCE__ETYPE;

	/**
	 * The feature id for the '<em><b>EGeneric Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__EGENERIC_TYPE = EcorePackage.EREFERENCE__EGENERIC_TYPE;

	/**
	 * The feature id for the '<em><b>Changeable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__CHANGEABLE = EcorePackage.EREFERENCE__CHANGEABLE;

	/**
	 * The feature id for the '<em><b>Volatile</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__VOLATILE = EcorePackage.EREFERENCE__VOLATILE;

	/**
	 * The feature id for the '<em><b>Transient</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__TRANSIENT = EcorePackage.EREFERENCE__TRANSIENT;

	/**
	 * The feature id for the '<em><b>Default Value Literal</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__DEFAULT_VALUE_LITERAL = EcorePackage.EREFERENCE__DEFAULT_VALUE_LITERAL;

	/**
	 * The feature id for the '<em><b>Default Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__DEFAULT_VALUE = EcorePackage.EREFERENCE__DEFAULT_VALUE;

	/**
	 * The feature id for the '<em><b>Unsettable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__UNSETTABLE = EcorePackage.EREFERENCE__UNSETTABLE;

	/**
	 * The feature id for the '<em><b>Derived</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__DERIVED = EcorePackage.EREFERENCE__DERIVED;

	/**
	 * The feature id for the '<em><b>EContaining Class</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__ECONTAINING_CLASS = EcorePackage.EREFERENCE__ECONTAINING_CLASS;

	/**
	 * The feature id for the '<em><b>Containment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__CONTAINMENT = EcorePackage.EREFERENCE__CONTAINMENT;

	/**
	 * The feature id for the '<em><b>Container</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__CONTAINER = EcorePackage.EREFERENCE__CONTAINER;

	/**
	 * The feature id for the '<em><b>Resolve Proxies</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__RESOLVE_PROXIES = EcorePackage.EREFERENCE__RESOLVE_PROXIES;

	/**
	 * The feature id for the '<em><b>EOpposite</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__EOPPOSITE = EcorePackage.EREFERENCE__EOPPOSITE;

	/**
	 * The feature id for the '<em><b>EReference Type</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__EREFERENCE_TYPE = EcorePackage.EREFERENCE__EREFERENCE_TYPE;

	/**
	 * The feature id for the '<em><b>EKeys</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__EKEYS = EcorePackage.EREFERENCE__EKEYS;

	/**
	 * The feature id for the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__TIMESTAMP = EcorePackage.EREFERENCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Extra</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__EXTRA = EcorePackage.EREFERENCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Locked</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__LOCKED = EcorePackage.EREFERENCE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Original Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE__ORIGINAL_NAME = EcorePackage.EREFERENCE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Service Reference</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE_FEATURE_COUNT = EcorePackage.EREFERENCE_FEATURE_COUNT + 4;

	/**
	 * The operation id for the '<em>Get EAnnotation</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE___GET_EANNOTATION__STRING = EcorePackage.EREFERENCE___GET_EANNOTATION__STRING;

	/**
	 * The operation id for the '<em>Get Feature ID</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE___GET_FEATURE_ID = EcorePackage.EREFERENCE___GET_FEATURE_ID;

	/**
	 * The operation id for the '<em>Get Container Class</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE___GET_CONTAINER_CLASS = EcorePackage.EREFERENCE___GET_CONTAINER_CLASS;

	/**
	 * The number of operations of the '<em>Service Reference</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SERVICE_REFERENCE_OPERATION_COUNT = EcorePackage.EREFERENCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.model.core.impl.ActionImpl <em>Action</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.model.core.impl.ActionImpl
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getAction()
	 * @generated
	 */
	int ACTION = 9;

	/**
	 * The feature id for the '<em><b>EAnnotations</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__EANNOTATIONS = EcorePackage.EOPERATION__EANNOTATIONS;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__NAME = EcorePackage.EOPERATION__NAME;

	/**
	 * The feature id for the '<em><b>Ordered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__ORDERED = EcorePackage.EOPERATION__ORDERED;

	/**
	 * The feature id for the '<em><b>Unique</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__UNIQUE = EcorePackage.EOPERATION__UNIQUE;

	/**
	 * The feature id for the '<em><b>Lower Bound</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__LOWER_BOUND = EcorePackage.EOPERATION__LOWER_BOUND;

	/**
	 * The feature id for the '<em><b>Upper Bound</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__UPPER_BOUND = EcorePackage.EOPERATION__UPPER_BOUND;

	/**
	 * The feature id for the '<em><b>Many</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__MANY = EcorePackage.EOPERATION__MANY;

	/**
	 * The feature id for the '<em><b>Required</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__REQUIRED = EcorePackage.EOPERATION__REQUIRED;

	/**
	 * The feature id for the '<em><b>EType</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__ETYPE = EcorePackage.EOPERATION__ETYPE;

	/**
	 * The feature id for the '<em><b>EGeneric Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__EGENERIC_TYPE = EcorePackage.EOPERATION__EGENERIC_TYPE;

	/**
	 * The feature id for the '<em><b>EContaining Class</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__ECONTAINING_CLASS = EcorePackage.EOPERATION__ECONTAINING_CLASS;

	/**
	 * The feature id for the '<em><b>EType Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__ETYPE_PARAMETERS = EcorePackage.EOPERATION__ETYPE_PARAMETERS;

	/**
	 * The feature id for the '<em><b>EParameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__EPARAMETERS = EcorePackage.EOPERATION__EPARAMETERS;

	/**
	 * The feature id for the '<em><b>EExceptions</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__EEXCEPTIONS = EcorePackage.EOPERATION__EEXCEPTIONS;

	/**
	 * The feature id for the '<em><b>EGeneric Exceptions</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__EGENERIC_EXCEPTIONS = EcorePackage.EOPERATION__EGENERIC_EXCEPTIONS;

	/**
	 * The feature id for the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__TIMESTAMP = EcorePackage.EOPERATION_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Extra</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__EXTRA = EcorePackage.EOPERATION_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Locked</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__LOCKED = EcorePackage.EOPERATION_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Original Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION__ORIGINAL_NAME = EcorePackage.EOPERATION_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Action</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION_FEATURE_COUNT = EcorePackage.EOPERATION_FEATURE_COUNT + 4;

	/**
	 * The operation id for the '<em>Get EAnnotation</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION___GET_EANNOTATION__STRING = EcorePackage.EOPERATION___GET_EANNOTATION__STRING;

	/**
	 * The operation id for the '<em>Get Operation ID</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION___GET_OPERATION_ID = EcorePackage.EOPERATION___GET_OPERATION_ID;

	/**
	 * The operation id for the '<em>Is Override Of</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION___IS_OVERRIDE_OF__EOPERATION = EcorePackage.EOPERATION___IS_OVERRIDE_OF__EOPERATION;

	/**
	 * The number of operations of the '<em>Action</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION_OPERATION_COUNT = EcorePackage.EOPERATION_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.model.core.impl.ActionParameterImpl <em>Action Parameter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.model.core.impl.ActionParameterImpl
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getActionParameter()
	 * @generated
	 */
	int ACTION_PARAMETER = 10;

	/**
	 * The feature id for the '<em><b>EAnnotations</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION_PARAMETER__EANNOTATIONS = EcorePackage.EPARAMETER__EANNOTATIONS;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION_PARAMETER__NAME = EcorePackage.EPARAMETER__NAME;

	/**
	 * The feature id for the '<em><b>Ordered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION_PARAMETER__ORDERED = EcorePackage.EPARAMETER__ORDERED;

	/**
	 * The feature id for the '<em><b>Unique</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION_PARAMETER__UNIQUE = EcorePackage.EPARAMETER__UNIQUE;

	/**
	 * The feature id for the '<em><b>Lower Bound</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION_PARAMETER__LOWER_BOUND = EcorePackage.EPARAMETER__LOWER_BOUND;

	/**
	 * The feature id for the '<em><b>Upper Bound</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION_PARAMETER__UPPER_BOUND = EcorePackage.EPARAMETER__UPPER_BOUND;

	/**
	 * The feature id for the '<em><b>Many</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION_PARAMETER__MANY = EcorePackage.EPARAMETER__MANY;

	/**
	 * The feature id for the '<em><b>Required</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION_PARAMETER__REQUIRED = EcorePackage.EPARAMETER__REQUIRED;

	/**
	 * The feature id for the '<em><b>EType</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION_PARAMETER__ETYPE = EcorePackage.EPARAMETER__ETYPE;

	/**
	 * The feature id for the '<em><b>EGeneric Type</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION_PARAMETER__EGENERIC_TYPE = EcorePackage.EPARAMETER__EGENERIC_TYPE;

	/**
	 * The feature id for the '<em><b>EOperation</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION_PARAMETER__EOPERATION = EcorePackage.EPARAMETER__EOPERATION;

	/**
	 * The feature id for the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION_PARAMETER__TIMESTAMP = EcorePackage.EPARAMETER_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Action Parameter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION_PARAMETER_FEATURE_COUNT = EcorePackage.EPARAMETER_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get EAnnotation</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION_PARAMETER___GET_EANNOTATION__STRING = EcorePackage.EPARAMETER___GET_EANNOTATION__STRING;

	/**
	 * The number of operations of the '<em>Action Parameter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ACTION_PARAMETER_OPERATION_COUNT = EcorePackage.EPARAMETER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.model.core.impl.ResourceMetadataImpl <em>Resource Metadata</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.model.core.impl.ResourceMetadataImpl
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getResourceMetadata()
	 * @generated
	 */
	int RESOURCE_METADATA = 12;

	/**
	 * The feature id for the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METADATA__TIMESTAMP = METADATA__TIMESTAMP;

	/**
	 * The feature id for the '<em><b>Extra</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METADATA__EXTRA = METADATA__EXTRA;

	/**
	 * The feature id for the '<em><b>Locked</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METADATA__LOCKED = METADATA__LOCKED;

	/**
	 * The feature id for the '<em><b>Original Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METADATA__ORIGINAL_NAME = METADATA__ORIGINAL_NAME;

	/**
	 * The number of structural features of the '<em>Resource Metadata</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METADATA_FEATURE_COUNT = METADATA_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>Resource Metadata</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METADATA_OPERATION_COUNT = METADATA_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.model.core.ResourceType <em>Resource Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.model.core.ResourceType
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getResourceType()
	 * @generated
	 */
	int RESOURCE_TYPE = 13;

	/**
	 * The meta object id for the '{@link org.eclipse.sensinact.model.core.ValueType <em>Value Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.model.core.ValueType
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getValueType()
	 * @generated
	 */
	int VALUE_TYPE = 14;

	/**
	 * The meta object id for the '<em>EGeo Json Object</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.sensinact.gateway.geojson.GeoJsonObject
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getEGeoJsonObject()
	 * @generated
	 */
	int EGEO_JSON_OBJECT = 15;

	/**
	 * The meta object id for the '<em>EInstant</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.time.Instant
	 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getEInstant()
	 * @generated
	 */
	int EINSTANT = 16;


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
	 * Returns the meta object for the containment reference list '{@link org.eclipse.sensinact.model.core.Metadata#getExtra <em>Extra</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Extra</em>'.
	 * @see org.eclipse.sensinact.model.core.Metadata#getExtra()
	 * @see #getMetadata()
	 * @generated
	 */
	EReference getMetadata_Extra();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.model.core.Metadata#isLocked <em>Locked</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Locked</em>'.
	 * @see org.eclipse.sensinact.model.core.Metadata#isLocked()
	 * @see #getMetadata()
	 * @generated
	 */
	EAttribute getMetadata_Locked();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.model.core.Metadata#getOriginalName <em>Original Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Original Name</em>'.
	 * @see org.eclipse.sensinact.model.core.Metadata#getOriginalName()
	 * @see #getMetadata()
	 * @generated
	 */
	EAttribute getMetadata_OriginalName();

	/**
	 * Returns the meta object for class '{@link java.util.Map.Entry <em>Feature Metadata</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Feature Metadata</em>'.
	 * @see java.util.Map.Entry
	 * @model keyType="org.eclipse.emf.ecore.ETypedElement"
	 *        valueType="org.eclipse.sensinact.model.core.ResourceMetadata" valueContainment="true"
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
	 * Returns the meta object for class '{@link org.eclipse.sensinact.model.core.AnnotationMetadata <em>Annotation Metadata</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Annotation Metadata</em>'.
	 * @see org.eclipse.sensinact.model.core.AnnotationMetadata
	 * @generated
	 */
	EClass getAnnotationMetadata();

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
	 * Returns the meta object for class '{@link org.eclipse.sensinact.model.core.ResourceAttribute <em>Resource Attribute</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Resource Attribute</em>'.
	 * @see org.eclipse.sensinact.model.core.ResourceAttribute
	 * @generated
	 */
	EClass getResourceAttribute();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.model.core.ResourceAttribute#getResourceType <em>Resource Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Resource Type</em>'.
	 * @see org.eclipse.sensinact.model.core.ResourceAttribute#getResourceType()
	 * @see #getResourceAttribute()
	 * @generated
	 */
	EAttribute getResourceAttribute_ResourceType();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.model.core.ResourceAttribute#getValueType <em>Value Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value Type</em>'.
	 * @see org.eclipse.sensinact.model.core.ResourceAttribute#getValueType()
	 * @see #getResourceAttribute()
	 * @generated
	 */
	EAttribute getResourceAttribute_ValueType();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.model.core.ResourceAttribute#isExternalGet <em>External Get</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>External Get</em>'.
	 * @see org.eclipse.sensinact.model.core.ResourceAttribute#isExternalGet()
	 * @see #getResourceAttribute()
	 * @generated
	 */
	EAttribute getResourceAttribute_ExternalGet();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.model.core.ResourceAttribute#isExternalSet <em>External Set</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>External Set</em>'.
	 * @see org.eclipse.sensinact.model.core.ResourceAttribute#isExternalSet()
	 * @see #getResourceAttribute()
	 * @generated
	 */
	EAttribute getResourceAttribute_ExternalSet();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.model.core.ResourceAttribute#getStale <em>Stale</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Stale</em>'.
	 * @see org.eclipse.sensinact.model.core.ResourceAttribute#getStale()
	 * @see #getResourceAttribute()
	 * @generated
	 */
	EAttribute getResourceAttribute_Stale();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.model.core.ServiceReference <em>Service Reference</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Service Reference</em>'.
	 * @see org.eclipse.sensinact.model.core.ServiceReference
	 * @generated
	 */
	EClass getServiceReference();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.model.core.Action <em>Action</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Action</em>'.
	 * @see org.eclipse.sensinact.model.core.Action
	 * @generated
	 */
	EClass getAction();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.model.core.ActionParameter <em>Action Parameter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Action Parameter</em>'.
	 * @see org.eclipse.sensinact.model.core.ActionParameter
	 * @generated
	 */
	EClass getActionParameter();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.model.core.Timestamped <em>Timestamped</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Timestamped</em>'.
	 * @see org.eclipse.sensinact.model.core.Timestamped
	 * @generated
	 */
	EClass getTimestamped();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.sensinact.model.core.Timestamped#getTimestamp <em>Timestamp</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Timestamp</em>'.
	 * @see org.eclipse.sensinact.model.core.Timestamped#getTimestamp()
	 * @see #getTimestamped()
	 * @generated
	 */
	EAttribute getTimestamped_Timestamp();

	/**
	 * Returns the meta object for class '{@link org.eclipse.sensinact.model.core.ResourceMetadata <em>Resource Metadata</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Resource Metadata</em>'.
	 * @see org.eclipse.sensinact.model.core.ResourceMetadata
	 * @generated
	 */
	EClass getResourceMetadata();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.sensinact.model.core.ResourceType <em>Resource Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Resource Type</em>'.
	 * @see org.eclipse.sensinact.model.core.ResourceType
	 * @generated
	 */
	EEnum getResourceType();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.sensinact.model.core.ValueType <em>Value Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Value Type</em>'.
	 * @see org.eclipse.sensinact.model.core.ValueType
	 * @generated
	 */
	EEnum getValueType();

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
		 * The meta object literal for the '{@link org.eclipse.sensinact.model.core.impl.ProviderImpl <em>Provider</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.model.core.impl.ProviderImpl
		 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getProvider()
		 * @generated
		 */
		EClass PROVIDER = eINSTANCE.getProvider();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROVIDER__ID = eINSTANCE.getProvider_Id();

		/**
		 * The meta object literal for the '<em><b>Admin</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PROVIDER__ADMIN = eINSTANCE.getProvider_Admin();

		/**
		 * The meta object literal for the '<em><b>Linked Providers</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PROVIDER__LINKED_PROVIDERS = eINSTANCE.getProvider_LinkedProviders();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.model.core.impl.AdminImpl <em>Admin</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.model.core.impl.AdminImpl
		 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getAdmin()
		 * @generated
		 */
		EClass ADMIN = eINSTANCE.getAdmin();

		/**
		 * The meta object literal for the '<em><b>Friendly Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ADMIN__FRIENDLY_NAME = eINSTANCE.getAdmin_FriendlyName();

		/**
		 * The meta object literal for the '<em><b>Location</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ADMIN__LOCATION = eINSTANCE.getAdmin_Location();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.model.core.impl.ServiceImpl <em>Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.model.core.impl.ServiceImpl
		 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getService()
		 * @generated
		 */
		EClass SERVICE = eINSTANCE.getService();

		/**
		 * The meta object literal for the '<em><b>Metadata</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SERVICE__METADATA = eINSTANCE.getService_Metadata();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.model.core.impl.MetadataImpl <em>Metadata</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.model.core.impl.MetadataImpl
		 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getMetadata()
		 * @generated
		 */
		EClass METADATA = eINSTANCE.getMetadata();

		/**
		 * The meta object literal for the '<em><b>Extra</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference METADATA__EXTRA = eINSTANCE.getMetadata_Extra();

		/**
		 * The meta object literal for the '<em><b>Locked</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute METADATA__LOCKED = eINSTANCE.getMetadata_Locked();

		/**
		 * The meta object literal for the '<em><b>Original Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute METADATA__ORIGINAL_NAME = eINSTANCE.getMetadata_OriginalName();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.model.core.impl.FeatureMetadataImpl <em>Feature Metadata</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.model.core.impl.FeatureMetadataImpl
		 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getFeatureMetadata()
		 * @generated
		 */
		EClass FEATURE_METADATA = eINSTANCE.getFeatureMetadata();

		/**
		 * The meta object literal for the '<em><b>Key</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FEATURE_METADATA__KEY = eINSTANCE.getFeatureMetadata_Key();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FEATURE_METADATA__VALUE = eINSTANCE.getFeatureMetadata_Value();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.model.core.impl.AnnotationMetadataImpl <em>Annotation Metadata</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.model.core.impl.AnnotationMetadataImpl
		 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getAnnotationMetadata()
		 * @generated
		 */
		EClass ANNOTATION_METADATA = eINSTANCE.getAnnotationMetadata();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.model.core.impl.FeatureCustomMetadataImpl <em>Feature Custom Metadata</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.model.core.impl.FeatureCustomMetadataImpl
		 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getFeatureCustomMetadata()
		 * @generated
		 */
		EClass FEATURE_CUSTOM_METADATA = eINSTANCE.getFeatureCustomMetadata();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FEATURE_CUSTOM_METADATA__NAME = eINSTANCE.getFeatureCustomMetadata_Name();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FEATURE_CUSTOM_METADATA__VALUE = eINSTANCE.getFeatureCustomMetadata_Value();

		/**
		 * The meta object literal for the '<em><b>Timestamp</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FEATURE_CUSTOM_METADATA__TIMESTAMP = eINSTANCE.getFeatureCustomMetadata_Timestamp();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.model.core.impl.ResourceAttributeImpl <em>Resource Attribute</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.model.core.impl.ResourceAttributeImpl
		 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getResourceAttribute()
		 * @generated
		 */
		EClass RESOURCE_ATTRIBUTE = eINSTANCE.getResourceAttribute();

		/**
		 * The meta object literal for the '<em><b>Resource Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RESOURCE_ATTRIBUTE__RESOURCE_TYPE = eINSTANCE.getResourceAttribute_ResourceType();

		/**
		 * The meta object literal for the '<em><b>Value Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RESOURCE_ATTRIBUTE__VALUE_TYPE = eINSTANCE.getResourceAttribute_ValueType();

		/**
		 * The meta object literal for the '<em><b>External Get</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RESOURCE_ATTRIBUTE__EXTERNAL_GET = eINSTANCE.getResourceAttribute_ExternalGet();

		/**
		 * The meta object literal for the '<em><b>External Set</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RESOURCE_ATTRIBUTE__EXTERNAL_SET = eINSTANCE.getResourceAttribute_ExternalSet();

		/**
		 * The meta object literal for the '<em><b>Stale</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RESOURCE_ATTRIBUTE__STALE = eINSTANCE.getResourceAttribute_Stale();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.model.core.impl.ServiceReferenceImpl <em>Service Reference</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.model.core.impl.ServiceReferenceImpl
		 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getServiceReference()
		 * @generated
		 */
		EClass SERVICE_REFERENCE = eINSTANCE.getServiceReference();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.model.core.impl.ActionImpl <em>Action</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.model.core.impl.ActionImpl
		 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getAction()
		 * @generated
		 */
		EClass ACTION = eINSTANCE.getAction();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.model.core.impl.ActionParameterImpl <em>Action Parameter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.model.core.impl.ActionParameterImpl
		 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getActionParameter()
		 * @generated
		 */
		EClass ACTION_PARAMETER = eINSTANCE.getActionParameter();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.model.core.Timestamped <em>Timestamped</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.model.core.Timestamped
		 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getTimestamped()
		 * @generated
		 */
		EClass TIMESTAMPED = eINSTANCE.getTimestamped();

		/**
		 * The meta object literal for the '<em><b>Timestamp</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TIMESTAMPED__TIMESTAMP = eINSTANCE.getTimestamped_Timestamp();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.model.core.impl.ResourceMetadataImpl <em>Resource Metadata</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.model.core.impl.ResourceMetadataImpl
		 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getResourceMetadata()
		 * @generated
		 */
		EClass RESOURCE_METADATA = eINSTANCE.getResourceMetadata();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.model.core.ResourceType <em>Resource Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.model.core.ResourceType
		 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getResourceType()
		 * @generated
		 */
		EEnum RESOURCE_TYPE = eINSTANCE.getResourceType();

		/**
		 * The meta object literal for the '{@link org.eclipse.sensinact.model.core.ValueType <em>Value Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.model.core.ValueType
		 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getValueType()
		 * @generated
		 */
		EEnum VALUE_TYPE = eINSTANCE.getValueType();

		/**
		 * The meta object literal for the '<em>EGeo Json Object</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.sensinact.gateway.geojson.GeoJsonObject
		 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getEGeoJsonObject()
		 * @generated
		 */
		EDataType EGEO_JSON_OBJECT = eINSTANCE.getEGeoJsonObject();

		/**
		 * The meta object literal for the '<em>EInstant</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.time.Instant
		 * @see org.eclipse.sensinact.model.core.impl.SensiNactPackageImpl#getEInstant()
		 * @generated
		 */
		EDataType EINSTANT = eINSTANCE.getEInstant();

	}

} //SensiNactPackage
