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

import java.util.Map;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.sensinact.model.core.provider.impl.ServiceImpl;

import org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage;
import org.eclipse.sensinact.sensorthings.models.extended.SensorThingLocationService;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sensor Thing Location Service</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingLocationServiceImpl#getEncodingType <em>Encoding Type</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingLocationServiceImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingLocationServiceImpl#getProperties <em>Properties</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SensorThingLocationServiceImpl extends ServiceImpl implements SensorThingLocationService {
	/**
	 * The default value of the '{@link #getEncodingType() <em>Encoding Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEncodingType()
	 * @generated
	 * @ordered
	 */
	protected static final String ENCODING_TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getEncodingType() <em>Encoding Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEncodingType()
	 * @generated
	 * @ordered
	 */
	protected String encodingType = ENCODING_TYPE_EDEFAULT;

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
	protected SensorThingLocationServiceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExtendedPackage.Literals.SENSOR_THING_LOCATION_SERVICE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getEncodingType() {
		return encodingType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEncodingType(String newEncodingType) {
		String oldEncodingType = encodingType;
		encodingType = newEncodingType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ENCODING_TYPE, oldEncodingType, encodingType));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ID, oldId, id));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__PROPERTIES, oldProperties, properties));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ENCODING_TYPE:
				return getEncodingType();
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ID:
				return getId();
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__PROPERTIES:
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
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ENCODING_TYPE:
				setEncodingType((String)newValue);
				return;
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ID:
				setId((String)newValue);
				return;
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__PROPERTIES:
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
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ENCODING_TYPE:
				setEncodingType(ENCODING_TYPE_EDEFAULT);
				return;
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ID:
				setId(ID_EDEFAULT);
				return;
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__PROPERTIES:
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
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ENCODING_TYPE:
				return ENCODING_TYPE_EDEFAULT == null ? encodingType != null : !ENCODING_TYPE_EDEFAULT.equals(encodingType);
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__PROPERTIES:
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
		result.append(" (encodingType: ");
		result.append(encodingType);
		result.append(", id: ");
		result.append(id);
		result.append(", properties: ");
		result.append(properties);
		result.append(')');
		return result.toString();
	}

} //SensorThingLocationServiceImpl
