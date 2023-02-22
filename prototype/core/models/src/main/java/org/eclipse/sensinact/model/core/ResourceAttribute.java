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

import org.eclipse.emf.ecore.EAttribute;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Resource Attribute</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.model.core.ResourceAttribute#getResourceType <em>Resource Type</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.ResourceAttribute#getValueType <em>Value Type</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.ResourceAttribute#isExternalGet <em>External Get</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.ResourceAttribute#isExternalSet <em>External Set</em>}</li>
 *   <li>{@link org.eclipse.sensinact.model.core.ResourceAttribute#getStale <em>Stale</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getResourceAttribute()
 * @model
 * @generated
 */
@ProviderType
public interface ResourceAttribute extends EAttribute, Metadata {
	/**
	 * Returns the value of the '<em><b>Resource Type</b></em>' attribute.
	 * The default value is <code>"SENSOR"</code>.
	 * The literals are from the enumeration {@link org.eclipse.sensinact.model.core.ResourceType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Resource Type</em>' attribute.
	 * @see org.eclipse.sensinact.model.core.ResourceType
	 * @see #setResourceType(ResourceType)
	 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getResourceAttribute_ResourceType()
	 * @model default="SENSOR"
	 * @generated
	 */
	ResourceType getResourceType();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.model.core.ResourceAttribute#getResourceType <em>Resource Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Resource Type</em>' attribute.
	 * @see org.eclipse.sensinact.model.core.ResourceType
	 * @see #getResourceType()
	 * @generated
	 */
	void setResourceType(ResourceType value);

	/**
	 * Returns the value of the '<em><b>Value Type</b></em>' attribute.
	 * The default value is <code>"MODIFIABLE"</code>.
	 * The literals are from the enumeration {@link org.eclipse.sensinact.model.core.ValueType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value Type</em>' attribute.
	 * @see org.eclipse.sensinact.model.core.ValueType
	 * @see #setValueType(ValueType)
	 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getResourceAttribute_ValueType()
	 * @model default="MODIFIABLE"
	 * @generated
	 */
	ValueType getValueType();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.model.core.ResourceAttribute#getValueType <em>Value Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value Type</em>' attribute.
	 * @see org.eclipse.sensinact.model.core.ValueType
	 * @see #getValueType()
	 * @generated
	 */
	void setValueType(ValueType value);

	/**
	 * Returns the value of the '<em><b>External Get</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>External Get</em>' attribute.
	 * @see #setExternalGet(boolean)
	 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getResourceAttribute_ExternalGet()
	 * @model
	 * @generated
	 */
	boolean isExternalGet();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.model.core.ResourceAttribute#isExternalGet <em>External Get</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>External Get</em>' attribute.
	 * @see #isExternalGet()
	 * @generated
	 */
	void setExternalGet(boolean value);

	/**
	 * Returns the value of the '<em><b>External Set</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>External Set</em>' attribute.
	 * @see #setExternalSet(boolean)
	 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getResourceAttribute_ExternalSet()
	 * @model
	 * @generated
	 */
	boolean isExternalSet();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.model.core.ResourceAttribute#isExternalSet <em>External Set</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>External Set</em>' attribute.
	 * @see #isExternalSet()
	 * @generated
	 */
	void setExternalSet(boolean value);

	/**
	 * Returns the value of the '<em><b>Stale</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Indicator when an external get needs to be triggered and the internal data chache becomes stale. Negative values are never stale, 0 is always stale and postive values indicate the number milliseconds till the last get.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Stale</em>' attribute.
	 * @see #setStale(int)
	 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getResourceAttribute_Stale()
	 * @model
	 * @generated
	 */
	int getStale();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.model.core.ResourceAttribute#getStale <em>Stale</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stale</em>' attribute.
	 * @see #getStale()
	 * @generated
	 */
	void setStale(int value);

} // ResourceAttribute
