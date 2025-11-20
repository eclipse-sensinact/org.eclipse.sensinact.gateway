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

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.sensinact.core.command.GetLevel;
import org.eclipse.sensinact.core.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.model.impl.ResourceImpl;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.model.core.provider.ResourceMetadata;
import org.eclipse.sensinact.model.core.provider.ResourceValueMetadata;
import org.eclipse.sensinact.model.core.provider.Service;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class SensinactResourceImpl extends CommandScopedImpl implements SensinactResource {

    private final SensinactServiceImpl svc;
    private final Provider provider;
    private final String serviceName;
    private final ETypedElement resource;
    private final Class<?> type;
    private final ModelNexus modelNexus;
    private final PromiseFactory promiseFactory;

    public SensinactResourceImpl(AtomicBoolean active, SensinactServiceImpl svc, Provider provider, String serviceName,
            ETypedElement resource, Class<?> type, ModelNexus nexusImpl, PromiseFactory promiseFactory) {
        super(active);
        this.svc = svc;
        this.provider = provider;
        this.serviceName = serviceName;
        this.resource = resource;
        this.type = type;
        this.modelNexus = nexusImpl;
        this.promiseFactory = promiseFactory;
    }

    @Override
    public Class<?> getType() {
        checkValid();
        return type;
    }

    @Override
    public ValueType getValueType() {
        checkValid();
        return ResourceImpl.findValueType(resource);
    }

    @Override
    public ResourceType getResourceType() {
        checkValid();
        return ResourceImpl.findResourceType(resource);
    }

    @Override
    public List<Map.Entry<String, Class<?>>> getArguments() {
        checkValid();
        if (getResourceType() != ResourceType.ACTION) {
            throw new IllegalArgumentException("This is not an action resource");
        }
        return ResourceImpl.findActionParameters((EOperation) resource);
    }

    @Override
    public String getName() {
        checkValid();
        return resource.getName();
    }

    /**
     * Returns the current value from the twin (cached state)
     *
     * @param <T>  Value type generic
     * @param type Value type class
     * @return The timed value from the service (both value and timestamp can be
     *         null)
     */
    @SuppressWarnings("unchecked")
    private <T> TimedValue<T> getValueFromTwin(final Class<T> type) {
        final Instant currentTimestamp;
        final T currentValue;
        final Service svc = provider.getService(serviceName);
        if (svc != null) {
            // Service is there
            EStructuralFeature feature = (EStructuralFeature) resource;
            final Object rawValue = svc.eGet(feature);
            if (feature.isMany()) {
                // TODO: How to handle this?
//                currentValue = ((Collection<?>) rawValue).stream().filter(o -> type.isAssignableFrom(o.getClass()))
//                        .map(type::cast).toList();
//            } else if (rawValue != null && type.isAssignableFrom(rawValue.getClass())) {
            }
            if (rawValue != null && type.isAssignableFrom(rawValue.getClass())) {
                currentValue = type.cast(rawValue);
            } else {
                currentValue = null;
            }
            // Get the resource metadata
            final ResourceValueMetadata metadata = svc.getMetadata().get(resource);
            if (metadata != null) {
                currentTimestamp = metadata.getTimestamp();
            } else {
                currentTimestamp = null;
            }
        } else {
            // Service (and resource) is not ready yet
            currentValue = null;
            currentTimestamp = null;
        }

        return new DefaultTimedValue<T>(currentValue, currentTimestamp);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Promise<Void> setValue(T value, Instant timestamp) {
        checkValid();

        if (getResourceType() == ResourceType.ACTION) {
            return promiseFactory.failed(new IllegalArgumentException("This is an action resource"));
        }

        try {
            final boolean hasExternalSetter;
            ResourceMetadata metadata = (ResourceMetadata) EMFUtil.getModelMetadata(resource);
            if (metadata != null) {
                // Resource created by ResourceBuilder
                hasExternalSetter = metadata.isExternalSet();
            } else {
                // Predefined resource (admin service)
                hasExternalSetter = false;
            }

            if (hasExternalSetter) {
                // Check new value type
                final TimedValue<?> cachedValue = getValueFromTwin(type);
                final TimedValue<T> newValue = new DefaultTimedValue<T>(value, timestamp);
                return modelNexus.pushValue(provider, serviceName, resource, (Class<T>) type,
                        (TimedValue<T>) cachedValue, newValue).map(x -> null);
            } else {
                // No external setter: update the twin
                modelNexus.handleDataUpdate(provider, serviceName, svc.getServiceEClass(),
                        (EStructuralFeature) resource, value, timestamp);
                return promiseFactory.resolved(null);
            }
        } catch (Exception e) {
            return promiseFactory.failed(e);
        }
    }

    @Override
    public <T> Promise<TimedValue<T>> getValue(final Class<T> type, final GetLevel getLevel) {
        checkValid();

        if (getResourceType() == ResourceType.ACTION) {
            return promiseFactory.failed(new IllegalArgumentException("This is an action resource"));
        }

        // Check if the resource is pull based
        final boolean hasExternalGetter;
        final Duration cacheThreshold;
        ResourceMetadata metadata = (ResourceMetadata) EMFUtil.getModelMetadata(resource);
        if (metadata != null) {
            // Resource created by ResourceBuilder
            hasExternalGetter = metadata.isExternalGet();
            cacheThreshold = Duration.of(metadata.getExternalGetCacheMs(), ChronoUnit.MILLIS);
        } else {
            // Predefined resource (admin service)
            hasExternalGetter = false;
            cacheThreshold = null;
        }

        // Get the currently cached value
        final TimedValue<T> cachedValue = getValueFromTwin(type);
        if (!hasExternalGetter || getLevel == GetLevel.WEAK) {
            // Push-based or weak get: return the cached value
            return promiseFactory.resolved(cachedValue);
        } else if (getLevel == GetLevel.STRONG || cachedValue.getTimestamp() == null || cacheThreshold == null
                || Instant.now().minus(cacheThreshold).isAfter(cachedValue.getTimestamp())) {
            // Hard get or no value or no cache policy or threshold exceed: pull the
            // value...
            return modelNexus.pullValue(provider, serviceName, resource, type, cachedValue);
        } else {
            // Return the cached value
            return promiseFactory.resolved(cachedValue);
        }
    }

    @Override
    public SensinactService getService() {
        checkValid();
        return svc;
    }

    @Override
    public Promise<Void> setMetadataValue(String name, Object value, Instant timestamp) {
        checkValid();

        try {
            if (!svc.isSet()) {
                modelNexus.createServiceInstance(provider, serviceName, svc.getServiceEClass());
            }

            modelNexus.setResourceMetadata(provider, serviceName, resource, name, value, timestamp);
            return promiseFactory.resolved(null);
        } catch (Throwable t) {
            return promiseFactory.failed(t);
        }
    }

    @Override
    public Promise<TimedValue<Object>> getMetadataValue(String name) {
        checkValid();

        final TimedValue<Object> resourceMetadata = modelNexus.getResourceMetadataValue(provider, serviceName, resource,
                name);
        if (resourceMetadata == null) {
            return promiseFactory.failed(new IllegalArgumentException("Resource metadata not found"));
        } else {
            return promiseFactory.resolved(resourceMetadata);
        }
    }

    @Override
    public Promise<Map<String, Object>> getMetadataValues() {
        checkValid();

        final Map<String, Object> resourceMetadata = modelNexus.getResourceMetadata(provider, serviceName, resource);
        if (resourceMetadata == null) {
            return promiseFactory.failed(new IllegalArgumentException("Resource not found"));
        } else {
            return promiseFactory.resolved(resourceMetadata);
        }
    }

    @Override
    public Promise<Object> act(Map<String, Object> parameters) {
        checkValid();
        if (getResourceType() != ResourceType.ACTION) {
            throw new UnsupportedOperationException(
                    "Resource " + String.format("%s/%s/%s", provider.getId(), svc.getName(), getName())
                            + " is not an ACTION. Only ACTION resources can use the ACT operation");
        }

        return modelNexus.act(provider, serviceName, resource, parameters);
    }

}
