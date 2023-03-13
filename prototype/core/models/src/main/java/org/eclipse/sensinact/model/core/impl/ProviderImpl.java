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

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.sensinact.model.core.Admin;
import org.eclipse.sensinact.model.core.Provider;
import org.eclipse.sensinact.model.core.SensiNactPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Provider</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.model.core.impl.ProviderImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.impl.ProviderImpl#getAdmin <em>Admin</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.impl.ProviderImpl#getLinkedProviders <em>Linked Providers</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ProviderImpl extends MinimalEObjectImpl.Container.Dynamic.Permissive implements Provider {
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
	 * This is true if the Id attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean idESet;

	/**
	 * The cached value of the '{@link #getAdmin() <em>Admin</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAdmin()
	 * @generated
	 * @ordered
	 */
	protected Admin admin;

	/**
	 * This is true if the Admin containment reference has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean adminESet;

	/**
	 * The cached value of the '{@link #getLinkedProviders() <em>Linked Providers</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLinkedProviders()
	 * @generated
	 * @ordered
	 */
	protected EList<Provider> linkedProviders;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ProviderImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SensiNactPackage.Literals.PROVIDER;
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
		boolean oldIdESet = idESet;
		idESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SensiNactPackage.PROVIDER__ID, oldId, id, !oldIdESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetId() {
		String oldId = id;
		boolean oldIdESet = idESet;
		id = ID_EDEFAULT;
		idESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, SensiNactPackage.PROVIDER__ID, oldId, ID_EDEFAULT, oldIdESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetId() {
		return idESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Admin getAdmin() {
		return admin;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetAdmin(Admin newAdmin, NotificationChain msgs) {
		Admin oldAdmin = admin;
		admin = newAdmin;
		boolean oldAdminESet = adminESet;
		adminESet = true;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SensiNactPackage.PROVIDER__ADMIN, oldAdmin, newAdmin, !oldAdminESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAdmin(Admin newAdmin) {
		if (newAdmin != admin) {
			NotificationChain msgs = null;
			if (admin != null)
				msgs = ((InternalEObject)admin).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - SensiNactPackage.PROVIDER__ADMIN, null, msgs);
			if (newAdmin != null)
				msgs = ((InternalEObject)newAdmin).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - SensiNactPackage.PROVIDER__ADMIN, null, msgs);
			msgs = basicSetAdmin(newAdmin, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldAdminESet = adminESet;
			adminESet = true;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.SET, SensiNactPackage.PROVIDER__ADMIN, newAdmin, newAdmin, !oldAdminESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicUnsetAdmin(NotificationChain msgs) {
		Admin oldAdmin = admin;
		admin = null;
		boolean oldAdminESet = adminESet;
		adminESet = false;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.UNSET, SensiNactPackage.PROVIDER__ADMIN, oldAdmin, null, oldAdminESet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetAdmin() {
		if (admin != null) {
			NotificationChain msgs = null;
			msgs = ((InternalEObject)admin).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - SensiNactPackage.PROVIDER__ADMIN, null, msgs);
			msgs = basicUnsetAdmin(msgs);
			if (msgs != null) msgs.dispatch();
		}
		else {
			boolean oldAdminESet = adminESet;
			adminESet = false;
			if (eNotificationRequired())
				eNotify(new ENotificationImpl(this, Notification.UNSET, SensiNactPackage.PROVIDER__ADMIN, null, null, oldAdminESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetAdmin() {
		return adminESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Provider> getLinkedProviders() {
		if (linkedProviders == null) {
			linkedProviders = new EObjectResolvingEList.Unsettable<Provider>(Provider.class, this, SensiNactPackage.PROVIDER__LINKED_PROVIDERS);
		}
		return linkedProviders;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetLinkedProviders() {
		if (linkedProviders != null) ((InternalEList.Unsettable<?>)linkedProviders).unset();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetLinkedProviders() {
		return linkedProviders != null && ((InternalEList.Unsettable<?>)linkedProviders).isSet();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SensiNactPackage.PROVIDER__ADMIN:
				return basicUnsetAdmin(msgs);
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
			case SensiNactPackage.PROVIDER__ID:
				return getId();
			case SensiNactPackage.PROVIDER__ADMIN:
				return getAdmin();
			case SensiNactPackage.PROVIDER__LINKED_PROVIDERS:
				return getLinkedProviders();
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
			case SensiNactPackage.PROVIDER__ID:
				setId((String)newValue);
				return;
			case SensiNactPackage.PROVIDER__ADMIN:
				setAdmin((Admin)newValue);
				return;
			case SensiNactPackage.PROVIDER__LINKED_PROVIDERS:
				getLinkedProviders().clear();
				getLinkedProviders().addAll((Collection<? extends Provider>)newValue);
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
			case SensiNactPackage.PROVIDER__ID:
				unsetId();
				return;
			case SensiNactPackage.PROVIDER__ADMIN:
				unsetAdmin();
				return;
			case SensiNactPackage.PROVIDER__LINKED_PROVIDERS:
				unsetLinkedProviders();
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
			case SensiNactPackage.PROVIDER__ID:
				return isSetId();
			case SensiNactPackage.PROVIDER__ADMIN:
				return isSetAdmin();
			case SensiNactPackage.PROVIDER__LINKED_PROVIDERS:
				return isSetLinkedProviders();
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
		if (idESet) result.append(id); else result.append("<unset>");
		result.append(')');
		return result.toString();
	}

} //ProviderImpl
