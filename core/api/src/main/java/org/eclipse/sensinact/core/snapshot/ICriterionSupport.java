/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;

class ResourceDataBackedProviderSnapshot implements ProviderSnapshot {
    final ResourceDataNotification rdn;

    final ResourceDataBackedServiceSnapshot service;

    public ResourceDataBackedProviderSnapshot(ResourceDataNotification rdn) {
        this.rdn = rdn;
        this.service = new ResourceDataBackedServiceSnapshot(this);
    }

    @Override
    public String getName() {
        return rdn.provider();
    }

    @Override
    public Instant getSnapshotTime() {
        return rdn.timestamp();
    }

    @Override
    public String getModelPackageUri() {
        return rdn.modelPackageUri();
    }

    @Override
    public String getModelName() {
        return rdn.model();
    }

    @Override
    public  List<ServiceSnapshot> getServices() {
        return List.of(service);
    }

    @Override
    public ServiceSnapshot getService(String name) {
        return Objects.equals(name, rdn.service()) ? service : null;
    }

    @Override
    public ResourceSnapshot getResource(String service, String resource) {
        return Objects.equals(service, rdn.service()) ? this.service.getResource(resource) :
                null;
    }

    @Override
    public List<LinkedProviderSnapshot> getLinkedProviders() {
        return List.of();
    }
}

class ResourceDataBackedServiceSnapshot implements ServiceSnapshot {
    final ResourceDataBackedProviderSnapshot provider;

    final ResourceDataBackedResourceSnapshot resource;

    public ResourceDataBackedServiceSnapshot(ResourceDataBackedProviderSnapshot provider) {
        this.provider = provider;
        this.resource = new ResourceDataBackedResourceSnapshot(this);
    }

    @Override
    public String getName() {
        return provider.rdn.service();
    }

    @Override
    public Instant getSnapshotTime() {
        return provider.rdn.timestamp();
    }

    @Override
    public ProviderSnapshot getProvider() {
        return provider;
    }

    @Override
    public List<ResourceSnapshot> getResources() {
        return List.of(resource);
    }

    @Override
    public ResourceSnapshot getResource(String name) {
        return Objects.equals(name, provider.rdn.resource()) ? resource : null;
    }

}

class ResourceDataBackedResourceSnapshot implements ResourceSnapshot {
    private final ResourceDataBackedServiceSnapshot service;

    public ResourceDataBackedResourceSnapshot(ResourceDataBackedServiceSnapshot service) {
        this.service = service;
    }

    @Override
    public String getName() {
        return service.provider.rdn.resource();
    }

    @Override
    public Instant getSnapshotTime() {
        return service.provider.rdn.timestamp();
    }

    @Override
    public ServiceSnapshot getService() {
        return service;
    }

    @Override
    public boolean isSet() {
        return service.provider.rdn.timestamp() != null;
    }

    @Override
    public Class<?> getType() {
        return service.provider.rdn.type();
    }

    @Override
    public TimedValue<?> getValue() {
        return new DefaultTimedValue<>(service.provider.rdn.newValue(), service.provider.rdn.timestamp());
    }

    @Override
    public Map<String, Object> getMetadata() {
        return service.provider.rdn.metadata();
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.SENSOR;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.UPDATABLE;
    }

}

abstract class CombinationCriterion implements ICriterion {

    protected final ICriterion a;
    protected final ICriterion b;

    public CombinationCriterion(ICriterion a, ICriterion b) {
        this.a = a;
        this.b = b;
    }

    protected <T> Predicate<T> and(Function<ICriterion, Predicate<T>> f) {
        Predicate<T> thisFilter = f.apply(a);
        if(thisFilter == null) {
            return f.apply(b);
        } else {
            Predicate<T> thatFilter = f.apply(b);
            if(thatFilter == null) {
                return thisFilter;
            } else {
                return thisFilter.and(thatFilter);
            }
        }
    }

    protected <T> Predicate<T> or(Function<ICriterion, Predicate<T>> f) {
        Predicate<T> thisFilter = f.apply(a);
        if(thisFilter == null) {
            return null;
        } else {
            Predicate<T> thatFilter = f.apply(b);
            if(thatFilter == null) {
                return null;
            } else {
                return thisFilter.or(thatFilter);
            }
        }
    }

    protected <T,U> BiPredicate<T,U> andDouble(Function<ICriterion, BiPredicate<T,U>> f) {
        BiPredicate<T,U> thisFilter = f.apply(a);
        if(thisFilter == null) {
            return f.apply(b);
        } else {
            BiPredicate<T,U> thatFilter = f.apply(b);
            if(thatFilter == null) {
                return thisFilter;
            } else {
                return thisFilter.and(thatFilter);
            }
        }
    }

    protected <T,U> BiPredicate<T,U> orDouble(Function<ICriterion, BiPredicate<T,U>> f) {
        BiPredicate<T,U> thisFilter = f.apply(a);
        if(thisFilter == null) {
            return null;
        } else {
            BiPredicate<T,U> thatFilter = f.apply(b);
            if(thatFilter == null) {
                return null;
            } else {
                return thisFilter.or(thatFilter);
            }
        }
    }

}

class AndCriterion extends CombinationCriterion {

    public AndCriterion(ICriterion a, ICriterion b) {
        super(a, b);
    }

    @Override
    public ResourceValueFilter getResourceValueFilter() {
        ResourceValueFilter thisFilter = a.getResourceValueFilter();
        if(thisFilter == null) {
            return b.getResourceValueFilter();
        } else {
            ResourceValueFilter thatFilter = b.getResourceValueFilter();
            if(thatFilter == null) {
                return thisFilter;
            } else {
                return (a,b) -> thisFilter.test(a, b) && thatFilter.test(a, b);
            }
        }
    }

    @Override
    public Predicate<ProviderSnapshot> getProviderFilter() {
        return and(ICriterion::getProviderFilter);
    }

    @Override
    public Predicate<ServiceSnapshot> getServiceFilter() {
        // Note that we *must* use an OR here to avoid
        // losing services that we need in one (but not
        // both) parts of the filter
        return or(ICriterion::getServiceFilter);
    }

    @Override
    public Predicate<ResourceSnapshot> getResourceFilter() {
        // Note that we *must* use an OR here to avoid
        // losing resources that we need in one (but not
        // both) parts of the filter
        return or(ICriterion::getResourceFilter);
    }

    @Override
    public BiPredicate<ProviderSnapshot, GeoJsonObject> getLocationFilter() {
        return andDouble(ICriterion::getLocationFilter);
    }

    @Override
    public List<String> dataTopics() {
        // TODO deduplicate further using wildcard matching and model/provider overlap
        return Stream
                .concat(a.dataTopics().stream(), b.dataTopics().stream())
                .distinct()
                .collect(toList());
    }
}

class OrCriterion extends CombinationCriterion {

    public OrCriterion(ICriterion a, ICriterion b) {
        super(a, b);
    }

    @Override
    public ResourceValueFilter getResourceValueFilter() {
        ResourceValueFilter thisFilter = a.getResourceValueFilter();
        if(thisFilter == null) {
            return null;
        } else {
            ResourceValueFilter thatFilter = b.getResourceValueFilter();
            if(thatFilter == null) {
                return null;
            } else {
                return (a,b) -> thisFilter.test(a, b) || thatFilter.test(a, b);
            }
        }
    }

    @Override
    public Predicate<ProviderSnapshot> getProviderFilter() {
        return or(ICriterion::getProviderFilter);
    }

    @Override
    public Predicate<ServiceSnapshot> getServiceFilter() {
        return or(ICriterion::getServiceFilter);
    }

    @Override
    public Predicate<ResourceSnapshot> getResourceFilter() {
        return or(ICriterion::getResourceFilter);
    }

    @Override
    public BiPredicate<ProviderSnapshot,GeoJsonObject> getLocationFilter() {
        return orDouble(ICriterion::getLocationFilter);
    }

    @Override
    public List<String> dataTopics() {
        // TODO deduplicate further using wildcard matching
        return Stream
                .concat(a.dataTopics().stream(), b.dataTopics().stream())
                .distinct()
                .collect(toList());
    }
}

class ResourceDataFilter implements Predicate<ResourceDataNotification> {

    private final ICriterion criterion;

    public ResourceDataFilter(ICriterion criterion) {
        this.criterion = criterion;
    }

    private <T> boolean nullSafePredicate(Predicate<T> test, T value) {
        return test == null ? true : test.test(value);
    }

    private <T,U> boolean nullSafeBiPredicate(BiPredicate<T,U> test, T value1, U value2) {
        return test == null ? true : test.test(value1, value2);
    }

    private boolean nullSafeFilter(ResourceValueFilter rvf, ProviderSnapshot p, List<ResourceSnapshot> rs) {
        return rvf == null ? true : rvf.test(p, rs);
    }

    @Override
    public boolean test(ResourceDataNotification rdn) {
        ResourceDataBackedProviderSnapshot ps = new ResourceDataBackedProviderSnapshot(rdn);

        boolean initial;
        if(Objects.equals("admin", rdn.service()) && Objects.equals("location", rdn.resource())) {
            initial = nullSafeBiPredicate(criterion.getLocationFilter(), ps, (GeoJsonObject) rdn.newValue());
        } else {
            initial = true;
        }

        return initial && nullSafePredicate(criterion.getProviderFilter(), ps)
                && nullSafePredicate(criterion.getServiceFilter(), ps.service)
                && nullSafePredicate(criterion.getResourceFilter(), ps.service.resource)
                && nullSafeFilter(criterion.getResourceValueFilter(), ps, List.of(ps.service.resource));
    }
}
