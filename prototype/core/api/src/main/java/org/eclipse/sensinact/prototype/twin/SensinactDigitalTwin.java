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
package org.eclipse.sensinact.prototype.twin;

import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.prototype.command.CommandScoped;
import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ServiceSnapshot;

/**
 * The {@link SensinactDigitalTwin} provides access to the in-memory digital
 * twin and is the basis for performing GET, SET, and ACT operations
 *
 * SUBSCRIBE and UNSUBSCRIBE operations occur using notifications in the
 * TypedEventBus
 */
public interface SensinactDigitalTwin extends CommandScoped {

    /**
     * List all the providers in the runtime
     *
     * @return
     */
    List<? extends SensinactProvider> getProviders();

    /**
     * List all providers for the named model
     *
     * @param model
     * @return
     */
    List<? extends SensinactProvider> getProviders(String model);

    /**
     * Get a provider by name
     *
     * @param providerName
     * @return
     */
    SensinactProvider getProvider(String providerName);

    /**
     * Get a provider by name and model
     *
     * @param model
     * @param providerName
     * @return
     */
    SensinactProvider getProvider(String model, String providerName);

    /**
     * Create a provider instance for the named model
     *
     * @param model
     * @param providerName
     * @return
     */
    SensinactProvider createProvider(String model, String providerName);

    /**
     * Create a provider instance for the named model
     *
     * @param model
     * @param providerName
     * @return
     */
    SensinactProvider createProvider(String model, String providerName, Instant created);

    /**
     * Get a service by model, provider name and service name
     *
     * @param model
     * @param providerName
     * @param service
     * @return
     */
    SensinactService getService(String model, String providerName, String service);

    /**
     * Get a service by provider name and service name
     *
     * @param providerName
     * @param service
     * @return
     */
    SensinactService getService(String providerName, String service);

    /**
     * Get a resource by model, provider name, service name and resource name
     *
     * @param model
     * @param providerName
     * @param service
     * @param resource
     * @return
     */
    SensinactResource getResource(String model, String providerName, String service, String resource);

    /**
     * Get a resource by provider name, service name and resource name
     *
     * @param providerName
     * @param service
     * @param resource
     * @return
     */
    SensinactResource getResource(String providerName, String service, String resource);

    /**
     * Returns a (filtered) snapshot of the model. All null filters are ignored, all
     * associated items are accepted.
     *
     * @param geoFilter      Provider location filter
     * @param providerFilter Provider filter (without services)
     * @param svcFilter      Service filter (without resources)
     * @param rcFilter       Resource filter (without values)
     * @return The filtered snapshot
     */
    List<ProviderSnapshot> filteredSnapshot(Predicate<GeoJsonObject> geoFilter,
            Predicate<ProviderSnapshot> providerFilter, Predicate<ServiceSnapshot> svcFilter,
            Predicate<ResourceSnapshot> rcFilter);

    /**
     * @param eClass
     * @param id
     */
    SensinactObject getSensinactObject(EClass eClass, String id);
}
