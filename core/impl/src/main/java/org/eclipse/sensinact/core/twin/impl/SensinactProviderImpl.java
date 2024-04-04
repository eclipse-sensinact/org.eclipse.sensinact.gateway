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
package org.eclipse.sensinact.core.twin.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFProvider;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.core.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class SensinactProviderImpl extends CommandScopedImpl implements SensinactEMFProvider {

    private final Provider provider;

    private final ModelNexus nexus;

    private final PromiseFactory promiseFactory;

    public SensinactProviderImpl(AtomicBoolean active, Provider provider, ModelNexus nexus,
            PromiseFactory promiseFactory) {
        super(active);
        this.provider = provider;
        this.nexus = nexus;
        this.promiseFactory = promiseFactory;
    }

    @Override
    public Map<String, SensinactService> getServices() {
        checkValid();
        return nexus.getServicesForModel(provider.eClass()).collect(Collectors.toMap(EReference::getName,
                r -> new SensinactServiceImpl(active, this, provider, r, nexus, promiseFactory)));
    }

    @Override
    public String getName() {
        checkValid();
        return provider.getId();
    }

    @Override
    public String getModelName() {
        checkValid();
        return EMFUtil.getModelName(provider.eClass());
    }

    @Override
    public String getModelPackageUri() {
        checkValid();
        return provider.eClass().getEPackage().getNsURI();
    }

    @Override
    public String toString() {
        checkValid();
        return String.format("SensiNactProvider(model=%s, name=%s, services=%s)", getModelName(), getName(),
                getServices().keySet());
    }

    @Override
    public List<SensinactEMFProvider> getLinkedProviders() {
        checkValid();
        return provider.getLinkedProviders().stream()
                .map(p -> new SensinactProviderImpl(active, provider, nexus, promiseFactory))
                .collect(Collectors.toList());
    }

    @Override
    public void addLinkedProvider(SensinactProvider provider) {
        checkValid();
        nexus.linkProviders(getName(), provider.getName(), Instant.now());
    }

    @Override
    public void removeLinkedProvider(SensinactProvider provider) {
        checkValid();
        nexus.unlinkProviders(getName(), provider.getName(), Instant.now());
    }

    @Override
    public void delete() {
        checkValid();
        nexus.deleteProvider(provider.eClass().getEPackage().getNsURI(), getModelName(), getName());
    }

    public Promise<Void> update(Provider newVersion) {

        checkValid();

        if (!newVersion.eClass().equals(newVersion.eClass())) {
            return promiseFactory.failed(new IllegalArgumentException(
                    "The given Object is of type " + newVersion.eClass() + " but expected " + provider.eClass()));
        }

        nexus.save(newVersion);

        return promiseFactory.resolved(null);

    }
}
