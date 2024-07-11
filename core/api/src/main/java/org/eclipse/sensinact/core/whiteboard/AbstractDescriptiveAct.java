/*********************************************************************
* Copyright (c) 2024 Kentyou.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Thomas Calmant (Kentyou) - initial implementation
**********************************************************************/
package org.eclipse.sensinact.core.whiteboard;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public abstract class AbstractDescriptiveAct<T> implements WhiteboardAct<T>, WhiteboardActDescription<T> {

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getReturnType() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public Promise<T> act(PromiseFactory pf, String modelPackageUri, String model, String provider, String service,
            String resource, Map<String, Object> arguments) {
        Promise<T> p = doAct(pf, modelPackageUri, model, provider, service, resource, arguments);
        if (p == null) {
            return pf.failed(new NullPointerException(getClass().getName() + " returned a null promise"));
        }
        return p;
    }

    protected abstract Promise<T> doAct(PromiseFactory promiseFactory, String modelPackageUri, String model,
            String provider, String service, String resource, Map<String, Object> arguments);
}
