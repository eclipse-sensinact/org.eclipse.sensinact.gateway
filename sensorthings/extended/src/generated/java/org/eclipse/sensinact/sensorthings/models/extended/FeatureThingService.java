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

import org.eclipse.emf.common.util.EList;

import org.eclipse.sensinact.model.core.provider.Service;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Feature Thing Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.FeatureThingService#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.FeatureThingService#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.FeatureThingService#getEncodingType <em>Encoding Type</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.FeatureThingService#isHasObs <em>Has Obs</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.FeatureThingService#getDatastreamIds <em>Datastream Ids</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getFeatureThingService()
 * @model
 * @generated
 */
@ProviderType
public interface FeatureThingService extends Service {
	/**
	 * Returns the value of the '<em><b>Properties</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Properties</em>' attribute.
	 * @see #setProperties(Object)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getFeatureThingService_Properties()
	 * @model
	 * @generated
	 */
	Object getProperties();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.FeatureThingService#getProperties <em>Properties</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Properties</em>' attribute.
	 * @see #getProperties()
	 * @generated
	 */
	void setProperties(Object value);

	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getFeatureThingService_Id()
	 * @model
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.FeatureThingService#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Encoding Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Encoding Type</em>' attribute.
	 * @see #setEncodingType(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getFeatureThingService_EncodingType()
	 * @model
	 * @generated
	 */
	String getEncodingType();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.FeatureThingService#getEncodingType <em>Encoding Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Encoding Type</em>' attribute.
	 * @see #getEncodingType()
	 * @generated
	 */
	void setEncodingType(String value);

	/**
	 * Returns the value of the '<em><b>Has Obs</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Has Obs</em>' attribute.
	 * @see #setHasObs(boolean)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getFeatureThingService_HasObs()
	 * @model
	 * @generated
	 */
	boolean isHasObs();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.FeatureThingService#isHasObs <em>Has Obs</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Has Obs</em>' attribute.
	 * @see #isHasObs()
	 * @generated
	 */
	void setHasObs(boolean value);

	/**
	 * Returns the value of the '<em><b>Datastream Ids</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Datastream Ids</em>' attribute list.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getFeatureThingService_DatastreamIds()
	 * @model
	 * @generated
	 */
	EList<String> getDatastreamIds();

} // FeatureThingService
