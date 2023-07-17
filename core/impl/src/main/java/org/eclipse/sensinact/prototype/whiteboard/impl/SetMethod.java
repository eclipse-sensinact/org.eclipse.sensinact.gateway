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
package org.eclipse.sensinact.prototype.whiteboard.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.core.annotation.verb.SetParam;
import org.eclipse.sensinact.core.annotation.verb.SetParam.SetSegment;
import org.eclipse.sensinact.core.twin.TimedValue;

class SetMethod extends AbstractResourceMethod {

    public SetMethod(Method method, Object instance, Long serviceId, Set<String> providers) {
        super(method, instance, serviceId, providers);
    }

    public <T> Object invoke(String model, String provider, String service, String resource, Class<T> resultType,
            TimedValue<T> cachedValue, TimedValue<T> newValue) throws Exception {
        final Map<Object, Object> params = new HashMap<>();
        params.put(SetSegment.RESULT_TYPE, resultType);
        params.put(SetSegment.CACHED_VALUE, cachedValue);
        params.put(SetSegment.NEW_VALUE, newValue);
        return super.invoke(model, provider, service, resource, params, SetParam.class, SetParam::value);
    }
}
