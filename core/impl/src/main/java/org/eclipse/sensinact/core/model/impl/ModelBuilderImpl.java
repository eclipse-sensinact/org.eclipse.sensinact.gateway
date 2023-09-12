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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.model.Model;
import org.eclipse.sensinact.core.model.ModelBuilder;
import org.eclipse.sensinact.core.model.ServiceBuilder;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;

public class ModelBuilderImpl extends AbstractBuilderImpl<Model> implements ModelBuilder {

    private final ModelNexus nexusImpl;
    private final String name;
    private final List<NestableBuilderImpl<?, ModelImpl, ?>> nested = new ArrayList<>();
    private Instant creationTime;
    private final EClass modelEClass;

    public ModelBuilderImpl(AtomicBoolean active, ModelNexus nexusImpl, String name) {
        super(active);
        this.nexusImpl = nexusImpl;
        this.name = name;
        this.modelEClass = null;
    }

    public ModelBuilderImpl(AtomicBoolean active, ModelNexus nexusImpl, EClass model) {
        super(active);
        this.nexusImpl = nexusImpl;
        this.modelEClass = model;
        this.name = EMFUtil.getModelName(model);
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
        if (modelEClass != null) {
            throw new RuntimeException("Extendable Ecore Models arent supported yet.");
        }
        ServiceBuilderImpl<ModelBuilder> sb = new ServiceBuilderImpl<>(active, this, null, name, nexusImpl);
        nested.add(sb);
        return sb;
    }

    @Override
    protected Model doBuild() {
        checkValid();
        if (modelEClass != null) {
            return new ModelImpl(active, name,
                    nexusImpl.registerModel(modelEClass, creationTime == null ? Instant.now() : creationTime),
                    nexusImpl);
        }
        ModelImpl modelImpl = new ModelImpl(active, name,
                nexusImpl.createModel(name, creationTime == null ? Instant.now() : creationTime), nexusImpl);
        nested.forEach(n -> n.doBuild(modelImpl));
        return modelImpl;
    }

}
