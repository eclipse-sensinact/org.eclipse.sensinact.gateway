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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.sensinact.prototype.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.prototype.twin.SensinactProvider;
import org.eclipse.sensinact.prototype.twin.SensinactResource;
import org.eclipse.sensinact.prototype.twin.SensinactService;

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
    public SensinactProvider getProvider() {
        return provider;
    }

    @Override
    public String toString() {
        return String.format("SensiNactService(provider=%s, name=%s, resources=%s)", provider.getName(), name,
                List.of());
    }
}
