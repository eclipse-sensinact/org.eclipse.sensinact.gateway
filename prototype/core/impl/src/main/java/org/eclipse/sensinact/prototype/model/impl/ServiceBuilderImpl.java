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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.prototype.model.ResourceBuilder;
import org.eclipse.sensinact.prototype.model.Service;
import org.eclipse.sensinact.prototype.model.ServiceBuilder;
import org.eclipse.sensinact.prototype.model.nexus.ModelNexus;

public class ServiceBuilderImpl<P> extends NestableBuilderImpl<P, ModelImpl, Service> implements ServiceBuilder<P> {

    private final String name;
    private final ModelNexus nexusImpl;
    private final List<NestableBuilderImpl<?, ServiceImpl, ?>> nested = new ArrayList<>();
    private Instant creationTimestamp;

    public ServiceBuilderImpl(AtomicBoolean active, P parent, ModelImpl built, String name, ModelNexus nexusImpl) {
        super(active, parent, built);
        this.name = name;
        this.nexusImpl = nexusImpl;
    }

    @Override
    public ServiceBuilder<P> exclusivelyOwned(boolean exclusive) {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ServiceBuilder<P> withAutoDeletion(boolean autoDelete) {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ServiceBuilder<P> withCreationTime(Instant creationTime) {
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

    protected Service doBuild(ModelImpl builtParent) {
        checkValid();
        ServiceImpl s = new ServiceImpl(active, builtParent, nexusImpl.createService(builtParent.getModelEClass(), name,
                creationTimestamp == null ? Instant.now() : creationTimestamp), nexusImpl);
        nested.forEach(n -> n.doBuild(s));
        return s;
    }

}
