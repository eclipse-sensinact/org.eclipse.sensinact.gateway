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
import org.eclipse.sensinact.core.model.nexus.ModelNexus;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.model.core.provider.DynamicProvider;
import org.osgi.util.promise.PromiseFactory;

public class SensinactDynamicProviderServiceImpl extends CommandScopedImpl implements SensinactService {

    private final SensinactProvider sensinactProvider;
    private final DynamicProvider provider;
    private final ModelNexus nexus;
    private final PromiseFactory promiseFactory;
    private String serviceName;

    /**
     * Creates a new instance that uses the services Map instead of a direct
     * Reference.
     */
    public SensinactDynamicProviderServiceImpl(AtomicBoolean active, SensinactProviderImpl sensinactProvider,
            DynamicProvider provider,
            String serviceName, ModelNexus nexus, PromiseFactory promiseFactory) {
        super(active);
        this.sensinactProvider = sensinactProvider;
        this.provider = provider;
        this.nexus = nexus;
        this.promiseFactory = promiseFactory;
        this.serviceName = serviceName;
    }

    @Override
    public Map<String, SensinactResource> getResources() {
        checkValid();
        EClass serviceEClass = ((DynamicProvider) provider).getServices().get(serviceName).eClass();
        ;
        return nexus.getResourcesForService(serviceEClass)
                .collect(Collectors.toMap(ETypedElement::getName, a -> new SensinactDynamicProviderResourceImpl(active,
                        this, provider, serviceName, a, a.getEType().getInstanceClass(), nexus, promiseFactory)));
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
}
