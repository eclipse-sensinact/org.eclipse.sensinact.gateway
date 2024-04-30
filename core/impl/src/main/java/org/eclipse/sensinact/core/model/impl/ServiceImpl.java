/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.core.model.impl;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.sensinact.core.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.core.model.Model;
import org.eclipse.sensinact.core.model.Resource;
import org.eclipse.sensinact.core.model.ResourceBuilder;
import org.eclipse.sensinact.core.model.Service;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;

public class ServiceImpl extends CommandScopedImpl implements Service {

    private final Model model;
    private final String serviceName;
    private final ModelNexus nexusImpl;
    private EClass serviceEClass;

    public ServiceImpl(AtomicBoolean active, Model model, String serviceName, EClass serviceEClass,
            ModelNexus nexusImpl) {
        super(active);
        this.model = model;
        this.serviceEClass = serviceEClass;
        this.serviceName = serviceName;
        this.nexusImpl = nexusImpl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.sensinact.core.model.Model#isFrozen()
     */
    @Override
    public boolean isFrozen() {
        checkValid();
        return ((EClassImpl) serviceEClass).isFrozen();
    }

    @Override
    public String getName() {
        checkValid();
        return serviceName;
    }

    @Override
    public boolean isExclusivelyOwned() {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isAutoDelete() {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResourceBuilder<Resource, Object> createResource(String resource) {
        checkValid();
        return new ResourceBuilderImpl<Resource, Object>(active, null, this, resource, nexusImpl);
    }

    @Override
    public Map<String, ? extends Resource> getResources() {
        checkValid();
        return nexusImpl.getResourcesForService(getServiceEClass())
                .collect(Collectors.toMap(f -> f.getName(), f -> new ResourceImpl(active, this, f)));
    }

    @Override
    public Model getModel() {
        checkValid();
        return model;
    }

    EClass getServiceEClass() {
        return serviceEClass;
    }
}
