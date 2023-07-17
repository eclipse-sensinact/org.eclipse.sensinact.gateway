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

import org.eclipse.sensinact.core.annotation.dto.NullAction;
import org.eclipse.sensinact.core.annotation.verb.GetParam;
import org.eclipse.sensinact.core.annotation.verb.GetParam.GetSegment;
import org.eclipse.sensinact.core.twin.TimedValue;

class GetMethod extends AbstractResourceMethod {

    /**
     * Action to apply when the result is null
     */
    private final NullAction nullAction;

    public GetMethod(Method method, Object instance, Long serviceId, Set<String> providers, NullAction onNull) {
        super(method, instance, serviceId, providers);
        this.nullAction = onNull;
    }

    public <T> Object invoke(String model, String provider, String service, String resource, Class<T> resultType,
            TimedValue<T> cachedValue) throws Exception {
        final Map<Object, Object> params = new HashMap<>();
        params.put(GetSegment.RESULT_TYPE, resultType);
        params.put(GetSegment.CACHED_VALUE, cachedValue);
        return super.invoke(model, provider, service, resource, params, GetParam.class, GetParam::value);
    }

    public NullAction actionOnNull() {
        return nullAction;
    }
}
