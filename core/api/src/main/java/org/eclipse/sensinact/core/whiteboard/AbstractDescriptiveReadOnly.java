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
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.eclipse.sensinact.core.twin.TimedValue;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public abstract class AbstractDescriptiveReadOnly<T> implements WhiteboardGet<T>, WhiteboardResourceDescription<T> {

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getResourceType() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public Duration getCacheDuration() {
        return Duration.of(30, ChronoUnit.SECONDS);
    }

    @Override
    public Promise<TimedValue<?>> pullValue(PromiseFactory pf, String modelPackageUri, String model, String provider,
            String service, String resource, Class<T> resourceType, TimedValue<?> cachedValue) {
        Promise<TimedValue<?>> p = doPullValue(pf, modelPackageUri, model, provider, service, resource, cachedValue);
        if (p == null) {
            return pf.failed(new NullPointerException(getClass().getName() + " returned a null promise"));
        }
        return p;
    }

    protected abstract Promise<TimedValue<?>> doPullValue(PromiseFactory promiseFactory, String modelPackageUri,
            String model, String provider, String service, String resource, TimedValue<?> cachedValue);
}
