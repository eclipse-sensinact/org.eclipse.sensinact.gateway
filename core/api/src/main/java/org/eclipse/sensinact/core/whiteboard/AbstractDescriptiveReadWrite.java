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

import org.eclipse.sensinact.core.twin.TimedValue;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public abstract class AbstractDescriptiveReadWrite<T> extends AbstractDescriptiveReadOnly<T>
        implements WhiteboardSet<T> {

    @Override
    public Promise<TimedValue<?>> pushValue(PromiseFactory pf, String modelPackageUri, String model, String provider,
            String service, String resource, Class<T> resourceType, TimedValue<?> cachedValue, TimedValue<?> newValue) {

        Promise<TimedValue<?>> p = doPushValue(pf, modelPackageUri, model, provider, service, resource, cachedValue,
                newValue);
        if (p == null) {
            return pf.failed(new NullPointerException(getClass().getName() + " returned a null promise"));
        }
        return p;
    }

    protected abstract Promise<TimedValue<?>> doPushValue(PromiseFactory promiseFactory, String modelPackageUri,
            String model, String provider, String service, String resource, TimedValue<?> cachedValue,
            TimedValue<?> newValue);
}
