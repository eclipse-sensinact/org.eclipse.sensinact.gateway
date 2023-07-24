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
package org.eclipse.sensinact.prototype.twin.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFDigitalTwin;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFProvider;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.model.core.provider.Metadata;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.model.core.provider.Service;
import org.eclipse.sensinact.prototype.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.prototype.impl.snapshot.ProviderSnapshotImpl;
import org.eclipse.sensinact.prototype.impl.snapshot.ResourceSnapshotImpl;
import org.eclipse.sensinact.prototype.impl.snapshot.ServiceSnapshotImpl;
import org.eclipse.sensinact.prototype.model.nexus.ModelNexus;
import org.osgi.util.promise.PromiseFactory;

public class SensinactDigitalTwinImpl extends CommandScopedImpl implements SensinactEMFDigitalTwin {

    private final ModelNexus nexusImpl;
    private final PromiseFactory pf;

    public SensinactDigitalTwinImpl(ModelNexus nexusImpl, PromiseFactory pf) {
        super(new AtomicBoolean(true));
        this.nexusImpl = nexusImpl;
        this.pf = pf;
    }

    /**
     * Returns the known providers
     */
    public List<SensinactProviderImpl> getProviders() {
        checkValid();
        return nexusImpl.getProviders().stream().map(this::toProvider).collect(Collectors.toList());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.sensinact.prototype.twin.SensinactDigitalTwin#getProvider(org.
     * eclipse.emf.ecore.EClass, java.lang.String)
     */
    @Override
    public SensinactProviderImpl getProvider(EClass model, String id) {
        if (model != ProviderPackage.Literals.PROVIDER
                || !model.getEAllSuperTypes().contains(ProviderPackage.Literals.PROVIDER)) {
            throw new IllegalArgumentException("The requested eClass must have Provider as a super class");
        }
        final Provider provider = nexusImpl.getProvider(model, id);
        if (provider == null) {
            return null;
        }

        return toProvider(provider);
    }

    /**
     * Returns the known providers
     */
    public List<SensinactProviderImpl> getProviders(String model) {
        checkValid();
        return nexusImpl.getProviders(model).stream().map(this::toProvider).collect(Collectors.toList());
    }

    @Override
    public SensinactProviderImpl getProvider(String model, String providerName) {
        checkValid();
        final Provider provider = nexusImpl.getProvider(model, providerName);
        if (provider == null) {
            return null;
        }

        return toProvider(provider);
    }

    @Override
    public SensinactProviderImpl getProvider(String providerName) {
        checkValid();
        final Provider provider = nexusImpl.getProvider(providerName);
        if (provider == null) {
            return null;
        }

        return toProvider(provider);
    }

    @Override
    public SensinactEMFProvider createProvider(String model, String providerName) {
        return toProvider(nexusImpl.createProviderInstance(model, providerName));
    }

    @Override
    public SensinactEMFProvider createProvider(String model, String providerName, Instant instant) {
        return instant == null ? createProvider(model, providerName)
                : toProvider(nexusImpl.createProviderInstance(model, providerName, instant));
    }

    @Override
    public SensinactServiceImpl getService(String model, String providerName, String service) {
        checkValid();
        return getService(nexusImpl.getProvider(model, providerName), model, service);
    }

    @Override
    public SensinactServiceImpl getService(String providerName, String service) {
        checkValid();
        Provider provider = nexusImpl.getProvider(providerName);
        return getService(provider, nexusImpl.getProviderModel(providerName), service);
    }

    private SensinactServiceImpl getService(Provider provider, String model, String service) {
        if (provider == null) {
            return null;
        }

        final EStructuralFeature svcFeature = provider.eClass().getEStructuralFeature(service);
        if (svcFeature == null) {
            return null;
        }

        final SensinactProviderImpl snProvider = toProvider(provider);
        return toService(snProvider, provider, (EReference) svcFeature);
    }

    @Override
    public SensinactResourceImpl getResource(String model, String providerName, String service, String resource) {
        checkValid();
        return getResource(nexusImpl.getProvider(model, providerName), model, service, resource);
    }

    public SensinactResourceImpl getResource(String providerName, String service, String resource) {
        checkValid();
        Provider provider = nexusImpl.getProvider(providerName);
        return getResource(provider, nexusImpl.getProviderModel(providerName), service, resource);
    }

    private SensinactResourceImpl getResource(Provider provider, String model, String service, String resource) {
        if (provider == null) {
            return null;
        }

        final EReference svcFeature = (EReference) provider.eClass().getEStructuralFeature(service);
        if (svcFeature == null) {
            return null;
        }

        final EClass serviceEClass = svcFeature.getEReferenceType();

        final ETypedElement rcFeature = Optional.ofNullable(serviceEClass.getEStructuralFeature(resource))
                .map(ETypedElement.class::cast)
                .or(() -> serviceEClass.getEOperations().stream()
                        .filter(o -> o.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE)
                        .filter(o -> resource.equals(o.getName())).map(ETypedElement.class::cast).findFirst())
                .orElseGet(() -> null);
        if (rcFeature == null) {
            return null;
        }

        // Construct the resource
        final SensinactProviderImpl snProvider = toProvider(provider);
        final SensinactServiceImpl snSvc = toService(snProvider, provider, svcFeature);
        return toResource(snSvc, provider, svcFeature, rcFeature);
    }

    public <T> TimedValue<T> getResourceValue(String model, String providerName, String service, String resource,
            Class<T> type) {
        checkValid();
        return getResourceValue(nexusImpl.getProvider(model, providerName), service, resource, type);
    }

    public <T> TimedValue<T> getResourceValue(String providerName, String service, String resource, Class<T> type) {
        checkValid();
        return getResourceValue(nexusImpl.getProvider(providerName), service, resource, type);
    }

    private <T> TimedValue<T> getResourceValue(Provider provider, String service, String resource, Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Resource type must not be null");
        }

        if (provider == null) {
            return null;
        }

        final EStructuralFeature svcFeature = provider.eClass().getEStructuralFeature(service);
        if (svcFeature == null) {
            return null;
        }
        final Service svc = (Service) provider.eGet(svcFeature);

        final EStructuralFeature rcFeature = svc.eClass().getEStructuralFeature(resource);

        if (rcFeature == null) {
            return null;
        }

        // Get the resource metadata
        final Metadata metadata = svc.getMetadata().get(rcFeature);
        final Instant timestamp;
        if (metadata != null) {
            timestamp = metadata.getTimestamp();
        } else {
            timestamp = null;
        }

        // Check value type
        final Object rawValue = svc.eGet(rcFeature);
        if (rawValue == null) {
            return new TimedValueImpl<T>(null, timestamp);
        } else if (!type.isAssignableFrom(rawValue.getClass())) {
            throw new IllegalArgumentException(
                    "Expected a " + type.getName() + " but resource is a " + rawValue.getClass().getName());
        } else {
            return new TimedValueImpl<T>(type.cast(rawValue), timestamp);
        }
    }

    private SensinactProviderImpl toProvider(final Provider modelProvider) {
        return new SensinactProviderImpl(active, modelProvider, nexusImpl, pf);
    }

    private SensinactServiceImpl toService(final SensinactProvider parent, Provider provider, EReference ref) {
        return new SensinactServiceImpl(active, parent, provider, ref, nexusImpl, pf);
    }

    private SensinactResourceImpl toResource(final SensinactService parent, Provider provider, EReference svcFeature,
            final ETypedElement rcFeature) {
        return new SensinactResourceImpl(active, parent, provider, svcFeature, rcFeature,
                rcFeature.getEType().getInstanceClass(), nexusImpl, pf);
    }

    /**
     * Fills the fields of the given resource snapshot
     *
     * @param rcSnapshot Resource snapshot
     */
    private void fillInResource(final ResourceSnapshotImpl rcSnapshot) {
        // Add resource value
        if (rcSnapshot.getResourceType() == ResourceType.ACTION) {
            // Skip values for actions
            return;
        }

        // Get the resource metadata
        final ETypedElement rcFeature = rcSnapshot.getFeature();
        final Service svc = rcSnapshot.getService().getModelService();
        if (!svc.eIsSet((EStructuralFeature) rcFeature)) {
            return;
        }

        final Metadata metadata = svc == null ? null : svc.getMetadata().get(rcFeature);
        final Instant timestamp;
        if (metadata != null) {
            timestamp = metadata.getTimestamp();
        } else {
            timestamp = null;
        }

        rcSnapshot.setValue(
                new TimedValueImpl<Object>(svc == null ? null : svc.eGet((EStructuralFeature) rcFeature), timestamp));
    }

    @Override
    public List<ProviderSnapshot> filteredSnapshot(Predicate<GeoJsonObject> geoFilter,
            Predicate<ProviderSnapshot> providerFilter, Predicate<ServiceSnapshot> svcFilter,
            Predicate<ResourceSnapshot> rcFilter) {

        final Instant snapshotTime = Instant.now();

        // Filter providers by location (raw provider)
        Stream<Provider> rawProvidersStream = nexusImpl.getProviders().stream();
        if (geoFilter != null) {
            // Filter the provider location
            rawProvidersStream = rawProvidersStream.filter(p -> geoFilter.test(p.getAdmin().getLocation()));
        }

        // Filter providers with their API model
        Stream<ProviderSnapshotImpl> providersStream = rawProvidersStream
                .map(p -> new ProviderSnapshotImpl(nexusImpl.getProviderModel(p.getId()), p, snapshotTime));
        if (providerFilter != null) {
            providersStream = providersStream.filter(providerFilter);
        }

        // Filter providers according to their services
        providersStream = providersStream.map(p -> {
            final Provider modelProvider = p.getModelProvider();
            nexusImpl.getServicesForModel(modelProvider.eClass()).filter(modelProvider::eIsSet).forEach((feature) -> {
                p.add(new ServiceSnapshotImpl(p, feature.getName(), (Service) modelProvider.eGet(feature),
                        snapshotTime));
            });
            return p;
        });
        if (svcFilter != null) {
            providersStream = providersStream.filter(p -> p.getServices().stream().anyMatch(svcFilter));
        }

        // Filter providers according to their resources
        providersStream = providersStream.map(p -> {
            p.getServices().stream().forEach(s -> {
                // Avoid ModelService which may be null
                EStructuralFeature sf = s.getProvider().getModelProvider().eClass().getEStructuralFeature(s.getName());
                nexusImpl.getResourcesForService((EClass) sf.getEType())
                        .forEach(f -> s.add(new ResourceSnapshotImpl(s, f, snapshotTime)));
            });
            return p;
        });
        if (rcFilter != null) {
            providersStream = providersStream
                    .filter(p -> p.getServices().stream().anyMatch(s -> s.getResources().stream().anyMatch(rcFilter)));
        }

        // Add resource value
        providersStream = providersStream.map(p -> {
            p.getServices().stream().forEach(s -> {
                s.getResources().stream().forEach(this::fillInResource);
            });
            p.filterEmptyServices();
            return p;
        });

        return providersStream.collect(Collectors.toList());
    }

    @Override
    public ProviderSnapshot snapshotProvider(String providerName) {
        final Instant snapshotTime = Instant.now();

        final Provider nexusProvider = nexusImpl.getProvider(providerName);
        if (nexusProvider == null) {
            // Provider not found
            return null;
        }

        final ProviderSnapshotImpl providerSnapshot = new ProviderSnapshotImpl(
                nexusImpl.getProviderModel(nexusProvider.getId()), nexusProvider, snapshotTime);

        // Add all services
        nexusImpl.getServicesForModel(nexusProvider.eClass()).filter(nexusProvider::eIsSet).forEach((svcFeature) -> {
            // Get the service
            final ServiceSnapshotImpl svcSnapshot = new ServiceSnapshotImpl(providerSnapshot, svcFeature.getName(),
                    (Service) nexusProvider.eGet(svcFeature), snapshotTime);

            // Get the resources
            final EStructuralFeature sf = nexusProvider.eClass().getEStructuralFeature(svcSnapshot.getName());
            nexusImpl.getResourcesForService((EClass) sf.getEType()).forEach(rcFeature -> {
                final ResourceSnapshotImpl rcSnapshot = new ResourceSnapshotImpl(svcSnapshot, rcFeature, snapshotTime);
                fillInResource(rcSnapshot);
                svcSnapshot.add(rcSnapshot);
            });

            providerSnapshot.add(svcSnapshot);
        });
        providerSnapshot.filterEmptyServices();
        return providerSnapshot;
    }

    @Override
    public ServiceSnapshot snapshotService(String providerName, String serviceName) {
        final Instant snapshotTime = Instant.now();

        final Provider nexusProvider = nexusImpl.getProvider(providerName);
        if (nexusProvider == null) {
            // Provider not found
            return null;
        }

        final Optional<EReference> foundSvc = nexusImpl.getServicesForModel(nexusProvider.eClass())
                .filter(nexusProvider::eIsSet).filter(f -> f.getName().equals(serviceName)).findFirst();
        if (foundSvc.isEmpty()) {
            // Service not found
            return null;
        }

        // Minimal snapshot of the provider owning the service
        final ProviderSnapshotImpl providerSnapshot = new ProviderSnapshotImpl(
                nexusImpl.getProviderModel(nexusProvider.getId()), nexusProvider, snapshotTime);

        // Describe the service
        final EReference svcFeature = foundSvc.get();
        final ServiceSnapshotImpl svcSnapshot = new ServiceSnapshotImpl(providerSnapshot, svcFeature.getName(),
                (Service) nexusProvider.eGet(svcFeature), snapshotTime);
        providerSnapshot.add(svcSnapshot);

        // Get the resources
        final EStructuralFeature sf = nexusProvider.eClass().getEStructuralFeature(svcSnapshot.getName());
        nexusImpl.getResourcesForService((EClass) sf.getEType()).forEach(rcFeature -> {
            final ResourceSnapshotImpl rcSnapshot = new ResourceSnapshotImpl(svcSnapshot, rcFeature, snapshotTime);
            fillInResource(rcSnapshot);
            svcSnapshot.add(rcSnapshot);
        });

        return svcSnapshot;
    }

    @Override
    public ResourceSnapshot snapshotResource(String providerName, String serviceName, String resourceName) {
        final Instant snapshotTime = Instant.now();

        final Provider nexusProvider = nexusImpl.getProvider(providerName);
        if (nexusProvider == null) {
            // Provider not found
            return null;
        }

        final Optional<EReference> foundSvc = nexusImpl.getServicesForModel(nexusProvider.eClass())
                .filter(nexusProvider::eIsSet).filter(f -> f.getName().equals(serviceName)).findFirst();
        if (foundSvc.isEmpty()) {
            // Service not found
            return null;
        }

        final EStructuralFeature sf = nexusProvider.eClass().getEStructuralFeature(serviceName);
        final Optional<ETypedElement> foundRc = nexusImpl.getResourcesForService((EClass) sf.getEType()).filter(f -> f.getName().equals(resourceName)).findFirst();
        if(foundRc.isEmpty()) {
            // Resource not found
            return null;
        }

        // Minimal description of the provider owning the service
        final ProviderSnapshotImpl providerSnapshot = new ProviderSnapshotImpl(
                nexusImpl.getProviderModel(nexusProvider.getId()), nexusProvider, snapshotTime);

        // Minimal description of the service owning the resource
        final EReference svcFeature = foundSvc.get();
        final ServiceSnapshotImpl svcSnapshot = new ServiceSnapshotImpl(providerSnapshot, svcFeature.getName(),
                (Service) nexusProvider.eGet(svcFeature), snapshotTime);
        providerSnapshot.add(svcSnapshot);

        // Describe the resource
        final ResourceSnapshotImpl rcSnapshot = new ResourceSnapshotImpl(svcSnapshot, foundRc.get(), snapshotTime);
        fillInResource(rcSnapshot);
        svcSnapshot.add(rcSnapshot);
        return rcSnapshot;
    }
}
