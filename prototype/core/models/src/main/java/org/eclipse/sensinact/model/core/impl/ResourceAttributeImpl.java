/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Data In Motion - initial API and implementation 
 */
package org.eclipse.sensinact.model.core.impl;

import java.time.Instant;
import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EAttributeImpl;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.sensinact.model.core.FeatureCustomMetadata;
import org.eclipse.sensinact.model.core.Metadata;
import org.eclipse.sensinact.model.core.ResourceAttribute;
import org.eclipse.sensinact.model.core.ResourceType;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.model.core.Timestamped;
import org.eclipse.sensinact.model.core.ValueType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Resource Attribute</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.model.core.impl.ResourceAttributeImpl#getTimestamp <em>Timestamp</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.impl.ResourceAttributeImpl#getExtra <em>Extra</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.impl.ResourceAttributeImpl#isLocked <em>Locked</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.impl.ResourceAttributeImpl#getOriginalName <em>Original Name</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.impl.ResourceAttributeImpl#getResourceType <em>Resource Type</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.impl.ResourceAttributeImpl#getValueType <em>Value Type</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.impl.ResourceAttributeImpl#isExternalGet <em>External Get</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.impl.ResourceAttributeImpl#isExternalSet <em>External Set</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.impl.ResourceAttributeImpl#getStale <em>Stale</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ResourceAttributeImpl extends EAttributeImpl implements ResourceAttribute {
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
	 * The cached value of the '{@link #getExtra() <em>Extra</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExtra()
	 * @generated
	 * @ordered
	 */
	protected EList<FeatureCustomMetadata> extra;

	/**
	 * The default value of the '{@link #isLocked() <em>Locked</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isLocked()
	 * @generated
	 * @ordered
	 */
	protected static final boolean LOCKED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isLocked() <em>Locked</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isLocked()
	 * @generated
	 * @ordered
	 */
	protected boolean locked = LOCKED_EDEFAULT;

	/**
	 * The default value of the '{@link #getOriginalName() <em>Original Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOriginalName()
	 * @generated
	 * @ordered
	 */
	protected static final String ORIGINAL_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getOriginalName() <em>Original Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOriginalName()
	 * @generated
	 * @ordered
	 */
	protected String originalName = ORIGINAL_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getResourceType() <em>Resource Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getResourceType()
	 * @generated
	 * @ordered
	 */
	protected static final ResourceType RESOURCE_TYPE_EDEFAULT = ResourceType.SENSOR;

	/**
	 * The cached value of the '{@link #getResourceType() <em>Resource Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getResourceType()
	 * @generated
	 * @ordered
	 */
	protected ResourceType resourceType = RESOURCE_TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getValueType() <em>Value Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueType()
	 * @generated
	 * @ordered
	 */
	protected static final ValueType VALUE_TYPE_EDEFAULT = ValueType.MODIFIABLE;

	/**
	 * The cached value of the '{@link #getValueType() <em>Value Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueType()
	 * @generated
	 * @ordered
	 */
	protected ValueType valueType = VALUE_TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #isExternalGet() <em>External Get</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isExternalGet()
	 * @generated
	 * @ordered
	 */
	protected static final boolean EXTERNAL_GET_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isExternalGet() <em>External Get</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isExternalGet()
	 * @generated
	 * @ordered
	 */
	protected boolean externalGet = EXTERNAL_GET_EDEFAULT;

	/**
	 * The default value of the '{@link #isExternalSet() <em>External Set</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isExternalSet()
	 * @generated
	 * @ordered
	 */
	protected static final boolean EXTERNAL_SET_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isExternalSet() <em>External Set</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isExternalSet()
	 * @generated
	 * @ordered
	 */
	protected boolean externalSet = EXTERNAL_SET_EDEFAULT;

	/**
	 * The default value of the '{@link #getStale() <em>Stale</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStale()
	 * @generated
	 * @ordered
	 */
	protected static final int STALE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getStale() <em>Stale</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStale()
	 * @generated
	 * @ordered
	 */
	protected int stale = STALE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ResourceAttributeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SensiNactPackage.Literals.RESOURCE_ATTRIBUTE;
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
			eNotify(new ENotificationImpl(this, Notification.SET, SensiNactPackage.RESOURCE_ATTRIBUTE__TIMESTAMP, oldTimestamp, timestamp));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<FeatureCustomMetadata> getExtra() {
		if (extra == null) {
			extra = new EObjectContainmentEList<FeatureCustomMetadata>(FeatureCustomMetadata.class, this, SensiNactPackage.RESOURCE_ATTRIBUTE__EXTRA);
		}
		return extra;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isLocked() {
		return locked;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLocked(boolean newLocked) {
		boolean oldLocked = locked;
		locked = newLocked;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SensiNactPackage.RESOURCE_ATTRIBUTE__LOCKED, oldLocked, locked));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getOriginalName() {
		return originalName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOriginalName(String newOriginalName) {
		String oldOriginalName = originalName;
		originalName = newOriginalName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SensiNactPackage.RESOURCE_ATTRIBUTE__ORIGINAL_NAME, oldOriginalName, originalName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceType getResourceType() {
		return resourceType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setResourceType(ResourceType newResourceType) {
		ResourceType oldResourceType = resourceType;
		resourceType = newResourceType == null ? RESOURCE_TYPE_EDEFAULT : newResourceType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SensiNactPackage.RESOURCE_ATTRIBUTE__RESOURCE_TYPE, oldResourceType, resourceType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ValueType getValueType() {
		return valueType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setValueType(ValueType newValueType) {
		ValueType oldValueType = valueType;
		valueType = newValueType == null ? VALUE_TYPE_EDEFAULT : newValueType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SensiNactPackage.RESOURCE_ATTRIBUTE__VALUE_TYPE, oldValueType, valueType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isExternalGet() {
		return externalGet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setExternalGet(boolean newExternalGet) {
		boolean oldExternalGet = externalGet;
		externalGet = newExternalGet;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SensiNactPackage.RESOURCE_ATTRIBUTE__EXTERNAL_GET, oldExternalGet, externalGet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isExternalSet() {
		return externalSet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setExternalSet(boolean newExternalSet) {
		boolean oldExternalSet = externalSet;
		externalSet = newExternalSet;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SensiNactPackage.RESOURCE_ATTRIBUTE__EXTERNAL_SET, oldExternalSet, externalSet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getStale() {
		return stale;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStale(int newStale) {
		int oldStale = stale;
		stale = newStale;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SensiNactPackage.RESOURCE_ATTRIBUTE__STALE, oldStale, stale));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SensiNactPackage.RESOURCE_ATTRIBUTE__EXTRA:
				return ((InternalEList<?>)getExtra()).basicRemove(otherEnd, msgs);
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
			case SensiNactPackage.RESOURCE_ATTRIBUTE__TIMESTAMP:
				return getTimestamp();
			case SensiNactPackage.RESOURCE_ATTRIBUTE__EXTRA:
				return getExtra();
			case SensiNactPackage.RESOURCE_ATTRIBUTE__LOCKED:
				return isLocked();
			case SensiNactPackage.RESOURCE_ATTRIBUTE__ORIGINAL_NAME:
				return getOriginalName();
			case SensiNactPackage.RESOURCE_ATTRIBUTE__RESOURCE_TYPE:
				return getResourceType();
			case SensiNactPackage.RESOURCE_ATTRIBUTE__VALUE_TYPE:
				return getValueType();
			case SensiNactPackage.RESOURCE_ATTRIBUTE__EXTERNAL_GET:
				return isExternalGet();
			case SensiNactPackage.RESOURCE_ATTRIBUTE__EXTERNAL_SET:
				return isExternalSet();
			case SensiNactPackage.RESOURCE_ATTRIBUTE__STALE:
				return getStale();
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
			case SensiNactPackage.RESOURCE_ATTRIBUTE__TIMESTAMP:
				setTimestamp((Instant)newValue);
				return;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__EXTRA:
				getExtra().clear();
				getExtra().addAll((Collection<? extends FeatureCustomMetadata>)newValue);
				return;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__LOCKED:
				setLocked((Boolean)newValue);
				return;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__ORIGINAL_NAME:
				setOriginalName((String)newValue);
				return;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__RESOURCE_TYPE:
				setResourceType((ResourceType)newValue);
				return;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__VALUE_TYPE:
				setValueType((ValueType)newValue);
				return;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__EXTERNAL_GET:
				setExternalGet((Boolean)newValue);
				return;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__EXTERNAL_SET:
				setExternalSet((Boolean)newValue);
				return;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__STALE:
				setStale((Integer)newValue);
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
			case SensiNactPackage.RESOURCE_ATTRIBUTE__TIMESTAMP:
				setTimestamp(TIMESTAMP_EDEFAULT);
				return;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__EXTRA:
				getExtra().clear();
				return;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__LOCKED:
				setLocked(LOCKED_EDEFAULT);
				return;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__ORIGINAL_NAME:
				setOriginalName(ORIGINAL_NAME_EDEFAULT);
				return;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__RESOURCE_TYPE:
				setResourceType(RESOURCE_TYPE_EDEFAULT);
				return;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__VALUE_TYPE:
				setValueType(VALUE_TYPE_EDEFAULT);
				return;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__EXTERNAL_GET:
				setExternalGet(EXTERNAL_GET_EDEFAULT);
				return;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__EXTERNAL_SET:
				setExternalSet(EXTERNAL_SET_EDEFAULT);
				return;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__STALE:
				setStale(STALE_EDEFAULT);
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
			case SensiNactPackage.RESOURCE_ATTRIBUTE__TIMESTAMP:
				return TIMESTAMP_EDEFAULT == null ? timestamp != null : !TIMESTAMP_EDEFAULT.equals(timestamp);
			case SensiNactPackage.RESOURCE_ATTRIBUTE__EXTRA:
				return extra != null && !extra.isEmpty();
			case SensiNactPackage.RESOURCE_ATTRIBUTE__LOCKED:
				return locked != LOCKED_EDEFAULT;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__ORIGINAL_NAME:
				return ORIGINAL_NAME_EDEFAULT == null ? originalName != null : !ORIGINAL_NAME_EDEFAULT.equals(originalName);
			case SensiNactPackage.RESOURCE_ATTRIBUTE__RESOURCE_TYPE:
				return resourceType != RESOURCE_TYPE_EDEFAULT;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__VALUE_TYPE:
				return valueType != VALUE_TYPE_EDEFAULT;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__EXTERNAL_GET:
				return externalGet != EXTERNAL_GET_EDEFAULT;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__EXTERNAL_SET:
				return externalSet != EXTERNAL_SET_EDEFAULT;
			case SensiNactPackage.RESOURCE_ATTRIBUTE__STALE:
				return stale != STALE_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == Timestamped.class) {
			switch (derivedFeatureID) {
				case SensiNactPackage.RESOURCE_ATTRIBUTE__TIMESTAMP: return SensiNactPackage.TIMESTAMPED__TIMESTAMP;
				default: return -1;
			}
		}
		if (baseClass == Metadata.class) {
			switch (derivedFeatureID) {
				case SensiNactPackage.RESOURCE_ATTRIBUTE__EXTRA: return SensiNactPackage.METADATA__EXTRA;
				case SensiNactPackage.RESOURCE_ATTRIBUTE__LOCKED: return SensiNactPackage.METADATA__LOCKED;
				case SensiNactPackage.RESOURCE_ATTRIBUTE__ORIGINAL_NAME: return SensiNactPackage.METADATA__ORIGINAL_NAME;
				default: return -1;
			}
		}
		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		if (baseClass == Timestamped.class) {
			switch (baseFeatureID) {
				case SensiNactPackage.TIMESTAMPED__TIMESTAMP: return SensiNactPackage.RESOURCE_ATTRIBUTE__TIMESTAMP;
				default: return -1;
			}
		}
		if (baseClass == Metadata.class) {
			switch (baseFeatureID) {
				case SensiNactPackage.METADATA__EXTRA: return SensiNactPackage.RESOURCE_ATTRIBUTE__EXTRA;
				case SensiNactPackage.METADATA__LOCKED: return SensiNactPackage.RESOURCE_ATTRIBUTE__LOCKED;
				case SensiNactPackage.METADATA__ORIGINAL_NAME: return SensiNactPackage.RESOURCE_ATTRIBUTE__ORIGINAL_NAME;
				default: return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
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
		result.append(" (timestamp: ");
		result.append(timestamp);
		result.append(", locked: ");
		result.append(locked);
		result.append(", originalName: ");
		result.append(originalName);
		result.append(", resourceType: ");
		result.append(resourceType);
		result.append(", valueType: ");
		result.append(valueType);
		result.append(", externalGet: ");
		result.append(externalGet);
		result.append(", externalSet: ");
		result.append(externalSet);
		result.append(", stale: ");
		result.append(stale);
		result.append(')');
		return result.toString();
	}

} //ResourceAttributeImpl
