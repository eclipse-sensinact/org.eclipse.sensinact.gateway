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

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Metadata</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.model.core.Metadata#getExtra <em>Extra</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.Metadata#isLocked <em>Locked</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.Metadata#getOriginalName <em>Original Name</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getMetadata()
 * @model abstract="true"
 * @generated
 */
@ProviderType
public interface Metadata extends Timestamped {
	/**
	 * Returns the value of the '<em><b>Extra</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.sensinact.model.core.FeatureCustomMetadata}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Extra</em>' containment reference list.
	 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getMetadata_Extra()
	 * @model containment="true"
	 * @generated
	 */
	EList<FeatureCustomMetadata> getExtra();

	/**
	 * Returns the value of the '<em><b>Locked</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Locked</em>' attribute.
	 * @see #isSetLocked()
	 * @see #unsetLocked()
	 * @see #setLocked(boolean)
	 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getMetadata_Locked()
	 * @model default="false" unsettable="true"
	 * @generated
	 */
	boolean isLocked();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.model.core.Metadata#isLocked <em>Locked</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Locked</em>' attribute.
	 * @see #isSetLocked()
	 * @see #unsetLocked()
	 * @see #isLocked()
	 * @generated
	 */
	void setLocked(boolean value);

	/**
	 * Unsets the value of the '{@link org.eclipse.sensinact.model.core.Metadata#isLocked <em>Locked</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetLocked()
	 * @see #isLocked()
	 * @see #setLocked(boolean)
	 * @generated
	 */
	void unsetLocked();

	/**
	 * Returns whether the value of the '{@link org.eclipse.sensinact.model.core.Metadata#isLocked <em>Locked</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Locked</em>' attribute is set.
	 * @see #unsetLocked()
	 * @see #isLocked()
	 * @see #setLocked(boolean)
	 * @generated
	 */
	boolean isSetLocked();

	/**
	 * Returns the value of the '<em><b>Original Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Original Name</em>' attribute.
	 * @see #isSetOriginalName()
	 * @see #unsetOriginalName()
	 * @see #setOriginalName(String)
	 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getMetadata_OriginalName()
	 * @model unsettable="true" required="true"
	 * @generated
	 */
	String getOriginalName();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.model.core.Metadata#getOriginalName <em>Original Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Original Name</em>' attribute.
	 * @see #isSetOriginalName()
	 * @see #unsetOriginalName()
	 * @see #getOriginalName()
	 * @generated
	 */
	void setOriginalName(String value);

	/**
	 * Unsets the value of the '{@link org.eclipse.sensinact.model.core.Metadata#getOriginalName <em>Original Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetOriginalName()
	 * @see #getOriginalName()
	 * @see #setOriginalName(String)
	 * @generated
	 */
	void unsetOriginalName();

	/**
	 * Returns whether the value of the '{@link org.eclipse.sensinact.model.core.Metadata#getOriginalName <em>Original Name</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Original Name</em>' attribute is set.
	 * @see #unsetOriginalName()
	 * @see #getOriginalName()
	 * @see #setOriginalName(String)
	 * @generated
	 */
	boolean isSetOriginalName();

} // Metadata
