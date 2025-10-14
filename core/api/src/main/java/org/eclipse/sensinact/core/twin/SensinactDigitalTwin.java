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
package org.eclipse.sensinact.core.twin;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.eclipse.sensinact.core.command.CommandScoped;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;

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
     * List all providers for the named model
     *
     * @param model
     * @return
     */
    List<? extends SensinactProvider> getProviders(String modelPackageUri, String model);

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
     * Get a provider by name and model
     *
     * @param model
     * @param providerName
     * @return
     */
    SensinactProvider getProvider(String modelPackageUri, String model, String providerName);

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
     * @param modelPackageUri
     * @param model
     * @param providerName
     * @return
     */
    SensinactProvider createProvider(String modelPackageUri, String model, String providerName);

    /**
     * Create a provider instance for the named model
     *
     * @param model
     * @param providerName
     * @param created
     * @return
     */
    SensinactProvider createProvider(String model, String providerName, Instant created);

    /**
     * Create a provider instance for the named model and model package uri
     *
     * @param modelPackageUri
     * @param model
     * @param providerName
     * @param created
     * @return
     */
    SensinactProvider createProvider(String modelPackageUri, String model, String providerName, Instant created);

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
     * Get a service by model, provider name and service name
     *
     * @param model
     * @param providerName
     * @param service
     * @return
     */
    SensinactService getService(String modelPackageUri, String model, String providerName, String service);

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
     * Get a resource by model, provider name, service name and resource name
     *
     * @param model
     * @param providerName
     * @param service
     * @param resource
     * @return
     */
    SensinactResource getResource(String modelPackageUri, String model, String providerName, String service, String resource);

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
     * associated items are accepted. Equivalent to calling
     * {@link #filteredSnapshot(BiPredicate, Predicate, Predicate, Predicate, EnumSet)}
     * with an empty set of options.
     *
     * @param geoFilter      Provider location filter
     * @param providerFilter Provider filter (without services)
     * @param svcFilter      Service filter (without resources)
     * @param rcFilter       Resource filter (without values)
     * @return The filtered snapshot
     */
    List<ProviderSnapshot> filteredSnapshot(BiPredicate<ProviderSnapshot, GeoJsonObject> geoFilter,
            Predicate<ProviderSnapshot> providerFilter, Predicate<ServiceSnapshot> svcFilter,
            Predicate<ResourceSnapshot> rcFilter);

    /**
     * Returns a (filtered) snapshot of the model. All null filters are ignored, all
     * associated items are accepted.
     *
     * @param geoFilter      Provider location filter
     * @param providerFilter Provider filter (without services)
     * @param svcFilter      Service filter (without resources)
     * @param rcFilter       Resource filter (without values)
     * @param options        The options for generating the snapshots
     * @return The filtered snapshot
     */
    List<ProviderSnapshot> filteredSnapshot(BiPredicate<ProviderSnapshot, GeoJsonObject> geoFilter,
            Predicate<ProviderSnapshot> providerFilter, Predicate<ServiceSnapshot> svcFilter,
            Predicate<ResourceSnapshot> rcFilter, EnumSet<SnapshotOption> options);

    /**
     * Returns the snapshot of the provider with the given name.
     * Equivalent to calling {@link #snapshotProvider(String, EnumSet)}.
     *
     * @param providerName Name of the provider
     * @return The snapshot of the provider, null if not found
     */
    ProviderSnapshot snapshotProvider(String providerName);

    /**
     * Returns the snapshot of the provider with the given name.
     * Equivalent to calling {@link #snapshotProvider(String, EnumSet)}.
     *
     * @param providerName Name of the provider
     * @param serviceFilter a filter to restrict the returned services
     * @param resourceFilter a filter to restrict the returned resources
     * @return The snapshot of the provider, null if not found
     */
    ProviderSnapshot snapshotProvider(String providerName, Predicate<ServiceSnapshot> serviceFilter,
            Predicate<ResourceSnapshot> resourceFilter);

    /**
     * Returns the snapshot of the provider with the given name.
     *
     * @param providerName Name of the provider
     * @param serviceFilter a filter to restrict the returned services
     * @param resourceFilter a filter to restrict the returned resources
     * @param options The options for generating the snapshots
     * @return The snapshot of the provider, null if not found
     */
    ProviderSnapshot snapshotProvider(String providerName, Predicate<ServiceSnapshot> serviceFilter,
            Predicate<ResourceSnapshot> resourceFilter, EnumSet<SnapshotOption> options);

    /**
     * Returns the snapshot of a service of a provider
     *
     * @param providerName Name of the provider
     * @param serviceName  Name of the service
     * @return The snapshot of the service, null if not found
     */
    ServiceSnapshot snapshotService(String providerName, String serviceName);

    /**
     * Returns the snapshot of a service of a provider
     *
     * @param providerName Name of the provider
     * @param serviceName  Name of the service
     * @param resourceFilter a filter to restrict the returned resources
     * @return The snapshot of the service, null if not found
     */
    ServiceSnapshot snapshotService(String providerName, String serviceName,
            Predicate<ResourceSnapshot> resourceFilter);

    /**
     * Returns the snapshot of a resource of a service of a provider
     *
     * @param providerName Name of the provider
     * @param serviceName  Name of the service
     * @param resourceName Name of the resource
     * @return The snapshot of the resource, null if not found
     */
    ResourceSnapshot snapshotResource(String providerName, String serviceName, String resourceName);

    public enum SnapshotOption {
        INCLUDE_LINKED_PROVIDER_IDS, INCLUDE_LINKED_PROVIDERS_FULL;
    }
}
