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
package org.eclipse.sensinact.prototype.twin.impl;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.prototype.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.prototype.model.nexus.ModelNexus;
import org.osgi.util.promise.PromiseFactory;

public class SensinactServiceImpl extends CommandScopedImpl implements SensinactService {

    private final SensinactProvider sensinactProvider;
    private final Provider provider;
    private final EReference svcFeature;
    private final ModelNexus nexus;
    private final PromiseFactory promiseFactory;

    public SensinactServiceImpl(AtomicBoolean active, SensinactProvider sensinactProvider, Provider provider,
            EReference svcFeature, ModelNexus nexus, PromiseFactory promiseFactory) {
        super(active);
        this.sensinactProvider = sensinactProvider;
        this.provider = provider;
        this.svcFeature = svcFeature;
        this.nexus = nexus;
        this.promiseFactory = promiseFactory;
    }

    @Override
    public Map<String, SensinactResource> getResources() {
        checkValid();
        return nexus.getResourcesForService(svcFeature.getEReferenceType())
                .collect(Collectors.toMap(ETypedElement::getName, a -> new SensinactResourceImpl(active, this, provider,
                        svcFeature, a, a.getEType().getInstanceClass(), nexus, promiseFactory)));
    }

    @Override
    public String getName() {
        checkValid();
        return svcFeature.getName();
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
