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
package org.eclipse.sensinact.prototype.model.nexus.impl.emf;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;

/**
 * As we modify the EClasses on the fly, we need to prevent the copier to call
 * {@link EObject#sIsSet} or {@link EObject#sSet} for features that are new, as
 * this would cause an {@link IndexOutOfBoundsException}
 *
 * @author Juergen Albert
 * @since 11 Oct 2022
 */
public class SensinactCopier extends Copier {

    /** serialVersionUID */
    private static final long serialVersionUID = 3455941336515352303L;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.emf.ecore.util.EcoreUtil.Copier#copyAttribute(org.eclipse.emf.
     * ecore.EAttribute, org.eclipse.emf.ecore.EObject,
     * org.eclipse.emf.ecore.EObject)
     */
    @Override
    protected void copyAttribute(EAttribute eAttribute, EObject eObject, EObject copyEObject) {
        int attributeVersion = EMFUtil.getVersion(eAttribute);
        int containerVersion = EMFUtil.getContainerVersion(eAttribute);
        // if a Version is -1, it has no Version and is usually the case for the
        // features are from the base EClass we inherit from
        if (attributeVersion == -1 || attributeVersion != containerVersion) {
            super.copyAttribute(eAttribute, eObject, copyEObject);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.emf.ecore.util.EcoreUtil.Copier#copyContainment(org.eclipse.emf.
     * ecore.EReference, org.eclipse.emf.ecore.EObject,
     * org.eclipse.emf.ecore.EObject)
     */
    @Override
    protected void copyContainment(EReference eReference, EObject eObject, EObject copyEObject) {
        int attributeVersion = EMFUtil.getVersion(eReference);
        int containerVersion = EMFUtil.getContainerVersion(eReference);
        // if a Version is -1, it has no Version and is usually the case for the
        // features are from the base EClass we inherit from
        if (attributeVersion == -1 || attributeVersion != containerVersion) {
            super.copyContainment(eReference, eObject, copyEObject);
        }
    }

    public static <T extends EObject> T copySelective(T eObject) {
        Copier copier = new SensinactCopier();
        EObject result = copier.copy(eObject);
        copier.copyReferences();

        @SuppressWarnings("unchecked")
        T t = (T) result;
        return t;
    }
}
