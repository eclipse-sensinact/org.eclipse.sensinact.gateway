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

import java.time.Instant;

import java.util.Map;

import org.eclipse.emf.common.util.EList;

import org.eclipse.sensinact.model.core.provider.Service;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Observed Property Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.ObservedPropertyService#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.ObservedPropertyService#getTimestamp <em>Timestamp</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.ObservedPropertyService#getObservedPropertyDefinition <em>Observed Property Definition</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.ObservedPropertyService#getObservedPropertyProperties <em>Observed Property Properties</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.ObservedPropertyService#getDatastreamIds <em>Datastream Ids</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getObservedPropertyService()
 * @model
 * @generated
 */
@ProviderType
public interface ObservedPropertyService extends Service {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getObservedPropertyService_Id()
	 * @model
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.ObservedPropertyService#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Timestamp</em>' attribute.
	 * @see #setTimestamp(Instant)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getObservedPropertyService_Timestamp()
	 * @model dataType="org.eclipse.sensinact.model.core.provider.EInstant"
	 * @generated
	 */
	Instant getTimestamp();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.ObservedPropertyService#getTimestamp <em>Timestamp</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Timestamp</em>' attribute.
	 * @see #getTimestamp()
	 * @generated
	 */
	void setTimestamp(Instant value);

	/**
	 * Returns the value of the '<em><b>Observed Property Definition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observed Property Definition</em>' attribute.
	 * @see #setObservedPropertyDefinition(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getObservedPropertyService_ObservedPropertyDefinition()
	 * @model
	 * @generated
	 */
	String getObservedPropertyDefinition();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.ObservedPropertyService#getObservedPropertyDefinition <em>Observed Property Definition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observed Property Definition</em>' attribute.
	 * @see #getObservedPropertyDefinition()
	 * @generated
	 */
	void setObservedPropertyDefinition(String value);

	/**
	 * Returns the value of the '<em><b>Observed Property Properties</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observed Property Properties</em>' attribute.
	 * @see #setObservedPropertyProperties(Map)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getObservedPropertyService_ObservedPropertyProperties()
	 * @model transient="true"
	 * @generated
	 */
	Map<?, ?> getObservedPropertyProperties();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.ObservedPropertyService#getObservedPropertyProperties <em>Observed Property Properties</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observed Property Properties</em>' attribute.
	 * @see #getObservedPropertyProperties()
	 * @generated
	 */
	void setObservedPropertyProperties(Map<?, ?> value);

	/**
	 * Returns the value of the '<em><b>Datastream Ids</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Datastream Ids</em>' attribute list.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getObservedPropertyService_DatastreamIds()
	 * @model
	 * @generated
	 */
	EList<String> getDatastreamIds();

} // ObservedPropertyService
