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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.prototype.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.prototype.model.Model;
import org.eclipse.sensinact.prototype.model.ModelBuilder;
import org.eclipse.sensinact.prototype.model.SensinactModelManager;
import org.eclipse.sensinact.prototype.model.nexus.impl.ModelNexus;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;

public class SensinactModelManagerImpl extends CommandScopedImpl implements SensinactModelManager {

    private final NotificationAccumulator accumulator;
    private final ModelNexus nexusImpl;

    public SensinactModelManagerImpl(NotificationAccumulator accumulator, ModelNexus nexusImpl) {
        super(new AtomicBoolean(true));
        this.accumulator = accumulator;
        this.nexusImpl = nexusImpl;
    }

    @Override
    public ModelBuilder createModel(String model) {
        checkValid();
        return new ModelBuilderImpl(active, accumulator, nexusImpl, model);
    }

    @Override
    public Model getModel(String model) {
        checkValid();
        return nexusImpl.getModel(model).map(eClass -> new ModelImpl(active, model, eClass, nexusImpl, accumulator))
                .orElse(null);
    }

    @Override
    public void deleteModel(String model) {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void registerModel(String model) {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void registerModel(InputStream model) {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Map<String, Model> getModels() {
        checkValid();
        return nexusImpl.getModelNames().stream().collect(toMap(identity(), this::getModel));
    }

}
