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

import org.eclipse.sensinact.prototype.model.Model;
import org.eclipse.sensinact.prototype.model.ModelBuilder;
import org.eclipse.sensinact.prototype.model.ServiceBuilder;
import org.eclipse.sensinact.prototype.model.nexus.impl.ModelNexus;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;

public class ModelBuilderImpl extends AbstractBuilderImpl<Model> implements ModelBuilder {

    private final NotificationAccumulator accumulator;
    private final ModelNexus nexusImpl;
    private final String name;
    private final List<NestableBuilderImpl<?, ModelImpl, ?>> nested = new ArrayList<>();
    private Instant creationTime;

    public ModelBuilderImpl(AtomicBoolean active, NotificationAccumulator accumulator, ModelNexus nexusImpl,
            String name) {
        super(active);
        this.accumulator = accumulator;
        this.nexusImpl = nexusImpl;
        this.name = name;
    }

    @Override
    public ModelBuilder exclusivelyOwned(boolean exclusive) {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ModelBuilder withAutoDeletion(boolean autoDelete) {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ModelBuilder withCreationTime(Instant creationTime) {
        checkValid();
        this.creationTime = creationTime;
        return this;
    }

    @Override
    public ServiceBuilder<ModelBuilder> withService(String name) {
        checkValid();
        ServiceBuilderImpl<ModelBuilder> sb = new ServiceBuilderImpl<>(active, this, null, name, nexusImpl,
                accumulator);
        nested.add(sb);
        return sb;
    }

    @Override
    protected Model doBuild() {
        checkValid();
        ModelImpl modelImpl = new ModelImpl(active, name,
                nexusImpl.createModel(name, creationTime == null ? Instant.now() : creationTime, accumulator),
                nexusImpl, accumulator);
        nested.forEach(n -> n.doBuild(modelImpl));
        return modelImpl;
    }

}
