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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.core.emf.model.EMFModel;
import org.eclipse.sensinact.core.emf.model.EMFModelBuilder;
import org.eclipse.sensinact.core.emf.model.SensinactEMFModelManager;
import org.eclipse.sensinact.core.model.Model;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;

public class SensinactModelManagerImpl extends CommandScopedImpl implements SensinactEMFModelManager {

    private final ModelNexus nexusImpl;

    public SensinactModelManagerImpl(ModelNexus nexusImpl) {
        super(new AtomicBoolean(true));
        this.nexusImpl = nexusImpl;
    }

    @Override
    public EMFModelBuilder createModel(String model) {
        checkValid();
        return new ModelBuilderImpl(active, nexusImpl, null, model);
    }

    @Override
    public EMFModelBuilder createModel(String packageUri, String model) {
        checkValid();
        return new ModelBuilderImpl(active, nexusImpl, packageUri, model);
    }

    @Override
    public EMFModel getModel(String model) {
        return getModel(null, model);
    }

    @Override
    public EMFModel getModel(String packageUri, String model) {
        checkValid();
        return nexusImpl.getModel(packageUri, model).map(eClass -> new ModelImpl(active, model, eClass, nexusImpl))
                .orElse(null);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.sensinact.core.model.SensinactModelManager#deleteModel(java.lang.
     * String)
     */
    @Override
    public void deleteModel(String model) {
        deleteModel(null, model);
    }

    @Override
    public void deleteModel(String packageUri, String model) {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void registerModel(String packageUri, String model) {
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
     * @see org.eclipse.sensinact.core.model.SensinactModelManager#getModel(org.
     * eclipse.emf.ecore.EClass)
     */
    @Override
    public EMFModel getModel(EClass model) {
        if (nexusImpl.registered(model)) {
            return new ModelImpl(active, EMFUtil.getModelName(model), model, nexusImpl);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.sensinact.core.model.SensinactModelManager#createModel(org.
     * eclipse.emf.ecore.EClass)
     */
    @Override
    public EMFModelBuilder createModel(EClass model) {
        return new ModelBuilderImpl(active, nexusImpl, model);
    }
}
