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
 * A representation of the model object '<em><b>Data Stream Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getSensorThingsId <em>Sensor Things Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getLatestObservation <em>Latest Observation</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getUnit <em>Unit</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getSensor <em>Sensor</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getObservedProperty <em>Observed Property</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage#getDataStreamService()
 * @model
 * @generated
 */
@ProviderType
public interface DataStreamService extends Service {
	/**
	 * Returns the value of the '<em><b>Sensor Things Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor Things Id</em>' attribute.
	 * @see #setSensorThingsId(Object)
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage#getDataStreamService_SensorThingsId()
	 * @model
	 * @generated
	 */
	Object getSensorThingsId();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getSensorThingsId <em>Sensor Things Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sensor Things Id</em>' attribute.
	 * @see #getSensorThingsId()
	 * @generated
	 */
	void setSensorThingsId(Object value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage#getDataStreamService_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getName <em>Name</em>}' attribute.
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
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage#getDataStreamService_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Latest Observation</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Latest Observation</em>' attribute.
	 * @see #setLatestObservation(Object)
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage#getDataStreamService_LatestObservation()
	 * @model
	 * @generated
	 */
	Object getLatestObservation();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getLatestObservation <em>Latest Observation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Latest Observation</em>' attribute.
	 * @see #getLatestObservation()
	 * @generated
	 */
	void setLatestObservation(Object value);

	/**
	 * Returns the value of the '<em><b>Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Unit</em>' attribute.
	 * @see #setUnit(String)
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage#getDataStreamService_Unit()
	 * @model
	 * @generated
	 */
	String getUnit();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getUnit <em>Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Unit</em>' attribute.
	 * @see #getUnit()
	 * @generated
	 */
	void setUnit(String value);

	/**
	 * Returns the value of the '<em><b>Sensor</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor</em>' attribute.
	 * @see #setSensor(String)
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage#getDataStreamService_Sensor()
	 * @model
	 * @generated
	 */
	String getSensor();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getSensor <em>Sensor</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sensor</em>' attribute.
	 * @see #getSensor()
	 * @generated
	 */
	void setSensor(String value);

	/**
	 * Returns the value of the '<em><b>Observed Property</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observed Property</em>' attribute.
	 * @see #setObservedProperty(String)
	 * @see org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage#getDataStreamService_ObservedProperty()
	 * @model
	 * @generated
	 */
	String getObservedProperty();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService#getObservedProperty <em>Observed Property</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observed Property</em>' attribute.
	 * @see #getObservedProperty()
	 * @generated
	 */
	void setObservedProperty(String value);

} // DataStreamService
