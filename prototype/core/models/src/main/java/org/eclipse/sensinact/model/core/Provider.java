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
package org.eclipse.sensinact.model.core;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Provider</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.model.core.Provider#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.Provider#getAdmin <em>Admin</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.Provider#getLinkedProviders <em>Linked Providers</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getProvider()
 * @model
 * @generated
 */
@ProviderType
public interface Provider extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getProvider_Id()
	 * @model id="true" required="true"
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.model.core.Provider#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Admin</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Admin</em>' containment reference.
	 * @see #setAdmin(Admin)
	 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getProvider_Admin()
	 * @model containment="true"
	 * @generated
	 */
	Admin getAdmin();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.model.core.Provider#getAdmin <em>Admin</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Admin</em>' containment reference.
	 * @see #getAdmin()
	 * @generated
	 */
	void setAdmin(Admin value);

	/**
	 * Returns the value of the '<em><b>Linked Providers</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.sensinact.model.core.Provider}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Linked Providers</em>' reference list.
	 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getProvider_LinkedProviders()
	 * @model
	 * @generated
	 */
	EList<Provider> getLinkedProviders();

} // Provider
