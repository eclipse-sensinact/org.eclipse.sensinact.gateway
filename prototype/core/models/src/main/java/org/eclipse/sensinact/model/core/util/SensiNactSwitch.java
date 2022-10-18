/*********************************************************************
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
**********************************************************************/
package org.eclipse.sensinact.model.core.util;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.ecore.util.Switch;

import org.eclipse.sensinact.model.core.*;

/**
 * <!-- begin-user-doc --> The <b>Switch</b> for the model's inheritance
 * hierarchy. It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object and proceeding up the
 * inheritance hierarchy until a non-null result is returned, which is the
 * result of the switch. <!-- end-user-doc -->
 * 
 * @see org.eclipse.sensinact.model.core.SensiNactPackage
 * @generated
 */
public class SensiNactSwitch<T> extends Switch<T> {
    /**
     * The cached model package <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected static SensiNactPackage modelPackage;

    /**
     * Creates an instance of the switch. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @generated
     */
    public SensiNactSwitch() {
        if (modelPackage == null) {
            modelPackage = SensiNactPackage.eINSTANCE;
        }
    }

    /**
     * Checks whether this is a switch for the given package. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * 
     * @param ePackage the package in question.
     * @return whether this is a switch for the given package.
     * @generated
     */
    @Override
    protected boolean isSwitchFor(EPackage ePackage) {
        return ePackage == modelPackage;
    }

    /**
     * Calls <code>caseXXX</code> for each class of the model until one returns a
     * non null result; it yields that result. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @return the first non-null result returned by a <code>caseXXX</code> call.
     * @generated
     */
    @Override
    protected T doSwitch(int classifierID, EObject theEObject) {
        switch (classifierID) {
        case SensiNactPackage.PROVIDER: {
            Provider provider = (Provider) theEObject;
            T result = caseProvider(provider);
            if (result == null)
                result = defaultCase(theEObject);
            return result;
        }
        case SensiNactPackage.ADMIN: {
            Admin admin = (Admin) theEObject;
            T result = caseAdmin(admin);
            if (result == null)
                result = caseService(admin);
            if (result == null)
                result = defaultCase(theEObject);
            return result;
        }
        case SensiNactPackage.SERVICE: {
            Service service = (Service) theEObject;
            T result = caseService(service);
            if (result == null)
                result = defaultCase(theEObject);
            return result;
        }
        case SensiNactPackage.METADATA: {
            Metadata metadata = (Metadata) theEObject;
            T result = caseMetadata(metadata);
            if (result == null)
                result = defaultCase(theEObject);
            return result;
        }
        case SensiNactPackage.FEATURE_METADATA: {
            @SuppressWarnings("unchecked")
            Map.Entry<EStructuralFeature, Metadata> featureMetadata = (Map.Entry<EStructuralFeature, Metadata>) theEObject;
            T result = caseFeatureMetadata(featureMetadata);
            if (result == null)
                result = defaultCase(theEObject);
            return result;
        }
        case SensiNactPackage.MODEL_METADATA: {
            ModelMetadata modelMetadata = (ModelMetadata) theEObject;
            T result = caseModelMetadata(modelMetadata);
            if (result == null)
                result = caseMetadata(modelMetadata);
            if (result == null)
                result = defaultCase(theEObject);
            return result;
        }
        default:
            return defaultCase(theEObject);
        }
    }

    /**
     * Returns the result of interpreting the object as an instance of
     * '<em>Provider</em>'. <!-- begin-user-doc --> This implementation returns
     * null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     * 
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of
     *         '<em>Provider</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseProvider(Provider object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of
     * '<em>Admin</em>'. <!-- begin-user-doc --> This implementation returns null;
     * returning a non-null result will terminate the switch. <!-- end-user-doc -->
     * 
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of
     *         '<em>Admin</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseAdmin(Admin object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of
     * '<em>Service</em>'. <!-- begin-user-doc --> This implementation returns null;
     * returning a non-null result will terminate the switch. <!-- end-user-doc -->
     * 
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of
     *         '<em>Service</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseService(Service object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of
     * '<em>Metadata</em>'. <!-- begin-user-doc --> This implementation returns
     * null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     * 
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of
     *         '<em>Metadata</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseMetadata(Metadata object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Feature
     * Metadata</em>'. <!-- begin-user-doc --> This implementation returns null;
     * returning a non-null result will terminate the switch. <!-- end-user-doc -->
     * 
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Feature
     *         Metadata</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseFeatureMetadata(Map.Entry<EStructuralFeature, Metadata> object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Model
     * Metadata</em>'. <!-- begin-user-doc --> This implementation returns null;
     * returning a non-null result will terminate the switch. <!-- end-user-doc -->
     * 
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Model
     *         Metadata</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseModelMetadata(ModelMetadata object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of
     * '<em>EObject</em>'. <!-- begin-user-doc --> This implementation returns null;
     * returning a non-null result will terminate the switch, but this is the last
     * case anyway. <!-- end-user-doc -->
     * 
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of
     *         '<em>EObject</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject)
     * @generated
     */
    @Override
    public T defaultCase(EObject object) {
        return null;
    }

} // SensiNactSwitch
