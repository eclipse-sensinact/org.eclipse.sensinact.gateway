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
package org.eclipse.sensinact.prototype.command.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.sensinact.prototype.command.SensinactProvider;
import org.eclipse.sensinact.prototype.command.SensinactService;

public class SensinactProviderImpl extends CommandScopedImpl implements SensinactProvider {

    private final String model;
    private final String name;

    /**
     * Service name -&gt; service bean
     */
    private final Map<String, SensinactService> services = new HashMap<>();

    public SensinactProviderImpl(AtomicBoolean active, String model, String name) {
        super(active);
        this.model = model;
        this.name = name;
    }

    @Override
    public Map<String, SensinactService> getServices() {
        return Map.copyOf(services);
    }

    /**
     * Bundle-private way to populate services
     */
    public void setServices(final Map<String, SensinactService> services) {
        synchronized (this.services) {
            this.services.clear();
            this.services.putAll(services);
        }
    }

    /**
     * Bundle-private way to populate services
     */
    public void setServices(final Collection<SensinactService> services) {
        setServices(services.stream().collect(Collectors.toMap(SensinactService::getName, Function.identity())));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getModelName() {
        return model;
    }

    @Override
    public String toString() {
        return String.format("SensiNactProvider(model=%s, name=%s, services=%s)", model, name, services.keySet());
    }

    @Override
    public List<SensinactProvider> getLinkedProviders() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addLinkedProvider(SensinactProvider provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeLinkedProvider(SensinactProvider provider) {
        // TODO Auto-generated method stub

    }
}
