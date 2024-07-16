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

package org.eclipse.sensinact.core.snapshot;

import java.util.Map;

import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.twin.TimedValue;

public interface ResourceSnapshot extends Snapshot {

    /**
     * Returns the snapshot of the parent service
     */
    ServiceSnapshot getService();

    /**
     * Flag to indicate if the resource has been set for that provider.
     */
    boolean isSet();

    /**
     * Returns the class of the value this resource holds. (Object for any)
     */
    Class<?> getType();

    /**
     * Returns the (cached) value of the resource at the time of the snapshot. Can
     * be null if the value was never set.
     */
    TimedValue<?> getValue();

    /**
     * Returns the metadata associated to the resource
     */
    Map<String, Object> getMetadata();

    /**
     * Returns the kind of resource, see {@link ResourceType}
     */
    ResourceType getResourceType();

    /**
     * Returns the value type, <em>i.e.</em> the kind of access it allows. See
     * {@link ValueType}.
     */
    ValueType getValueType();
}
