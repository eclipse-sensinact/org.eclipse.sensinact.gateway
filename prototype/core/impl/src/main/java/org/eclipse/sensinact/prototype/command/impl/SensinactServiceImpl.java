/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.prototype.command.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.sensinact.prototype.command.SensinactProvider;
import org.eclipse.sensinact.prototype.command.SensinactResource;
import org.eclipse.sensinact.prototype.command.SensinactService;
import org.eclipse.sensinact.prototype.model.ResourceBuilder;

public class SensinactServiceImpl extends CommandScopedImpl implements SensinactService {

    private final SensinactProvider provider;
    private final String name;

    /**
     * Resource name -&gt; resource bean
     */
    private final Map<String, SensinactResource> resources = new HashMap<>();

    public SensinactServiceImpl(AtomicBoolean active, SensinactProvider provider, String name) {
        super(active);
        this.provider = provider;
        this.name = name;
    }

    @Override
    public ResourceBuilder<?> createResource(String resource) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, SensinactResource> getResources() {
        return Map.copyOf(resources);
    }

    /**
     * Bundle-private way to populate resources
     */
    public void setResources(final Map<String, SensinactResource> resources) {
        synchronized (this.resources) {
            this.resources.clear();
            this.resources.putAll(resources);
        }
    }

    /**
     * Bundle-private way to populate resources
     */
    public void setResources(final Collection<SensinactResource> resources) {
        setResources(resources.stream().collect(Collectors.toMap(SensinactResource::getName, Function.identity())));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isExclusivelyOwned() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAutoDelete() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public SensinactProvider getProvider() {
        return provider;
    }

    @Override
    public String toString() {
        return String.format("SensiNactService(provider=%s, name=%s, resources=%s)", provider.getName(), name,
                List.of());
    }
}
