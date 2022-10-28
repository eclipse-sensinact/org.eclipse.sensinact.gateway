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

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc --> The <b>Factory</b> for the model. It provides a
 * create method for each non-abstract class of the model. <!-- end-user-doc -->
 * 
 * @see org.eclipse.sensinact.model.core.SensiNactPackage
 * @generated
 */
public interface SensiNactFactory extends EFactory {
    /**
     * The singleton instance of the factory. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    SensiNactFactory eINSTANCE = org.eclipse.sensinact.model.core.impl.SensiNactFactoryImpl.init();

    /**
     * Returns a new object of class '<em>Provider</em>'. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @return a new object of class '<em>Provider</em>'.
     * @generated
     */
    Provider createProvider();

    /**
     * Returns a new object of class '<em>Admin</em>'. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @return a new object of class '<em>Admin</em>'.
     * @generated
     */
    Admin createAdmin();

    /**
     * Returns a new object of class '<em>Service</em>'. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @return a new object of class '<em>Service</em>'.
     * @generated
     */
    Service createService();

    /**
     * Returns a new object of class '<em>Metadata</em>'. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @return a new object of class '<em>Metadata</em>'.
     * @generated
     */
    Metadata createMetadata();

    /**
     * Returns a new object of class '<em>Model Metadata</em>'. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * 
     * @return a new object of class '<em>Model Metadata</em>'.
     * @generated
     */
    ModelMetadata createModelMetadata();

    /**
     * Returns the package supported by this factory. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @return the package supported by this factory.
     * @generated
     */
    SensiNactPackage getSensiNactPackage();

} // SensiNactFactory
