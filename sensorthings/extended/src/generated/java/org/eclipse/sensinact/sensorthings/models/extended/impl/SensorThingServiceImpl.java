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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.sensinact.model.core.provider.impl.ServiceImpl;

import org.eclipse.sensinact.sensorthings.models.extended.DataStreamService;
import org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage;
import org.eclipse.sensinact.sensorthings.models.extended.SensorThingService;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sensor Thing Service</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingServiceImpl#getDataStreams <em>Data Streams</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingServiceImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingServiceImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingServiceImpl#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingServiceImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.SensorThingServiceImpl#getLocationIds <em>Location Ids</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SensorThingServiceImpl extends ServiceImpl implements SensorThingService {
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
	 * The default value of the '{@link #getProperties() <em>Properties</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProperties()
	 * @generated
	 * @ordered
	 */
	protected static final Object PROPERTIES_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getProperties() <em>Properties</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProperties()
	 * @generated
	 * @ordered
	 */
	protected Object properties = PROPERTIES_EDEFAULT;

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
	 * The default value of the '{@link #getLocationIds() <em>Location Ids</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocationIds()
	 * @generated
	 * @ordered
	 */
	protected static final Object LOCATION_IDS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLocationIds() <em>Location Ids</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocationIds()
	 * @generated
	 * @ordered
	 */
	protected Object locationIds = LOCATION_IDS_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SensorThingServiceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExtendedPackage.Literals.SENSOR_THING_SERVICE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<DataStreamService> getDataStreams() {
		if (dataStreams == null) {
			dataStreams = new EObjectContainmentEList<DataStreamService>(DataStreamService.class, this, ExtendedPackage.SENSOR_THING_SERVICE__DATA_STREAMS);
		}
		return dataStreams;
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
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_THING_SERVICE__NAME, oldName, name));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_THING_SERVICE__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getProperties() {
		return properties;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setProperties(Object newProperties) {
		Object oldProperties = properties;
		properties = newProperties;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_THING_SERVICE__PROPERTIES, oldProperties, properties));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_THING_SERVICE__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getLocationIds() {
		return locationIds;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLocationIds(Object newLocationIds) {
		Object oldLocationIds = locationIds;
		locationIds = newLocationIds;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.SENSOR_THING_SERVICE__LOCATION_IDS, oldLocationIds, locationIds));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ExtendedPackage.SENSOR_THING_SERVICE__DATA_STREAMS:
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
			case ExtendedPackage.SENSOR_THING_SERVICE__DATA_STREAMS:
				return getDataStreams();
			case ExtendedPackage.SENSOR_THING_SERVICE__NAME:
				return getName();
			case ExtendedPackage.SENSOR_THING_SERVICE__DESCRIPTION:
				return getDescription();
			case ExtendedPackage.SENSOR_THING_SERVICE__PROPERTIES:
				return getProperties();
			case ExtendedPackage.SENSOR_THING_SERVICE__ID:
				return getId();
			case ExtendedPackage.SENSOR_THING_SERVICE__LOCATION_IDS:
				return getLocationIds();
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
			case ExtendedPackage.SENSOR_THING_SERVICE__DATA_STREAMS:
				getDataStreams().clear();
				getDataStreams().addAll((Collection<? extends DataStreamService>)newValue);
				return;
			case ExtendedPackage.SENSOR_THING_SERVICE__NAME:
				setName((String)newValue);
				return;
			case ExtendedPackage.SENSOR_THING_SERVICE__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case ExtendedPackage.SENSOR_THING_SERVICE__PROPERTIES:
				setProperties(newValue);
				return;
			case ExtendedPackage.SENSOR_THING_SERVICE__ID:
				setId((String)newValue);
				return;
			case ExtendedPackage.SENSOR_THING_SERVICE__LOCATION_IDS:
				setLocationIds(newValue);
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
			case ExtendedPackage.SENSOR_THING_SERVICE__DATA_STREAMS:
				getDataStreams().clear();
				return;
			case ExtendedPackage.SENSOR_THING_SERVICE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ExtendedPackage.SENSOR_THING_SERVICE__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case ExtendedPackage.SENSOR_THING_SERVICE__PROPERTIES:
				setProperties(PROPERTIES_EDEFAULT);
				return;
			case ExtendedPackage.SENSOR_THING_SERVICE__ID:
				setId(ID_EDEFAULT);
				return;
			case ExtendedPackage.SENSOR_THING_SERVICE__LOCATION_IDS:
				setLocationIds(LOCATION_IDS_EDEFAULT);
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
			case ExtendedPackage.SENSOR_THING_SERVICE__DATA_STREAMS:
				return dataStreams != null && !dataStreams.isEmpty();
			case ExtendedPackage.SENSOR_THING_SERVICE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ExtendedPackage.SENSOR_THING_SERVICE__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case ExtendedPackage.SENSOR_THING_SERVICE__PROPERTIES:
				return PROPERTIES_EDEFAULT == null ? properties != null : !PROPERTIES_EDEFAULT.equals(properties);
			case ExtendedPackage.SENSOR_THING_SERVICE__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case ExtendedPackage.SENSOR_THING_SERVICE__LOCATION_IDS:
				return LOCATION_IDS_EDEFAULT == null ? locationIds != null : !LOCATION_IDS_EDEFAULT.equals(locationIds);
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
		result.append(", properties: ");
		result.append(properties);
		result.append(", id: ");
		result.append(id);
		result.append(", locationIds: ");
		result.append(locationIds);
		result.append(')');
		return result.toString();
	}

} //SensorThingServiceImpl
