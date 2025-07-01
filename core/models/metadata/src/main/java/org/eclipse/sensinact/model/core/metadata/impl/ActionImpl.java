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
package org.eclipse.sensinact.model.core.metadata.impl;

import java.time.Instant;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EOperationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.sensinact.model.core.metadata.Action;
import org.eclipse.sensinact.model.core.metadata.MetadataPackage;
import org.eclipse.sensinact.model.core.metadata.NexusMetadata;

import org.eclipse.sensinact.model.core.provider.FeatureCustomMetadata;
import org.eclipse.sensinact.model.core.provider.Metadata;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Action</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.model.core.metadata.impl.ActionImpl#getExtra <em>Extra</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.metadata.impl.ActionImpl#getTimestamp <em>Timestamp</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.metadata.impl.ActionImpl#isLocked <em>Locked</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.metadata.impl.ActionImpl#getOriginalName <em>Original Name</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ActionImpl extends EOperationImpl implements Action {
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
	 * This is true if the Locked attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean lockedESet;

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
	 * This is true if the Original Name attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean originalNameESet;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ActionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MetadataPackage.Literals.ACTION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<FeatureCustomMetadata> getExtra() {
		if (extra == null) {
			extra = new EObjectContainmentEList<FeatureCustomMetadata>(FeatureCustomMetadata.class, this, MetadataPackage.ACTION__EXTRA);
		}
		return extra;
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
			eNotify(new ENotificationImpl(this, Notification.SET, MetadataPackage.ACTION__TIMESTAMP, oldTimestamp, timestamp));
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
		boolean oldLockedESet = lockedESet;
		lockedESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MetadataPackage.ACTION__LOCKED, oldLocked, locked, !oldLockedESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetLocked() {
		boolean oldLocked = locked;
		boolean oldLockedESet = lockedESet;
		locked = LOCKED_EDEFAULT;
		lockedESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, MetadataPackage.ACTION__LOCKED, oldLocked, LOCKED_EDEFAULT, oldLockedESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetLocked() {
		return lockedESet;
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
		boolean oldOriginalNameESet = originalNameESet;
		originalNameESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MetadataPackage.ACTION__ORIGINAL_NAME, oldOriginalName, originalName, !oldOriginalNameESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetOriginalName() {
		String oldOriginalName = originalName;
		boolean oldOriginalNameESet = originalNameESet;
		originalName = ORIGINAL_NAME_EDEFAULT;
		originalNameESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, MetadataPackage.ACTION__ORIGINAL_NAME, oldOriginalName, ORIGINAL_NAME_EDEFAULT, oldOriginalNameESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetOriginalName() {
		return originalNameESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case MetadataPackage.ACTION__EXTRA:
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
			case MetadataPackage.ACTION__EXTRA:
				return getExtra();
			case MetadataPackage.ACTION__TIMESTAMP:
				return getTimestamp();
			case MetadataPackage.ACTION__LOCKED:
				return isLocked();
			case MetadataPackage.ACTION__ORIGINAL_NAME:
				return getOriginalName();
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
			case MetadataPackage.ACTION__EXTRA:
				getExtra().clear();
				getExtra().addAll((Collection<? extends FeatureCustomMetadata>)newValue);
				return;
			case MetadataPackage.ACTION__TIMESTAMP:
				setTimestamp((Instant)newValue);
				return;
			case MetadataPackage.ACTION__LOCKED:
				setLocked((Boolean)newValue);
				return;
			case MetadataPackage.ACTION__ORIGINAL_NAME:
				setOriginalName((String)newValue);
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
			case MetadataPackage.ACTION__EXTRA:
				getExtra().clear();
				return;
			case MetadataPackage.ACTION__TIMESTAMP:
				setTimestamp(TIMESTAMP_EDEFAULT);
				return;
			case MetadataPackage.ACTION__LOCKED:
				unsetLocked();
				return;
			case MetadataPackage.ACTION__ORIGINAL_NAME:
				unsetOriginalName();
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
			case MetadataPackage.ACTION__EXTRA:
				return extra != null && !extra.isEmpty();
			case MetadataPackage.ACTION__TIMESTAMP:
				return TIMESTAMP_EDEFAULT == null ? timestamp != null : !TIMESTAMP_EDEFAULT.equals(timestamp);
			case MetadataPackage.ACTION__LOCKED:
				return isSetLocked();
			case MetadataPackage.ACTION__ORIGINAL_NAME:
				return isSetOriginalName();
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
		if (baseClass == Metadata.class) {
			switch (derivedFeatureID) {
				case MetadataPackage.ACTION__EXTRA: return ProviderPackage.METADATA__EXTRA;
				case MetadataPackage.ACTION__TIMESTAMP: return ProviderPackage.METADATA__TIMESTAMP;
				default: return -1;
			}
		}
		if (baseClass == NexusMetadata.class) {
			switch (derivedFeatureID) {
				case MetadataPackage.ACTION__LOCKED: return MetadataPackage.NEXUS_METADATA__LOCKED;
				case MetadataPackage.ACTION__ORIGINAL_NAME: return MetadataPackage.NEXUS_METADATA__ORIGINAL_NAME;
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
		if (baseClass == Metadata.class) {
			switch (baseFeatureID) {
				case ProviderPackage.METADATA__EXTRA: return MetadataPackage.ACTION__EXTRA;
				case ProviderPackage.METADATA__TIMESTAMP: return MetadataPackage.ACTION__TIMESTAMP;
				default: return -1;
			}
		}
		if (baseClass == NexusMetadata.class) {
			switch (baseFeatureID) {
				case MetadataPackage.NEXUS_METADATA__LOCKED: return MetadataPackage.ACTION__LOCKED;
				case MetadataPackage.NEXUS_METADATA__ORIGINAL_NAME: return MetadataPackage.ACTION__ORIGINAL_NAME;
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
		if (lockedESet) result.append(locked); else result.append("<unset>");
		result.append(", originalName: ");
		if (originalNameESet) result.append(originalName); else result.append("<unset>");
		result.append(')');
		return result.toString();
	}

} //ActionImpl
