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
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
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
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.core.model.nexus.emf.NamingUtils;
import org.eclipse.sensinact.core.model.nexus.emf.compare.EMFCompareUtil;
import org.eclipse.sensinact.core.notification.NotificationAccumulator;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.core.whiteboard.impl.SensinactWhiteboard;
import org.eclipse.sensinact.model.core.metadata.Action;
import org.eclipse.sensinact.model.core.metadata.ActionParameter;
import org.eclipse.sensinact.model.core.metadata.AnnotationMetadata;
import org.eclipse.sensinact.model.core.metadata.MetadataFactory;
import org.eclipse.sensinact.model.core.metadata.ResourceAttribute;
import org.eclipse.sensinact.model.core.metadata.ResourceMetadata;
import org.eclipse.sensinact.model.core.metadata.ServiceReference;
import org.eclipse.sensinact.model.core.provider.Admin;
import org.eclipse.sensinact.model.core.provider.FeatureCustomMetadata;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.model.core.provider.ProviderFactory;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
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
        loadEPackages(Paths.get(BASIC_EPACKAGES));

        if (!resourceSet.getPackageRegistry().containsKey(EMFUtil.DEFAULT_SENSINACT_PACKAGE_URI)) {
            EMFUtil.createPackage("base", EMFUtil.DEFAULT_SENSINACT_PACKAGE_URI, "sensinactBase", this.resourceSet);
        }

        loadInstances(Paths.get(BASIC_PROVIDERS));
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
                    .orElseGet(() -> createService(sensiNactModel, "system", now));
            EClass svcClass = svc.getEReferenceType();
            EStructuralFeature versionResource = Optional.ofNullable(svcClass.getEStructuralFeature("version"))
                    .orElseGet(() -> createResource(svcClass, "version", double.class, now, null));
            EStructuralFeature startedResource = Optional.ofNullable(svcClass.getEStructuralFeature("started"))
                    .orElseGet(() -> createResource(svcClass, "started", Instant.class, now, null));

            Provider provider = Optional.ofNullable(getProvider("sensiNact"))
                    .orElseGet(() -> doCreateProvider(sensiNactModel, "sensiNact", now));

            handleDataUpdate(provider, svc, versionResource, 0.1D, now);
            handleDataUpdate(provider, svc, startedResource, now, now);
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
     * child do not exist, they will be created.
     *
     * @param parentModel    name of the type of providers for the parent. Can be
     *                       <code>null</code>
     * @param parentProvider The provider name of the parent. The name will be used
     *                       as ID and in the first setup as the friendlyname for
     *                       the Admin Service
     * @param childModel     name of the type of providers for the parent. Can be
     *                       <code>null</code>
     * @param childProvider  The provider name of the child. The name will be used
     *                       as ID and in the first setup as the friendlyname for
     *                       the Admin service
     * @param timestamp      the timestamp when the link is created. If null, the
     *                       current timestamp is used.
     */
    public void linkProviders(String parentProvider, String childProvider, Instant timestamp) {

        Instant metaTimestamp = timestamp == null ? Instant.now() : timestamp;

        NotificationAccumulator accumulator = notificationAccumulator.get();

        Provider parent = providers.get(parentProvider);

        Provider child = providers.get(childProvider);

        if (parent == null) {
            throw new IllegalArgumentException("No parent provider " + parentProvider);
        }
        if (child == null) {
            throw new IllegalArgumentException("No child provider " + childProvider);
        }
        ((Provider) parent).getLinkedProviders().add((Provider) child);

        // TODO link event
        // accumulator.link(...)
    }

    /**
     * Will disassociate the given Parent provider with the given child.
     *
     * @param parentModel    name of the type of providers for the parent. Can be
     *                       <code>null</code>.
     * @param parentProvider The provider name of the parent.
     * @param childModel     name of the type of providers for the parent. Can be
     *                       <code>null</code>.
     * @param childProvider  The provider name of the child.
     * @param timestamp      the timestamp when the link is created. If null, the
     *                       current timestamp is used.
     */
    public void unlinkProviders(String parentProvider, String childProvider, Instant timestamp) {

        Instant metaTimestamp = timestamp == null ? Instant.now() : timestamp;

        Provider parent = providers.get(parentProvider);

        Provider child = providers.get(childProvider);

        if (parent == null) {
            throw new IllegalArgumentException("No parent provider " + parentProvider);
        }
        if (child == null) {
            throw new IllegalArgumentException("No child provider " + childProvider);
        }
        ((Provider) parent).getLinkedProviders().remove(child);

        // TODO unlink event
        // accumulator.unlink(...)
    }

    public void handleDataUpdate(Provider provider, EStructuralFeature serviceFeature,
            EStructuralFeature resourceFeature, Object data, Instant timestamp) {

        Instant metaTimestamp = timestamp == null ? Instant.now() : timestamp;
        String providerName = provider.getId();
        String modelName = EMFUtil.getModelName(provider.eClass());
        NotificationAccumulator accumulator = notificationAccumulator.get();

        String packageUri = provider.eClass().getEPackage().getNsURI();

        Service service = (Service) provider.eGet(serviceFeature);
        if (service == null) {
            service = (Service) EcoreUtil.create((EClass) serviceFeature.getEType());
            provider.eSet(serviceFeature, service);
            accumulator.addService(packageUri, modelName, providerName, serviceFeature.getName());
        }

        ResourceMetadata metadata = (ResourceMetadata) service.getMetadata().get(resourceFeature);

        Map<String, Object> oldMetaData = null;
        Object oldValue = service.eGet(resourceFeature);
        if (metadata != null) {
            oldMetaData = EMFCompareUtil.extractMetadataMap(oldValue, metadata, resourceFeature);
        }
        if (oldValue == null) {
            accumulator.addResource(packageUri, modelName, providerName, serviceFeature.getName(),
                    resourceFeature.getName());
        }

        // Allow an update if the resource didn't exist or if the update timestamp is
        // equal to or after the one of the current value
        if (metadata == null || !metadata.getTimestamp().isAfter(timestamp.plusMillis(1))) {
            EClassifier resourceType = resourceFeature.getEType();

            if (metadata == null) {
                metadata = MetadataFactory.eINSTANCE.createResourceMetadata();
                service.getMetadata().put(resourceFeature, metadata);
            }
            metadata.setTimestamp(metaTimestamp);

            if (data == null || resourceType.isInstance(data)) {
                service.eSet(resourceFeature, data);
            } else {
                service.eSet(resourceFeature, EMFUtil.convertToTargetType(resourceType, data));
            }
            accumulator.resourceValueUpdate(packageUri, modelName, providerName, serviceFeature.getName(),
                    resourceFeature.getName(), resourceType.getInstanceClass(), oldValue, data, timestamp);
        } else {
            return;
        }

//        if (metadata == null) {
//            metadata = MetadataFactory.eINSTANCE.createResourceMetadata();
//            service.getMetadata().put(resourceFeature, metadata);
//        }
        metadata.setTimestamp(timestamp);

        Map<String, Object> newMetaData = EMFCompareUtil.extractMetadataMap(data, metadata, resourceFeature);

        accumulator.metadataValueUpdate(packageUri, modelName, providerName, serviceFeature.getName(),
                resourceFeature.getName(), oldMetaData, newMetaData, timestamp);
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
                    || resourceFeature == ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI
                    || resourceFeature == ProviderPackage.Literals.ADMIN__MODEL
                    ) {
                ResourceMetadata metadata = MetadataFactory.eINSTANCE.createResourceMetadata();
                metadata.setOriginalName(resourceFeature.getName());
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
        EClass m = getMandatoryModel(modelPackageUri, model);
        // Don't use isInstance as subtypes have a different model
        return providers.values().stream().filter(p -> EMFUtil.getModelName(p.eClass()).equals(m))
                .collect(Collectors.toList());
    }

    public EAttribute createResource(EClass service, String resource, Class<?> type, Instant timestamp,
            Object defaultValue) {
        return createResource(service, resource, type, timestamp, defaultValue, false, 0, false);
    }

    public EAttribute createResource(EClass service, String resource, Class<?> type, Instant timestamp,
            Object defaultValue, boolean hasGetter, long getterCacheMs, boolean hasSetter) {
        // FIXME: WIP
        FeatureCustomMetadata resourceType = ProviderFactory.eINSTANCE.createFeatureCustomMetadata();
        resourceType.setName("resourceType");
        resourceType.setValue(ResourceType.SENSOR);
        resourceType.setTimestamp(Instant.EPOCH);

        return doCreateResource(service, resource, type, timestamp, defaultValue, List.of(resourceType), hasGetter,
                getterCacheMs, hasSetter);
    }

    private EAttribute doCreateResource(EClass service, String resource, Class<?> type, Instant timestamp,
            Object defaultValue, List<FeatureCustomMetadata> metadata, boolean hasGetter, long getterCacheMs,
            boolean hasSetter) {
        assertResourceNotExist(service, resource);
        ResourceAttribute feature = EMFUtil.createResourceAttribute(service, resource, type, defaultValue);
        feature.setExternalGet(hasGetter);
        feature.setExternalSet(hasSetter);
        if (getterCacheMs > 0) {
            feature.setExternalGetCacheMs(getterCacheMs);
        }
        EMFUtil.fillMetadata(feature, timestamp, false, resource, List.of());
        return feature;
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
        EClass model = EMFUtil.createEClass(modelClassName, ePackage,
                (ec) -> createEClassAnnotations(modelName, timestamp), ProviderPackage.Literals.PROVIDER);
        return model;
    }

    private EReference doCreateService(EClass model, String name, Instant timestamp) {
        EPackage ePackage = model.getEPackage();
        EClass service = EMFUtil.createEClass(NamingUtils.sanitizeName(name, false), ePackage,
                (ec) -> createEClassAnnotations(timestamp), ProviderPackage.Literals.SERVICE);
        ServiceReference ref = EMFUtil.createServiceReference(model, name, service, true);
        EMFUtil.fillMetadata(ref, timestamp, false, name, List.of());
        return ref;
    }

    private List<EAnnotation> createEClassAnnotations(Instant timestamp) {
        AnnotationMetadata meta = MetadataFactory.eINSTANCE.createAnnotationMetadata();
        meta.setTimestamp(timestamp);
        EAnnotation annotation = EMFUtil.createEAnnotation("metadata", Collections.singletonList(meta));
        return Collections.singletonList(annotation);
    }

    private List<EAnnotation> createEClassAnnotations(String model, Instant timestamp) {
        // TODO make this part of the ModelMetadata?
        return List.of(createEClassAnnotations(timestamp).get(0),
                EMFUtil.createEAnnotation("model", Map.of("name", model)));
    }


    public Map<String, Object> getResourceMetadata(Provider provider, EStructuralFeature svcFeature,
            final ETypedElement rcFeature) {
        final Service svc = (Service) provider.eGet(svcFeature);
        if (svc == null) {
            return Map.of();
        }

        final ResourceMetadata metadata = (ResourceMetadata) svc.getMetadata().get(rcFeature);
        if (metadata == null) {
            return Map.of();
        } else {
            return toMetadataMap(rcFeature, metadata);
        }
    }

    private Map<String, Object> toMetadataMap(final ETypedElement rcFeature, final ResourceMetadata metadata) {
        final Map<String, Object> rcMeta = new HashMap<>();
        rcMeta.putAll(EMFUtil.toMetadataAttributesToMap(metadata, rcFeature));
        return rcMeta;
    }

    public void setResourceMetadata(Provider provider, EStructuralFeature svcFeature, ETypedElement resource,
            String metadataKey, Object value, Instant timestamp) {
        if (metadataKey == null || metadataKey.isEmpty()) {
            throw new IllegalArgumentException("Empty metadata key");
        }

        if (timestamp == null) {
            throw new IllegalArgumentException("Invalid timestamp");
        }
        final Service svc = (Service) provider.eGet(svcFeature);

        ResourceMetadata metadata = svc == null ? null : (ResourceMetadata) svc.getMetadata().get(resource);

        if (metadata == null) {
            throw new IllegalStateException("No existing metadata for resource");
        }

        Map<String, Object> oldMetadata = toMetadataMap(resource, metadata);

        metadata.setTimestamp(timestamp);
        metadata.getExtra().stream().filter(fcm -> fcm.getName().equals(metadataKey)).findFirst()
                .ifPresentOrElse(fcm -> handleFeatureCustomMetadata(fcm, metadataKey, timestamp, value),
                        () -> metadata.getExtra()
                                .add(handleFeatureCustomMetadata(
                                        ProviderFactory.eINSTANCE.createFeatureCustomMetadata(), metadataKey, timestamp,
                                        value)));

        Map<String, Object> newMetadata = toMetadataMap(resource, metadata);

        notificationAccumulator.get().metadataValueUpdate(provider.eClass().getEPackage().getNsURI(),
                EMFUtil.getModelName(provider.eClass()), provider.getId(), svcFeature.getName(), resource.getName(),
                oldMetadata, newMetadata, timestamp);

    }

    private FeatureCustomMetadata handleFeatureCustomMetadata(FeatureCustomMetadata customMetadata, String metadataKey,
            Instant timestamp, Object value) {
        customMetadata.setName(metadataKey);
        customMetadata.setTimestamp(timestamp);
        customMetadata.setValue(value);
        return customMetadata;
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
        return resourceSet.getPackageRegistry().getEPackage(ePackage.getNsURI()) != null;
    }

    private EClass getMandatoryModel(String modelPackageUri, String modelName) {
        return getModel(modelPackageUri, modelName)
                .orElseThrow(() -> new IllegalArgumentException("No model with name " + modelName));
    }

    public EReference createService(EClass model, String service, Instant creationTimestamp) {
        if (model.getEStructuralFeature(service) != null) {
            throw new IllegalArgumentException(
                    "There is an existing service with name " + service + " in model " + model);
        }
        return doCreateService(model, service, creationTimestamp);
    }

    public Stream<EReference> getServicesForModel(EClass model) {
        EClass svcClass = ProviderPackage.Literals.SERVICE;
        return model.getEAllReferences().stream().filter(r -> svcClass.isSuperTypeOf(r.getEReferenceType()));
    }

    private EReference getServiceForModel(EClass model, String serviceName) {
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
                svcClass.getEAllAttributes().stream()
                        .filter(o -> o.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE),
                svcClass.getEAllOperations().stream()
                        .filter(o -> o.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE)
                        .filter(Predicate.not(ProviderPackage.Literals.SERVICE___EIS_SET__ESTRUCTURALFEATURE::equals)));
    }

    public EOperation createActionResource(EClass serviceEClass, String name, Class<?> type,
            List<Entry<String, Class<?>>> namedParameterTypes) {

        assertResourceNotExist(serviceEClass, name);

        List<ActionParameter> params = namedParameterTypes.stream().map(EMFUtil::createActionParameter)
                .collect(Collectors.toList());

        Action action = EMFUtil.createAction(serviceEClass, name, type, params);

        return action;
    }

    /**
     * Uses the white board to call an action handler
     *
     * @param provider   Provider instance
     * @param service    Service instance
     * @param resource   Resource instance
     * @param parameters Call parameters
     * @return The promise of the result of the action
     */
    public Promise<Object> act(Provider provider, EStructuralFeature service, ETypedElement resource,
            Map<String, Object> parameters) {
        if (whiteboard == null) {
            return Promises.failed(new IllegalAccessError("Trying to act on a value without an action handler"));
        }

        try {
            return whiteboard.act(provider.eClass().getEPackage().getNsURI(), EMFUtil.getModelName(provider.eClass()),
                    provider.getId(), service.getName(), resource.getName(), parameters);
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
    public <T> Promise<TimedValue<T>> pullValue(Provider provider, EStructuralFeature service, ETypedElement resource,
            Class<T> valueType, TimedValue<T> cachedValue) {
        if (whiteboard == null) {
            return Promises.failed(new IllegalAccessError("Trying to pull a value without a pull handler"));
        }

        try {
            final String modelName = EMFUtil.getModelName(provider.eClass());
            return whiteboard.pullValue(provider.eClass().getEPackage().getNsURI(), modelName, provider.getId(),
                    service.getName(), resource.getName(), valueType, cachedValue, (tv) -> {
                        if (tv != null) {
                            handleDataUpdate(provider, service, (EStructuralFeature) resource, tv.getValue(),
                                    tv.getTimestamp());
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
    public <T> Promise<TimedValue<T>> pushValue(Provider provider, EStructuralFeature service, ETypedElement resource,
            Class<T> valueType, TimedValue<T> cachedValue, TimedValue<T> newValue) {
        if (whiteboard == null) {
            return Promises.failed(new IllegalAccessError("Trying to push a value without a push handler"));
        }

        try {
            final String modelName = EMFUtil.getModelName(provider.eClass());
            return whiteboard.pushValue(provider.eClass().getEPackage().getNsURI(), modelName, provider.getId(),
                    service.getName(), resource.getName(), valueType, cachedValue, newValue, (tv) -> {
                        if (tv != null) {
                            handleDataUpdate(provider, service, (EStructuralFeature) resource, tv.getValue(),
                                    tv.getTimestamp());
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
        providers.remove(name);
        notificationAccumulator.get().removeProvider(modelPackageUri, model, name);
    }

    public Provider save(Provider eObject) {
        String id = EMFUtil.getProviderName(eObject);

        Provider original = providers.get(id);

        if (original == null) {
            original = doCreateProvider(eObject.eClass(), id, Instant.now(), eObject.getAdmin() == null);
        }

        EMFCompareUtil.compareAndSet(eObject, original, notificationAccumulator.get());

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
            getProviderEClassesFromEPackage(ePackage).forEach(ec -> registerModel(ec, Instant.now(), false));
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
        }
    }
}
