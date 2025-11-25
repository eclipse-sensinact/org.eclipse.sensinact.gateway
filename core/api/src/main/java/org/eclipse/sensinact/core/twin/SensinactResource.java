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
package org.eclipse.sensinact.core.twin;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.command.CommandScoped;
import org.eclipse.sensinact.core.command.GetLevel;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;
import org.osgi.util.promise.Promise;

public interface SensinactResource extends CommandScoped {

    /**
     * The resource name. Defined by the model
     *
     * @return
     */
    String getName();

    /**
     * The type of the resource. Defined by the model
     *
     * @return
     */
    Class<?> getType();

    /**
     * The type of the value
     *
     * @return
     */
    ValueType getValueType();

    /**
     * The type of the resource
     *
     * @return
     */
    ResourceType getResourceType();

    /**
     * The list of arguments for a {@link ResourceType#ACTION} resource
     *
     * @return
     * @throws IllegalStateException if this resource is not an action resource
     */
    List<Map.Entry<String, Class<?>>> getArguments();

    /**
     * The minimum number of entries supported by this resource.
     * @return the lower bound for this resource defined by the model
     */
    int getLowerBound();

    /**
     * The maximum number of entries supported by this resource. If greater than
     * one then this is a multiple resource (see {@link #isMultiple()})
     * @return the upper bound for this resource defined by the model
     */
    int getUpperBound();

    /**
     * Whether this resource is multi-valued (i.e. the model has an upper bound not equal to 1)
     * @return <code>true</code> if this resource is multi-valued
     */
    boolean isMultiple();

    /**
     * Set the value of the resource with the current time
     *
     * @param value If the value is a Collection and this resource
     * is multi-valued then the values in the collection will be used
     * to set the values in the resource.
     * @return a {@link Promise} representing the result of setting the
     * value. The {@link Promise} will be failed if the value cannot be
     * set, for example:
     * <ul>
     *   <li>Due to a conversion failure</li>
     *   <li>If the value violates the minumum or maximum bounds for this resource</li>
     * </ul>
     */
    default Promise<Void> setValue(Object value) {
        return setValue(value, Instant.now());
    };

    /**
     * Set the value of the resource. If this resource is multi-valued then
     * this value will become the only entry
     *
     * @param value The single value to use as the value of this resource
     * @param timestamp
     * @return a {@link Promise} representing the result of setting the
     * value. The {@link Promise} will be failed if the value cannot be
     * set, for example:
     * <ul>
     *   <li>Due to a conversion failure</li>
     *   <li>If the value violates the minumum or maximum bounds for this resource</li>
     * </ul>
     */
    <T> Promise<Void> setValue(T value, Instant timestamp);

    /**
     * Set the value of a multi-valued resource
     *
     * @param value
     * @return a {@link Promise} representing the result of setting the
     * value. The {@link Promise} will be failed if the value cannot be
     * set, for example:
     * <ul>
     *   <li>Due to a conversion failure</li>
     *   <li>If the value violates the minumum or maximum bounds for this resource</li>
     * </ul>
     */
    default <T> Promise<Void> setValue(List<? extends T> value){
        return setValue(value, Instant.now());
    }

    /**
     * Set the value of a multi-valued resource
     *
     * @param value
     * @param timestamp
     * @return a {@link Promise} representing the result of setting the
     * value. The {@link Promise} will be failed if the value cannot be
     * set, for example:
     * <ul>
     *   <li>Due to a conversion failure</li>
     *   <li>If the value violates the minumum or maximum bounds for this resource</li>
     * </ul>
     */
    <T> Promise<Void> setValue(List<? extends T> value, Instant timestamp);

    /**
     * Get the value of the resource
     * @return a {@link Promise} containing the value. This may be a List if
     * this resource {@link #isMultiple()}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    default Promise<TimedValue<?>> getValue() {
        return isMultiple() ? getMultiValue((Class) Object.class, GetLevel.NORMAL) :
            getValue((Class) Object.class, GetLevel.NORMAL);
    }

    /**
     * Get the typed value of the resource. If the value doesn't match the expected
     * type, null is returned. If this resource {@link #isMultiple()} then only the
     * first value is returned in the {@link Promise}
     *
     * @param type Expected resource type
     * @return The timed and typed value of the resource
     */
    default <T> Promise<TimedValue<T>> getValue(Class<T> type) {
        return getValue(type, GetLevel.NORMAL);
    }

    /**
     * Get the typed value of the resource.
     * If the value doesn't match the expected type, null is returned.
     * If this resource {@link #isMultiple()} then only the first
     * value is returned in the {@link Promise}
     *
     * @param type Expected resource type
     * @param getLevel Get command level for pull-based resources
     * @return The timed and typed value of the resource
     */
    <T> Promise<TimedValue<T>> getValue(Class<T> type, GetLevel getLevel);

    /**
     * Get the typed value of the resource.
     * If the value doesn't match the expected type, null is returned.
     * If this resource is not {@link #isMultiple()} then the single
     * value is returned wrapped in a {@link List} in the {@link Promise}
     *
     * @param type Expected resource type
     * @return The timed and typed value of the resource
     */
    default <T> Promise<TimedValue<List<T>>> getMultiValue(Class<T> type) {
        return getMultiValue(type, GetLevel.NORMAL);
    }

    /**
     * Get the typed value of the resource.
     * If the value doesn't match the expected type, null is returned.
     * If this resource is not {@link #isMultiple()} then the single
     * value is returned wrapped in a {@link List} in the {@link Promise}
     *
     * @param type Expected resource type
     * @param getLevel Get command level for pull-based resources
     * @return The timed and typed value of the resource
     */
    <T> Promise<TimedValue<List<T>>> getMultiValue(Class<T> type, GetLevel getLevel);

    /**
     * Set a metadata value for the resource
     *
     * @param value
     * @param timestamp
     * @return
     */
    Promise<Void> setMetadataValue(String name, Object value, Instant timestamp);

    /**
     * Get a metadata value for the resource
     *
     * @param value
     * @param timestamp
     * @return
     */
    Promise<TimedValue<Object>> getMetadataValue(String name);

    /**
     * Get all metadata values for the resource
     *
     * @param value
     * @param timestamp
     * @return
     */
    Promise<Map<String, Object>> getMetadataValues();

    /**
     * Get the service instance which holds this resource
     *
     * @return
     */
    SensinactService getService();

    /**
     * Act on this resource
     *
     * @param parameters
     * @return
     */
    Promise<Object> act(Map<String, Object> parameters);

}
