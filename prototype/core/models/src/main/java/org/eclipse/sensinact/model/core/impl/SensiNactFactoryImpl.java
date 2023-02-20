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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.model.core.Action;
import org.eclipse.sensinact.model.core.ActionParameter;
import org.eclipse.sensinact.model.core.Admin;
import org.eclipse.sensinact.model.core.AnnotationMetadata;
import org.eclipse.sensinact.model.core.FeatureCustomMetadata;
import org.eclipse.sensinact.model.core.Provider;
import org.eclipse.sensinact.model.core.ResourceAttribute;
import org.eclipse.sensinact.model.core.ResourceMetadata;
import org.eclipse.sensinact.model.core.ResourceType;
import org.eclipse.sensinact.model.core.SensiNactFactory;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.model.core.Service;
import org.eclipse.sensinact.model.core.ServiceReference;
import org.eclipse.sensinact.model.core.ValueType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class SensiNactFactoryImpl extends EFactoryImpl implements SensiNactFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static SensiNactFactory init() {
		try {
			SensiNactFactory theSensiNactFactory = (SensiNactFactory)EPackage.Registry.INSTANCE.getEFactory(SensiNactPackage.eNS_URI);
			if (theSensiNactFactory != null) {
				return theSensiNactFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new SensiNactFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SensiNactFactoryImpl() {
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
			case SensiNactPackage.PROVIDER: return createProvider();
			case SensiNactPackage.ADMIN: return createAdmin();
			case SensiNactPackage.SERVICE: return createService();
			case SensiNactPackage.FEATURE_METADATA: return (EObject)createFeatureMetadata();
			case SensiNactPackage.ANNOTATION_METADATA: return createAnnotationMetadata();
			case SensiNactPackage.FEATURE_CUSTOM_METADATA: return createFeatureCustomMetadata();
			case SensiNactPackage.RESOURCE_ATTRIBUTE: return createResourceAttribute();
			case SensiNactPackage.SERVICE_REFERENCE: return createServiceReference();
			case SensiNactPackage.ACTION: return createAction();
			case SensiNactPackage.ACTION_PARAMETER: return createActionParameter();
			case SensiNactPackage.RESOURCE_METADATA: return createResourceMetadata();
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
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case SensiNactPackage.RESOURCE_TYPE:
				return createResourceTypeFromString(eDataType, initialValue);
			case SensiNactPackage.VALUE_TYPE:
				return createValueTypeFromString(eDataType, initialValue);
			case SensiNactPackage.EGEO_JSON_OBJECT:
				return createEGeoJsonObjectFromString(eDataType, initialValue);
			case SensiNactPackage.EINSTANT:
				return createEInstantFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case SensiNactPackage.RESOURCE_TYPE:
				return convertResourceTypeToString(eDataType, instanceValue);
			case SensiNactPackage.VALUE_TYPE:
				return convertValueTypeToString(eDataType, instanceValue);
			case SensiNactPackage.EGEO_JSON_OBJECT:
				return convertEGeoJsonObjectToString(eDataType, instanceValue);
			case SensiNactPackage.EINSTANT:
				return convertEInstantToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Provider createProvider() {
		ProviderImpl provider = new ProviderImpl();
		return provider;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Admin createAdmin() {
		AdminImpl admin = new AdminImpl();
		return admin;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Service createService() {
		ServiceImpl service = new ServiceImpl();
		return service;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Map.Entry<ETypedElement, ResourceMetadata> createFeatureMetadata() {
		FeatureMetadataImpl featureMetadata = new FeatureMetadataImpl();
		return featureMetadata;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AnnotationMetadata createAnnotationMetadata() {
		AnnotationMetadataImpl annotationMetadata = new AnnotationMetadataImpl();
		return annotationMetadata;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FeatureCustomMetadata createFeatureCustomMetadata() {
		FeatureCustomMetadataImpl featureCustomMetadata = new FeatureCustomMetadataImpl();
		return featureCustomMetadata;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceAttribute createResourceAttribute() {
		ResourceAttributeImpl resourceAttribute = new ResourceAttributeImpl();
		return resourceAttribute;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ServiceReference createServiceReference() {
		ServiceReferenceImpl serviceReference = new ServiceReferenceImpl();
		return serviceReference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Action createAction() {
		ActionImpl action = new ActionImpl();
		return action;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ActionParameter createActionParameter() {
		ActionParameterImpl actionParameter = new ActionParameterImpl();
		return actionParameter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceMetadata createResourceMetadata() {
		ResourceMetadataImpl resourceMetadata = new ResourceMetadataImpl();
		return resourceMetadata;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ResourceType createResourceTypeFromString(EDataType eDataType, String initialValue) {
		ResourceType result = ResourceType.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertResourceTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ValueType createValueTypeFromString(EDataType eDataType, String initialValue) {
		ValueType result = ValueType.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertValueTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GeoJsonObject createEGeoJsonObject(final String it) {
		try { return new com.fasterxml.jackson.databind.ObjectMapper().readValue(it, GeoJsonObject.class); } catch (com.fasterxml.jackson.core.JsonProcessingException e) { throw new IllegalArgumentException(e); }
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GeoJsonObject createEGeoJsonObjectFromString(EDataType eDataType, String initialValue) {
		return createEGeoJsonObject(initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertEGeoJsonObject(final GeoJsonObject it) {
		try { return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(it); } catch (com.fasterxml.jackson.core.JsonProcessingException e) { throw new IllegalArgumentException(e); }
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertEGeoJsonObjectToString(EDataType eDataType, Object instanceValue) {
		return convertEGeoJsonObject((GeoJsonObject)instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Instant createEInstant(final String it) {
		return Instant.parse(it);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Instant createEInstantFromString(EDataType eDataType, String initialValue) {
		return createEInstant(initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertEInstant(final Instant it) {
		return it.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertEInstantToString(EDataType eDataType, Object instanceValue) {
		return convertEInstant((Instant)instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SensiNactPackage getSensiNactPackage() {
		return (SensiNactPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static SensiNactPackage getPackage() {
		return SensiNactPackage.eINSTANCE;
	}

} //SensiNactFactoryImpl
