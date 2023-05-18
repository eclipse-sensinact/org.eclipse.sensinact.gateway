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
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.core.command;

import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public abstract class ResourceCommand<T> extends AbstractTwinCommand<T> {

    private final String model;
    private final String provider;
    private final String service;
    private final String resource;

    public ResourceCommand(String provider, String service, String resource) {
        this(null, provider, service, resource);
    }

    public ResourceCommand(String model, String provider, String service, String resource) {
        super();
        this.model = model;
        this.provider = provider;
        this.service = service;
        this.resource = resource;
    }

    @Override
    protected final Promise<T> call(SensinactDigitalTwin twin, PromiseFactory pf) {
        SensinactResource r = model == null ? twin.getResource(provider, service, resource)
                : twin.getResource(model, provider, service, resource);
        if (r == null) {
            return pf.failed(new IllegalArgumentException("Resource not found"));
        } else {
            return call(r, pf);
        }
    }

    protected abstract Promise<T> call(SensinactResource resource, PromiseFactory pf);

}
