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
package org.eclipse.sensinact.northbound.filters.sensorthings;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.prototype.twin.TimedValue;

/**
 * @author thoma
 *
 */
public class RcUtils {

    static ProviderSnapshot makeProvider(final String providerName) {
        return makeProvider(providerName, providerName);
    }

    static ProviderSnapshot makeProvider(final String modelName, final String providerName) {
        return new ProviderSnapshot() {
            final List<ServiceSnapshot> services = new ArrayList<>();

            @Override
            public Instant getSnapshotTime() {
                return Instant.now();
            }

            @Override
            public String getName() {
                return providerName;
            }

            @Override
            public List<ServiceSnapshot> getServices() {
                return services;
            }

            @Override
            public String getModelName() {
                return modelName;
            }
        };
    }

    static ServiceSnapshot addService(final ProviderSnapshot provider, final String svcName) {
        ServiceSnapshot svc = new ServiceSnapshot() {
            final List<ResourceSnapshot> resources = new ArrayList<>();

            @Override
            public Instant getSnapshotTime() {
                return Instant.now();
            }

            @Override
            public String getName() {
                return svcName;
            }

            @Override
            public List<ResourceSnapshot> getResources() {
                return resources;
            }

            @Override
            public ProviderSnapshot getProvider() {
                return provider;
            }
        };
        provider.getServices().add(svc);
        return svc;
    }

    static ResourceSnapshot addResource(ServiceSnapshot svc, final String rcName, final Object value) {
        return addResource(svc, rcName, value, Instant.now());
    }

    static ResourceSnapshot addResource(ServiceSnapshot svc, final String rcName, final Object value,
            final Instant rcTime) {
        ResourceSnapshot rc = new ResourceSnapshot() {
            @Override
            public String getName() {
                return rcName;
            }

            @Override
            public TimedValue<?> getValue() {
                return new TimedValue<Object>() {
                    @Override
                    public Instant getTimestamp() {
                        return Instant.now();
                    }

                    @Override
                    public Object getValue() {
                        return value;
                    }
                };
            }

            @Override
            public Instant getSnapshotTime() {
                return rcTime;
            }

            @Override
            public ServiceSnapshot getService() {
                return svc;
            }
        };

        svc.getResources().add(rc);
        return rc;
    }
}
