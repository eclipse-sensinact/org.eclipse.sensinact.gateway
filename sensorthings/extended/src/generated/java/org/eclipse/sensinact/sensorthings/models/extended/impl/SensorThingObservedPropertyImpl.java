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

import org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage;
import org.eclipse.sensinact.sensorthings.models.extended.ObservedPropertyService;
import org.eclipse.sensinact.sensorthings.models.extended.SensorThingObservedProperty;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sensor Thing Observed Property</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingObservedPropertyImpl#getObservedproperty <em>Observedproperty</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SensorThingObservedPropertyImpl extends DynamicProviderImpl implements SensorThingObservedProperty {
	/**
	 * The cached value of the '{@link #getObservedproperty() <em>Observedproperty</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservedproperty()
	 * @generated
	 * @ordered
	 */
	protected ObservedPropertyService observedproperty;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SensorThingObservedPropertyImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExtendedPackage.Literals.SENSOR_THING_OBSERVED_PROPERTY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ObservedPropertyService getObservedproperty() {
		return observedproperty;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetObservedproperty(ObservedPropertyService newObservedproperty, NotificationChain msgs) {
		ObservedPropertyService oldObservedproperty = observedproperty;
		observedproperty = newObservedproperty;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_THING_OBSERVED_PROPERTY__OBSERVEDPROPERTY, oldObservedproperty, newObservedproperty);
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
	public void setObservedproperty(ObservedPropertyService newObservedproperty) {
		if (newObservedproperty != observedproperty) {
			NotificationChain msgs = null;
			if (observedproperty != null)
				msgs = ((InternalEObject)observedproperty).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ExtendedPackage.SENSOR_THING_OBSERVED_PROPERTY__OBSERVEDPROPERTY, null, msgs);
			if (newObservedproperty != null)
				msgs = ((InternalEObject)newObservedproperty).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ExtendedPackage.SENSOR_THING_OBSERVED_PROPERTY__OBSERVEDPROPERTY, null, msgs);
			msgs = basicSetObservedproperty(newObservedproperty, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_THING_OBSERVED_PROPERTY__OBSERVEDPROPERTY, newObservedproperty, newObservedproperty));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ExtendedPackage.SENSOR_THING_OBSERVED_PROPERTY__OBSERVEDPROPERTY:
				return basicSetObservedproperty(null, msgs);
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
			case ExtendedPackage.SENSOR_THING_OBSERVED_PROPERTY__OBSERVEDPROPERTY:
				return getObservedproperty();
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
			case ExtendedPackage.SENSOR_THING_OBSERVED_PROPERTY__OBSERVEDPROPERTY:
				setObservedproperty((ObservedPropertyService)newValue);
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
			case ExtendedPackage.SENSOR_THING_OBSERVED_PROPERTY__OBSERVEDPROPERTY:
				setObservedproperty((ObservedPropertyService)null);
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
			case ExtendedPackage.SENSOR_THING_OBSERVED_PROPERTY__OBSERVEDPROPERTY:
				return observedproperty != null;
		}
		return super.eIsSet(featureID);
	}

} //SensorThingObservedPropertyImpl
