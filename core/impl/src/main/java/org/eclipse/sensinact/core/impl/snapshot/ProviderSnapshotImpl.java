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

package org.eclipse.sensinact.core.impl.snapshot;

import static org.eclipse.sensinact.core.twin.SensinactDigitalTwin.SnapshotOption.INCLUDE_LINKED_PROVIDERS_FULL;
import static org.eclipse.sensinact.core.twin.SensinactDigitalTwin.SnapshotOption.INCLUDE_LINKED_PROVIDER_IDS;

import java.time.Instant;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.core.snapshot.LinkedProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin.SnapshotOption;
import org.eclipse.sensinact.model.core.provider.Provider;

public class ProviderSnapshotImpl extends AbstractSnapshot implements ProviderSnapshot {

    /**
     * List of provider services
     */
    private final Map<String, ServiceSnapshotImpl> services = new LinkedHashMap<>();

    /**
     * Provider model package URI
     */
    private final String modelPackageUri;

    /**
     * Provider model name
     */
    private final String modelName;

    /**
     * Provider model
     */
    private final Provider modelProvider;

    private final List<LinkedProviderSnapshot> linked;

    /**
     * @param modelPackageUri Provider model package URI
     * @param modelName       Provider model name
     * @param provider        Provider model
     * @param snapshotInstant Instant of snapshot
     */
    public ProviderSnapshotImpl(final Provider provider,
            final Instant snapshotInstant, EnumSet<SnapshotOption> snapshotOptions) {
        super(provider.getId(), snapshotInstant);
        EClass eClass = provider.eClass();
        this.modelPackageUri = eClass.getEPackage().getNsURI();
        this.modelName = EMFUtil.getModelName(eClass);
        this.modelProvider = provider;
        if(snapshotOptions.contains(INCLUDE_LINKED_PROVIDERS_FULL)) {
            linked = provider.getLinkedProviders().stream()
                    .<LinkedProviderSnapshot>map(p -> new LinkedProviderSnapshotImpl(p, snapshotInstant, true))
                    .toList();
        } else if(snapshotOptions.contains(INCLUDE_LINKED_PROVIDER_IDS)) {
            linked = provider.getLinkedProviders().stream()
                    .<LinkedProviderSnapshot>map(p -> new LinkedProviderSnapshotImpl(p, snapshotInstant, false))
                    .toList();
        } else {
            linked = List.of();
        }
    }

    @Override
    public String toString() {
        return String.format("ProviderSnapshot(%s/%s/%s, %s)", modelPackageUri, modelName, getName(),
                getSnapshotTime());
    }

    @Override
    public String getModelPackageUri() {
        return modelPackageUri;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    public void add(final ServiceSnapshotImpl svc) {
        this.services.put(svc.getName(), svc);
    }

    @Override
    public List<ServiceSnapshot> getServices() {
        return List.copyOf(services.values());
    }

    @Override
    public ServiceSnapshotImpl getService(String name) {
        return services.get(name);
    }

    @Override
    public ResourceSnapshotImpl getResource(String service, String resource) {
        ServiceSnapshotImpl svc = services.get(service);
        return svc != null ? svc.getResource(resource) : null;
    }

    public Provider getModelProvider() {
        return modelProvider;
    }

    @Override
    public List<LinkedProviderSnapshot> getLinkedProviders() {
        return linked;
    }
}
