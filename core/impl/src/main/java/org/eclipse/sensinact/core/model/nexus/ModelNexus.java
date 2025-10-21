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
*   Data In Motion - initial API and implementation
*   Kentyou - fixes and updates to include a basic sensiNact provider
**********************************************************************/
package org.eclipse.sensinact.core.model.nexus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sensinact.core.command.impl.ActionHandler;
import org.eclipse.sensinact.core.command.impl.ResourcePullHandler;
import org.eclipse.sensinact.core.command.impl.ResourcePushHandler;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.core.model.nexus.emf.NamingUtils;
import org.eclipse.sensinact.core.model.nexus.emf.compare.EMFCompareUtil;
import org.eclipse.sensinact.core.notification.impl.NotificationAccumulator;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.core.whiteboard.impl.SensinactWhiteboard;
import org.eclipse.sensinact.model.core.provider.Admin;
import org.eclipse.sensinact.model.core.provider.DynamicProvider;
import org.eclipse.sensinact.model.core.provider.Metadata;
import org.eclipse.sensinact.model.core.provider.MetadataValue;
import org.eclipse.sensinact.model.core.provider.ModelMetadata;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.model.core.provider.ProviderFactory;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.model.core.provider.ResourceMetadata;
import org.eclipse.sensinact.model.core.provider.ResourceValueMetadata;
import org.eclipse.sensinact.model.core.provider.Service;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Promises;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Central Nexus for Models
 *
 * @author Juergen Albert
 * @since 26 Sep 2022
 */
public class ModelNexus {

    private static final Logger LOG = LoggerFactory.getLogger(ModelNexus.class);

    private static final String BASE = "data/";
    private static final String BASIC_EPACKAGES = BASE + "ePackages.ecore";
    private static final String BASIC_PROVIDERS = BASE + "providers.xmi";

    private final ResourceSet resourceSet;
    private final ProviderPackage providerPackage;
    private final Supplier<NotificationAccumulator> notificationAccumulator;

    private final Map<String, Provider> providers = new HashMap<>();

    /**
     * The reverse mapping of child providers to parent providers
     */
    private final Map<String, Set<String>> childToParents = new HashMap<>();

//    private final Map<String, EClass> models = new HashMap<>();

    private final SensinactWhiteboard whiteboard;

    public ModelNexus(ResourceSet resourceSet, ProviderPackage ProviderPackage,
            Supplier<NotificationAccumulator> accumulator) {
        this(resourceSet, ProviderPackage, accumulator, (SensinactWhiteboard) null);
    }

    public ModelNexus(ResourceSet resourceSet, ProviderPackage ProviderPackage,
            Supplier<NotificationAccumulator> accumulator, ActionHandler actionHandler) {
        this(resourceSet, ProviderPackage, accumulator, actionHandler, null, null);
    }

    public ModelNexus(ResourceSet resourceSet, ProviderPackage ProviderPackage,
            Supplier<NotificationAccumulator> accumulator, ActionHandler actionHandler,
            ResourcePullHandler resourceValuePullHandler, ResourcePushHandler resourceValuePushHandler) {
        this(resourceSet, ProviderPackage, accumulator, new SensinactWhiteboard(null, null) {
            @Override
            public Promise<Object> act(String modelPackageUri, String model, String provider, String service,
                    String resource, Map<String, Object> arguments) {
                if (actionHandler != null) {
                    return actionHandler.act(modelPackageUri, model, provider, service, resource, arguments);
                }
                throw new RuntimeException("No action handler set");
            }

            @Override
            public <T> Promise<TimedValue<T>> pullValue(String modelPackageUri, String model, String provider,
                    String service, String resource, Class<T> type, TimedValue<T> cachedValue,
                    Consumer<TimedValue<T>> gatewayUpdate) {
                if (resourceValuePullHandler != null) {
                    return resourceValuePullHandler.pullValue(modelPackageUri, model, provider, service, resource, type,
                            cachedValue, gatewayUpdate);
                }
                throw new RuntimeException("No pullValue handler set");
            }

            @Override
            public <T> Promise<TimedValue<T>> pushValue(String modelPackageUri, String model, String provider,
                    String service, String resource, Class<T> type, TimedValue<T> cachedValue, TimedValue<T> newValue,
                    Consumer<TimedValue<T>> gatewayUpdate) {
                if (resourceValuePushHandler != null) {
                    return resourceValuePushHandler.pushValue(modelPackageUri, model, provider, service, resource, type,
                            cachedValue, newValue, gatewayUpdate);
                }
                throw new RuntimeException("No pushValue handler set");
            }
        });
    }

    public ModelNexus(ResourceSet resourceSet, ProviderPackage ProviderPackage,
            Supplier<NotificationAccumulator> accumulator, SensinactWhiteboard whiteboard) {
        this.resourceSet = resourceSet;
        this.providerPackage = ProviderPackage;
        this.notificationAccumulator = accumulator;
        this.whiteboard = whiteboard;
        // TODO we need a general Working Directory for such data
//		loadEPackages(Paths.get(BASIC_EPACKAGES));

        if (!resourceSet.getPackageRegistry().containsKey(EMFUtil.DEFAULT_SENSINACT_PACKAGE_URI)) {
            EMFUtil.createPackage("base", EMFUtil.DEFAULT_SENSINACT_PACKAGE_URI, "sensinactBase", this.resourceSet);
        }

//		loadInstances(Paths.get(BASIC_PROVIDERS));
        setupSensinactProvider();

    }

    private void loadEPackages(Path fileName) {
        if (Files.isRegularFile(fileName)) {
            Resource resource = resourceSet.createResource(URI.createFileURI(fileName.toString()));
            try {
                resource.load(null);
                if (!resource.getContents().isEmpty()) {

                    resource.getContents().forEach(e -> {
                        EPackage ePackage = (EPackage) e;
                        resource.setURI(URI.createURI(ePackage.getNsURI()));
                        EcoreUtil.resolveAll(ePackage);
                        resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
                    });

                    resourceSet.getResources().remove(resource);
                }
            } catch (IOException e) {
                LOG.error(
                        "THIS WILL BE A RUNTIME EXCPETION FOR NOW: Error Loading default EPackage from persistent file: {}",
                        fileName, e);
                throw new RuntimeException(e);
            }
        }
    }

    private void loadInstances(Path fileName) {
        if (Files.isRegularFile(fileName)) {
            try {
                // TODO should we fire events here?
                URI uri = URI.createFileURI(fileName.toString());
                Resource resource = resourceSet.createResource(uri);
                resource.load(null);
                if (!resource.getContents().isEmpty()) {
                    resource.getContents().forEach(e -> {
                        Provider provider = (Provider) e;
                        EClass eClass = provider.eClass();
                        registerModel(eClass, Instant.ofEpochMilli(resource.getTimeStamp()), true);
                        providers.put(provider.getId(), provider);
                    });
                }
                resource.getContents().clear();
                resourceSet.getResources().remove(resource);
            } catch (IOException e) {
                LOG.error("THIS WILL BE A RUNTIME EXCPETION FOR NOW: Error loading provider from Path: {}", fileName,
                        e);
                throw new RuntimeException(e);
            }
        }
    }

    // TODO: This needs to become a predefined model
    private void setupSensinactProvider() {
        if (!providers.containsKey("sensinact")) {
            Instant now = Instant.now();
            EClass sensiNactModel = getModel(EMFUtil.DEFAULT_SENSINACT_PACKAGE_URI, "sensinact")
                    .orElseGet(() -> createModel(EMFUtil.DEFAULT_SENSINACT_PACKAGE_URI, "sensinact", now));
            EReference svc = Optional.ofNullable(getServiceForModel(sensiNactModel, "system"))
                    .orElseGet(() -> createService(sensiNactModel, "system", "System", now));
            EClass svcClass = svc.getEReferenceType();
            EStructuralFeature versionResource = Optional.ofNullable(svcClass.getEStructuralFeature("version"))
                    .orElseGet(() -> createResource(svcClass, "version", double.class, now, null));
            EStructuralFeature startedResource = Optional.ofNullable(svcClass.getEStructuralFeature("started"))
                    .orElseGet(() -> createResource(svcClass, "started", Instant.class, now, null));

            Provider provider = Optional.ofNullable(getProvider("sensiNact"))
                    .orElseGet(() -> doCreateProvider(sensiNactModel, "sensiNact", now));

            handleDataUpdate(provider, svc.getName(), svcClass, versionResource, 0.1D, now);
            handleDataUpdate(provider, svc.getName(), svcClass, startedResource, now, now);
        }
    }

    public void shutDown() {

        Resource providers = resourceSet.createResource(URI.createURI(BASIC_PROVIDERS));
        this.providers.values().forEach(providers.getContents()::add);

        try {
            providers.save(Collections.emptyMap());
        } catch (IOException e) {
            LOG.error("Could not save provider instances", e);
        }

        // Always do this last, because it would mess up the Package URIs in the saved
        // providers and complicate loading
        Resource ePackages = resourceSet.createResource(URI.createURI(BASIC_EPACKAGES));
        resourceSet.getPackageRegistry().entrySet().stream()
                .filter(e -> !EPackage.Registry.INSTANCE.containsKey(e.getKey())).map(Entry::getValue)
                .map(EPackage.class::cast).forEach(ePackages.getContents()::add);

        try {
            ePackages.save(Collections.emptyMap());
        } catch (IOException e) {
            LOG.error("Could not save EPackages", e);
        }

        resourceSet.getResources().clear();

    }

    /**
     * Will associate the given Parent provider with the given child. If parent or
     * child do not exist then an exception will be raised.
     *
     * @param parentProvider The provider name of the parent. The name will be used
     *                       as ID and in the first setup as the friendlyname for
     *                       the Admin Service
     * @param childProvider  The provider name of the child. The name will be used
     *                       as ID and in the first setup as the friendlyname for
     *                       the Admin service
     * @param timestamp      the timestamp when the link is created. If null, the
     *                       current timestamp is used.
     */
    public void linkProviders(String parentProvider, String childProvider, Instant timestamp) {

        Provider parent = providers.get(parentProvider);

        Provider child = providers.get(childProvider);

        if (parent == null) {
            throw new IllegalArgumentException("No parent provider " + parentProvider);
        }
        if (child == null) {
            throw new IllegalArgumentException("No child provider " + childProvider);
        }

        Instant metaTimestamp = timestamp == null ? Instant.now() : timestamp;

        ProviderPackage pp = ProviderPackage.eINSTANCE;

        Admin admin = parent.getAdmin();
        ResourceValueMetadata metadata = getOrInitializeResourceMetadata(admin, pp.getProvider_LinkedProviders());
        Instant oldTs = metadata.getTimestamp();
        if (oldTs == null || !oldTs.isAfter(metaTimestamp)) {
            Set<String> set = childToParents.get(childProvider);
            if (set == null) {
                set = new HashSet<>();
                childToParents.put(childProvider, set);
            }

            if (set.add(parentProvider)) {
                if (!parent.isSetLinkedProviders()) {
                    parent.eSet(pp.getProvider_LinkedProviders(), new BasicEList<>());
                }
                metadata.setTimestamp(metaTimestamp);
                EList<Provider> linkedProviders = parent.getLinkedProviders();
                linkedProviders.add(child);
                notificationAccumulator.get().link(admin.getModelPackageUri(), admin.getModel(), parentProvider,
                        linkedProviders.stream().map(Provider::getId).toList(), childProvider, metaTimestamp);
            } else {
                LOG.debug("The parent provider {} already has a linked child {}", parentProvider, childProvider);
            }
        } else {
            LOG.debug("The existing parent provider linked providers update time {} is after the new update time {}",
                    oldTs, metaTimestamp);
        }
    }

    /**
     * Will disassociate the given Parent provider with the given child.
     *
     * @param parentProvider The provider name of the parent.
     * @param childProvider  The provider name of the child.
     * @param timestamp      the timestamp when the link is removed. If null, the
     *                       current timestamp is used.
     */
    public void unlinkProviders(String parentProvider, String childProvider, Instant timestamp) {

        Provider parent = providers.get(parentProvider);

        Provider child = providers.get(childProvider);

        if (parent == null) {
            throw new IllegalArgumentException("No parent provider " + parentProvider);
        }
        if (child == null) {
            throw new IllegalArgumentException("No child provider " + childProvider);
        }

        Instant metaTimestamp = timestamp == null ? Instant.now() : timestamp;

        ProviderPackage pp = ProviderPackage.eINSTANCE;

        Admin admin = parent.getAdmin();
        ResourceValueMetadata metadata = getOrInitializeResourceMetadata(admin, pp.getProvider_LinkedProviders());
        Instant oldTs = metadata.getTimestamp();
        if (oldTs == null || !oldTs.isAfter(metaTimestamp)) {
            Set<String> set = childToParents.get(childProvider);

            if (set != null && set.remove(parentProvider)) {
                metadata.setTimestamp(metaTimestamp);
                EList<Provider> linkedProviders = parent.getLinkedProviders();
                linkedProviders.remove(child);
                notificationAccumulator.get().unlink(admin.getModelPackageUri(), admin.getModel(), parentProvider,
                        linkedProviders.stream().map(Provider::getId).toList(), childProvider, metaTimestamp);
            } else {
                LOG.debug("The parent provider {} has no linked child {}", parentProvider, childProvider);
            }
        } else {
            LOG.debug("The existing parent provider linked providers update time {} is after the new update time {}",
                    oldTs, metaTimestamp);
        }
    }

    public void handleDataUpdate(Provider provider, String serviceName, EClass serviceEClass,
            EStructuralFeature resourceFeature, Object data, Instant timestamp) {
        handleDataUpdate(provider, serviceName, null, serviceEClass, resourceFeature, data, timestamp);
    }

    public void handleDataUpdate(Provider provider, String serviceName, EReference serviceReferece,
            EClass serviceEClass, EStructuralFeature resourceFeature, Object data, Instant timestamp) {

        Service service = provider.getService(serviceName);
        String providerName = provider.getId();
        String modelName = EMFUtil.getModelName(provider.eClass());
        String packageUri = provider.eClass().getEPackage().getNsURI();
        NotificationAccumulator accumulator = notificationAccumulator.get();
        if (service == null) {
            service = createServiceInstance(provider, serviceName, serviceEClass, serviceReferece);
        }

        handleDataUpdate(provider, serviceName, service, resourceFeature, data, timestamp, accumulator, packageUri,
                modelName, providerName);
    }

    public Service createServiceInstance(Provider provider, String serviceName, EClass serviceEClass) {
        return createServiceInstance(provider, serviceName, serviceEClass, null);
    }

    public Service createServiceInstance(Provider provider, String serviceName, EClass serviceEClass,
            EReference serviceReferece) {
        String providerName = provider.getId();
        String modelName = EMFUtil.getModelName(provider.eClass());
        String packageUri = provider.eClass().getEPackage().getNsURI();
        NotificationAccumulator accumulator = notificationAccumulator.get();
        Service service = null;
        Optional<EReference> serviceFeature = Optional.ofNullable(serviceReferece)
                .or(() -> getServiceReferencesForModel(provider.eClass())
                        .filter(ref -> serviceName.equals(ref.getName())).findFirst());
        if (serviceFeature.isEmpty() && !(provider instanceof DynamicProvider)) {
            throw new IllegalArgumentException("No Service with name " + serviceName + " exists for provider "
                    + provider.getId() + " of model " + EMFUtil.getModelName(provider.eClass()));
        }
        if (serviceFeature.isEmpty() && provider instanceof DynamicProvider) {
            service = (Service) EcoreUtil.create(serviceEClass);
            ((DynamicProvider) provider).getServices().put(serviceName, service);
        } else {
            service = (Service) EcoreUtil.create((EClass) serviceFeature.get().getEType());
            provider.eSet(serviceFeature.get(), service);
        }
        accumulator.addService(packageUri, modelName, providerName, serviceName);
        return service;
    }

    private void handleDataUpdate(Provider provider, String serviceName, Service service,
            EStructuralFeature resourceFeature, Object data, Instant timestamp, NotificationAccumulator accumulator,
            String packageUri, String modelName, String providerName) {

        Instant metaTimestamp = timestamp == null ? Instant.now() : timestamp;

        ResourceValueMetadata metadata = service.getMetadata().get(resourceFeature);

        Map<String, Object> oldMetaData = null;
        Object oldValue = service.eGet(resourceFeature);
        if (metadata != null) {
            oldMetaData = EMFCompareUtil.extractMetadataMap(oldValue, metadata, resourceFeature);
        }
        if (oldValue == null) {
            accumulator.addResource(packageUri, modelName, providerName, serviceName, resourceFeature.getName());
        }

        // Allow an update if the resource didn't exist or if the update timestamp is
        // equal to or after the one of the current value
        if (metadata == null || metadata.getTimestamp() == null
                || !metadata.getTimestamp().isAfter(metaTimestamp.plusMillis(1))) {
            EClassifier resourceType = resourceFeature.getEType();

            if (metadata == null) {
                metadata = ProviderFactory.eINSTANCE.createResourceValueMetadata();
                if (resourceFeature instanceof Metadata) {
                    metadata.getExtra().addAll(((Metadata) resourceFeature).getExtra());
                }
                service.getMetadata().put(resourceFeature, metadata);
            }
            metadata.setTimestamp(metaTimestamp);

            final Object storedData;
            if (data == null || resourceType.isInstance(data)) {
                storedData = data;
            } else {
                storedData = EMFUtil.convertToTargetType(resourceType, data);
            }
            service.eSet(resourceFeature, storedData);

            Map<String, Object> newMetaData = EMFCompareUtil.extractMetadataMap(storedData, metadata, resourceFeature);

            accumulator.resourceValueUpdate(packageUri, modelName, providerName, serviceName, resourceFeature.getName(),
                    resourceType.getInstanceClass(), oldValue, storedData, newMetaData, metaTimestamp);
            accumulator.metadataValueUpdate(packageUri, modelName, providerName, serviceName, resourceFeature.getName(),
                    oldMetaData, newMetaData, timestamp);
        } else {
            return;
        }
    }

    /**
     * Expects the caller to have checked that no provider exists for the given name
     *
     * @param modelName
     * @param providerName
     * @param timestamp
     * @param accumulator
     * @return
     */
    private Provider doCreateProvider(EClass model, String providerName, Instant timestamp) {
        return doCreateProvider(model, providerName, timestamp, true);
    }

    private Provider doCreateProvider(EClass model, String providerName, Instant timestamp, boolean createAdmin) {

        Provider provider = (Provider) EcoreUtil.create(model);
        provider.setId(providerName);

        notificationAccumulator.get().addProvider(model.getEPackage().getNsURI(), EMFUtil.getModelName(model),
                providerName);
        if (createAdmin) {
            createAdminServiceForProvider(provider, timestamp);
        }

        providers.put(providerName, provider);

        return provider;
    }

    private void createAdminServiceForProvider(Provider original, Instant timestamp) {

        Provider provider = EcoreUtil.copy(original);

        final Admin adminSvc = ProviderFactory.eINSTANCE.createAdmin();
        provider.setAdmin(adminSvc);

        // Set a timestamp to admin resources to indicate them as valued
        for (EStructuralFeature resourceFeature : provider.getAdmin().eClass().getEStructuralFeatures()) {
            if (resourceFeature == ProviderPackage.Literals.ADMIN__FRIENDLY_NAME
                    || resourceFeature == ProviderPackage.Literals.ADMIN__DESCRIPTION
                    || resourceFeature == ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI
                    || resourceFeature == ProviderPackage.Literals.ADMIN__MODEL) {
                ResourceValueMetadata metadata = ProviderFactory.eINSTANCE.createResourceValueMetadata();
                // N.B. We currently don't copy default metadata for the admin resources
                metadata.setTimestamp(timestamp);
                adminSvc.getMetadata().put(resourceFeature, metadata);
            }
        }
        adminSvc.setFriendlyName(provider.getId());
        adminSvc.setModelPackageUri(provider.eClass().getEPackage().getNsURI());
        adminSvc.setModel(EMFUtil.getModelName(provider.eClass()));

        EMFCompareUtil.compareAndSet(provider, original, notificationAccumulator.get());

    }

    public Provider createProviderInstance(String modelName, String providerName) {
        return createProviderInstance(EMFUtil.constructPackageUri(modelName), modelName, providerName, Instant.now());
    }

    public Provider createProviderInstance(String modelName, String providerName, Instant timestamp) {
        return createProviderInstance(EMFUtil.constructPackageUri(modelName), modelName, providerName, timestamp);
    }

    public Provider createProviderInstance(String modelPackageUri, String modelName, String providerName) {
        return createProviderInstance(modelPackageUri, modelName, providerName, Instant.now());
    }

    public Provider createProviderInstance(String modelPackageUri, String modelName, String providerName,
            Instant timestamp) {

        Provider provider = getProvider(providerName);
        if (provider != null) {
            String m = EMFUtil.getModelName(provider.eClass());
            if (!m.equals(modelName)) {
                throw new IllegalArgumentException(
                        "The provider " + providerName + " already exists with a different model " + m);
            } else {
                throw new IllegalArgumentException(
                        "The provider " + providerName + " already exists with the model " + modelName);
            }
        } else {
            provider = doCreateProvider(getMandatoryModel(modelPackageUri, modelName), providerName, timestamp);
        }

        return provider;
    }

    public Provider getProvider(String providerName) {
        return providers.get(providerName);
    }

    public String getProviderModel(String providerName) {
        return Optional.ofNullable(providers.get(providerName)).map(p -> EMFUtil.getModelName(p.eClass())).orElse(null);
    }

    public String getProviderPackageUri(String providerName) {
        return Optional.ofNullable(providers.get(providerName)).map(p -> p.eClass().getEPackage().getNsURI())
                .orElse(null);
    }

    public Provider getProvider(String modelPackageUri, String model, String providerName) {
        Provider p = providers.get(providerName);
        if (p != null) {
            String m = EMFUtil.getModelName(p.eClass());
            String mp = modelPackageUri == null ? EMFUtil.constructPackageUri(model) : modelPackageUri;
            if (!m.equals(model) || !p.eClass().getEPackage().getNsURI().equals(mp)) {
                LOG.warn("Provider {} exists but with model {} or package {} not model {} of package {}", providerName,
                        m, p.eClass().getEPackage().getNsURI(), model, mp);
                p = null;
            }
        }
        return p;
    }

    public Collection<Provider> getProviders() {
        return Collections.unmodifiableCollection(providers.values());
    }

    /**
     * Lists know providers
     */
    public List<Provider> getProviders(String modelPackageUri, String model) {
        return getProviders(getMandatoryModel(modelPackageUri, model));
    }

    private List<Provider> getProviders(EClass model) {
        // Don't use isInstance as subtypes have a different model
        return providers.values().stream().filter(p -> p.eClass().equals(model)).collect(Collectors.toList());
    }

    public EAttribute createResource(EClass service, String resource, Class<?> type, Instant timestamp,
            Object defaultValue) {
        return createResource(service, resource, type, timestamp, defaultValue, Map.of(), false, 0, false);
    }

    public EAttribute createResource(EClass service, String resource, Class<?> type, Instant timestamp,
            Object defaultValue, Map<String, Object> defaultMetadata, boolean hasGetter, long getterCacheMs,
            boolean hasSetter) {

        return doCreateResource(service, resource, type, timestamp, defaultValue, defaultMetadata, List.of(), hasGetter,
                getterCacheMs, hasSetter);
    }

    private EAttribute doCreateResource(EClass service, String resource, Class<?> type, Instant timestamp,
            Object defaultValue, Map<String, Object> defaultMetadata, List<MetadataValue> metadata, boolean hasGetter,
            long getterCacheMs, boolean hasSetter) {
        assertResourceNotExist(service, resource);
        ResourceMetadata resourceMetaData = EMFUtil.createResourceAttribute(service, resource, type, defaultValue);
        resourceMetaData.setExternalGet(hasGetter);
        resourceMetaData.setExternalSet(hasSetter);
        if (getterCacheMs > 0) {
            resourceMetaData.setExternalGetCacheMs(getterCacheMs);
        }
        EMap<String, MetadataValue> defaultFeatureMetadata = defaultMetadata == null ? ECollections.emptyEMap()
                : toDefaultMetadataValue(defaultMetadata);
        EMFUtil.fillMetadata(resourceMetaData, timestamp, false, resource, defaultFeatureMetadata);
        return (EAttribute) resourceMetaData.eContainer().eContainer();
    }

    private EMap<String, MetadataValue> toDefaultMetadataValue(Map<String, Object> defaultMetadata) {
        HashMap<String, MetadataValue> m = new HashMap<>();
        for (Entry<String, Object> entry : defaultMetadata.entrySet()) {
            m.put(entry.getKey(), EMFUtil.createMetadataValue(null, entry.getValue()));
        }
        return new BasicEMap<>(m);
    }

    private void assertResourceNotExist(EClass service, String resource) {
        ETypedElement element = service.getEOperations().stream().filter(o -> o.getName().equals(resource))
                .map(ETypedElement.class::cast).findFirst().orElseGet(() -> service.getEStructuralFeature(resource));
        if (element != null) {
            throw new IllegalArgumentException(
                    "There is an existing resource with name " + resource + " in service " + service + " in model "
                            + EMFUtil.getModelName(service.eContainingFeature().getEContainingClass()));
        }
    }

    public EClass createModel(String modelName, Instant timestamp) {
        return createModel(EMFUtil.constructPackageUri(modelName), modelName, timestamp);
    }

    public EClass createModel(String theModelPackageUri, String modelName, Instant timestamp) {
        if (theModelPackageUri == null || theModelPackageUri.isBlank()) {
            return createModel(modelName, timestamp);
        }
        if (getModel(theModelPackageUri, modelName).isPresent()) {
            throw new IllegalArgumentException("There is an existing model with name " + modelName);
        }

        String modelClassName = NamingUtils.sanitizeName(modelName, false);
        EPackage ePackage = resourceSet.getPackageRegistry().getEPackage(theModelPackageUri);
        if (ePackage == null) {
            ePackage = EMFUtil.createPackage(modelName, theModelPackageUri, modelName, resourceSet);

        }
        EClass model = EMFUtil.createEClass(modelClassName, ePackage, null, ProviderPackage.Literals.PROVIDER);
        ModelMetadata metadata = ProviderFactory.eINSTANCE.createModelMetadata();
        EMFUtil.addMetaDataAnnnotation(model, metadata);
        EMFUtil.fillMetadata(metadata, timestamp, false, modelName, ECollections.emptyEMap());
        return model;
    }

    private EReference doCreateService(EClass model, String refName, String serviceModelName, Instant timestamp) {
        EPackage ePackage = model.getEPackage();
        EClass service = EMFUtil.createEClass(NamingUtils.sanitizeName(serviceModelName, false), ePackage, null,
                ProviderPackage.Literals.SERVICE);
        EReference ref = EMFUtil.createServiceReference(model, refName, service, true);
        EMFUtil.fillMetadata(EMFUtil.getModelMetadata(ref), timestamp, false, refName, ECollections.emptyEMap());
        return ref;
    }

    private Map<String, Object> getResourceMetadata(Provider provider, Service svc, String serviceName,
            final ETypedElement rcFeature) {
        if (svc == null) {
            svc = createServiceInstance(provider, serviceName, null, null);
        }
        ResourceValueMetadata metadata = getOrInitializeResourceMetadata(svc, rcFeature);
        return EMFUtil.toMetadataAttributesToMap(metadata, rcFeature);
    }

    private ResourceValueMetadata getOrInitializeResourceMetadata(Service svc, final ETypedElement rcFeature) {
        ResourceValueMetadata metadata = svc.getMetadata().get(rcFeature);
        if (metadata == null) {
            metadata = ProviderFactory.eINSTANCE.createResourceValueMetadata();
            if (EMFUtil.getModelMetadata(rcFeature) != null) {
                metadata.getExtra().addAll(EMFUtil.getModelMetadata(rcFeature).getExtra());
            }
            svc.getMetadata().put(rcFeature, metadata);
        }
        return metadata;
    }

    public Map<String, Object> getResourceMetadata(Provider provider, String serviceName,
            final ETypedElement rcFeature) {
        Service svc = provider.getService(serviceName);
        return getResourceMetadata(provider, svc, serviceName, rcFeature);
    }

    public TimedValue<Object> getResourceMetadataValue(Provider provider, String serviceName,
            final ETypedElement rcFeature, String key) {
        final Service svc = provider.getService(serviceName);
        if (svc == null) {
            return null;
        }

        final ResourceValueMetadata metadata = getOrInitializeResourceMetadata(svc, rcFeature);
        if (metadata != null) {
            EMap<String, MetadataValue> extra = metadata.getExtra();
            MetadataValue MetadataValue = extra.get(key);
            if (MetadataValue != null)
                return new DefaultTimedValue<>(MetadataValue.getValue(), MetadataValue.getTimestamp());
            else
                // If the resource exists but has no metadata for that key then return an
                // empty timed value indicating that the resource exists but the metadata
                // is not set
                return new DefaultTimedValue<>(null, null);
        }
        return null;
    }

    public void setResourceMetadata(Provider provider, EStructuralFeature svcFeature, ETypedElement resource,
            String metadataKey, Object value, Instant timestamp) {
        final Service svc = (Service) provider.eGet(svcFeature);
        setResourceMetadata(provider, svcFeature.getName(), svc, resource, metadataKey, value, timestamp);
    }

    public void setResourceMetadata(Provider provider, String serviceName, ETypedElement resource, String metadataKey,
            Object value, Instant timestamp) {
        EStructuralFeature feature = provider.eClass().getEStructuralFeature(serviceName);
        if (feature != null) {
            setResourceMetadata(provider, feature, resource, metadataKey, value, timestamp);
            return;
        } else if (provider instanceof DynamicProvider) {
            Service svc = ((DynamicProvider) provider).getServices().get(serviceName);
            if (svc != null) {
                setResourceMetadata(provider, serviceName, svc, resource, metadataKey, value, timestamp);
            }
        }
    }

    private void setResourceMetadata(Provider provider, String serviceName, Service svc, ETypedElement resource,
            String metadataKey, Object value, Instant timestamp) {
        if (svc == null) {
            throw new IllegalArgumentException("Service must not be null");
        }
        if (metadataKey == null || metadataKey.isEmpty()) {
            throw new IllegalArgumentException("Empty metadata key");
        }

        if (timestamp == null) {
            throw new IllegalArgumentException("Invalid timestamp");
        }

        final ResourceValueMetadata metadata = getOrInitializeResourceMetadata(svc, resource);

        Map<String, Object> oldMetadata = EMFUtil.toMetadataAttributesToMap(metadata, resource);

        EMap<String, MetadataValue> extra = metadata.getExtra();
        MetadataValue fcm = extra.get(metadataKey);
        if (fcm == null) {
            extra.put(metadataKey, EMFUtil.createMetadataValue(timestamp, value));
        } else {
            EMFUtil.handleMetadataValue(fcm, timestamp, value);
        }
        Map<String, Object> newMetadata = EMFUtil.toMetadataAttributesToMap(metadata, resource);

        notificationAccumulator.get().metadataValueUpdate(provider.eClass().getEPackage().getNsURI(),
                EMFUtil.getModelName(provider.eClass()), provider.getId(), serviceName, resource.getName(), oldMetadata,
                newMetadata, timestamp);

    }

    public Set<String> getModelNames() {
        // TODO what do we do here now?
        throw new UnsupportedOperationException("Not implemented yet1");
//        return Set.copyOf(models.keySet());
    }

    public Set<String> getModelNames(String modelPackageUri) {
        EPackage ePackage = resourceSet.getPackageRegistry().getEPackage(modelPackageUri);
        return getModelNames(ePackage);
    }

    public Set<String> getModelNames(EPackage ePackage) {
        return getProviderEClassesFromEPackage(ePackage).map(EMFUtil::getModelName).collect(Collectors.toSet());

    }

    public Optional<EClass> getModel(String modelPackageUri, String modelName) {
        String themodelPackageUri = modelPackageUri;
        if (themodelPackageUri == null || modelPackageUri.isBlank()) {
            themodelPackageUri = EMFUtil.constructPackageUri(modelName);
        }

        EPackage ePackage = resourceSet.getPackageRegistry().getEPackage(themodelPackageUri);

        if (ePackage == null) {
            return Optional.empty();
        }

        EClass result = (EClass) ePackage.getEClassifier(modelName);

        if (result != null) {
            return Optional.of(result);
        }

        return ePackage.getEClassifiers().stream().filter(EClass.class::isInstance).map(EClass.class::cast)
                .filter(ec -> EMFUtil.getModelName(ec).equals(modelName)).findFirst();
    }

    public boolean registered(EClass eClass) {
        EPackage ePackage = eClass.getEPackage();
        // if a package is registered, all EClasses are automatically registered
        return ePackage != null && registered(ePackage) && ePackage.getEClassifiers().contains(eClass);
    }

    public boolean registered(EPackage ePackage) {
        // if a package is registered, all EClasses are automatically registered
        return ePackage.equals(resourceSet.getPackageRegistry().getEPackage(ePackage.getNsURI()));
    }

    private EClass getMandatoryModel(String modelPackageUri, String modelName) {
        return getModel(modelPackageUri, modelName)
                .orElseThrow(() -> new IllegalArgumentException("No model with name " + modelName));
    }

    public EReference createService(EClass model, String service, String serviceModelName, Instant creationTimestamp) {
        if (model.getEStructuralFeature(service) != null) {
            throw new IllegalArgumentException(
                    "There is an existing service with name " + service + " in model " + model);
        }
        return doCreateService(model, service, serviceModelName, creationTimestamp);
    }

    public Stream<EReference> getServiceReferencesForModel(EClass model) {
        EClass svcClass = ProviderPackage.Literals.SERVICE;
        return model.getEAllReferences().stream().filter(r -> svcClass.isSuperTypeOf(r.getEReferenceType()));
    }

    public Map<String, EClass> getDefinedServiceForProvider(Provider provider) {
        Map<String, EClass> result = new LinkedHashMap<>();
        getServiceReferencesForModel(provider.eClass()).forEach((feature) -> {
            result.put(feature.getName(), feature.getEReferenceType());
        });

        if (provider instanceof DynamicProvider) {
            ((DynamicProvider) provider).getServices().forEach(e -> result.put(e.getKey(), e.getValue().eClass()));
        }
        return Collections.unmodifiableMap(result);
    }

    public Map<String, Entry<EClass, Service>> getServiceInstancesForProvider(Provider provider) {
        Map<String, Entry<EClass, Service>> result = new LinkedHashMap<>();
        getServiceReferencesForModel(provider.eClass()).forEach((feature) -> {
            result.put(feature.getName(), new AbstractMap.SimpleImmutableEntry<>((EClass) feature.getEType(),
                    provider.eIsSet(feature) ? (Service) provider.eGet(feature) : null));
        });

        if (provider instanceof DynamicProvider) {
            ((DynamicProvider) provider).getServices().forEach(e -> result.put(e.getKey(),
                    new AbstractMap.SimpleImmutableEntry<>(e.getValue().eClass(), e.getValue())));
        }
        return Collections.unmodifiableMap(result);
    }

    public EReference getServiceForModel(EClass model, String serviceName) {
        EStructuralFeature feature = model.getEStructuralFeature(serviceName);
        EClass serviceEClass = ProviderPackage.Literals.SERVICE;
        if (feature != null && (!(feature instanceof EReference)
                || !serviceEClass.isSuperTypeOf(((EReference) feature).getEReferenceType()))) {
            throw new IllegalArgumentException("The field " + serviceName + " exists in the model "
                    + EMFUtil.getModelName(model) + " and is not a service");
        }
        return feature == null ? null : (EReference) feature;
    }

    public Stream<ETypedElement> getResourcesForService(EClass svcClass) {
        return Stream.concat(
                Stream.concat(
                        svcClass.getEAllAttributes().stream()
                                .filter(o -> o.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE),
                        svcClass.getEAllOperations().stream()
                                .filter(o -> o.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE)
                                .filter(Predicate
                                        .not(ProviderPackage.Literals.SERVICE___EIS_SET__ESTRUCTURALFEATURE::equals))),
                svcClass.getEAllReferences().stream().filter(EReference::isContainment).filter(ref -> {
                    return ref != ProviderPackage.Literals.SERVICE__METADATA;
                }).filter(o -> o.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE));
    }

    public EOperation createActionResource(EClass serviceEClass, String name, Class<?> type,
            List<Entry<String, Class<?>>> namedParameterTypes, Map<String, Object> defaultMetadata) {

        assertResourceNotExist(serviceEClass, name);

        List<EParameter> params = namedParameterTypes.stream()
                .map(e -> EMFUtil.createActionParameter(e, serviceEClass.getEPackage())).collect(Collectors.toList());

        EMap<String, MetadataValue> defaultFeatureMetadata = defaultMetadata == null ? new BasicEMap<>()
                : toDefaultMetadataValue(defaultMetadata);

        EOperation action = EMFUtil.createAction(serviceEClass, name, type, params);
        EMFUtil.fillMetadata(EMFUtil.getModelMetadata(action), null, false, name, defaultFeatureMetadata);

        return action;
    }

    /**
     * Uses the white board to call an action handler
     *
     * @param provider   Provider instance
     * @param service    Service reference
     * @param resource   Resource instance
     * @param parameters Call parameters
     * @return The promise of the result of the action
     */
    public Promise<Object> act(Provider provider, EStructuralFeature service, ETypedElement resource,
            Map<String, Object> parameters) {
        return act(provider, service.getName(), resource, parameters);
    }

    /**
     * Uses the white board to call an action handler
     *
     * @param provider   Provider instance
     * @param service    Service name
     * @param resource   Resource instance
     * @param parameters Call parameters
     * @return The promise of the result of the action
     */
    public Promise<Object> act(Provider provider, String service, ETypedElement resource,
            Map<String, Object> parameters) {
        if (whiteboard == null) {
            return Promises.failed(new IllegalAccessError("Trying to act on a value without an action handler"));
        }

        try {
            return whiteboard.act(provider.eClass().getEPackage().getNsURI(), EMFUtil.getModelName(provider.eClass()),
                    provider.getId(), service, resource.getName(), parameters);
        } catch (Throwable t) {
            return Promises.failed(t);
        }
    }

    /**
     * Uses the white board to pull the value from a resource external getter and
     * updates the twin on success.
     *
     * @param <T>         Resource data type
     * @param provider    Dynamic Provider instance
     * @param serviceName the service identifier in the services map
     * @param resource    Resource instance
     * @param valueType   Resource data class
     * @param cachedValue Current twin value
     * @return The promise of the new value
     */
    public <T> Promise<TimedValue<T>> pullValue(Provider provider, String serviceName, ETypedElement resource,
            Class<T> valueType, TimedValue<T> cachedValue) {
        if (whiteboard == null) {
            return Promises.failed(new IllegalAccessError("Trying to pull a value without a pull handler"));
        }

        try {
            final String modelName = EMFUtil.getModelName(provider.eClass());
            return whiteboard.pullValue(provider.eClass().getEPackage().getNsURI(), modelName, provider.getId(),
                    serviceName, resource.getName(), valueType, cachedValue, (tv) -> {
                        if (tv != null) {
                            handleDataUpdate(provider, serviceName, (EClass) resource.eContainer(),
                                    (EStructuralFeature) resource, tv.getValue(), tv.getTimestamp());
                        }
                    });
        } catch (Throwable t) {
            return Promises.failed(t);
        }
    }

    /**
     * Uses the white board to pull the value from a resource external getter and
     * updates the twin on success.
     *
     * @param <T>         Resource data type
     * @param provider    Provider instance
     * @param service     Service instance
     * @param resource    Resource instance
     * @param valueType   Resource data class
     * @param cachedValue Current twin value
     * @return The promise of the new value
     */
    public <T> Promise<TimedValue<T>> pullValue(Provider provider, EReference service, ETypedElement resource,
            Class<T> valueType, TimedValue<T> cachedValue) {
        if (whiteboard == null) {
            return Promises.failed(new IllegalAccessError("Trying to pull a value without a pull handler"));
        }

        try {
            final String modelName = EMFUtil.getModelName(provider.eClass());
            return whiteboard.pullValue(provider.eClass().getEPackage().getNsURI(), modelName, provider.getId(),
                    service.getName(), resource.getName(), valueType, cachedValue, (tv) -> {
                        if (tv != null) {
                            handleDataUpdate(provider, service.getName(), service, (EClass) service.getEType(),
                                    (EStructuralFeature) resource, tv.getValue(), tv.getTimestamp());
                        }
                    });
        } catch (Throwable t) {
            return Promises.failed(t);
        }
    }

    /**
     * Uses the white board to push the value to a resource external setter and
     * updates the twin on success.
     *
     * @param <T>         Resource data type
     * @param provider    Dynamic Provider instance
     * @param serviceName the service identifier in the services map
     * @param resource    Resource instance
     * @param valueType   Resource data class
     * @param cachedValue Current twin value
     * @param newValue    New value to be pushed
     * @return The promise of a new resource value (can differ from
     *         <code>newValue</code>)
     */
    public <T> Promise<TimedValue<T>> pushValue(Provider provider, String serviceName, ETypedElement resource,
            Class<T> valueType, TimedValue<T> cachedValue, TimedValue<T> newValue) {
        if (whiteboard == null) {
            return Promises.failed(new IllegalAccessError("Trying to push a value without a push handler"));
        }

        try {
            final String modelName = EMFUtil.getModelName(provider.eClass());
            return whiteboard.pushValue(provider.eClass().getEPackage().getNsURI(), modelName, provider.getId(),
                    serviceName, resource.getName(), valueType, cachedValue, newValue, (tv) -> {
                        if (tv != null) {
                            handleDataUpdate(provider, serviceName, (EClass) resource.eContainer(),
                                    (EStructuralFeature) resource, (Object) tv.getValue(), tv.getTimestamp());
                        }
                    });
        } catch (Throwable t) {
            return Promises.failed(t);
        }
    }

    /**
     * Uses the white board to push the value to a resource external setter and
     * updates the twin on success.
     *
     * @param <T>         Resource data type
     * @param provider    Provider instance
     * @param service     Service instance
     * @param resource    Resource instance
     * @param valueType   Resource data class
     * @param cachedValue Current twin value
     * @param newValue    New value to be pushed
     * @return The promise of a new resource value (can differ from
     *         <code>newValue</code>)
     */
    public <T> Promise<TimedValue<T>> pushValue(Provider provider, EReference service, ETypedElement resource,
            Class<T> valueType, TimedValue<T> cachedValue, TimedValue<T> newValue) {
        if (whiteboard == null) {
            return Promises.failed(new IllegalAccessError("Trying to push a value without a push handler"));
        }

        try {
            final String modelName = EMFUtil.getModelName(provider.eClass());
            return whiteboard.pushValue(provider.eClass().getEPackage().getNsURI(), modelName, provider.getId(),
                    service.getName(), resource.getName(), valueType, cachedValue, newValue, (tv) -> {
                        if (tv != null) {
                            handleDataUpdate(provider, service.getName(), service, (EClass) service.getEType(),
                                    (EStructuralFeature) resource, (Object) tv.getValue(), tv.getTimestamp());
                        }
                    });
        } catch (Throwable t) {
            return Promises.failed(t);
        }
    }

    public void deleteProvider(String packageUri, String model, String name) {
        String m = getProviderModel(name);
        String p = getProviderPackageUri(name);
        if (m != null) {
            if (m.equals(model) && p.equals(packageUri)) {
                doDeleteProvider(p, model, name);
            } else {
                LOG.warn(
                        "Unable to remove the provider {} with model {} of package as the actual model was {} of package {}",
                        name, model, packageUri, m, p);
            }
        } else {
            LOG.info("The provider {} does not exist and cannot be removed", name);
        }
    }

    private void doDeleteProvider(String modelPackageUri, String model, String name) {
        Set<String> parents = childToParents.get(name);
        if (parents != null) {
            Instant now = Instant.now();
            for (String parent : parents) {
                unlinkProviders(parent, name, now);
            }
            childToParents.remove(name);
        }
        Provider p = providers.remove(name);
        List<Provider> linked = Optional.<List<Provider>>ofNullable(p.getLinkedProviders()).orElse(List.of());

        for (Provider prov : linked) {
            String id = prov.getId();
            childToParents.getOrDefault(id, Set.of()).remove(name);
        }

        notificationAccumulator.get().removeProvider(modelPackageUri, model, name);
    }

    public Provider save(Provider provider) {

        String id = validateAndGetName(provider);

        Provider original = providers.get(id);

        if (original == null) {
            original = doCreateProvider(provider.eClass(), id, Instant.now(), provider.getAdmin() == null);
        } else if (provider instanceof DynamicProvider && !(original instanceof DynamicProvider)) {
            // the incoming Provider my now be a dynamic provider, so we have to elevate and
            // replace the original
            DynamicProvider dynamicProvider = (DynamicProvider) EcoreUtil.create(provider.eClass());
            original.eClass().getEAllStructuralFeatures().forEach(e -> dynamicProvider.eSet(e, provider.eGet(e)));
            original = dynamicProvider;
            providers.put(id, dynamicProvider);
        }

        EMFCompareUtil.compareAndSet(provider, original, notificationAccumulator.get());

        return EcoreUtil.copy(original);
    }

//    private void printProvider(Provider provider) {
//
//        Resource resource = resourceSet.createResource(URI.createURI("file://temp.xmi"));
//        resource.getContents().add(EcoreUtil.copy(provider));
//        try {
//            resource.save(System.out, null);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } finally {
//            resourceSet.getResources().remove(resource);
//        }
//    }

    private String validateAndGetName(Provider provider) {
        String id = EMFUtil.getProviderName(provider);
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(String.format("Missing name/id for provider %s", provider.toString()));
        }
        if (provider instanceof DynamicProvider) {
            EClass providerEClass = provider.eClass();
            List<String> duplicates = ((DynamicProvider) provider).getServices().keySet().stream()
                    .map(providerEClass::getEStructuralFeature).filter(Objects::nonNull)
                    .filter(EReference.class::isInstance).map(EReference.class::cast)
                    .filter(ref -> ProviderPackage.Literals.SERVICE.isSuperTypeOf(ref.getEReferenceType()))
                    .map(ENamedElement::getName).collect(Collectors.toList());

            if (!duplicates.isEmpty()) {
                StringJoiner joiner = new StringJoiner(",");
                duplicates.forEach(joiner::add);
                throw new IllegalArgumentException(String.format(
                        "Provider %s has services in the service map with the same as a defined service reference: %s",
                        id, joiner.toString()));
            }
        }

        return id;
    }

    public Provider getProvider(EClass model, String id) {
        Provider provider = providers.get(id);
        if (provider.eClass() != model) {
            throw new IllegalArgumentException("Provider " + id + " does not have the same model expected "
                    + EMFUtil.getModelName(model) + ", but provider is " + EMFUtil.getModelName(provider.eClass()));
        }
        return provider;
    }

    public EClass registerModel(EClass modelEClass, Instant timestamp, boolean ignoreExisting) {
        if (!ignoreExisting && registered(modelEClass)) {
            throw new IllegalArgumentException(
                    "There is an existing model with name " + EMFUtil.getModelName(modelEClass));
        }
        EPackage ePackage = modelEClass.getEPackage();
        resourceSet.getPackageRegistry().putIfAbsent(ePackage.getNsURI(), ePackage);
        return modelEClass;
    }

    /**
     * @param ePackage a registered {@link EPackage} to scan for registered
     *                 Provideres
     */
    public void addEPackage(EPackage ePackage) {
        if (ePackage != providerPackage) {
            getProviderEClassesFromEPackage(ePackage).filter(Predicate.not(this::registered))
                    .forEach(ec -> registerModel(ec, Instant.now(), false));
        }
    }

    private Stream<EClass> getProviderEClassesFromEPackage(EPackage ePackage) {
        return ePackage.getEClassifiers().stream().filter(EClass.class::isInstance).map(EClass.class::cast)
                .filter(ProviderPackage.Literals.PROVIDER::isSuperTypeOf);
    }

    private Stream<Provider> getProviderofEPackage(EPackage ePackage) {
        return providers.values().stream().filter(p -> p.eClass().getEPackage().equals(ePackage));
    }

    /**
     * TODO how do we handle existing Instances of such a model?
     *
     * @param ePackage a registered {@link EPackage} to remove
     */
    public void removeEPackage(EPackage ePackage) {
        if (ePackage != providerPackage) {
            getProviderofEPackage(ePackage).collect(Collectors.toSet())
                    .forEach(p -> doDeleteProvider(ePackage.getNsURI(), EMFUtil.getModelName(p.eClass()), p.getId()));
            removeEPackageInternal(ePackage);
        }
    }

    private void removeEPackageInternal(EPackage ePackage) {
        if (ePackage != providerPackage) {
            resourceSet.getPackageRegistry().remove(ePackage.getNsURI());
        }
    }

    public void deleteModel(String packageUri, String model) {
        getModel(packageUri, model).ifPresent(e -> {
            // Clear any providers using the model
            getProviders(e).forEach(p -> doDeleteProvider(packageUri, model, p.getId()));
            EPackage ePackage = e.getEPackage();
            ePackage.getEClassifiers().remove(e);
            if (ePackage.getEClassifiers().isEmpty()) {
                removeEPackageInternal(ePackage);
            }
        });
    }
}
