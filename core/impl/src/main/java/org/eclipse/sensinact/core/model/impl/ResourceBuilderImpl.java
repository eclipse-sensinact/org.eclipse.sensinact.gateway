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
package org.eclipse.sensinact.core.model.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.sensinact.core.model.Resource;
import org.eclipse.sensinact.core.model.ResourceBuilder;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;

public class ResourceBuilderImpl<R, T> extends NestableBuilderImpl<R, ServiceImpl, Resource>
        implements ResourceBuilder<R, T> {

    private final String name;
    private final ModelNexus nexusImpl;
    private Class<?> type;
    private Object initialValue;
    private Instant timestamp;
    private ResourceType resourceType = null;
    private List<Entry<String, Class<?>>> namedParameterTypes;
    private boolean hasGetter;
    private long getterCacheMs;
    private boolean hasSetter;

    public ResourceBuilderImpl(AtomicBoolean active, R parent, ServiceImpl builtParent, String name,
            ModelNexus nexusImpl) {
        super(active, parent, builtParent);
        this.name = name;
        this.nexusImpl = nexusImpl;
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
    public ResourceBuilder<R, T> withAction(List<Entry<String, Class<?>>> namedParameterTypes) {
        checkValid();
        resourceType = ResourceType.ACTION;
        this.namedParameterTypes = namedParameterTypes;
        return this;
    }

    @Override
    public ResourceBuilder<R, T> withGetter(boolean hasGet) {
        checkValid();
        this.hasGetter = hasGet;
        return this;
    }

    @Override
    public ResourceBuilder<R, T> withGetterCache(Duration cacheDuration) {
        checkValid();
        if (cacheDuration == null || cacheDuration.isNegative()) {
            this.getterCacheMs = 0;
        } else {
            this.getterCacheMs = cacheDuration.toMillis();
        }
        return this;
    }

    @Override
    public ResourceBuilder<R, T> withSetter(boolean hasSet) {
        checkValid();
        this.hasSetter = hasSet;
        return this;
    }

    @Override
    protected void doValidate() {
        super.doValidate();
        if (resourceType == null) {
            resourceType = ResourceType.SENSOR;
        }

        if (resourceType == ResourceType.SENSOR) {
            if (namedParameterTypes != null) {
                throw new IllegalArgumentException("Action details cannot be set for a SENSOR resource");
            }

            if (type == null && initialValue == null) {
                throw new IllegalArgumentException("The resource " + name + " must define a type or a value");
            } else if (type == null) {
                type = initialValue.getClass();
            } else if (initialValue != null && !type.isInstance(initialValue)) {
                throw new IllegalArgumentException("The initial value " + initialValue + " for resource " + name
                        + " is not compatible with the type " + type.getName());
            }
        } else if (resourceType == ResourceType.ACTION) {
            if (type == null) {
                throw new IllegalArgumentException("The action resource " + name + " must define a type");
            }
            if (namedParameterTypes == null) {
                throw new IllegalArgumentException("The action resource " + name + " must define parameters");
            }
            if (hasGetter || hasSetter) {
                throw new IllegalArgumentException("Can't define an external get or set on an ACTION resource");
            }
        } else {
            throw new RuntimeException("No implemented support for type " + resourceType);
        }
    }

    @Override
    protected Resource doBuild(ServiceImpl builtParent) {
        ETypedElement createResource;
        switch (resourceType) {
        case ACTION:
            createResource = nexusImpl.createActionResource(builtParent.getServiceEClass(), name, type,
                    namedParameterTypes);
            break;
        case SENSOR:
            createResource = nexusImpl.createResource(builtParent.getServiceEClass(), name, type, timestamp,
                    initialValue, hasGetter, getterCacheMs, hasSetter);
            break;
        case PROPERTY:
        case STATE_VARIABLE:
        default:
            throw new RuntimeException("Should be unreachable");
        }
        return new ResourceImpl(active, builtParent, createResource);
    }

}
