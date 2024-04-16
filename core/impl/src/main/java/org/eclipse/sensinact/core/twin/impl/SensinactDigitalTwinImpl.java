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
package org.eclipse.sensinact.core.twin.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.sensinact.core.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFDigitalTwin;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFProvider;
import org.eclipse.sensinact.core.impl.snapshot.ProviderSnapshotImpl;
import org.eclipse.sensinact.core.impl.snapshot.ResourceSnapshotImpl;
import org.eclipse.sensinact.core.impl.snapshot.ServiceSnapshotImpl;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.nexus.ModelNexus;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.model.core.provider.Metadata;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.model.core.provider.Service;
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
     * @see org.eclipse.sensinact.core.twin.SensinactDigitalTwin#getProvider(org.
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
    public List<SensinactProviderImpl> getProviders(String modelPackageUri, String model) {
        checkValid();
        return nexusImpl.getProviders(modelPackageUri, model).stream().map(this::toProvider)
                .collect(Collectors.toList());
    }

    @Override
    public SensinactProviderImpl getProvider(String modelPackageUri, String model, String providerName) {
        checkValid();
        final Provider provider = nexusImpl.getProvider(modelPackageUri, model, providerName);
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
    public SensinactEMFProvider createProvider(String modelPackageUri, String model, String providerName) {
        return toProvider(nexusImpl.createProviderInstance(modelPackageUri, model, providerName));
    }

    @Override
    public SensinactEMFProvider createProvider(String modelPackageUri, String model, String providerName,
            Instant instant) {
        return instant == null ? createProvider(model, providerName)
                : toProvider(nexusImpl.createProviderInstance(modelPackageUri, model, providerName, instant));
    }

    @Override
    public SensinactEMFProvider createProvider(Provider provider) {
        return createProvider(provider.eClass().getEPackage().getNsURI(), EMFUtil.getModelName(provider.eClass()),
                provider.getId());
    }

    @Override
    public SensinactServiceImpl getService(String modelPackageUri, String model, String providerName, String service) {
        checkValid();
        return getService(nexusImpl.getProvider(modelPackageUri, model, providerName), model, service);
    }

    @Override
    public SensinactServiceImpl getService(String providerName, String service) {
        checkValid();
        Provider provider = nexusImpl.getProvider(providerName);
        return getService(provider, nexusImpl.getProviderModel(providerName), service);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.sensinact.core.twin.SensinactDigitalTwin#getService(java.lang.
     * String, java.lang.String, java.lang.String)
     */
    @Override
    public SensinactService getService(String model, String providerName, String service) {
        return getService(null, model, providerName, service);
    }

    private SensinactServiceImpl getService(Provider provider, String model, String serviceName) {
        if (provider == null) {
            return null;
        }
        EClass serviceType = provider.getServiceEClass(serviceName);
        if (serviceType == null) {
            return null;
        }
        final SensinactProviderImpl snProvider = toProvider(provider);
        return toService(provider, serviceName, serviceType, snProvider);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.sensinact.core.twin.SensinactDigitalTwin#getResource(java.lang.
     * String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public SensinactResource getResource(String model, String providerName, String service, String resource) {
        return getResource(null, model, providerName, service, resource);
    }

    @Override
    public SensinactResourceImpl getResource(String modelPackageUri, String model, String providerName, String service,
            String resource) {
        checkValid();
        return getResource(nexusImpl.getProvider(modelPackageUri, model, providerName), model, service, resource);
    }

    public SensinactResourceImpl getResource(String providerName, String service, String resource) {
        checkValid();
        Provider provider = nexusImpl.getProvider(providerName);
        return getResource(provider, nexusImpl.getProviderModel(providerName), service, resource);
    }

    private SensinactResourceImpl getResource(Provider provider, String model, String serviceName, String resource) {
        if (provider == null) {
            return null;
        }
        final EClass serviceEClass = provider.getServiceEClass(serviceName);
        if (serviceEClass == null) {
            return null;
        }

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
        final SensinactServiceImpl snService = toService(provider, serviceName, serviceEClass, snProvider);
        return toResource(snService, provider, serviceName, rcFeature);
    }

    public <T> TimedValue<T> getResourceValue(String modelPackageUri, String model, String providerName, String service,
            String resource, Class<T> type) {
        checkValid();
        return getResourceValue(nexusImpl.getProvider(modelPackageUri, model, providerName), service, resource, type);
    }

    public <T> TimedValue<T> getResourceValue(String providerName, String service, String resource, Class<T> type) {
        checkValid();
        return getResourceValue(nexusImpl.getProvider(providerName), service, resource, type);
    }

    private <T> TimedValue<T> getResourceValue(Provider provider, String serviceName, String resource, Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Resource type must not be null");
        }

        if (provider == null) {
            return null;
        }

        final Service svc = provider.getService(serviceName);
        if (svc == null) {
            return null;
        }

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

    private SensinactResourceImpl toResource(final SensinactService parent, Provider provider, String serviceName,
            final ETypedElement rcFeature) {
        return new SensinactResourceImpl(active, parent, provider, serviceName, rcFeature,
                rcFeature.getEType().getInstanceClass(), nexusImpl, pf);
    }

    private SensinactServiceImpl toService(Provider provider, String serviceName, final EClass serviceEClass,
            final SensinactProviderImpl snProvider) {
        return new SensinactServiceImpl(active, snProvider, provider, serviceName, serviceEClass, nexusImpl, pf);
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

        // Filter providers with their API model
        Stream<ProviderSnapshotImpl> providersStream = nexusImpl.getProviders().stream()
                .map(p -> new ProviderSnapshotImpl(p.eClass().getEPackage().getNsURI(),
                        EMFUtil.getModelName(p.eClass()), p, snapshotTime));
        if (providerFilter != null) {
            providersStream = providersStream.filter(providerFilter);
        }

        // Filter providers by location (raw provider)
        if (geoFilter != null) {
            // Filter the provider location
            providersStream = providersStream.filter(p -> geoFilter.test(p.getModelProvider().getAdmin().getLocation()));
        }


        // Filter providers according to their services
        providersStream = providersStream.map(p -> {
            final Provider modelProvider = p.getModelProvider();
            nexusImpl.getServiceInstancesForProvider(modelProvider)
                    .forEach((k, v) -> p.add(new ServiceSnapshotImpl(p, k, v, snapshotTime)));
            return p;
        });
        if (svcFilter != null) {
            providersStream = providersStream.filter(p -> p.getServices().stream().anyMatch(svcFilter));
        }

        // Filter providers according to their resources
        providersStream = providersStream.map(p -> {
            p.getServices().stream().forEach(s -> {
                nexusImpl.getResourcesForService(s.getModelService().eClass())
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
                nexusImpl.getProviderPackageUri(nexusProvider.getId()),
                nexusImpl.getProviderModel(nexusProvider.getId()), nexusProvider, snapshotTime);

        // Add all services
        nexusImpl.getServiceInstancesForProvider(nexusProvider).forEach((serviceName, service) -> {
            // Get the service
            final ServiceSnapshotImpl svcSnapshot = new ServiceSnapshotImpl(providerSnapshot, serviceName, service,
                    snapshotTime);

            // Get the resources
            nexusImpl.getResourcesForService(service.eClass()).forEach(rcFeature -> {
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

        Service service = nexusImpl.getServiceInstancesForProvider(nexusProvider).get(serviceName);

        if (service == null) {
            // Service not found
            return null;
        }

        // Minimal snapshot of the provider owning the service
        final ProviderSnapshotImpl providerSnapshot = new ProviderSnapshotImpl(
                nexusImpl.getProviderPackageUri(nexusProvider.getId()),
                nexusImpl.getProviderModel(nexusProvider.getId()), nexusProvider, snapshotTime);

        // Describe the service
        final ServiceSnapshotImpl svcSnapshot = new ServiceSnapshotImpl(providerSnapshot, serviceName, service,
                snapshotTime);
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

        Service service = nexusImpl.getServiceInstancesForProvider(nexusProvider).get(serviceName);

        if (service == null) {
            // Service not found
            return null;
        }

        final EStructuralFeature sf = nexusProvider.eClass().getEStructuralFeature(serviceName);
        final Optional<ETypedElement> foundRc = nexusImpl.getResourcesForService((EClass) sf.getEType())
                .filter(f -> f.getName().equals(resourceName)).findFirst();
        if (foundRc.isEmpty()) {
            // Resource not found
            return null;
        }

        // Minimal description of the provider owning the service
        final ProviderSnapshotImpl providerSnapshot = new ProviderSnapshotImpl(
                nexusImpl.getProviderPackageUri(nexusProvider.getId()),
                nexusImpl.getProviderModel(nexusProvider.getId()), nexusProvider, snapshotTime);

        // Minimal description of the service owning the resource
        final ServiceSnapshotImpl svcSnapshot = new ServiceSnapshotImpl(providerSnapshot, serviceName, service,
                snapshotTime);
        providerSnapshot.add(svcSnapshot);

        // Describe the resource
        final ResourceSnapshotImpl rcSnapshot = new ResourceSnapshotImpl(svcSnapshot, foundRc.get(), snapshotTime);
        fillInResource(rcSnapshot);
        svcSnapshot.add(rcSnapshot);
        return rcSnapshot;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.sensinact.core.emf.twin.SensinactEMFDigitalTwin#getProviders(java
     * .lang.String)
     */
    @Override
    public List<? extends SensinactEMFProvider> getProviders(String model) {
        return getProviders(null, model);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.sensinact.core.emf.twin.SensinactEMFDigitalTwin#getProvider(java.
     * lang.String, java.lang.String)
     */
    @Override
    public SensinactEMFProvider getProvider(String model, String providerName) {
        return getProvider(null, model, providerName);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.sensinact.core.emf.twin.SensinactEMFDigitalTwin#createProvider(
     * java.lang.String, java.lang.String)
     */
    @Override
    public SensinactEMFProvider createProvider(String model, String providerName) {
        return createProvider(null, model, providerName);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.sensinact.core.emf.twin.SensinactEMFDigitalTwin#createProvider(
     * java.lang.String, java.lang.String, java.time.Instant)
     */
    @Override
    public SensinactEMFProvider createProvider(String model, String providerName, Instant created) {
        return createProvider(null, model, providerName, created);
    }
}
