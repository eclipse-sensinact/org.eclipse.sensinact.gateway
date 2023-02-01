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
package org.eclipse.sensinact.northbound.filters.ldap.impl;

import org.eclipse.sensinact.prototype.snapshot.ResourceSnapshot;

/**
 * Parsed sensiNact path
 */
public class SensiNactPath {

    public final String service;

    public final String resource;

    public SensiNactPath(final String service, final String resource) {
        this.service = service;
        this.resource = resource;
    }

    @Override
    public String toString() {
        return String.format("%s.%s", service != null ? service : "*", resource);
    }

    public String getResource() {
        return resource;
    }

    public String getService() {
        return service;
    }

    public boolean accept(final ResourceSnapshot rcSnapshot) {
        if (service != null) {
            if (!service.equals(rcSnapshot.getService().getName())) {
                return false;
            }
        }

        return resource.equals(rcSnapshot.getName());
    }
}
