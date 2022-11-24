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
package org.eclipse.sensinact.prototype.model.nexus.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.model.core.Provider;

class ProviderTypeWrapper {

    private EClass provider;
    Map<URI, Provider> instances = new ConcurrentHashMap<>();

    public ProviderTypeWrapper(EClass provider) {
        this.provider = provider;
    }

    /**
     * Returns the provider.
     *
     * @return the provider
     */
    public EClass getProviderType() {
        return provider;
    }

    /**
     * Returns the instances.
     *
     * @return the instances
     */
    public Map<URI, Provider> getInstances() {
        return instances;
    }
}
