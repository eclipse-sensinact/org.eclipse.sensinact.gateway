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

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.sensinact.core.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.core.emf.model.EMFModel;
import org.eclipse.sensinact.core.emf.model.EMFService;
import org.eclipse.sensinact.core.emf.model.EMFServiceBuilder;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;

public class ModelImpl extends CommandScopedImpl implements EMFModel {

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
    public boolean isFrozen() {
        checkValid();
        return ((EClassImpl) eClass).isFrozen();
    }

    @Override
    public boolean isDynamic() {
        checkValid();
        return !ProviderPackage.Literals.DYNAMIC_PROVIDER.isSuperTypeOf(eClass);
    }

    @Override
    public String getName() {
        checkValid();
        return name;
    }

    @Override
    public String getPackageUri() {
        checkValid();
        return eClass.getEPackage().getNsURI();
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
    public EMFServiceBuilder<EMFService> createService(String service) {
        checkValid();
        if (isFrozen()) {
            throw new IllegalStateException("Model " + name + " is frozen and can't be modified.");
        }
        return new ServiceBuilderImpl<>(active, null, this, service, nexusImpl);
    }

    @Override
    public EMFService createDynamicService(String svc, EClass svcEClass) {
        return new ServiceImpl(active, this, svc, svcEClass, nexusImpl);
    }

    @Override
    public Map<String, ? extends EMFService> getServices() {
        checkValid();
        // Use nexusImpl to get services reliably
        return nexusImpl.getServiceReferencesForModel(eClass).collect(toMap(EReference::getName,
                r -> new ServiceImpl(active, this, r.getName(), r.getEReferenceType(), nexusImpl)));
    }

    @Override
    public EClass getModelEClass() {
        return eClass;
    }

}
