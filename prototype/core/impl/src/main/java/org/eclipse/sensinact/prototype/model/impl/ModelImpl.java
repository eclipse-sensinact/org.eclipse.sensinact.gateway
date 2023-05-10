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
package org.eclipse.sensinact.prototype.model.impl;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sensinact.prototype.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.prototype.model.Model;
import org.eclipse.sensinact.prototype.model.Service;
import org.eclipse.sensinact.prototype.model.ServiceBuilder;
import org.eclipse.sensinact.prototype.model.nexus.ModelNexus;

public class ModelImpl extends CommandScopedImpl implements Model {

    private final String name;

    private final EClass eClass;

    private ModelNexus nexusImpl;

    public ModelImpl(AtomicBoolean active, String name, EClass eClass, ModelNexus nexusImpl) {
        super(active);
        this.name = name;
        this.eClass = eClass;
        this.nexusImpl = nexusImpl;
    }

    @Override
    public String getName() {
        checkValid();
        return name;
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
    public ServiceBuilder<Service> createService(String service) {
        checkValid();
        return new ServiceBuilderImpl<>(active, null, this, service, nexusImpl);
    }

    @Override
    public Map<String, ? extends Service> getServices() {
        checkValid();
        // Use nexusImpl to get services reliably
        return nexusImpl.getServicesForModel(eClass)
                .collect(toMap(EReference::getName, r -> new ServiceImpl(active, this, r, nexusImpl)));
    }

    EClass getModelEClass() {
        return eClass;
    }

}
