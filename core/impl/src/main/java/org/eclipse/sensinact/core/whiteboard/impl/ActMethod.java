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
import java.util.Map;
import java.util.Set;

class ActMethod extends AbstractResourceMethod {

    public ActMethod(Method method, Object instance, Long serviceId, Set<String> providers) {
        super(method, instance, serviceId, providers);
    }

    public Object invoke(String modelPackageUri,String model, String provider, String service, String resource, Map<String, Object> params)
            throws Exception {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Map<Object, Object> rawParam = (Map) params;
        return super.invoke(modelPackageUri, model, provider, service, resource, rawParam, null, null);
    }
}
