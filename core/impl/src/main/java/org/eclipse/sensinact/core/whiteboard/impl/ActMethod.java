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
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.core.annotation.verb.ActParam;
import org.eclipse.sensinact.core.whiteboard.WhiteboardAct;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

class ActMethod extends AbstractResourceMethod implements WhiteboardAct<Object> {

    public ActMethod(Method method, Object instance, Long serviceId, Set<String> providers) {
        super(method, instance, serviceId, providers);
    }

    @Override
    public Promise<Object> act(PromiseFactory promiseFactory, String modelPackageUri, String model, String provider,
            String service, String resource, Map<String, Object> arguments) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Map<Object, Object> rawParam = (Map) arguments;
        try {
            Object value = super.invoke(modelPackageUri, model, provider, service, resource, rawParam, null, null);
            Promise<Object> result;
            if(value instanceof Promise<?> p) {
                // Ensure use of the correct threads
                result = promiseFactory.resolvedWith(p);
            } else {
                result = promiseFactory.resolved(value);
            }
            return result;
        } catch (Exception e) {
            return promiseFactory.failed(e);
        }
    }

    @Override
    protected boolean isAnnotatedParam(Parameter param) {
        return param.isAnnotationPresent(ActParam.class);
    }

    @Override
    protected void validateArg(Parameter param) {
        if(isPromise(param.getParameterizedType())) {
            throw new IllegalArgumentException("Act method parameters must not be Promises");
        }
    }
}
