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
*   Kentyou - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.core.whiteboard.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.core.annotation.dto.NullAction;
import org.eclipse.sensinact.core.annotation.verb.GetParam;
import org.eclipse.sensinact.core.annotation.verb.GetParam.GetSegment;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.core.whiteboard.WhiteboardGet;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

class GetMethod extends AbstractResourceMethod implements WhiteboardGet<Object> {

    /**
     * Action to apply when the result is null
     */
    private final NullAction nullAction;

    public GetMethod(Method method, Object instance, Long serviceId, Set<String> providers, NullAction onNull) {
        super(method, instance, serviceId, providers);
        this.nullAction = onNull;
    }

    public NullAction actionOnNull() {
        return nullAction;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Promise<TimedValue<Object>> pullValue(PromiseFactory pf, String modelPackageUri, String model,
            String provider, String service, String resource, Class<Object> resourceType, TimedValue<Object> cachedValue) {

        try {
            final Map<Object, Object> params = new HashMap<>();
            params.put(GetSegment.RESULT_TYPE, resourceType);
            params.put(GetSegment.CACHED_VALUE, cachedValue);
            Object result = super.invoke(modelPackageUri, model, provider, service, resource, params, GetParam.class,
                    GetParam::value);

            if (result instanceof Promise) {
                return (Promise<TimedValue<Object>>) result;
            } else if (result instanceof TimedValue) {
                return pf.resolved((TimedValue<Object>) result);
            } else if (result == null) {
                switch (nullAction) {
                case IGNORE:
                    return pf.resolved(null);
                case UPDATE_IF_PRESENT:
                    return pf.resolved(cachedValue == null || cachedValue.getTimestamp() == null ? null
                            : new DefaultTimedValue<Object>(null));
                case UPDATE:
                    return pf.resolved(new DefaultTimedValue<Object>(null));
                default:
                    return pf.failed(new IllegalArgumentException("Unknown null action: " + nullAction));
                }
            } else if (resourceType.isAssignableFrom(result.getClass())) {
                return pf.resolved(new DefaultTimedValue<Object>(resourceType.cast(result)));
            } else {
                return pf.failed(new Exception("Invalid result type: " + result.getClass()));
            }
        } catch (Exception e) {
            return pf.failed(e);
        }
    }
}
