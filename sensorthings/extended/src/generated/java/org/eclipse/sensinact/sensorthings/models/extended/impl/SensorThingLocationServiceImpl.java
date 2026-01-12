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

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;

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
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingLocationServiceImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingLocationServiceImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingLocationServiceImpl#getEncodingType <em>Encoding Type</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingLocationServiceImpl#getLocation <em>Location</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingLocationServiceImpl#getId <em>Id</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SensorThingLocationServiceImpl extends ServiceImpl implements SensorThingLocationService {
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
	 * The default value of the '{@link #getLocation() <em>Location</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocation()
	 * @generated
	 * @ordered
	 */
	protected static final GeoJsonObject LOCATION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLocation() <em>Location</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocation()
	 * @generated
	 * @ordered
	 */
	protected GeoJsonObject location = LOCATION_EDEFAULT;

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
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__NAME, oldName, name));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__DESCRIPTION, oldDescription, description));
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
	public GeoJsonObject getLocation() {
		return location;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLocation(GeoJsonObject newLocation) {
		GeoJsonObject oldLocation = location;
		location = newLocation;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__LOCATION, oldLocation, location));
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
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__NAME:
				return getName();
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__DESCRIPTION:
				return getDescription();
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ENCODING_TYPE:
				return getEncodingType();
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__LOCATION:
				return getLocation();
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ID:
				return getId();
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
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__NAME:
				setName((String)newValue);
				return;
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ENCODING_TYPE:
				setEncodingType((String)newValue);
				return;
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__LOCATION:
				setLocation((GeoJsonObject)newValue);
				return;
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ID:
				setId((String)newValue);
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
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ENCODING_TYPE:
				setEncodingType(ENCODING_TYPE_EDEFAULT);
				return;
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__LOCATION:
				setLocation(LOCATION_EDEFAULT);
				return;
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ID:
				setId(ID_EDEFAULT);
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
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ENCODING_TYPE:
				return ENCODING_TYPE_EDEFAULT == null ? encodingType != null : !ENCODING_TYPE_EDEFAULT.equals(encodingType);
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__LOCATION:
				return LOCATION_EDEFAULT == null ? location != null : !LOCATION_EDEFAULT.equals(location);
			case ExtendedPackage.SENSOR_THING_LOCATION_SERVICE__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
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
		result.append(" (name: ");
		result.append(name);
		result.append(", description: ");
		result.append(description);
		result.append(", encodingType: ");
		result.append(encodingType);
		result.append(", location: ");
		result.append(location);
		result.append(", id: ");
		result.append(id);
		result.append(')');
		return result.toString();
	}

} //SensorThingLocationServiceImpl
