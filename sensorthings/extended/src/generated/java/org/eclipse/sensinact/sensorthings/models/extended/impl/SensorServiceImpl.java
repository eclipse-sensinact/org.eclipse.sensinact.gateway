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

import java.util.Collection;
import java.util.Map;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import org.eclipse.sensinact.model.core.provider.impl.ServiceImpl;

import org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage;
import org.eclipse.sensinact.sensorthings.models.extended.SensorService;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sensor Service</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorServiceImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorServiceImpl#getTimestamp <em>Timestamp</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorServiceImpl#getSensorEncodingType <em>Sensor Encoding Type</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorServiceImpl#getSensorMetadata <em>Sensor Metadata</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorServiceImpl#getSensorProperties <em>Sensor Properties</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorServiceImpl#getDatastreamIds <em>Datastream Ids</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SensorServiceImpl extends ServiceImpl implements SensorService {
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
	 * The default value of the '{@link #getSensorEncodingType() <em>Sensor Encoding Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorEncodingType()
	 * @generated
	 * @ordered
	 */
	protected static final String SENSOR_ENCODING_TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSensorEncodingType() <em>Sensor Encoding Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorEncodingType()
	 * @generated
	 * @ordered
	 */
	protected String sensorEncodingType = SENSOR_ENCODING_TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getSensorMetadata() <em>Sensor Metadata</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorMetadata()
	 * @generated
	 * @ordered
	 */
	protected static final Object SENSOR_METADATA_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSensorMetadata() <em>Sensor Metadata</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorMetadata()
	 * @generated
	 * @ordered
	 */
	protected Object sensorMetadata = SENSOR_METADATA_EDEFAULT;

	/**
	 * The cached value of the '{@link #getSensorProperties() <em>Sensor Properties</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorProperties()
	 * @generated
	 * @ordered
	 */
	protected Map<?, ?> sensorProperties;

	/**
	 * The cached value of the '{@link #getDatastreamIds() <em>Datastream Ids</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDatastreamIds()
	 * @generated
	 * @ordered
	 */
	protected EList<String> datastreamIds;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SensorServiceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExtendedPackage.Literals.SENSOR_SERVICE;
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
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_SERVICE__ID, oldId, id));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_SERVICE__TIMESTAMP, oldTimestamp, timestamp));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSensorEncodingType() {
		return sensorEncodingType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSensorEncodingType(String newSensorEncodingType) {
		String oldSensorEncodingType = sensorEncodingType;
		sensorEncodingType = newSensorEncodingType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_SERVICE__SENSOR_ENCODING_TYPE, oldSensorEncodingType, sensorEncodingType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getSensorMetadata() {
		return sensorMetadata;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSensorMetadata(Object newSensorMetadata) {
		Object oldSensorMetadata = sensorMetadata;
		sensorMetadata = newSensorMetadata;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_SERVICE__SENSOR_METADATA, oldSensorMetadata, sensorMetadata));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Map<?, ?> getSensorProperties() {
		return sensorProperties;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSensorProperties(Map<?, ?> newSensorProperties) {
		Map<?, ?> oldSensorProperties = sensorProperties;
		sensorProperties = newSensorProperties;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_SERVICE__SENSOR_PROPERTIES, oldSensorProperties, sensorProperties));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getDatastreamIds() {
		if (datastreamIds == null) {
			datastreamIds = new EDataTypeUniqueEList<String>(String.class, this, ExtendedPackage.SENSOR_SERVICE__DATASTREAM_IDS);
		}
		return datastreamIds;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ExtendedPackage.SENSOR_SERVICE__ID:
				return getId();
			case ExtendedPackage.SENSOR_SERVICE__TIMESTAMP:
				return getTimestamp();
			case ExtendedPackage.SENSOR_SERVICE__SENSOR_ENCODING_TYPE:
				return getSensorEncodingType();
			case ExtendedPackage.SENSOR_SERVICE__SENSOR_METADATA:
				return getSensorMetadata();
			case ExtendedPackage.SENSOR_SERVICE__SENSOR_PROPERTIES:
				return getSensorProperties();
			case ExtendedPackage.SENSOR_SERVICE__DATASTREAM_IDS:
				return getDatastreamIds();
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
			case ExtendedPackage.SENSOR_SERVICE__ID:
				setId((String)newValue);
				return;
			case ExtendedPackage.SENSOR_SERVICE__TIMESTAMP:
				setTimestamp((Instant)newValue);
				return;
			case ExtendedPackage.SENSOR_SERVICE__SENSOR_ENCODING_TYPE:
				setSensorEncodingType((String)newValue);
				return;
			case ExtendedPackage.SENSOR_SERVICE__SENSOR_METADATA:
				setSensorMetadata(newValue);
				return;
			case ExtendedPackage.SENSOR_SERVICE__SENSOR_PROPERTIES:
				setSensorProperties((Map<?, ?>)newValue);
				return;
			case ExtendedPackage.SENSOR_SERVICE__DATASTREAM_IDS:
				getDatastreamIds().clear();
				getDatastreamIds().addAll((Collection<? extends String>)newValue);
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
			case ExtendedPackage.SENSOR_SERVICE__ID:
				setId(ID_EDEFAULT);
				return;
			case ExtendedPackage.SENSOR_SERVICE__TIMESTAMP:
				setTimestamp(TIMESTAMP_EDEFAULT);
				return;
			case ExtendedPackage.SENSOR_SERVICE__SENSOR_ENCODING_TYPE:
				setSensorEncodingType(SENSOR_ENCODING_TYPE_EDEFAULT);
				return;
			case ExtendedPackage.SENSOR_SERVICE__SENSOR_METADATA:
				setSensorMetadata(SENSOR_METADATA_EDEFAULT);
				return;
			case ExtendedPackage.SENSOR_SERVICE__SENSOR_PROPERTIES:
				setSensorProperties((Map<?, ?>)null);
				return;
			case ExtendedPackage.SENSOR_SERVICE__DATASTREAM_IDS:
				getDatastreamIds().clear();
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
			case ExtendedPackage.SENSOR_SERVICE__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case ExtendedPackage.SENSOR_SERVICE__TIMESTAMP:
				return TIMESTAMP_EDEFAULT == null ? timestamp != null : !TIMESTAMP_EDEFAULT.equals(timestamp);
			case ExtendedPackage.SENSOR_SERVICE__SENSOR_ENCODING_TYPE:
				return SENSOR_ENCODING_TYPE_EDEFAULT == null ? sensorEncodingType != null : !SENSOR_ENCODING_TYPE_EDEFAULT.equals(sensorEncodingType);
			case ExtendedPackage.SENSOR_SERVICE__SENSOR_METADATA:
				return SENSOR_METADATA_EDEFAULT == null ? sensorMetadata != null : !SENSOR_METADATA_EDEFAULT.equals(sensorMetadata);
			case ExtendedPackage.SENSOR_SERVICE__SENSOR_PROPERTIES:
				return sensorProperties != null;
			case ExtendedPackage.SENSOR_SERVICE__DATASTREAM_IDS:
				return datastreamIds != null && !datastreamIds.isEmpty();
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
		result.append(", sensorEncodingType: ");
		result.append(sensorEncodingType);
		result.append(", sensorMetadata: ");
		result.append(sensorMetadata);
		result.append(", sensorProperties: ");
		result.append(sensorProperties);
		result.append(", datastreamIds: ");
		result.append(datastreamIds);
		result.append(')');
		return result.toString();
	}

} //SensorServiceImpl
