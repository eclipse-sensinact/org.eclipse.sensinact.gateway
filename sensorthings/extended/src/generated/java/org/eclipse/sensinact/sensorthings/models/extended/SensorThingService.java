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

import org.eclipse.sensinact.model.core.provider.Service;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sensor Thing Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getLocationIds <em>Location Ids</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getDatastreamIds <em>Datastream Ids</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorThingService()
 * @model
 * @generated
 */
@ProviderType
public interface SensorThingService extends Service {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorThingService_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorThingService_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Properties</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Properties</em>' attribute.
	 * @see #setProperties(Object)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorThingService_Properties()
	 * @model
	 * @generated
	 */
	Object getProperties();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getProperties <em>Properties</em>}' attribute.
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
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorThingService_Id()
	 * @model
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Location Ids</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Location Ids</em>' attribute.
	 * @see #setLocationIds(Object)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorThingService_LocationIds()
	 * @model
	 * @generated
	 */
	Object getLocationIds();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getLocationIds <em>Location Ids</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Location Ids</em>' attribute.
	 * @see #getLocationIds()
	 * @generated
	 */
	void setLocationIds(Object value);

	/**
	 * Returns the value of the '<em><b>Datastream Ids</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Datastream Ids</em>' attribute.
	 * @see #setDatastreamIds(Object)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorThingService_DatastreamIds()
	 * @model
	 * @generated
	 */
	Object getDatastreamIds();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingService#getDatastreamIds <em>Datastream Ids</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Datastream Ids</em>' attribute.
	 * @see #getDatastreamIds()
	 * @generated
	 */
	void setDatastreamIds(Object value);

} // SensorThingService
