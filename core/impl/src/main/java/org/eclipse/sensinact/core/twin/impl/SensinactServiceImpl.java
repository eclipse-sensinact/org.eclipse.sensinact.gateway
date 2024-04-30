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

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.sensinact.core.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFService;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.osgi.util.promise.PromiseFactory;

public class SensinactServiceImpl extends CommandScopedImpl implements SensinactEMFService {

    private final SensinactProvider sensinactProvider;
    private final Provider provider;
    private final String serviceName;
    private final ModelNexus nexus;
    private final PromiseFactory promiseFactory;
    private EClass service;

    public SensinactServiceImpl(AtomicBoolean active, SensinactProvider sensinactProvider, Provider provider,
            String serviceName, EClass service, ModelNexus nexus, PromiseFactory promiseFactory) {
        super(active);
        this.sensinactProvider = sensinactProvider;
        this.provider = provider;
        this.serviceName = serviceName;
        this.service = service;
        this.nexus = nexus;
        this.promiseFactory = promiseFactory;
    }

    @Override
    public Map<String, SensinactResource> getResources() {
        checkValid();
        return nexus.getResourcesForService(service)
                .collect(Collectors.toMap(ETypedElement::getName, a -> new SensinactResourceImpl(active, this, provider,
                        serviceName, a, a.getEType().getInstanceClass(), nexus, promiseFactory)));
    }

    @Override
    public String getName() {
        checkValid();
        return serviceName;
    }

    @Override
    public SensinactProvider getProvider() {
        checkValid();
        return sensinactProvider;
    }

    @Override
    public String toString() {
        checkValid();
        return String.format("SensiNactService(provider=%s, name=%s, resources=%s)", provider.getId(), getName(),
                getResources().keySet());
    }

    @Override
    public EClass getServiceEClass() {
        checkValid();
        return service;
    }
}
