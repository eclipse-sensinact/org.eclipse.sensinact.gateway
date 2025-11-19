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

import org.eclipse.sensinact.model.core.provider.Service;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sensor Things Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.sensorthings.SensorThingsService#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.sensorthings.SensorThingsService#getDescription <em>Description</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage#getSensorThingsService()
 * @model
 * @generated
 */
@ProviderType
public interface SensorThingsService extends Service {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(Object)
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage#getSensorThingsService_Id()
	 * @model
	 * @generated
	 */
	Object getId();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.SensorThingsService#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(Object value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage#getSensorThingsService_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.SensorThingsService#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

} // SensorThingsService
