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
import org.eclipse.sensinact.sensorthings.models.extended.ObservedPropertyService;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Observed Property Service</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.ObservedPropertyServiceImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.ObservedPropertyServiceImpl#getTimestamp <em>Timestamp</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.ObservedPropertyServiceImpl#getObservedPropertyDefinition <em>Observed Property Definition</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.ObservedPropertyServiceImpl#getObservedPropertyProperties <em>Observed Property Properties</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.ObservedPropertyServiceImpl#getDatastreamIds <em>Datastream Ids</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ObservedPropertyServiceImpl extends ServiceImpl implements ObservedPropertyService {
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
	 * The default value of the '{@link #getObservedPropertyDefinition() <em>Observed Property Definition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservedPropertyDefinition()
	 * @generated
	 * @ordered
	 */
	protected static final String OBSERVED_PROPERTY_DEFINITION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getObservedPropertyDefinition() <em>Observed Property Definition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservedPropertyDefinition()
	 * @generated
	 * @ordered
	 */
	protected String observedPropertyDefinition = OBSERVED_PROPERTY_DEFINITION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getObservedPropertyProperties() <em>Observed Property Properties</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservedPropertyProperties()
	 * @generated
	 * @ordered
	 */
	protected Map<?, ?> observedPropertyProperties;

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
	protected ObservedPropertyServiceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExtendedPackage.Literals.OBSERVED_PROPERTY_SERVICE;
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
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.OBSERVED_PROPERTY_SERVICE__ID, oldId, id));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.OBSERVED_PROPERTY_SERVICE__TIMESTAMP, oldTimestamp, timestamp));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getObservedPropertyDefinition() {
		return observedPropertyDefinition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setObservedPropertyDefinition(String newObservedPropertyDefinition) {
		String oldObservedPropertyDefinition = observedPropertyDefinition;
		observedPropertyDefinition = newObservedPropertyDefinition;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.OBSERVED_PROPERTY_SERVICE__OBSERVED_PROPERTY_DEFINITION, oldObservedPropertyDefinition, observedPropertyDefinition));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Map<?, ?> getObservedPropertyProperties() {
		return observedPropertyProperties;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setObservedPropertyProperties(Map<?, ?> newObservedPropertyProperties) {
		Map<?, ?> oldObservedPropertyProperties = observedPropertyProperties;
		observedPropertyProperties = newObservedPropertyProperties;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.OBSERVED_PROPERTY_SERVICE__OBSERVED_PROPERTY_PROPERTIES, oldObservedPropertyProperties, observedPropertyProperties));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getDatastreamIds() {
		if (datastreamIds == null) {
			datastreamIds = new EDataTypeUniqueEList<String>(String.class, this, ExtendedPackage.OBSERVED_PROPERTY_SERVICE__DATASTREAM_IDS);
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
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__ID:
				return getId();
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__TIMESTAMP:
				return getTimestamp();
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__OBSERVED_PROPERTY_DEFINITION:
				return getObservedPropertyDefinition();
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__OBSERVED_PROPERTY_PROPERTIES:
				return getObservedPropertyProperties();
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__DATASTREAM_IDS:
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
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__ID:
				setId((String)newValue);
				return;
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__TIMESTAMP:
				setTimestamp((Instant)newValue);
				return;
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__OBSERVED_PROPERTY_DEFINITION:
				setObservedPropertyDefinition((String)newValue);
				return;
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__OBSERVED_PROPERTY_PROPERTIES:
				setObservedPropertyProperties((Map<?, ?>)newValue);
				return;
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__DATASTREAM_IDS:
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
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__ID:
				setId(ID_EDEFAULT);
				return;
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__TIMESTAMP:
				setTimestamp(TIMESTAMP_EDEFAULT);
				return;
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__OBSERVED_PROPERTY_DEFINITION:
				setObservedPropertyDefinition(OBSERVED_PROPERTY_DEFINITION_EDEFAULT);
				return;
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__OBSERVED_PROPERTY_PROPERTIES:
				setObservedPropertyProperties((Map<?, ?>)null);
				return;
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__DATASTREAM_IDS:
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
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__TIMESTAMP:
				return TIMESTAMP_EDEFAULT == null ? timestamp != null : !TIMESTAMP_EDEFAULT.equals(timestamp);
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__OBSERVED_PROPERTY_DEFINITION:
				return OBSERVED_PROPERTY_DEFINITION_EDEFAULT == null ? observedPropertyDefinition != null : !OBSERVED_PROPERTY_DEFINITION_EDEFAULT.equals(observedPropertyDefinition);
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__OBSERVED_PROPERTY_PROPERTIES:
				return observedPropertyProperties != null;
			case ExtendedPackage.OBSERVED_PROPERTY_SERVICE__DATASTREAM_IDS:
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
		result.append(", observedPropertyDefinition: ");
		result.append(observedPropertyDefinition);
		result.append(", observedPropertyProperties: ");
		result.append(observedPropertyProperties);
		result.append(", datastreamIds: ");
		result.append(datastreamIds);
		result.append(')');
		return result.toString();
	}

} //ObservedPropertyServiceImpl
