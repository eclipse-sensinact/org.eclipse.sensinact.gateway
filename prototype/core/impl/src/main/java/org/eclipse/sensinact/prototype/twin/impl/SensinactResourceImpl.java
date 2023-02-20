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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.sensinact.model.core.Provider;
import org.eclipse.sensinact.model.core.ResourceMetadata;
import org.eclipse.sensinact.model.core.Service;
import org.eclipse.sensinact.prototype.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.prototype.model.ResourceType;
import org.eclipse.sensinact.prototype.model.ValueType;
import org.eclipse.sensinact.prototype.model.impl.ResourceImpl;
import org.eclipse.sensinact.prototype.model.nexus.impl.ModelNexus;
import org.eclipse.sensinact.prototype.twin.SensinactResource;
import org.eclipse.sensinact.prototype.twin.SensinactService;
import org.eclipse.sensinact.prototype.twin.TimedValue;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class SensinactResourceImpl extends CommandScopedImpl implements SensinactResource {

    private final SensinactService svc;
    private final Provider provider;
    private final EReference service;
    private final ETypedElement resource;
    private final Class<?> type;
    private final ModelNexus modelNexus;
    private final PromiseFactory promiseFactory;

    public SensinactResourceImpl(AtomicBoolean active, SensinactService svc, Provider provider, EReference service,
            ETypedElement resource, Class<?> type, ModelNexus nexusImpl, PromiseFactory promiseFactory) {
        super(active);
        this.svc = svc;
        this.provider = provider;
        this.service = service;
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

    @Override
    public Promise<Void> setValue(Object value, Instant timestamp) {
        checkValid();

        if (getResourceType() == ResourceType.ACTION) {
            return promiseFactory.failed(new IllegalArgumentException("This is an action resource"));
        }

        modelNexus.handleDataUpdate(modelNexus.getModelName(provider.eClass()), provider, service,
                (EStructuralFeature) resource, value, timestamp);
        return promiseFactory.resolved(null);
    }

    @Override
    public Promise<TimedValue<?>> getValue() {
        checkValid();

        if (getResourceType() == ResourceType.ACTION) {
            return promiseFactory.failed(new IllegalArgumentException("This is an action resource"));
        }

        final Service svc = (Service) provider.eGet(service);
        final Instant timestamp;
        final Object value;
        if (svc != null) {
            value = svc.eGet((EAttribute) resource);
            // Get the resource metadata
            final ResourceMetadata metadata = svc.getMetadata().get(resource);
            if (metadata != null) {
                timestamp = metadata.getTimestamp();
            } else {
                timestamp = null;
            }
        } else {
            value = null;
            timestamp = null;
        }

        return promiseFactory.resolved(new TimedValueImpl<Object>(value, timestamp));
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
            modelNexus.setResourceMetadata(provider, service, resource, name, value, timestamp);
            return promiseFactory.resolved(null);
        } catch (Throwable t) {
            return promiseFactory.failed(t);
        }
    }

    @Override
    public Promise<Object> getMetadataValue(String name) {
        checkValid();

        final Map<String, Object> resourceMetadata = modelNexus.getResourceMetadata(provider, service, resource);
        if (resourceMetadata == null) {
            return promiseFactory.failed(new IllegalArgumentException("Resource not found"));
        } else {
            return promiseFactory.resolved(resourceMetadata.get(name));
        }
    }

    @Override
    public Promise<Map<String, Object>> getMetadataValues() {
        checkValid();

        final Map<String, Object> resourceMetadata = modelNexus.getResourceMetadata(provider, service, resource);
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

        return modelNexus.act(provider, service, resource, parameters);
    }

}
