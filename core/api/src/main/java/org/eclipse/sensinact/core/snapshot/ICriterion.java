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
import java.util.Objects;
import java.util.function.Function;
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
     * @param criterion
     * @return
     */
    default ICriterion and(ICriterion criterion) {
        final ICriterion this_ = this;
        return new ICriterion() {

            @Override
            public Predicate<ServiceSnapshot> getServiceFilter() {
                return and(ICriterion::getServiceFilter);
            }

            private <T> Predicate<T> and(Function<ICriterion, Predicate<T>> f) {
                Predicate<T> thisFilter = f.apply(this_);
                if(thisFilter == null) {
                    return f.apply(criterion);
                } else {
                    Predicate<T> thatFilter = f.apply(criterion);
                    if(thatFilter == null) {
                        return thisFilter;
                    } else {
                        return thisFilter.and(thatFilter);
                    }
                }
            }

            @Override
            public ResourceValueFilter getResourceValueFilter() {
                ResourceValueFilter thisFilter = this_.getResourceValueFilter();
                if(thisFilter == null) {
                    return criterion.getResourceValueFilter();
                } else {
                    ResourceValueFilter thatFilter = criterion.getResourceValueFilter();
                    if(thatFilter == null) {
                        return thisFilter;
                    } else {
                        return (a,b) -> thisFilter.test(a, b) && thatFilter.test(a, b);
                    }
                }
            }

            @Override
            public Predicate<ResourceSnapshot> getResourceFilter() {
                return and(ICriterion::getResourceFilter);
            }

            @Override
            public Predicate<ProviderSnapshot> getProviderFilter() {
                return and(ICriterion::getProviderFilter);
            }

            @Override
            public Predicate<GeoJsonObject> getLocationFilter() {
                return and(ICriterion::getLocationFilter);
            }
        };
    }

    /**
     * Combine this filter with another filter as a logical OR
     * @param criterion
     * @return
     */
    default ICriterion or(ICriterion criterion) {
        final ICriterion this_ = this;
        return new ICriterion() {

            @Override
            public Predicate<ServiceSnapshot> getServiceFilter() {
                return or(ICriterion::getServiceFilter);
            }

            private <T> Predicate<T> or(Function<ICriterion, Predicate<T>> f) {
                Predicate<T> thisFilter = f.apply(this_);
                if(thisFilter == null) {
                    return null;
                } else {
                    Predicate<T> thatFilter = f.apply(criterion);
                    if(thatFilter == null) {
                        return null;
                    } else {
                        return thisFilter.or(thatFilter);
                    }
                }
            }

            @Override
            public ResourceValueFilter getResourceValueFilter() {
                ResourceValueFilter thisFilter = this_.getResourceValueFilter();
                if(thisFilter == null) {
                    return null;
                } else {
                    ResourceValueFilter thatFilter = criterion.getResourceValueFilter();
                    if(thatFilter == null) {
                        return null;
                    } else {
                        return (a,b) -> thisFilter.test(a, b) || thatFilter.test(a, b);
                    }
                }
            }

            @Override
            public Predicate<ResourceSnapshot> getResourceFilter() {
                return or(ICriterion::getResourceFilter);
            }

            @Override
            public Predicate<ProviderSnapshot> getProviderFilter() {
                return or(ICriterion::getProviderFilter);
            }

            @Override
            public Predicate<GeoJsonObject> getLocationFilter() {
                return or(ICriterion::getLocationFilter);
            }
        };
    }

    /**
     * Negate this filter
     * @param criterion
     * @return
     */
    default ICriterion negate() {
        final ICriterion this_ = this;
        return new ICriterion() {

            @Override
            public Predicate<ServiceSnapshot> getServiceFilter() {
                return this_.getServiceFilter().negate();
            }

            @Override
            public ResourceValueFilter getResourceValueFilter() {
                ResourceValueFilter thisFilter = this_.getResourceValueFilter();
                return (a,b) -> !thisFilter.test(a, b);
            }

            @Override
            public Predicate<ResourceSnapshot> getResourceFilter() {
                return this_.getResourceFilter().negate();
            }

            @Override
            public Predicate<ProviderSnapshot> getProviderFilter() {
                return this_.getProviderFilter().negate();
            }

            @Override
            public Predicate<GeoJsonObject> getLocationFilter() {
                return this_.getLocationFilter().negate();
            }
        };
    }

    default List<String> dataTopics() {
        return List.of("DATA/*");
    }

    default Predicate<ResourceDataNotification> dataEventFilter() {
        return rdn -> {
            ResourceDataBackedProviderSnapshot ps = new ResourceDataBackedProviderSnapshot(rdn);

            boolean initial;
            if(Objects.equals("admin", rdn.service) && Objects.equals("location", rdn.resource)) {
                initial = getLocationFilter().test((GeoJsonObject) rdn.newValue);
            } else {
                initial = true;
            }

            return initial && getProviderFilter().test(ps) && getServiceFilter().test(ps.service)
                    && getResourceFilter().test(ps.service.resource)
                    && getResourceValueFilter().test(ps, List.of(ps.service.resource));
        };
    }
}
