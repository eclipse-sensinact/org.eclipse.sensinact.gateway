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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.prototype.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.prototype.model.Model;
import org.eclipse.sensinact.prototype.model.ModelBuilder;
import org.eclipse.sensinact.prototype.model.SensinactModelManager;
import org.eclipse.sensinact.prototype.model.nexus.ModelNexus;
import org.eclipse.sensinact.prototype.model.nexus.emf.EMFUtil;

public class SensinactModelManagerImpl extends CommandScopedImpl implements SensinactModelManager {

    private final ModelNexus nexusImpl;

    public SensinactModelManagerImpl(ModelNexus nexusImpl) {
        super(new AtomicBoolean(true));
        this.nexusImpl = nexusImpl;
    }

    @Override
    public ModelBuilder createModel(String model) {
        checkValid();
        return new ModelBuilderImpl(active, nexusImpl, model);
    }

    @Override
    public Model getModel(String model) {
        checkValid();
        return nexusImpl.getModel(model).map(eClass -> new ModelImpl(active, model, eClass, nexusImpl)).orElse(null);
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.sensinact.prototype.model.SensinactModelManager#getModel(org.
     * eclipse.emf.ecore.EClass)
     */
    @Override
    public Model getModel(EClass model) {
        if (nexusImpl.registered(model)) {
            return new ModelImpl(active, EMFUtil.getModelName(model), model, nexusImpl);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.sensinact.prototype.model.SensinactModelManager#createModel(org.
     * eclipse.emf.ecore.EClass)
     */
    @Override
    public ModelBuilder createModel(EClass model) {
        return new ModelBuilderImpl(active, nexusImpl, model);
    }

}
