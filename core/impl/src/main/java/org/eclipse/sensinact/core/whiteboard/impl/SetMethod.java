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

import org.eclipse.sensinact.core.annotation.verb.SetParam;
import org.eclipse.sensinact.core.annotation.verb.SetParam.SetSegment;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.core.whiteboard.WhiteboardSet;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

class SetMethod extends AbstractResourceMethod implements WhiteboardSet<Object> {

    public SetMethod(Method method, Object instance, Long serviceId, Set<String> providers) {
        super(method, instance, serviceId, providers);
    }

    @Override
    public Promise<TimedValue<?>> pushValue(PromiseFactory pf, String modelPackageUri, String model,
            String provider, String service, String resource, Class<Object> resourceType,
            TimedValue<?> cachedValue, TimedValue<?> newValue) {

        final Map<Object, Object> params = new HashMap<>();
        params.put(SetSegment.RESULT_TYPE, resourceType);
        params.put(SetSegment.CACHED_VALUE, cachedValue);
        params.put(SetSegment.NEW_VALUE, newValue);
        try {
            Object result = super.invoke(modelPackageUri, model, provider, service, resource, params, SetParam.class,
                    SetParam::value);
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

    @Override
    public Promise<TimedValue<?>> pullValue(PromiseFactory promiseFactory, String modelPackageUri, String model,
            String provider, String service, String resource, Class<Object> resourceType,
            TimedValue<?> cachedValue) {
        return promiseFactory.resolved(cachedValue);
    }

    private TimedValue<?> convertToTvIfNeeded(Object o, Class<?> resourceType, TimedValue<?> cachedValue) throws Exception {
        if(o == null) {
            return new DefaultTimedValue<>(null);
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
        return param.isAnnotationPresent(SetParam.class);
    }

    @Override
    protected void validateArg(Parameter param) {
        SetParam set = param.getAnnotation(SetParam.class);
        if(set == null) {
            throw new IllegalArgumentException("The parameter " + param + " in method " +
                    param.getDeclaringExecutable() + " is not annotated with SetParam");
        }
        switch (set.value()) {
        case NEW_VALUE:
        case CACHED_VALUE:
            if(isPromise(param.getType())) {
                throw new IllegalArgumentException("The parameter " + param + " in method " +
                        param.getDeclaringExecutable() + " has a SetParam value " + set.value() + " but receives a Promise");
            }
            break;
        case RESULT_TYPE:
            if(!param.getType().equals(Class.class)) {
                throw new IllegalArgumentException("The parameter " + param + " in method " +
                        param.getDeclaringExecutable() + " has a SetParam value RESULT_TYPE but is not of type Class");
            }
            break;
        default:
            throw new IllegalArgumentException("The parameter " + param + " in method " +
                    param.getDeclaringExecutable() + " has an unknown SetParam value " + set.value());
        }
    }
}
