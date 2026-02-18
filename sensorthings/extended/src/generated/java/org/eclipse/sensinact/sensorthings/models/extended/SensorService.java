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
 * A representation of the model object '<em><b>Sensor Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.SensorService#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.SensorService#getTimestamp <em>Timestamp</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.SensorService#getSensorEncodingType <em>Sensor Encoding Type</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.SensorService#getSensorMetadata <em>Sensor Metadata</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.SensorService#getSensorProperties <em>Sensor Properties</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.SensorService#getDatastreamIds <em>Datastream Ids</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorService()
 * @model
 * @generated
 */
@ProviderType
public interface SensorService extends Service {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorService_Id()
	 * @model
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorService#getId <em>Id</em>}' attribute.
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
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorService_Timestamp()
	 * @model dataType="org.eclipse.sensinact.model.core.provider.EInstant"
	 * @generated
	 */
	Instant getTimestamp();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorService#getTimestamp <em>Timestamp</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Timestamp</em>' attribute.
	 * @see #getTimestamp()
	 * @generated
	 */
	void setTimestamp(Instant value);

	/**
	 * Returns the value of the '<em><b>Sensor Encoding Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor Encoding Type</em>' attribute.
	 * @see #setSensorEncodingType(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorService_SensorEncodingType()
	 * @model
	 * @generated
	 */
	String getSensorEncodingType();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorService#getSensorEncodingType <em>Sensor Encoding Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sensor Encoding Type</em>' attribute.
	 * @see #getSensorEncodingType()
	 * @generated
	 */
	void setSensorEncodingType(String value);

	/**
	 * Returns the value of the '<em><b>Sensor Metadata</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor Metadata</em>' attribute.
	 * @see #setSensorMetadata(Object)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorService_SensorMetadata()
	 * @model
	 * @generated
	 */
	Object getSensorMetadata();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorService#getSensorMetadata <em>Sensor Metadata</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sensor Metadata</em>' attribute.
	 * @see #getSensorMetadata()
	 * @generated
	 */
	void setSensorMetadata(Object value);

	/**
	 * Returns the value of the '<em><b>Sensor Properties</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor Properties</em>' attribute.
	 * @see #setSensorProperties(Map)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorService_SensorProperties()
	 * @model transient="true"
	 * @generated
	 */
	Map<?, ?> getSensorProperties();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorService#getSensorProperties <em>Sensor Properties</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sensor Properties</em>' attribute.
	 * @see #getSensorProperties()
	 * @generated
	 */
	void setSensorProperties(Map<?, ?> value);

	/**
	 * Returns the value of the '<em><b>Datastream Ids</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Datastream Ids</em>' attribute list.
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorService_DatastreamIds()
	 * @model
	 * @generated
	 */
	EList<String> getDatastreamIds();

} // SensorService
