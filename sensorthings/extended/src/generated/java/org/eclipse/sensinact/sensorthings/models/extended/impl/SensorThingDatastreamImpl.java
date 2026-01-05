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
package org.eclipse.sensinact.sensorthings.models.extended.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.sensinact.model.core.provider.impl.DynamicProviderImpl;

import org.eclipse.sensinact.sensorthings.models.extended.DataStreamService;
import org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage;
import org.eclipse.sensinact.sensorthings.models.extended.SensorThingDatastream;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sensor Thing Datastream</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingDatastreamImpl#getDataStreams <em>Data Streams</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SensorThingDatastreamImpl extends DynamicProviderImpl implements SensorThingDatastream {
	/**
	 * The cached value of the '{@link #getDataStreams() <em>Data Streams</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataStreams()
	 * @generated
	 * @ordered
	 */
	protected EList<DataStreamService> dataStreams;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SensorThingDatastreamImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExtendedPackage.Literals.SENSOR_THING_DATASTREAM;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<DataStreamService> getDataStreams() {
		if (dataStreams == null) {
			dataStreams = new EObjectContainmentEList<DataStreamService>(DataStreamService.class, this, ExtendedPackage.SENSOR_THING_DATASTREAM__DATA_STREAMS);
		}
		return dataStreams;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ExtendedPackage.SENSOR_THING_DATASTREAM__DATA_STREAMS:
				return ((InternalEList<?>)getDataStreams()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ExtendedPackage.SENSOR_THING_DATASTREAM__DATA_STREAMS:
				return getDataStreams();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ExtendedPackage.SENSOR_THING_DATASTREAM__DATA_STREAMS:
				getDataStreams().clear();
				getDataStreams().addAll((Collection<? extends DataStreamService>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ExtendedPackage.SENSOR_THING_DATASTREAM__DATA_STREAMS:
				getDataStreams().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ExtendedPackage.SENSOR_THING_DATASTREAM__DATA_STREAMS:
				return dataStreams != null && !dataStreams.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //SensorThingDatastreamImpl
