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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

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
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingDatastreamImpl#getDatastream <em>Datastream</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SensorThingDatastreamImpl extends DynamicProviderImpl implements SensorThingDatastream {
	/**
	 * The cached value of the '{@link #getDatastream() <em>Datastream</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDatastream()
	 * @generated
	 * @ordered
	 */
	protected DataStreamService datastream;

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
	public DataStreamService getDatastream() {
		return datastream;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetDatastream(DataStreamService newDatastream, NotificationChain msgs) {
		DataStreamService oldDatastream = datastream;
		datastream = newDatastream;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_THING_DATASTREAM__DATASTREAM, oldDatastream, newDatastream);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDatastream(DataStreamService newDatastream) {
		if (newDatastream != datastream) {
			NotificationChain msgs = null;
			if (datastream != null)
				msgs = ((InternalEObject)datastream).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExtendedPackage.SENSOR_THING_DATASTREAM__DATASTREAM, null, msgs);
			if (newDatastream != null)
				msgs = ((InternalEObject)newDatastream).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExtendedPackage.SENSOR_THING_DATASTREAM__DATASTREAM, null, msgs);
			msgs = basicSetDatastream(newDatastream, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_THING_DATASTREAM__DATASTREAM, newDatastream, newDatastream));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ExtendedPackage.SENSOR_THING_DATASTREAM__DATASTREAM:
				return basicSetDatastream(null, msgs);
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
			case ExtendedPackage.SENSOR_THING_DATASTREAM__DATASTREAM:
				return getDatastream();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ExtendedPackage.SENSOR_THING_DATASTREAM__DATASTREAM:
				setDatastream((DataStreamService)newValue);
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
			case ExtendedPackage.SENSOR_THING_DATASTREAM__DATASTREAM:
				setDatastream((DataStreamService)null);
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
			case ExtendedPackage.SENSOR_THING_DATASTREAM__DATASTREAM:
				return datastream != null;
		}
		return super.eIsSet(featureID);
	}

} //SensorThingDatastreamImpl
