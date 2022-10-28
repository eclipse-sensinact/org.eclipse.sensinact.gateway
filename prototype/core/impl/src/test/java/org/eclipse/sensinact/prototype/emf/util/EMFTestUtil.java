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
package org.eclipse.sensinact.prototype.emf.util;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sensinact.model.core.SensiNactPackage;

/**
 * Some helpful methods to be reused by all the tests
 * 
 * @author Juergen Albert
 * @since 28 Oct 2022
 */
public class EMFTestUtil {

    /**
     * @return a fully configured {@link ResourceSet} to be used in non OSGi tests
     */
    public static ResourceSet createResourceSet() {
        ResourceSet resourceSet = new ResourceSetImpl();

        // Register the package to ensure it is available during loading.
        resourceSet.getPackageRegistry().put(SensiNactPackage.eNS_URI, SensiNactPackage.eINSTANCE);
        return resourceSet;
    }

}
