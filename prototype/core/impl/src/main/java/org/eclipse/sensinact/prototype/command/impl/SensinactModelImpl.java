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

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sensinact.model.core.Metadata;
import org.eclipse.sensinact.model.core.Provider;
import org.eclipse.sensinact.model.core.Service;
import org.eclipse.sensinact.prototype.command.SensinactModel;
import org.eclipse.sensinact.prototype.command.SensinactProvider;
import org.eclipse.sensinact.prototype.command.SensinactResource;
import org.eclipse.sensinact.prototype.command.SensinactService;
import org.eclipse.sensinact.prototype.command.TimedValue;
import org.eclipse.sensinact.prototype.model.nexus.impl.NexusImpl;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.osgi.util.promise.PromiseFactory;

public class SensinactModelImpl extends CommandScopedImpl implements SensinactModel {

    private final NotificationAccumulator accumulator;
    private final NexusImpl nexusImpl;
    private final PromiseFactory pf;

    public SensinactModelImpl(NotificationAccumulator accumulator, NexusImpl nexusImpl, PromiseFactory pf) {
        super(new AtomicBoolean(true));
        this.accumulator = accumulator;
        this.nexusImpl = nexusImpl;
        this.pf = pf;
    }

    @Override
    public SensinactResource getOrCreateResource(String model, String provider, String service, String resource,
            Class<?> valueType) {
        checkValid();

        SensinactProvider p = new SensinactProviderImpl(active, model, provider);
        SensinactService s = new SensinactServiceImpl(active, p, service);

        return new SensinactResourceImpl(active, s, resource, valueType, accumulator, nexusImpl, pf);
    }

    /**
     * Returns the known providers
     */
    public List<SensinactProvider> getProviders() {
        return nexusImpl.getProviders().stream().map(this::toProvider).collect(Collectors.toList());
    }

    @Override
    public SensinactProvider getProvider(String model, String providerName) {
        final Provider provider = nexusImpl.getProvider(model, providerName);
        if (provider == null) {
            return null;
        }

        return toProvider(provider);
    }

    @Override
    public SensinactService getService(String model, String providerName, String service) {
        final Provider provider = nexusImpl.getProvider(model, providerName);
        if (provider == null) {
            return null;
        }

        final EStructuralFeature svcFeature = provider.eClass().getEStructuralFeature(service);
        if (svcFeature == null) {
            return null;
        }
        final Service svc = (Service) provider.eGet(svcFeature);

        final SensinactProviderImpl snProvider = new SensinactProviderImpl(new AtomicBoolean(true), model,
                providerName);
        return toService(snProvider, svc);
    }

    @Override
    public SensinactResource getResource(String model, String providerName, String service, String resource) {
        final Provider provider = nexusImpl.getProvider(model, providerName);
        if (provider == null) {
            return null;
        }

        final EStructuralFeature svcFeature = provider.eClass().getEStructuralFeature(service);
        if (svcFeature == null) {
            return null;
        }

        final Service svc = (Service) provider.eGet(svcFeature);

        final EStructuralFeature rcFeature = svc.eClass().getEStructuralFeature(resource);

        // Construct the resource
        final AtomicBoolean active = new AtomicBoolean(rcFeature != null);
        final SensinactProviderImpl snProvider = new SensinactProviderImpl(active, model, providerName);
        final SensinactServiceImpl snSvc = new SensinactServiceImpl(active, snProvider,
                svc.eContainingFeature().getName());
        snProvider.setServices(List.of(snSvc));

        final SensinactResourceImpl snResource;
        if (rcFeature != null) {
            // Known resource
            snResource = new SensinactResourceImpl(active, snSvc, rcFeature.getName(),
                    rcFeature.getEType().getInstanceClass(), accumulator, nexusImpl, pf);
            snSvc.setResources(List.of(snResource));
        } else {
            // Unknown resource
            snResource = new SensinactResourceImpl(active, snSvc, resource, null, accumulator, nexusImpl, pf);
        }
        return snResource;
    }

    @Override
    public <T> TimedValue<T> getResourceValue(String model, String providerName, String service, String resource,
            Class<T> type) {
        final Provider provider = nexusImpl.getProvider(model, providerName);
        if (provider == null) {
            return null;
        }

        final EStructuralFeature svcFeature = provider.eClass().getEStructuralFeature(service);
        if (svcFeature == null) {
            return null;
        }
        final Service svc = (Service) provider.eGet(svcFeature);

        final EStructuralFeature rcFeature = svc.eClass().getEStructuralFeature(resource);
        if (rcFeature == null) {
            // No value
            return new TimedValueImpl<T>(null, null);
        }

        // Get the resource metadata
        final Metadata metadata = svc.getMetadata().get(rcFeature);
        final Instant timestamp;
        if (metadata != null) {
            timestamp = metadata.getTimestamp();
        } else {
            timestamp = null;
        }

        // FIXME: check its type
        T val = (T) svc.eGet(rcFeature);
        return new TimedValueImpl<T>(val, timestamp);
    }

    @Override
    public void setOrCreateResource(String model, String providerName, String service, String resource, Class<?> type,
            Object value, Instant instant) {
        checkValid();

        nexusImpl.handleDataUpdate(model, providerName, service, resource, type, value, instant);
    }

    private SensinactProviderImpl toProvider(final Provider modelProvider) {
        return toProvider(modelProvider, true);
    }

    private SensinactProviderImpl toProvider(final Provider modelProvider, boolean loadServices) {
        // Construct the provider bean
        final SensinactProviderImpl snProvider = new SensinactProviderImpl(new AtomicBoolean(true),
                modelProvider.eClass().getName(), modelProvider.getId());

        if (loadServices) {
            // List services
            final List<SensinactService> services = modelProvider.eClass().getEStructuralFeatures().stream()
                    .map((feature) -> toService(snProvider, (Service) modelProvider.eGet(feature)))
                    .collect(Collectors.toList());
            services.add(toService(snProvider, modelProvider.getAdmin()));
            snProvider.setServices(services);
        }
        return snProvider;
    }

    private SensinactService toService(final SensinactProvider parent, final Service svcObject) {
        final SensinactServiceImpl snSvc = new SensinactServiceImpl(new AtomicBoolean(true), parent,
                svcObject.eContainingFeature().getName());

        // List resources
        snSvc.setResources(svcObject.eClass().getEStructuralFeatures().stream()
                .map((feature) -> toResource(snSvc, feature)).collect(Collectors.toList()));
        return snSvc;
    }

    private SensinactResource toResource(final SensinactService parent, final EStructuralFeature rcFeature) {
        return new SensinactResourceImpl(new AtomicBoolean(true), parent, rcFeature.getName(),
                rcFeature.getEType().getInstanceClass(), accumulator, nexusImpl, pf);
    }
}
