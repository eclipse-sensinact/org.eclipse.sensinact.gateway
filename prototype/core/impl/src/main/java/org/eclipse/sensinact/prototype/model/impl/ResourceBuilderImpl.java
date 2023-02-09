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
package org.eclipse.sensinact.prototype.model.impl;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.sensinact.prototype.model.Resource;
import org.eclipse.sensinact.prototype.model.ResourceBuilder;
import org.eclipse.sensinact.prototype.model.ResourceType;
import org.eclipse.sensinact.prototype.model.Service;
import org.eclipse.sensinact.prototype.model.ValueType;
import org.eclipse.sensinact.prototype.model.nexus.impl.ModelNexus;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;

public class ResourceBuilderImpl<R, T> extends NestableBuilderImpl<R, Service, Resource>
        implements ResourceBuilder<R, T> {

    private final String name;
    private final ModelNexus nexusImpl;
    private final NotificationAccumulator accumulator;
    private Class<?> type;
    private Object initialValue;
    private Instant timestamp;
    private ResourceType resourceType = null;

    public ResourceBuilderImpl(AtomicBoolean active, R parent, Service builtParent, String name, ModelNexus nexusImpl,
            NotificationAccumulator accumulator) {
        super(active, parent, builtParent);
        this.name = name;
        this.nexusImpl = nexusImpl;
        this.accumulator = accumulator;
    }

    @Override
    public ResourceBuilder<R, T> exclusivelyOwned(boolean exclusive) {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResourceBuilder<R, T> withAutoDeletion(boolean autoDelete) {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U extends T> ResourceBuilder<R, U> withType(Class<U> type) {
        checkValid();
        this.type = type;
        return (ResourceBuilder<R, U>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U extends T> ResourceBuilder<R, U> withInitialValue(U initialValue) {
        checkValid();
        this.initialValue = initialValue;
        return (ResourceBuilder<R, U>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U extends T> ResourceBuilder<R, U> withInitialValue(U initialValue, Instant timestamp) {
        checkValid();
        this.initialValue = initialValue;
        this.timestamp = timestamp;
        return (ResourceBuilder<R, U>) this;
    }

    @Override
    public ResourceBuilder<R, T> withValueType(ValueType valueType) {
        checkValid();
        // TODO make this set some metadata?
        return this;
    }

    @Override
    public ResourceBuilder<R, T> withResourceType(ResourceType resourceType) {
        checkValid();
        this.resourceType = resourceType;
        return this;
    }

    @Override
    public ResourceBuilder<R, T> withAction(Function<Object[], T> action, Class<?>... argumentTypes) {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResourceBuilder<R, T> withGetter(Supplier<T> getter) {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResourceBuilder<R, T> withSetter(Consumer<T> setter) {
        checkValid();
        throw new RuntimeException("Not implemented");
    }

    @Override
    protected void doValidate() {
        super.doValidate();
        if (resourceType == null) {
            resourceType = ResourceType.SENSOR;
        }

        if (resourceType == ResourceType.SENSOR) {
            if (type == null && initialValue == null) {
                throw new IllegalArgumentException("The resource " + name + " must define a type or a value");
            } else if (type == null) {
                type = initialValue.getClass();
            } else if (initialValue != null && !type.isInstance(initialValue)) {
                throw new IllegalArgumentException("The initial value " + initialValue + " for resource " + name
                        + " is not compatible with the type " + type.getName());
            }
        }
    }

    @Override
    protected Resource doBuild(Service builtParent) {
        return new ResourceImpl(active, builtParent, nexusImpl.createResource(builtParent.getModel().getName(),
                builtParent.getName(), name, type, initialValue, timestamp, accumulator));
    }

}
