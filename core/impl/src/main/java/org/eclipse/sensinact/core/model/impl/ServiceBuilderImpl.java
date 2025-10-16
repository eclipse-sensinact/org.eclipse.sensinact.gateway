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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.sensinact.core.emf.model.EMFService;
import org.eclipse.sensinact.core.emf.model.EMFServiceBuilder;
import org.eclipse.sensinact.core.model.ResourceBuilder;
import org.eclipse.sensinact.core.model.ServiceBuilder;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;

public class ServiceBuilderImpl<P> extends NestableBuilderImpl<P, ModelImpl, EMFService>
        implements EMFServiceBuilder<P> {

    private final String name;
    private final ModelNexus nexusImpl;
    private final List<NestableBuilderImpl<?, ServiceImpl, ?>> nested = new ArrayList<>();
    private Instant creationTimestamp;
    private String serviceModelName;

    public ServiceBuilderImpl(AtomicBoolean active, P parent, ModelImpl built, String name, String serviceModelName,
            ModelNexus nexusImpl) {
        super(active, parent, built);
        this.name = name;
        this.serviceModelName = serviceModelName == null ? name : serviceModelName;
        this.nexusImpl = nexusImpl;
    }

    @Override
    public EMFServiceBuilder<P> exclusivelyOwned(boolean exclusive) {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public EMFServiceBuilder<P> withAutoDeletion(boolean autoDelete) {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public EMFServiceBuilder<P> withCreationTime(Instant creationTime) {
        checkValid();
        this.creationTimestamp = creationTime;
        return this;
    }

    @Override
    public ResourceBuilder<ServiceBuilder<P>, Object> withResource(String name) {
        checkValid();
        ResourceBuilderImpl<ServiceBuilder<P>, Object> rb = new ResourceBuilderImpl<>(active, this, null, name,
                nexusImpl);
        nested.add(rb);
        return rb;
    }

    protected EMFService doBuild(ModelImpl builtParent) {
        checkValid();
        EReference service = nexusImpl.createService(builtParent.getModelEClass(), name, serviceModelName,
                creationTimestamp == null ? Instant.now() : creationTimestamp);
        ServiceImpl s = new ServiceImpl(active, builtParent, service.getName(), service.getEReferenceType(), nexusImpl);
        nested.forEach(n -> n.doBuild(s));
        return s;
    }

}
