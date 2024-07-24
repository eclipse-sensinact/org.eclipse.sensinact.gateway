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
package org.eclipse.sensinact.core.model;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A builder for programmatically registering models
 */
public interface ResourceBuilder<B, T> {

    ResourceBuilder<B, T> exclusivelyOwned(boolean exclusive);

    ResourceBuilder<B, T> withAutoDeletion(boolean autoDelete);

    /**
     * The type of the resource. If not set then it is inferred from the type of the
     * initial value. If neither is set then String is used.
     *
     * @param <R>
     * @param type
     * @return
     */
    <U extends T> ResourceBuilder<B, U> withType(Class<U> type);

    /**
     * The initial value of the resource, set at the time of resource creation
     *
     * @param initialValue
     * @return
     */
    <U extends T> ResourceBuilder<B, U> withInitialValue(U initialValue);

    /**
     * The initial value of the resource, set at the provided time
     *
     * @param initialValue
     * @return
     */
    <U extends T> ResourceBuilder<B, U> withInitialValue(U initialValue, Instant timestamp);

    /**
     * Default metadata that should be provided by the model for this resource
     * @param defaultMetadata
     * @return
     */
    ResourceBuilder<B, T> withDefaultMetadata(Map<String, Object> defaultMetadata);

    /**
     * The type of the value - must be consistent with the built model, e.g. if
     * {@link #withSetter(Consumer)} is called then the valueType must be
     * {@link ValueType#MODIFIABLE}
     *
     * @param valueType
     * @return
     */
    ResourceBuilder<B, T> withValueType(ValueType valueType);

    /**
     * The resource type - must be consistent with the built model, e.g. if
     * {@link #withAction(Function, Class...)} is called then the resource type must
     * be {@link ResourceType#ACTION}
     *
     * @param resourceType
     * @return
     */
    ResourceBuilder<B, T> withResourceType(ResourceType resourceType);

    /**
     * Set a whiteboard action to be called, including the types of any arguments
     * that should be passed
     *
     * @param returnType
     * @param namedParameterTypes
     * @return
     */
    ResourceBuilder<B, T> withAction(List<Entry<String, Class<?>>> namedParameterTypes);

    /**
     * This resource has a dynamic get behavior. This can't be applied on an action
     * resource.
     */
    default ResourceBuilder<B, T> withGetter() {
        return withGetter(true);
    }

    /**
     * Indicates if the resource has a dynamic get behavior. Action resources can't
     * have such a behavior.
     *
     * @param hasGetter Flag to indicate if the resource has a dynamic get or not
     */
    ResourceBuilder<B, T> withGetter(boolean hasGetter);

    /**
     * Sets the cache duration for dynamic get calls
     *
     * @param cacheDuration Duration of the cache
     */
    ResourceBuilder<B, T> withGetterCache(Duration cacheDuration);

    /**
     * This resource has a dynamic set behavior. This can't be applied on an action
     * resource.
     */
    default ResourceBuilder<B, T> withSetter() {
        return withSetter(true);
    }

    /**
     * Indicates if the resource has a dynamic set behavior. Action resources can't
     * have such a behavior.
     *
     * @param hasSetter Flag to indicate if the resource has a dynamic set or not
     */
    ResourceBuilder<B, T> withSetter(boolean hasSetter);

    /**
     * Build the resource
     *
     * @return
     * @throws IllegalArgumentException if an invalid model is defined, e.g. having
     *                                  a getter and an action.
     */
    B build();

    void buildAll();
}
