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
package org.eclipse.sensinact.core.snapshot;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;

/**
 * Provides the various parts of a parsed filter
 */
public interface ICriterion {

    /**
     * Early provider predicate executed during the snapshot. Only the location of
     * the provider are available when the predicate is called.
     * <p>
     * This predicate is executed in the gateway thread.
     */
    Predicate<GeoJsonObject> getLocationFilter();

    /**
     * Early provider predicate executed during the snapshot. Only the model and
     * name of the provider are available when the predicate is called.
     * <p>
     * This predicate is executed in the gateway thread.
     */
    Predicate<ProviderSnapshot> getProviderFilter();

    /**
     * Early service filter executed during the snapshot. The resources of the
     * service are not available when the predicate is called. Useful to filter out
     * unwanted services by name.
     * <p>
     * This predicate is executed in the gateway thread.
     */
    Predicate<ServiceSnapshot> getServiceFilter();

    /**
     * Early resource filter executed during the snapshot. The value of the resource
     * is not available when the predicate is called. Useful to filter out unwanted
     * resources by name.
     * <p>
     * This predicate is executed in the gateway thread.
     */
    Predicate<ResourceSnapshot> getResourceFilter();

    /**
     * Post-snapshot provider filter, that will be given each provider and its
     * valued resources.
     * <p>
     * This filter will be executed on the snapshot, outside of the gateway thread
     */
    ResourceValueFilter getResourceValueFilter();

    /**
     * Combine this filter with another filter as a logical AND
     *
     * <p>
     * Note that the included services and resources will still
     * use an OR semantic so that their values are available
     * for the {@link ResourceValueFilter}
     *
     * @param criterion
     * @return
     */
    default ICriterion and(ICriterion criterion) {
        return new AndCriterion(this, criterion);
    }

    /**
     * Combine this filter with another filter as a logical OR
     * @param criterion
     * @return
     */
    default ICriterion or(ICriterion criterion) {
        return new OrCriterion(this, criterion);
    }

    /**
     * Negate this filter
     * @param criterion
     * @return
     */
    default ICriterion negate() {
        final ICriterion this_ = this;
        return new ICriterion() {

            private <T> Predicate<T> negate(Predicate<T> p) {
                return p == null ? null : p.negate();
            }

            @Override
            public Predicate<ServiceSnapshot> getServiceFilter() {
                return negate(this_.getServiceFilter());
            }

            @Override
            public ResourceValueFilter getResourceValueFilter() {
                ResourceValueFilter thisFilter = this_.getResourceValueFilter();
                return thisFilter == null ? null : (a,b) -> !thisFilter.test(a, b);
            }

            @Override
            public Predicate<ResourceSnapshot> getResourceFilter() {
                return negate(this_.getResourceFilter());
            }

            @Override
            public Predicate<ProviderSnapshot> getProviderFilter() {
                return negate(this_.getProviderFilter());
            }

            @Override
            public Predicate<GeoJsonObject> getLocationFilter() {
                return negate(this_.getLocationFilter());
            }
        };
    }

    default List<String> dataTopics() {
        return List.of("DATA/*");
    }

    default Predicate<ResourceDataNotification> dataEventFilter() {
        return new ResourceDataFilter(this);
    }
}
