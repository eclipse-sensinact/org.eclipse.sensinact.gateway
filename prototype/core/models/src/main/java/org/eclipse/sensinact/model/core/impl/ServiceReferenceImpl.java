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
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EReferenceImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.sensinact.model.core.FeatureCustomMetadata;
import org.eclipse.sensinact.model.core.Metadata;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.model.core.ServiceReference;
import org.eclipse.sensinact.model.core.Timestamped;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Service Reference</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.model.core.impl.ServiceReferenceImpl#getTimestamp <em>Timestamp</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.impl.ServiceReferenceImpl#getExtra <em>Extra</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.impl.ServiceReferenceImpl#isLocked <em>Locked</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.impl.ServiceReferenceImpl#getOriginalName <em>Original Name</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ServiceReferenceImpl extends EReferenceImpl implements ServiceReference {
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ServiceReferenceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SensiNactPackage.Literals.SERVICE_REFERENCE;
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
			eNotify(new ENotificationImpl(this, Notification.SET, SensiNactPackage.SERVICE_REFERENCE__TIMESTAMP, oldTimestamp, timestamp));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<FeatureCustomMetadata> getExtra() {
		if (extra == null) {
			extra = new EObjectContainmentEList<FeatureCustomMetadata>(FeatureCustomMetadata.class, this, SensiNactPackage.SERVICE_REFERENCE__EXTRA);
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
			eNotify(new ENotificationImpl(this, Notification.SET, SensiNactPackage.SERVICE_REFERENCE__LOCKED, oldLocked, locked));
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
			eNotify(new ENotificationImpl(this, Notification.SET, SensiNactPackage.SERVICE_REFERENCE__ORIGINAL_NAME, oldOriginalName, originalName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SensiNactPackage.SERVICE_REFERENCE__EXTRA:
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
			case SensiNactPackage.SERVICE_REFERENCE__TIMESTAMP:
				return getTimestamp();
			case SensiNactPackage.SERVICE_REFERENCE__EXTRA:
				return getExtra();
			case SensiNactPackage.SERVICE_REFERENCE__LOCKED:
				return isLocked();
			case SensiNactPackage.SERVICE_REFERENCE__ORIGINAL_NAME:
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
			case SensiNactPackage.SERVICE_REFERENCE__TIMESTAMP:
				setTimestamp((Instant)newValue);
				return;
			case SensiNactPackage.SERVICE_REFERENCE__EXTRA:
				getExtra().clear();
				getExtra().addAll((Collection<? extends FeatureCustomMetadata>)newValue);
				return;
			case SensiNactPackage.SERVICE_REFERENCE__LOCKED:
				setLocked((Boolean)newValue);
				return;
			case SensiNactPackage.SERVICE_REFERENCE__ORIGINAL_NAME:
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
			case SensiNactPackage.SERVICE_REFERENCE__TIMESTAMP:
				setTimestamp(TIMESTAMP_EDEFAULT);
				return;
			case SensiNactPackage.SERVICE_REFERENCE__EXTRA:
				getExtra().clear();
				return;
			case SensiNactPackage.SERVICE_REFERENCE__LOCKED:
				setLocked(LOCKED_EDEFAULT);
				return;
			case SensiNactPackage.SERVICE_REFERENCE__ORIGINAL_NAME:
				setOriginalName(ORIGINAL_NAME_EDEFAULT);
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
			case SensiNactPackage.SERVICE_REFERENCE__TIMESTAMP:
				return TIMESTAMP_EDEFAULT == null ? timestamp != null : !TIMESTAMP_EDEFAULT.equals(timestamp);
			case SensiNactPackage.SERVICE_REFERENCE__EXTRA:
				return extra != null && !extra.isEmpty();
			case SensiNactPackage.SERVICE_REFERENCE__LOCKED:
				return locked != LOCKED_EDEFAULT;
			case SensiNactPackage.SERVICE_REFERENCE__ORIGINAL_NAME:
				return ORIGINAL_NAME_EDEFAULT == null ? originalName != null : !ORIGINAL_NAME_EDEFAULT.equals(originalName);
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
				case SensiNactPackage.SERVICE_REFERENCE__TIMESTAMP: return SensiNactPackage.TIMESTAMPED__TIMESTAMP;
				default: return -1;
			}
		}
		if (baseClass == Metadata.class) {
			switch (derivedFeatureID) {
				case SensiNactPackage.SERVICE_REFERENCE__EXTRA: return SensiNactPackage.METADATA__EXTRA;
				case SensiNactPackage.SERVICE_REFERENCE__LOCKED: return SensiNactPackage.METADATA__LOCKED;
				case SensiNactPackage.SERVICE_REFERENCE__ORIGINAL_NAME: return SensiNactPackage.METADATA__ORIGINAL_NAME;
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
				case SensiNactPackage.TIMESTAMPED__TIMESTAMP: return SensiNactPackage.SERVICE_REFERENCE__TIMESTAMP;
				default: return -1;
			}
		}
		if (baseClass == Metadata.class) {
			switch (baseFeatureID) {
				case SensiNactPackage.METADATA__EXTRA: return SensiNactPackage.SERVICE_REFERENCE__EXTRA;
				case SensiNactPackage.METADATA__LOCKED: return SensiNactPackage.SERVICE_REFERENCE__LOCKED;
				case SensiNactPackage.METADATA__ORIGINAL_NAME: return SensiNactPackage.SERVICE_REFERENCE__ORIGINAL_NAME;
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
		result.append(')');
		return result.toString();
	}

} //ServiceReferenceImpl
