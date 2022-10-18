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
package org.eclipse.sensinact.prototype.model;

import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A builder for programmatically registering models
 */
public interface ResourceBuilder<T> {

    ResourceBuilder<T> exclusivelyOwned(boolean exclusive);

    ResourceBuilder<T> withAutoDeletion(boolean autoDelete);

    /**
     * The type of the resource. If not set then it is inferred from the type of the
     * initial value. If neither is set then String is used.
     * 
     * @param <R>
     * @param type
     * @return
     */
    <R> ResourceBuilder<R> withType(Class<R> type);

    /**
     * The initial value of the resource, set at the time of resource creation
     * 
     * @param initialValue
     * @return
     */
    ResourceBuilder<T> withInitialValue(T initialValue);

    /**
     * The initial value of the resource, set at the provided time
     * 
     * @param initialValue
     * @return
     */
    ResourceBuilder<T> withInitialValue(T initialValue, Instant timestamp);

    /**
     * The type of the value - must be consistent with the built model, e.g. if
     * {@link #withSetter(Consumer)} is called then the valueType must be
     * {@link ValueType#MODIFIABLE}
     * 
     * @param valueType
     * @return
     */
    ResourceBuilder<T> withValueType(ValueType valueType);

    /**
     * The resource type - must be consistent with the built model, e.g. if
     * {@link #withAction(Function, Class...)} is called then the resource type must
     * be {@link ResourceType#ACTION}
     * 
     * @param resourceType
     * @return
     */
    ResourceBuilder<T> withResourceType(ResourceType resourceType);

    /**
     * Set an action function to be called, including the types of any arguments
     * that should be passed
     * 
     * @param action
     * @param arguments
     * @return
     */
    ResourceBuilder<T> withAction(Function<Object[], T> action, Class<?>... argumentTypes);

    /**
     * Set a getter function to be called
     * 
     * @param getter
     * @return
     */
    ResourceBuilder<T> withGetter(Supplier<T> getter);

    /**
     * Set a setter function to be called
     * 
     * @param setter
     * @return
     */
    ResourceBuilder<T> withSetter(Consumer<T> setter);

    /**
     * Build the resource
     * 
     * @return
     * @throws IllegalArgumentException if an invalid model is defined, e.g. having
     *                                  a getter and an action.
     */
    Resource build();
}
