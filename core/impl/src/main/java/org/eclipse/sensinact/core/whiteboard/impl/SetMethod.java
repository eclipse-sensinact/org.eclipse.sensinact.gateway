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

import org.eclipse.sensinact.core.annotation.verb.SetParam;
import org.eclipse.sensinact.core.annotation.verb.SetParam.SetSegment;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.core.twin.impl.TimedValueImpl;
import org.eclipse.sensinact.core.whiteboard.WhiteboardSet;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

class SetMethod extends AbstractResourceMethod implements WhiteboardSet<Object> {

    public SetMethod(Method method, Object instance, Long serviceId, Set<String> providers) {
        super(method, instance, serviceId, providers);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Promise<TimedValue<Object>> pushValue(PromiseFactory pf, String modelPackageUri, String model,
            String provider, String service, String resource, Class<Object> resourceType,
            TimedValue<Object> cachedValue, TimedValue<Object> newValue) {

        final Map<Object, Object> params = new HashMap<>();
        params.put(SetSegment.RESULT_TYPE, resourceType);
        params.put(SetSegment.CACHED_VALUE, cachedValue);
        params.put(SetSegment.NEW_VALUE, newValue);
        try {
            Object o = super.invoke(modelPackageUri, model, provider, service, resource, params, SetParam.class,
                    SetParam::value);
            if (o instanceof Promise) {
                return ((Promise<TimedValue<Object>>) o);
            } else if (o instanceof TimedValue) {
                return pf.resolved((TimedValue<Object>) o);
            } else if (o == null) {
                return pf.resolved(null);
            } else if (resourceType.isAssignableFrom(o.getClass())) {
                return pf.resolved(new TimedValueImpl<Object>(resourceType.cast(o)));
            } else {
                return pf.failed(new Exception("Invalid result type: " + o.getClass()));
            }
        } catch (Exception e) {
            return pf.failed(e);
        }
    }

    @Override
    public Promise<TimedValue<Object>> pullValue(PromiseFactory promiseFactory, String modelPackageUri, String model,
            String provider, String service, String resource, Class<Object> resourceType,
            TimedValue<Object> cachedValue) {
        return promiseFactory.resolved(cachedValue);
    }
}
