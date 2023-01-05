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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sensinact.model.core.Metadata;
import org.eclipse.sensinact.model.core.Provider;
import org.eclipse.sensinact.model.core.Service;
import org.eclipse.sensinact.prototype.command.SensinactModel;
import org.eclipse.sensinact.prototype.command.SensinactProvider;
import org.eclipse.sensinact.prototype.command.SensinactResource;
import org.eclipse.sensinact.prototype.command.SensinactService;
import org.eclipse.sensinact.prototype.command.TimedValue;
import org.eclipse.sensinact.prototype.model.nexus.impl.ModelNexus;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.osgi.util.promise.PromiseFactory;

public class SensinactModelImpl extends CommandScopedImpl implements SensinactModel {

    private final NotificationAccumulator accumulator;
    private final ModelNexus nexusImpl;
    private final PromiseFactory pf;

    public SensinactModelImpl(NotificationAccumulator accumulator, ModelNexus nexusImpl, PromiseFactory pf) {
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
    public SensinactProvider getProvider(String providerName) {
        final Provider provider = nexusImpl.getProvider(providerName);
        if (provider == null) {
            return null;
        }

        return toProvider(provider);
    }

    @Override
    public SensinactService getService(String model, String providerName, String service) {
        return getService(nexusImpl.getProvider(model, providerName), model, service);
    }

    @Override
    public SensinactService getService(String providerName, String service) {
        Provider provider = nexusImpl.getProvider(providerName);
        return getService(provider, getProviderModel(provider), service);
    }

    private String getProviderModel(Provider provider) {

        if (provider == null) {
            return null;
        }

        URI instanceUri = EcoreUtil.getURI(provider);
        if (instanceUri.segmentCount() < 2) {
            // TODO is this correct?
            return provider.getId();
        } else {
            return instanceUri.segment(instanceUri.segmentCount() - 2);
        }
    }

    private SensinactService getService(Provider provider, String model, String service) {
        if (provider == null) {
            return null;
        }

        final EStructuralFeature svcFeature = provider.eClass().getEStructuralFeature(service);
        if (svcFeature == null) {
            return null;
        }
        final Service svc = (Service) provider.eGet(svcFeature);

        final SensinactProviderImpl snProvider = new SensinactProviderImpl(new AtomicBoolean(true),
                provider.eClass().getName(), provider.getId());
        return toService(snProvider, svc);
    }

    @Override
    public SensinactResource getResource(String model, String providerName, String service, String resource) {
        return getResource(nexusImpl.getProvider(model, providerName), model, service, resource);
    }

    public SensinactResource getResource(String providerName, String service, String resource) {
        Provider provider = nexusImpl.getProvider(providerName);
        return getResource(provider, getProviderModel(provider), service, resource);
    }

    private SensinactResource getResource(Provider provider, String model, String service, String resource) {
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
        final SensinactProviderImpl snProvider = new SensinactProviderImpl(active, model, provider.getId());
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
        return getResourceValue(nexusImpl.getProvider(model, providerName), service, resource, type);
    }

    public <T> TimedValue<T> getResourceValue(String providerName, String service, String resource, Class<T> type) {
        return getResourceValue(nexusImpl.getProvider(providerName), service, resource, type);
    }

    public <T> TimedValue<T> getResourceValue(Provider provider, String service, String resource, Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Resource type must not be null");
        }

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

        // Check value type
        final Object rawValue = svc.eGet(rcFeature);
        if (rawValue == null) {
            return new TimedValueImpl<T>(null, timestamp);
        } else if (!type.isAssignableFrom(rawValue.getClass())) {
            throw new IllegalArgumentException(
                    "Expected a " + type.getName() + " but resource is a " + rawValue.getClass().getName());
        } else {
            return new TimedValueImpl<T>(type.cast(rawValue), timestamp);
        }
    }

    @Override
    public void setOrCreateResource(String model, String providerName, String service, String resource, Class<?> type,
            Object value, Instant instant) {
        checkValid();

        nexusImpl.handleDataUpdate(model, providerName, service, resource, type, value, instant);
    }

    public void setOrCreateResource(String providerName, String service, String resource, Class<?> type, Object value,
            Instant instant) {
        checkValid();

        Provider provider = nexusImpl.getProvider(providerName);

        String modelName;
        if (provider != null) {
            modelName = getProviderModel(provider);
        } else {
            modelName = providerName;
        }

        nexusImpl.handleDataUpdate(modelName, providerName, service, resource, type, value, instant);
    }

    private SensinactProviderImpl toProvider(final Provider modelProvider) {
        return toProvider(modelProvider, true);
    }

    private SensinactProviderImpl toProvider(final Provider modelProvider, boolean loadServices) {
        // Construct the provider bean
        final SensinactProviderImpl snProvider = new SensinactProviderImpl(new AtomicBoolean(true),
                getProviderModel(modelProvider), modelProvider.getId());

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
