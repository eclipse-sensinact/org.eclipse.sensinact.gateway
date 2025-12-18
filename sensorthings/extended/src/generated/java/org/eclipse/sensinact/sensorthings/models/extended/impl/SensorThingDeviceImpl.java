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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.sensinact.model.core.provider.impl.DynamicProviderImpl;

import org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage;
import org.eclipse.sensinact.sensorthings.models.extended.SensorThingDevice;
import org.eclipse.sensinact.sensorthings.models.extended.SensorThingService;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sensor Thing Device</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingDeviceImpl#getThing <em>Thing</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SensorThingDeviceImpl extends DynamicProviderImpl implements SensorThingDevice {
	/**
	 * The cached value of the '{@link #getThing() <em>Thing</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThing()
	 * @generated
	 * @ordered
	 */
	protected SensorThingService thing;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SensorThingDeviceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExtendedPackage.Literals.SENSOR_THING_DEVICE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SensorThingService getThing() {
		if (thing != null && thing.eIsProxy()) {
			InternalEObject oldThing = (InternalEObject)thing;
			thing = (SensorThingService)eResolveProxy(oldThing);
			if (thing != oldThing) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ExtendedPackage.SENSOR_THING_DEVICE__THING, oldThing, thing));
			}
		}
		return thing;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SensorThingService basicGetThing() {
		return thing;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setThing(SensorThingService newThing) {
		SensorThingService oldThing = thing;
		thing = newThing;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_THING_DEVICE__THING, oldThing, thing));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ExtendedPackage.SENSOR_THING_DEVICE__THING:
				if (resolve) return getThing();
				return basicGetThing();
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
			case ExtendedPackage.SENSOR_THING_DEVICE__THING:
				setThing((SensorThingService)newValue);
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
			case ExtendedPackage.SENSOR_THING_DEVICE__THING:
				setThing((SensorThingService)null);
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
			case ExtendedPackage.SENSOR_THING_DEVICE__THING:
				return thing != null;
		}
		return super.eIsSet(featureID);
	}

} //SensorThingDeviceImpl
