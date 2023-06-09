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
     * Set the value of the resource
     *
     * @param value
     * @param timestamp
     * @return
     */
    default Promise<Void> setValue(Object value) {
        return setValue(value, Instant.now());
    };

    /**
     * Set the value of the resource
     *
     * @param value
     * @param timestamp
     * @return
     */
    <T> Promise<Void> setValue(T value, Instant timestamp);

    /**
     * Get the value of the resource
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    default Promise<TimedValue<?>> getValue() {
        return getValue((Class) Object.class, GetLevel.CACHED);
    }

    /**
     * Get the typed value of the resource. If the value doesn't match the expected
     * type, null is returned.
     *
     * @param type Expected resource type
     * @return The timed and typed value of the resource
     */
    default <T> Promise<TimedValue<T>> getValue(Class<T> type) {
        return getValue(type, GetLevel.CACHED);
    }

    /**
     * Get the typed value of the resource.
     * If the value doesn't match the expected type, null is returned.
     *
     * @param type Expected resource type
     * @param getLevel Get command level for pull-based resources
     * @return The timed and typed value of the resource
     */
    <T> Promise<TimedValue<T>> getValue(Class<T> type, GetLevel getLevel);

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
    Promise<Object> getMetadataValue(String name);

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
