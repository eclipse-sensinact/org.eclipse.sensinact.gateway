/**
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

import java.time.Instant;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * <!-- begin-user-doc --> A representation of the model object
 * '<em><b>Metadata</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.sensinact.model.core.Metadata#getFeature
 * <em>Feature</em>}</li>
 * <li>{@link org.eclipse.sensinact.model.core.Metadata#getTimestamp
 * <em>Timestamp</em>}</li>
 * <li>{@link org.eclipse.sensinact.model.core.Metadata#getSource
 * <em>Source</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.model.core.SensiNactPackage#getMetadata()
 * @model
 * @generated
 */
public interface Metadata extends EObject {
    /**
     * Returns the value of the '<em><b>Feature</b></em>' reference. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Feature</em>' reference.
     * @see #setFeature(EStructuralFeature)
     * @see org.eclipse.sensinact.model.core.SensiNactPackage#getMetadata_Feature()
     * @model
     * @generated
     */
    EStructuralFeature getFeature();

    /**
     * Sets the value of the
     * '{@link org.eclipse.sensinact.model.core.Metadata#getFeature
     * <em>Feature</em>}' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Feature</em>' reference.
     * @see #getFeature()
     * @generated
     */
    void setFeature(EStructuralFeature value);

    /**
     * Returns the value of the '<em><b>Timestamp</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Timestamp</em>' attribute.
     * @see #setTimestamp(Instant)
     * @see org.eclipse.sensinact.model.core.SensiNactPackage#getMetadata_Timestamp()
     * @model dataType="org.eclipse.sensinact.model.core.EInstant"
     * @generated
     */
    Instant getTimestamp();

    /**
     * Sets the value of the
     * '{@link org.eclipse.sensinact.model.core.Metadata#getTimestamp
     * <em>Timestamp</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Timestamp</em>' attribute.
     * @see #getTimestamp()
     * @generated
     */
    void setTimestamp(Instant value);

    /**
     * Returns the value of the '<em><b>Source</b></em>' reference. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Source</em>' reference.
     * @see #setSource(EObject)
     * @see org.eclipse.sensinact.model.core.SensiNactPackage#getMetadata_Source()
     * @model
     * @generated
     */
    EObject getSource();

    /**
     * Sets the value of the
     * '{@link org.eclipse.sensinact.model.core.Metadata#getSource <em>Source</em>}'
     * reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Source</em>' reference.
     * @see #getSource()
     * @generated
     */
    void setSource(EObject value);

} // Metadata
