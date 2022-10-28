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
package org.eclipse.sensinact.model.core.configuration;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.model.core.SensiNactPackage;

import org.gecko.emf.osgi.EMFNamespaces;
import org.gecko.emf.osgi.EPackageConfigurator;

/**
 * <!-- begin-user-doc --> The <b>EPackageConfiguration</b> and
 * <b>ResourceFactoryConfigurator</b> for the model. The package will be
 * registered into a OSGi base model registry. <!-- end-user-doc -->
 * 
 * @see EPackageConfigurator
 * @generated
 */
public class SensiNactEPackageConfigurator implements EPackageConfigurator {

    private SensiNactPackage ePackage;

    protected SensiNactEPackageConfigurator(SensiNactPackage ePackage) {
        this.ePackage = ePackage;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.gecko.emf.osgi.EPackageRegistryConfigurator#configureEPackage(org.eclipse.emf.ecore.EPackage.Registry)
     * @generated
     */
    @Override
    public void configureEPackage(org.eclipse.emf.ecore.EPackage.Registry registry) {
        registry.put(SensiNactPackage.eNS_URI, ePackage);
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.gecko.emf.osgi.EPackageRegistryConfigurator#unconfigureEPackage(org.eclipse.emf.ecore.EPackage.Registry)
     * @generated
     */
    @Override
    public void unconfigureEPackage(org.eclipse.emf.ecore.EPackage.Registry registry) {
        registry.remove(SensiNactPackage.eNS_URI);
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.gecko.emf.osgi.EPackageConfigurator#getServiceProperties()
     * @generated
     */
    @Override
    public Map<String, Object> getServiceProperties() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(EMFNamespaces.EMF_MODEL_NAME, SensiNactPackage.eNAME);
        properties.put(EMFNamespaces.EMF_MODEL_NSURI, SensiNactPackage.eNS_URI);
        properties.put(EMFNamespaces.EMF_MODEL_FILE_EXT, "sensinact");
        properties.put(EMFNamespaces.EMF_CONFIGURATOR_VERSION, "1.0");
        return properties;
    }
}