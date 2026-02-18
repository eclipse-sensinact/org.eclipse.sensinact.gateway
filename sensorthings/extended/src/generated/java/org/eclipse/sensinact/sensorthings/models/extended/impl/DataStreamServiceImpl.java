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

import java.time.Instant;

import java.util.Map;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.sensinact.model.core.provider.impl.ServiceImpl;

import org.eclipse.sensinact.sensorthings.models.extended.DataStreamService;
import org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Data Stream Service</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getTimestamp <em>Timestamp</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getUnitName <em>Unit Name</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getUnitSymbol <em>Unit Symbol</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getUnitDefinition <em>Unit Definition</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getObservedPropertyId <em>Observed Property Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getSensorId <em>Sensor Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getLastObservation <em>Last Observation</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getObservationType <em>Observation Type</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getThingId <em>Thing Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getProperties <em>Properties</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DataStreamServiceImpl extends ServiceImpl implements DataStreamService {
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getTimestamp() <em>Timestamp</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTimestamp()
	 * @generated
	 * @ordered
	 */
	protected static final Instant TIMESTAMP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTimestamp() <em>Timestamp</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTimestamp()
	 * @generated
	 * @ordered
	 */
	protected Instant timestamp = TIMESTAMP_EDEFAULT;

	/**
	 * The default value of the '{@link #getUnitName() <em>Unit Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnitName()
	 * @generated
	 * @ordered
	 */
	protected static final String UNIT_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getUnitName() <em>Unit Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnitName()
	 * @generated
	 * @ordered
	 */
	protected String unitName = UNIT_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getUnitSymbol() <em>Unit Symbol</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnitSymbol()
	 * @generated
	 * @ordered
	 */
	protected static final String UNIT_SYMBOL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getUnitSymbol() <em>Unit Symbol</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnitSymbol()
	 * @generated
	 * @ordered
	 */
	protected String unitSymbol = UNIT_SYMBOL_EDEFAULT;

	/**
	 * The default value of the '{@link #getUnitDefinition() <em>Unit Definition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnitDefinition()
	 * @generated
	 * @ordered
	 */
	protected static final String UNIT_DEFINITION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getUnitDefinition() <em>Unit Definition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnitDefinition()
	 * @generated
	 * @ordered
	 */
	protected String unitDefinition = UNIT_DEFINITION_EDEFAULT;

	/**
	 * The default value of the '{@link #getObservedPropertyId() <em>Observed Property Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservedPropertyId()
	 * @generated
	 * @ordered
	 */
	protected static final String OBSERVED_PROPERTY_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getObservedPropertyId() <em>Observed Property Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservedPropertyId()
	 * @generated
	 * @ordered
	 */
	protected String observedPropertyId = OBSERVED_PROPERTY_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getSensorId() <em>Sensor Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorId()
	 * @generated
	 * @ordered
	 */
	protected static final String SENSOR_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSensorId() <em>Sensor Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorId()
	 * @generated
	 * @ordered
	 */
	protected String sensorId = SENSOR_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getLastObservation() <em>Last Observation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastObservation()
	 * @generated
	 * @ordered
	 */
	protected static final String LAST_OBSERVATION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLastObservation() <em>Last Observation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastObservation()
	 * @generated
	 * @ordered
	 */
	protected String lastObservation = LAST_OBSERVATION_EDEFAULT;

	/**
	 * The default value of the '{@link #getObservationType() <em>Observation Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservationType()
	 * @generated
	 * @ordered
	 */
	protected static final String OBSERVATION_TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getObservationType() <em>Observation Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservationType()
	 * @generated
	 * @ordered
	 */
	protected String observationType = OBSERVATION_TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getThingId() <em>Thing Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThingId()
	 * @generated
	 * @ordered
	 */
	protected static final String THING_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getThingId() <em>Thing Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThingId()
	 * @generated
	 * @ordered
	 */
	protected String thingId = THING_ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getProperties() <em>Properties</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProperties()
	 * @generated
	 * @ordered
	 */
	protected Map<?, ?> properties;

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
		return ExtendedPackage.Literals.DATA_STREAM_SERVICE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setId(String newId) {
		String oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Instant getTimestamp() {
		return timestamp;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTimestamp(Instant newTimestamp) {
		Instant oldTimestamp = timestamp;
		timestamp = newTimestamp;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__TIMESTAMP, oldTimestamp, timestamp));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getUnitName() {
		return unitName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUnitName(String newUnitName) {
		String oldUnitName = unitName;
		unitName = newUnitName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__UNIT_NAME, oldUnitName, unitName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getUnitSymbol() {
		return unitSymbol;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUnitSymbol(String newUnitSymbol) {
		String oldUnitSymbol = unitSymbol;
		unitSymbol = newUnitSymbol;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__UNIT_SYMBOL, oldUnitSymbol, unitSymbol));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getUnitDefinition() {
		return unitDefinition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUnitDefinition(String newUnitDefinition) {
		String oldUnitDefinition = unitDefinition;
		unitDefinition = newUnitDefinition;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__UNIT_DEFINITION, oldUnitDefinition, unitDefinition));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getObservedPropertyId() {
		return observedPropertyId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setObservedPropertyId(String newObservedPropertyId) {
		String oldObservedPropertyId = observedPropertyId;
		observedPropertyId = newObservedPropertyId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_ID, oldObservedPropertyId, observedPropertyId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSensorId() {
		return sensorId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSensorId(String newSensorId) {
		String oldSensorId = sensorId;
		sensorId = newSensorId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_ID, oldSensorId, sensorId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLastObservation() {
		return lastObservation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLastObservation(String newLastObservation) {
		String oldLastObservation = lastObservation;
		lastObservation = newLastObservation;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__LAST_OBSERVATION, oldLastObservation, lastObservation));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getObservationType() {
		return observationType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setObservationType(String newObservationType) {
		String oldObservationType = observationType;
		observationType = newObservationType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__OBSERVATION_TYPE, oldObservationType, observationType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getThingId() {
		return thingId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setThingId(String newThingId) {
		String oldThingId = thingId;
		thingId = newThingId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__THING_ID, oldThingId, thingId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Map<?, ?> getProperties() {
		return properties;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setProperties(Map<?, ?> newProperties) {
		Map<?, ?> oldProperties = properties;
		properties = newProperties;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__PROPERTIES, oldProperties, properties));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ExtendedPackage.DATA_STREAM_SERVICE__ID:
				return getId();
			case ExtendedPackage.DATA_STREAM_SERVICE__TIMESTAMP:
				return getTimestamp();
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_NAME:
				return getUnitName();
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_SYMBOL:
				return getUnitSymbol();
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_DEFINITION:
				return getUnitDefinition();
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_ID:
				return getObservedPropertyId();
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_ID:
				return getSensorId();
			case ExtendedPackage.DATA_STREAM_SERVICE__LAST_OBSERVATION:
				return getLastObservation();
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVATION_TYPE:
				return getObservationType();
			case ExtendedPackage.DATA_STREAM_SERVICE__THING_ID:
				return getThingId();
			case ExtendedPackage.DATA_STREAM_SERVICE__PROPERTIES:
				return getProperties();
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
			case ExtendedPackage.DATA_STREAM_SERVICE__ID:
				setId((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__TIMESTAMP:
				setTimestamp((Instant)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_NAME:
				setUnitName((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_SYMBOL:
				setUnitSymbol((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_DEFINITION:
				setUnitDefinition((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_ID:
				setObservedPropertyId((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_ID:
				setSensorId((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__LAST_OBSERVATION:
				setLastObservation((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVATION_TYPE:
				setObservationType((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__THING_ID:
				setThingId((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__PROPERTIES:
				setProperties((Map<?, ?>)newValue);
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
			case ExtendedPackage.DATA_STREAM_SERVICE__ID:
				setId(ID_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__TIMESTAMP:
				setTimestamp(TIMESTAMP_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_NAME:
				setUnitName(UNIT_NAME_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_SYMBOL:
				setUnitSymbol(UNIT_SYMBOL_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_DEFINITION:
				setUnitDefinition(UNIT_DEFINITION_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_ID:
				setObservedPropertyId(OBSERVED_PROPERTY_ID_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_ID:
				setSensorId(SENSOR_ID_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__LAST_OBSERVATION:
				setLastObservation(LAST_OBSERVATION_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVATION_TYPE:
				setObservationType(OBSERVATION_TYPE_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__THING_ID:
				setThingId(THING_ID_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__PROPERTIES:
				setProperties((Map<?, ?>)null);
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
			case ExtendedPackage.DATA_STREAM_SERVICE__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case ExtendedPackage.DATA_STREAM_SERVICE__TIMESTAMP:
				return TIMESTAMP_EDEFAULT == null ? timestamp != null : !TIMESTAMP_EDEFAULT.equals(timestamp);
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_NAME:
				return UNIT_NAME_EDEFAULT == null ? unitName != null : !UNIT_NAME_EDEFAULT.equals(unitName);
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_SYMBOL:
				return UNIT_SYMBOL_EDEFAULT == null ? unitSymbol != null : !UNIT_SYMBOL_EDEFAULT.equals(unitSymbol);
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_DEFINITION:
				return UNIT_DEFINITION_EDEFAULT == null ? unitDefinition != null : !UNIT_DEFINITION_EDEFAULT.equals(unitDefinition);
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_ID:
				return OBSERVED_PROPERTY_ID_EDEFAULT == null ? observedPropertyId != null : !OBSERVED_PROPERTY_ID_EDEFAULT.equals(observedPropertyId);
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_ID:
				return SENSOR_ID_EDEFAULT == null ? sensorId != null : !SENSOR_ID_EDEFAULT.equals(sensorId);
			case ExtendedPackage.DATA_STREAM_SERVICE__LAST_OBSERVATION:
				return LAST_OBSERVATION_EDEFAULT == null ? lastObservation != null : !LAST_OBSERVATION_EDEFAULT.equals(lastObservation);
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVATION_TYPE:
				return OBSERVATION_TYPE_EDEFAULT == null ? observationType != null : !OBSERVATION_TYPE_EDEFAULT.equals(observationType);
			case ExtendedPackage.DATA_STREAM_SERVICE__THING_ID:
				return THING_ID_EDEFAULT == null ? thingId != null : !THING_ID_EDEFAULT.equals(thingId);
			case ExtendedPackage.DATA_STREAM_SERVICE__PROPERTIES:
				return properties != null;
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
		result.append(" (id: ");
		result.append(id);
		result.append(", timestamp: ");
		result.append(timestamp);
		result.append(", unitName: ");
		result.append(unitName);
		result.append(", unitSymbol: ");
		result.append(unitSymbol);
		result.append(", unitDefinition: ");
		result.append(unitDefinition);
		result.append(", observedPropertyId: ");
		result.append(observedPropertyId);
		result.append(", sensorId: ");
		result.append(sensorId);
		result.append(", lastObservation: ");
		result.append(lastObservation);
		result.append(", observationType: ");
		result.append(observationType);
		result.append(", thingId: ");
		result.append(thingId);
		result.append(", properties: ");
		result.append(properties);
		result.append(')');
		return result.toString();
	}

} //DataStreamServiceImpl
