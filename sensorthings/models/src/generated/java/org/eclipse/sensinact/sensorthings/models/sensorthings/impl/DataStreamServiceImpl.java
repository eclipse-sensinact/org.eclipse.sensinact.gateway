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
package org.eclipse.sensinact.sensorthings.models.sensorthings.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.sensinact.model.core.provider.impl.ServiceImpl;

import org.eclipse.sensinact.sensorthings.models.sensorthings.DataStreamService;
import org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Data Stream Service</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.sensorthings.impl.DataStreamServiceImpl#getSensorThingsId <em>Sensor Things Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.sensorthings.impl.DataStreamServiceImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.sensorthings.impl.DataStreamServiceImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.sensorthings.impl.DataStreamServiceImpl#getLatestObservation <em>Latest Observation</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.sensorthings.impl.DataStreamServiceImpl#getUnit <em>Unit</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.sensorthings.impl.DataStreamServiceImpl#getSensor <em>Sensor</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.sensorthings.impl.DataStreamServiceImpl#getObservedProperty <em>Observed Property</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DataStreamServiceImpl extends ServiceImpl implements DataStreamService {
	/**
	 * The default value of the '{@link #getSensorThingsId() <em>Sensor Things Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorThingsId()
	 * @generated
	 * @ordered
	 */
	protected static final Object SENSOR_THINGS_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSensorThingsId() <em>Sensor Things Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorThingsId()
	 * @generated
	 * @ordered
	 */
	protected Object sensorThingsId = SENSOR_THINGS_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected String description = DESCRIPTION_EDEFAULT;

	/**
	 * The default value of the '{@link #getLatestObservation() <em>Latest Observation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLatestObservation()
	 * @generated
	 * @ordered
	 */
	protected static final Object LATEST_OBSERVATION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLatestObservation() <em>Latest Observation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLatestObservation()
	 * @generated
	 * @ordered
	 */
	protected Object latestObservation = LATEST_OBSERVATION_EDEFAULT;

	/**
	 * The default value of the '{@link #getUnit() <em>Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnit()
	 * @generated
	 * @ordered
	 */
	protected static final String UNIT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getUnit() <em>Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnit()
	 * @generated
	 * @ordered
	 */
	protected String unit = UNIT_EDEFAULT;

	/**
	 * The default value of the '{@link #getSensor() <em>Sensor</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensor()
	 * @generated
	 * @ordered
	 */
	protected static final String SENSOR_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSensor() <em>Sensor</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensor()
	 * @generated
	 * @ordered
	 */
	protected String sensor = SENSOR_EDEFAULT;

	/**
	 * The default value of the '{@link #getObservedProperty() <em>Observed Property</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservedProperty()
	 * @generated
	 * @ordered
	 */
	protected static final String OBSERVED_PROPERTY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getObservedProperty() <em>Observed Property</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservedProperty()
	 * @generated
	 * @ordered
	 */
	protected String observedProperty = OBSERVED_PROPERTY_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DataStreamServiceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SensorthingsPackage.Literals.DATA_STREAM_SERVICE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getSensorThingsId() {
		return sensorThingsId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSensorThingsId(Object newSensorThingsId) {
		Object oldSensorThingsId = sensorThingsId;
		sensorThingsId = newSensorThingsId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SensorthingsPackage.DATA_STREAM_SERVICE__SENSOR_THINGS_ID, oldSensorThingsId, sensorThingsId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SensorthingsPackage.DATA_STREAM_SERVICE__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDescription(String newDescription) {
		String oldDescription = description;
		description = newDescription;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SensorthingsPackage.DATA_STREAM_SERVICE__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getLatestObservation() {
		return latestObservation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLatestObservation(Object newLatestObservation) {
		Object oldLatestObservation = latestObservation;
		latestObservation = newLatestObservation;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SensorthingsPackage.DATA_STREAM_SERVICE__LATEST_OBSERVATION, oldLatestObservation, latestObservation));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getUnit() {
		return unit;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUnit(String newUnit) {
		String oldUnit = unit;
		unit = newUnit;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SensorthingsPackage.DATA_STREAM_SERVICE__UNIT, oldUnit, unit));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSensor() {
		return sensor;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSensor(String newSensor) {
		String oldSensor = sensor;
		sensor = newSensor;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SensorthingsPackage.DATA_STREAM_SERVICE__SENSOR, oldSensor, sensor));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getObservedProperty() {
		return observedProperty;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setObservedProperty(String newObservedProperty) {
		String oldObservedProperty = observedProperty;
		observedProperty = newObservedProperty;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SensorthingsPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY, oldObservedProperty, observedProperty));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case SensorthingsPackage.DATA_STREAM_SERVICE__SENSOR_THINGS_ID:
				return getSensorThingsId();
			case SensorthingsPackage.DATA_STREAM_SERVICE__NAME:
				return getName();
			case SensorthingsPackage.DATA_STREAM_SERVICE__DESCRIPTION:
				return getDescription();
			case SensorthingsPackage.DATA_STREAM_SERVICE__LATEST_OBSERVATION:
				return getLatestObservation();
			case SensorthingsPackage.DATA_STREAM_SERVICE__UNIT:
				return getUnit();
			case SensorthingsPackage.DATA_STREAM_SERVICE__SENSOR:
				return getSensor();
			case SensorthingsPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY:
				return getObservedProperty();
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
			case SensorthingsPackage.DATA_STREAM_SERVICE__SENSOR_THINGS_ID:
				setSensorThingsId(newValue);
				return;
			case SensorthingsPackage.DATA_STREAM_SERVICE__NAME:
				setName((String)newValue);
				return;
			case SensorthingsPackage.DATA_STREAM_SERVICE__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case SensorthingsPackage.DATA_STREAM_SERVICE__LATEST_OBSERVATION:
				setLatestObservation(newValue);
				return;
			case SensorthingsPackage.DATA_STREAM_SERVICE__UNIT:
				setUnit((String)newValue);
				return;
			case SensorthingsPackage.DATA_STREAM_SERVICE__SENSOR:
				setSensor((String)newValue);
				return;
			case SensorthingsPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY:
				setObservedProperty((String)newValue);
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
			case SensorthingsPackage.DATA_STREAM_SERVICE__SENSOR_THINGS_ID:
				setSensorThingsId(SENSOR_THINGS_ID_EDEFAULT);
				return;
			case SensorthingsPackage.DATA_STREAM_SERVICE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case SensorthingsPackage.DATA_STREAM_SERVICE__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case SensorthingsPackage.DATA_STREAM_SERVICE__LATEST_OBSERVATION:
				setLatestObservation(LATEST_OBSERVATION_EDEFAULT);
				return;
			case SensorthingsPackage.DATA_STREAM_SERVICE__UNIT:
				setUnit(UNIT_EDEFAULT);
				return;
			case SensorthingsPackage.DATA_STREAM_SERVICE__SENSOR:
				setSensor(SENSOR_EDEFAULT);
				return;
			case SensorthingsPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY:
				setObservedProperty(OBSERVED_PROPERTY_EDEFAULT);
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
			case SensorthingsPackage.DATA_STREAM_SERVICE__SENSOR_THINGS_ID:
				return SENSOR_THINGS_ID_EDEFAULT == null ? sensorThingsId != null : !SENSOR_THINGS_ID_EDEFAULT.equals(sensorThingsId);
			case SensorthingsPackage.DATA_STREAM_SERVICE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case SensorthingsPackage.DATA_STREAM_SERVICE__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case SensorthingsPackage.DATA_STREAM_SERVICE__LATEST_OBSERVATION:
				return LATEST_OBSERVATION_EDEFAULT == null ? latestObservation != null : !LATEST_OBSERVATION_EDEFAULT.equals(latestObservation);
			case SensorthingsPackage.DATA_STREAM_SERVICE__UNIT:
				return UNIT_EDEFAULT == null ? unit != null : !UNIT_EDEFAULT.equals(unit);
			case SensorthingsPackage.DATA_STREAM_SERVICE__SENSOR:
				return SENSOR_EDEFAULT == null ? sensor != null : !SENSOR_EDEFAULT.equals(sensor);
			case SensorthingsPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY:
				return OBSERVED_PROPERTY_EDEFAULT == null ? observedProperty != null : !OBSERVED_PROPERTY_EDEFAULT.equals(observedProperty);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (sensorThingsId: ");
		result.append(sensorThingsId);
		result.append(", name: ");
		result.append(name);
		result.append(", description: ");
		result.append(description);
		result.append(", latestObservation: ");
		result.append(latestObservation);
		result.append(", unit: ");
		result.append(unit);
		result.append(", sensor: ");
		result.append(sensor);
		result.append(", observedProperty: ");
		result.append(observedProperty);
		result.append(')');
		return result.toString();
	}

} //DataStreamServiceImpl
