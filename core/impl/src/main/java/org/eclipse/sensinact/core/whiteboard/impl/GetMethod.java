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
import java.lang.reflect.Parameter;
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

    @Override
    public Promise<TimedValue<?>> pullValue(PromiseFactory pf, String modelPackageUri, String model,
            String provider, String service, String resource, Class<Object> resourceType, TimedValue<?> cachedValue) {

        try {
            final Map<Object, Object> params = new HashMap<>();
            params.put(GetSegment.RESULT_TYPE, resourceType);
            params.put(GetSegment.CACHED_VALUE, cachedValue);
            Object result = super.invoke(modelPackageUri, model, provider, service, resource, params, GetParam.class,
                    GetParam::value);

            Promise<TimedValue<?>> toReturn;
            if (result instanceof Promise<?> p) {
                toReturn = pf.resolvedWith(p).map(o -> convertToTvIfNeeded(o, resourceType, cachedValue));
            } else {
                toReturn = pf.resolved(convertToTvIfNeeded(result, resourceType, cachedValue));
            }

            return toReturn;
        } catch (Exception e) {
            return pf.failed(e);
        }
    }

    private TimedValue<?> convertToTvIfNeeded(Object o, Class<?> resourceType, TimedValue<?> cachedValue) throws Exception {
        if(o == null) {
            switch (nullAction) {
            case IGNORE:
                return null;
            case UPDATE_IF_PRESENT:
                return cachedValue == null || cachedValue.getTimestamp() == null ? null
                        : new DefaultTimedValue<>(null);
            case UPDATE:
                return new DefaultTimedValue<>(null);
            default:
                throw new IllegalArgumentException("Unknown null action: " + nullAction);
            }
        } else if(o instanceof TimedValue<?> t) {
            if(t.isEmpty() || t.getValue() == null) {
                return t;
            }
            return new DefaultTimedValue<>(convertValueIfNeeded(t.getValue(), resourceType), t.getTimestamp());
        } else {
            return new DefaultTimedValue<>(convertValueIfNeeded(o, resourceType));
        }
    }

    @Override
    protected boolean isAnnotatedParam(Parameter param) {
        return param.isAnnotationPresent(GetParam.class);
    }

    @Override
    protected void validateArg(Parameter param) {
        GetParam get = param.getAnnotation(GetParam.class);
        if(get == null) {
            throw new IllegalArgumentException("The parameter " + param + " in method " +
                    param.getDeclaringExecutable() + " is not annotated with GetParam");
        }
        switch (get.value()) {
        case CACHED_VALUE:
            if(isPromise(param.getType())) {
                throw new IllegalArgumentException("The parameter " + param + " in method " +
                        param.getDeclaringExecutable() + " has a GetParam value CACHED_VALUE but receives a Promise");
            }
            break;
        case RESULT_TYPE:
            if(!param.getType().equals(Class.class)) {
                throw new IllegalArgumentException("The parameter " + param + " in method " +
                        param.getDeclaringExecutable() + " has a GetParam value RESULT_TYPE but is not of type Class");
            }
            break;
        default:
            throw new IllegalArgumentException("The parameter " + param + " in method " +
                    param.getDeclaringExecutable() + " has an unknown GetParam value " + get.value());
        }
    }
}
