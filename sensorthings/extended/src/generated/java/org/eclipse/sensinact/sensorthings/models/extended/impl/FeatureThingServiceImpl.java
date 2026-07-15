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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import org.eclipse.sensinact.model.core.provider.impl.ServiceImpl;

import org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage;
import org.eclipse.sensinact.sensorthings.models.extended.FeatureThingService;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Feature Thing Service</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.FeatureThingServiceImpl#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.FeatureThingServiceImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.FeatureThingServiceImpl#getEncodingType <em>Encoding Type</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.FeatureThingServiceImpl#isHasObs <em>Has Obs</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.FeatureThingServiceImpl#getDatastreamIds <em>Datastream Ids</em>}</li>
 * </ul>
 *
 * @generated
 */
public class FeatureThingServiceImpl extends ServiceImpl implements FeatureThingService {
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
	 * The default value of the '{@link #isHasObs() <em>Has Obs</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHasObs()
	 * @generated
	 * @ordered
	 */
	protected static final boolean HAS_OBS_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isHasObs() <em>Has Obs</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHasObs()
	 * @generated
	 * @ordered
	 */
	protected boolean hasObs = HAS_OBS_EDEFAULT;

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
	protected FeatureThingServiceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExtendedPackage.Literals.FEATURE_THING_SERVICE;
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
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.FEATURE_THING_SERVICE__PROPERTIES, oldProperties, properties));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.FEATURE_THING_SERVICE__ID, oldId, id));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.FEATURE_THING_SERVICE__ENCODING_TYPE, oldEncodingType, encodingType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isHasObs() {
		return hasObs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setHasObs(boolean newHasObs) {
		boolean oldHasObs = hasObs;
		hasObs = newHasObs;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.FEATURE_THING_SERVICE__HAS_OBS, oldHasObs, hasObs));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getDatastreamIds() {
		if (datastreamIds == null) {
			datastreamIds = new EDataTypeUniqueEList<String>(String.class, this, ExtendedPackage.FEATURE_THING_SERVICE__DATASTREAM_IDS);
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
			case ExtendedPackage.FEATURE_THING_SERVICE__PROPERTIES:
				return getProperties();
			case ExtendedPackage.FEATURE_THING_SERVICE__ID:
				return getId();
			case ExtendedPackage.FEATURE_THING_SERVICE__ENCODING_TYPE:
				return getEncodingType();
			case ExtendedPackage.FEATURE_THING_SERVICE__HAS_OBS:
				return isHasObs();
			case ExtendedPackage.FEATURE_THING_SERVICE__DATASTREAM_IDS:
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
			case ExtendedPackage.FEATURE_THING_SERVICE__PROPERTIES:
				setProperties(newValue);
				return;
			case ExtendedPackage.FEATURE_THING_SERVICE__ID:
				setId((String)newValue);
				return;
			case ExtendedPackage.FEATURE_THING_SERVICE__ENCODING_TYPE:
				setEncodingType((String)newValue);
				return;
			case ExtendedPackage.FEATURE_THING_SERVICE__HAS_OBS:
				setHasObs((Boolean)newValue);
				return;
			case ExtendedPackage.FEATURE_THING_SERVICE__DATASTREAM_IDS:
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
			case ExtendedPackage.FEATURE_THING_SERVICE__PROPERTIES:
				setProperties(PROPERTIES_EDEFAULT);
				return;
			case ExtendedPackage.FEATURE_THING_SERVICE__ID:
				setId(ID_EDEFAULT);
				return;
			case ExtendedPackage.FEATURE_THING_SERVICE__ENCODING_TYPE:
				setEncodingType(ENCODING_TYPE_EDEFAULT);
				return;
			case ExtendedPackage.FEATURE_THING_SERVICE__HAS_OBS:
				setHasObs(HAS_OBS_EDEFAULT);
				return;
			case ExtendedPackage.FEATURE_THING_SERVICE__DATASTREAM_IDS:
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
			case ExtendedPackage.FEATURE_THING_SERVICE__PROPERTIES:
				return PROPERTIES_EDEFAULT == null ? properties != null : !PROPERTIES_EDEFAULT.equals(properties);
			case ExtendedPackage.FEATURE_THING_SERVICE__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case ExtendedPackage.FEATURE_THING_SERVICE__ENCODING_TYPE:
				return ENCODING_TYPE_EDEFAULT == null ? encodingType != null : !ENCODING_TYPE_EDEFAULT.equals(encodingType);
			case ExtendedPackage.FEATURE_THING_SERVICE__HAS_OBS:
				return hasObs != HAS_OBS_EDEFAULT;
			case ExtendedPackage.FEATURE_THING_SERVICE__DATASTREAM_IDS:
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
		result.append(" (properties: ");
		result.append(properties);
		result.append(", id: ");
		result.append(id);
		result.append(", encodingType: ");
		result.append(encodingType);
		result.append(", hasObs: ");
		result.append(hasObs);
		result.append(", datastreamIds: ");
		result.append(datastreamIds);
		result.append(')');
		return result.toString();
	}

} //FeatureThingServiceImpl
