/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.prototype.model.nexus.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sensinact.model.core.Admin;
import org.eclipse.sensinact.model.core.FeatureCustomMetadata;
import org.eclipse.sensinact.model.core.Metadata;
import org.eclipse.sensinact.model.core.ModelMetadata;
import org.eclipse.sensinact.model.core.Provider;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.model.core.Service;
import org.eclipse.sensinact.prototype.model.nexus.impl.ModelTransaction.ModelTransactionState;
import org.eclipse.sensinact.prototype.model.nexus.impl.emf.EMFUtil;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
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
    private static final String INSTANCES = BASE + "instances/";
    private static final String BASIC_BASE_ECORE = BASE + "models/basic/base.ecore";
    /** HTTPS_ECLIPSE_ORG_SENSINACT_BASE */
    private static final String DEFAULT_URI = "https://eclipse.org/sensinact/base";
    private static final URI DEFAULT_URI_OBJECT = URI.createURI(DEFAULT_URI);

    private final ResourceSet resourceSet;
    private final SensiNactPackage sensinactPackage;
    private final Supplier<NotificationAccumulator> notificationAccumulator;

    private Map<URI, ProviderTypeWrapper> providerCache = new ConcurrentHashMap<>();
    private Map<URI, EPackage> packageCache = new ConcurrentHashMap<>();
    private EPackage defaultPackage;

    public ModelNexus(ResourceSet resourceSet, SensiNactPackage sensinactPackage,
            Supplier<NotificationAccumulator> accumulator) {
        this.resourceSet = resourceSet;
        this.sensinactPackage = sensinactPackage;
        this.notificationAccumulator = accumulator;
        // TODO we need a general Working Directory for such data
        Optional<EPackage> packageOptional = loadDefaultPackage(Paths.get(BASIC_BASE_ECORE));

        defaultPackage = packageOptional
                .orElseGet(() -> EMFUtil.createPackage("base", DEFAULT_URI, "sensinactBase", this.resourceSet));

        defaultPackage.setEFactoryInstance(new EFactoryImpl() {
            @Override
            protected EObject basicCreate(EClass eClass) {
                return eClass.getInstanceClassName() == "java.util.Map$Entry"
                        ? new MinimalEObjectImpl.Container.Dynamic.BasicEMapEntry<String, String>(eClass)
                        : new MinimalEObjectImpl.Container.Dynamic.Permissive(eClass);
            }
        });

        packageCache.put(DEFAULT_URI_OBJECT, defaultPackage);
        loadInstances();
    }

    private Optional<EPackage> loadDefaultPackage(Path fileName) {
        Resource resource = resourceSet.createResource(URI.createFileURI(fileName.toString()));
        if (Files.isRegularFile(fileName)) {
            try {
                resource.load(null);
                if (!resource.getContents().isEmpty()) {
                    EPackage defaultPackage = (EPackage) resource.getContents().get(0);
                    resource.setURI(URI.createURI(defaultPackage.getNsURI()));
                    return Optional.of(defaultPackage);
                }
            } catch (IOException e) {
                LOG.error(
                        "THIS WILL BE A RUNTIME EXCPETION FOR NOW: Error Loading default EPackage from persistent file: {}",
                        fileName, e);
                throw new RuntimeException(e);
            }
        }
        return Optional.empty();
    }

    private void loadInstances() {

        Path instancesPath = Paths.get(INSTANCES);
        if (Files.isDirectory(instancesPath)) {
            try {
                Files.walk(instancesPath).forEach(this::load);
            } catch (IOException e) {
                LOG.error("THIS WILL BE A RUNTIME EXCPETION FOR NOW: Error loading instances from Path: {}",
                        instancesPath, e);
                throw new RuntimeException(e);
            }
        }
    }

    private void load(Path path) {
        try {
            if (Files.isDirectory(path)) {
                return;
            } else {
                URI uri = URI.createFileURI(path.toString());
                Resource resource = resourceSet.createResource(uri);
                resource.load(null);
                if (!resource.getContents().isEmpty()) {
                    Provider provider = (Provider) resource.getContents().get(0);
                    EClass eClass = provider.eClass();
                    URI providerUri = EcoreUtil.getURI(eClass);
                    ProviderTypeWrapper wrapper = new ProviderTypeWrapper(eClass);
                    ProviderTypeWrapper temp = providerCache.putIfAbsent(providerUri, wrapper);
                    wrapper = temp == null ? wrapper : temp;
                    uri = createURI(provider);
                    resource.setURI(providerUri.trimFragment());
                    wrapper.getInstances().put(uri, provider);

                }
            }
        } catch (IOException e) {
            LOG.error("THIS WILL BE A RUNTIME EXCPETION FOR NOW: Error loading provider from Path: {}", path, e);
            throw new RuntimeException(e);
        }

    }

    public void shutDown() {
        defaultPackage.eResource().setURI(URI.createFileURI(BASIC_BASE_ECORE));
        try {
            defaultPackage.eResource().save(null);
            providerCache.values().forEach(this::saveInstance);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
    public void linkProviders(String parentModel, String parentProvider, String childModel, String childProvider,
            Instant timestamp) {

        if (parentModel == null) {
            parentModel = parentProvider;
        }
        if (childModel == null) {
            childModel = childProvider;
        }

        Instant metaTimestamp = timestamp == null ? Instant.now() : timestamp;

        NotificationAccumulator accumulator = notificationAccumulator.get();

        ProviderTypeWrapper parentWrapper = getOrCreateProviderType(parentModel, DEFAULT_URI, metaTimestamp,
                accumulator);
        Provider parent = getProvider(parentWrapper, parentModel, parentProvider, timestamp, accumulator, true);

        ProviderTypeWrapper childWrapper = getOrCreateProviderType(childModel, DEFAULT_URI, metaTimestamp, accumulator);
        Provider child = getProvider(childWrapper, childModel, childProvider, timestamp, accumulator, true);

        parent.getLinkedProviders().add(child);
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
    public void unlinkProviders(String parentModel, String parentProvider, String childModel, String childProvider,
            Instant timestamp) {

        if (parentModel == null) {
            parentModel = parentProvider;
        }
        if (childModel == null) {
            childModel = childProvider;
        }

        Instant metaTimestamp = timestamp == null ? Instant.now() : timestamp;

        NotificationAccumulator accumulator = notificationAccumulator.get();

        ProviderTypeWrapper parentWrapper = getProviderType(parentModel, DEFAULT_URI, metaTimestamp, accumulator,
                false);
        Provider parent = parentWrapper == null ? null
                : getProvider(parentWrapper, parentModel, parentProvider, timestamp, accumulator, false);

        if (parent == null) {
            throw new IllegalArgumentException("parent Provider " + parentProvider + " does not exist");
        }

        ProviderTypeWrapper childWrapper = getProviderType(childModel, DEFAULT_URI, metaTimestamp, accumulator, false);
        Provider child = childWrapper == null ? null
                : getProvider(childWrapper, childModel, childProvider, timestamp, accumulator, false);

        if (child == null) {
            throw new IllegalArgumentException("child Provider " + childProvider + " does not exist");
        }
        parent.getLinkedProviders().remove(child);
    }

    public void handleDataUpdate(String model, String provider, String service, String resource, Class<?> type,
            Object data, Instant timestamp) {
        if (model == null) {
            model = provider;
        }

        Instant metaTimestamp = timestamp == null ? Instant.now() : timestamp;

        NotificationAccumulator accumulator = notificationAccumulator.get();

        ProviderTypeWrapper wrapper = getOrCreateProviderType(model, DEFAULT_URI, metaTimestamp, accumulator);
        ModelTransaction transaction = getOrCreateService(service, wrapper, metaTimestamp, accumulator);
        transaction = getOrCreateResource(wrapper, resource, type, transaction, metaTimestamp, accumulator);
//        updateInstances(wrapper, transaction);
        setData(wrapper, transaction, model, provider, data, metaTimestamp, accumulator);
    }

    /**
     * @param transaction
     * @param data
     */
    private void setData(ProviderTypeWrapper wrapper, ModelTransaction transaction, String modelName,
            String providerName, Object data, Instant timestamp, NotificationAccumulator accumulator) {
        Provider provider = getProvider(wrapper, modelName, providerName, timestamp, accumulator, true);

        EStructuralFeature serviceFeature = transaction.getFeaturePath().get(0);

        Service service = (Service) provider.eGet(serviceFeature);
        if (service == null) {
            service = (Service) EcoreUtil.create((EClass) serviceFeature.getEType());
            provider.eSet(serviceFeature, service);
            accumulator.addService(providerName, serviceFeature.getName());
        }

        EStructuralFeature resourceFeature = transaction.getFeaturePath().get(1);

        // Handle Metadata

        Metadata metadata = service.getMetadata().get(resourceFeature);

        Map<String, Object> oldMetaData = null;
        Object oldValue = service.eGet(resourceFeature);
        if (metadata != null) {
            oldMetaData = EMFUtil.toEObjectAttributesToMap(metadata);
            oldMetaData.put("value", oldValue);
        }
        if (oldValue == null) {
            accumulator.addResource(providerName, serviceFeature.getName(), resourceFeature.getName());
        }

        // Allow an update if the resource didn't exist or if the update timestamp is
        // equal to or after the one of the current value
        if (metadata == null || !metadata.getTimestamp().isAfter(timestamp)) {
            service.eSet(resourceFeature, data);
            accumulator.resourceValueUpdate(providerName, serviceFeature.getName(), resourceFeature.getName(), oldValue,
                    data, timestamp);
        } else {
            return;
        }

        if (metadata == null) {
            metadata = sensinactPackage.getSensiNactFactory().createMetadata();
            metadata.setFeature(resourceFeature);
            metadata.setSource(service);
            service.getMetadata().put(resourceFeature, metadata);
        }
        metadata.setTimestamp(timestamp);

        Map<String, Object> newMetaData = EMFUtil.toEObjectAttributesToMap(metadata);
        newMetaData.put("value", data);

        accumulator.metadataValueUpdate(providerName, serviceFeature.getName(), resourceFeature.getName(), oldMetaData,
                newMetaData, timestamp);
    }

    /**
     * @param wrapper
     * @param modelName
     * @param providerName
     * @param timestamp
     * @param accumulator
     * @return
     */
    private Provider getProvider(ProviderTypeWrapper wrapper, String modelName, String providerName, Instant timestamp,
            NotificationAccumulator accumulator, boolean create) {
        URI instanceUri = createURI(modelName, providerName);
        Provider provider = wrapper.getInstances().get(instanceUri);
        if (provider == null && create) {
            provider = (Provider) EcoreUtil.create(wrapper.getProviderType());
            provider.setId(providerName);

            final Admin adminSvc = sensinactPackage.getSensiNactFactory().createAdmin();
            provider.setAdmin(adminSvc);
            adminSvc.setFriendlyName(providerName);

            // Set a timestamp to admin resources to indicate them as valued
            for (EStructuralFeature resourceFeature : provider.getAdmin().eClass().getEStructuralFeatures()) {
                Metadata metadata = sensinactPackage.getSensiNactFactory().createMetadata();
                metadata.setFeature(resourceFeature);
                metadata.setSource(provider.getAdmin());
                metadata.setTimestamp(timestamp);
                adminSvc.getMetadata().put(resourceFeature, metadata);
            }

            wrapper.getInstances().put(instanceUri, provider);
            accumulator.addProvider(providerName);
        }
        return provider;
    }

    public Provider getProvider(String providerName) {
        return getProvider(providerName, providerName);
    }

    public Provider getProvider(String model, String providerName) {
        URI packageUri = URI.createURI(DEFAULT_URI);
        URI providerUri = packageUri.appendFragment("//" + firstToUpper(model));
        ProviderTypeWrapper wrapper = providerCache.get(providerUri);
        return wrapper == null ? null : wrapper.getInstances().get(createURI(model, providerName));
    }

    /**
     * Lists know providers
     */
    public List<Provider> getProviders() {
        return providerCache.values().stream().flatMap((wrapper) -> wrapper.instances.values().stream())
                .collect(Collectors.toList());
    }

    /**
     * @param dto
     * @return
     */
    private URI createURI(String model, String provider) {
        if (model == null) {
            model = provider;
        }
        URI uri = URI.createURI(model).appendSegment(provider).appendFragment(provider);
        return uri;
    }

    private URI createURI(Provider provider) {
        return createURI(provider.eClass().getName(), provider.getId());
    }

    /**
     * @param service
     * @param resource
     * @param type
     * @param transaction
     * @return
     */
    private ModelTransaction getOrCreateResource(ProviderTypeWrapper wrapper, String resource, Class<?> type,
            ModelTransaction transaction, Instant timestamp, NotificationAccumulator accumulator) {
        EClass service = transaction.getService();
        EAttribute feature = (EAttribute) service.getEStructuralFeature(resource);
        if (feature == null) {
            // The EClass does not have a reference to the service. This means we need to
            // create an EClass representing the Service and add a reference to our provider
            // EClass
            if (transaction.getServiceState() != ModelTransactionState.NEW) {
                transaction.setResourceState(ModelTransactionState.NEW);
            }
            updateMetadata(service);
            feature = EMFUtil.createEAttribute(service, resource, type, ef -> createEFeatureAnnotation(ef, timestamp));
            service.getEStructuralFeatures().add(feature);
        }
        transaction.addFeature(feature);
        return transaction;
    }

    private ProviderTypeWrapper getOrCreateProviderType(String rawProviderName, String thePackageUri, Instant timestamp,
            NotificationAccumulator accumulator) {
        return getProviderType(rawProviderName, thePackageUri, timestamp, accumulator, true);
    }

    private ProviderTypeWrapper getProviderType(String rawProviderName, String thePackageUri, Instant timestamp,
            NotificationAccumulator accumulator, boolean create) {
        String providerName = firstToUpper(rawProviderName);
        URI packageUri = URI.createURI(thePackageUri);
        EPackage ePackage = packageCache.get(packageUri);
        EClass providerClass = (EClass) ePackage.getEClassifier(providerName);
        if (providerClass != null) {
            return providerCache.get(EcoreUtil.getURI(providerClass));
        } else if (!create) {
            return null;
        }

        providerClass = EMFUtil.createEClass(providerName, ePackage, (ec) -> createEClassAnnotations(timestamp),
                sensinactPackage.getProvider());
        ProviderTypeWrapper wrapper = new ProviderTypeWrapper(providerClass);
        providerCache.put(EcoreUtil.getURI(providerClass), wrapper);
        return wrapper;
    }

    private ModelTransaction getOrCreateService(String serviceName, ProviderTypeWrapper wrapper, Instant timestamp,
            NotificationAccumulator accumulator) {
        ModelTransaction transaction = new ModelTransaction();
        EClass provider = wrapper.getProviderType();
        EClass service = null;
        EStructuralFeature feature = provider.getEStructuralFeature(serviceName);
        if (feature == null) {
            // The EClass does not have a reference to the service. This means we need to
            // create an EClass representing the Service and add a reference to our provider
            // EClass
            transaction.setServiceState(ModelTransactionState.NEW);
            EPackage ePackage = wrapper.getProviderType().getEPackage();
            updateMetadata(provider);
            service = EMFUtil.createEClass(constructServiceEClassName(provider.getName(), serviceName), ePackage,
                    (ec) -> createEClassAnnotations(timestamp), sensinactPackage.getService());
            feature = EMFUtil.createEReference(provider, serviceName, service, true,
                    ef -> createEFeatureAnnotation(ef, timestamp));
        } else {
            service = (EClass) feature.getEType();
        }
        transaction.setService(service);
        transaction.addFeature(feature);
        return transaction;
    }

    private void updateMetadata(EClass provider) {
        EAnnotation metadata = provider.getEAnnotation("metadata");
        ModelMetadata meta = (ModelMetadata) metadata.getContents().get(0);
        meta.setVersion(meta.getVersion() + 1);
    }

    private List<EAnnotation> createEClassAnnotations(Instant timestamp) {
        ModelMetadata meta = sensinactPackage.getSensiNactFactory().createModelMetadata();
        meta.setTimestamp(timestamp);
        meta.setVersion(1);
        EAnnotation annotation = EMFUtil.createEAnnotation("metadata", Collections.singletonList(meta));
        return Collections.singletonList(annotation);
    }

    private List<EAnnotation> createEFeatureAnnotation(EStructuralFeature feature, Instant timestamp) {
        ModelMetadata meta = sensinactPackage.getSensiNactFactory().createModelMetadata();
        meta.setTimestamp(timestamp);
        meta.setVersion(EMFUtil.getContainerVersion(feature));
        EAnnotation annotation = EMFUtil.createEAnnotation("metadata", Collections.singletonList(meta));
        return Collections.singletonList(annotation);
    }

    /**
     * We need a Unique name for the Service Class if they reside in the same
     * Package. Thus we create a hopefully unique name.
     *
     * TODO: Place each Provider in its own Subpackage?
     *
     * @param providerName
     * @param serviceName
     * @return
     */
    private String constructServiceEClassName(String providerName, String serviceName) {
        return firstToUpper(providerName) + firstToUpper(serviceName);
    }

    private String firstToUpper(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void saveInstance(ProviderTypeWrapper wrapper) {
        URI baseUri = URI.createURI(INSTANCES);
        wrapper.getInstances().forEach((u, e) -> {
            URI instanceUri = baseUri.appendSegments(u.segments()).appendFileExtension("xmi");
            Resource res = resourceSet.createResource(instanceUri);
            res.getContents().add(e);
            try {
                e.eResource().save(null);
            } catch (IOException ex) {
                LOG.error("THIS WILL BE A RUNTIME EXCPETION FOR NOW: Error saving provider fro URI: {}", instanceUri,
                        e);
                throw new RuntimeException(ex);
            }
        });
    }

    public Map<String, Object> getResourceMetadata(String modelName, String providerName, String serviceName,
            String resourceName) {
        final Provider provider = getProvider(modelName, providerName);
        if (provider == null) {
            return null;
        }

        final EStructuralFeature svcFeature = provider.eClass().getEStructuralFeature(serviceName);
        if (svcFeature == null) {
            return null;
        }

        final Service svc = (Service) provider.eGet(svcFeature);
        final EStructuralFeature rcFeature = svc.eClass().getEStructuralFeature(resourceName);
        if (rcFeature == null) {
            return Map.of();
        }

        final Metadata metadata = svc.getMetadata().get(rcFeature);
        if (metadata == null) {
            return Map.of();
        } else {
            final Map<String, Object> rcMeta = new HashMap<>();
            for (FeatureCustomMetadata entry : metadata.getExtra()) {
                rcMeta.put(entry.getName(), entry.getValue());
            }
            rcMeta.putAll(EMFUtil.toEObjectAttributesToMap(metadata));
            return rcMeta;
        }
    }

    public void setResourceMetadata(String modelName, String providerName, String serviceName, String resourceName,
            String metadataKey, Object value, Instant timestamp) {
        if (metadataKey == null || metadataKey.isEmpty()) {
            throw new IllegalArgumentException("Empty metadata key");
        }

        if (timestamp == null) {
            throw new IllegalArgumentException("Invalid timestamp");
        }

        final Provider provider = getProvider(modelName, providerName);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown provider");
        }

        final EStructuralFeature svcFeature = provider.eClass().getEStructuralFeature(serviceName);
        if (svcFeature == null) {
            throw new IllegalArgumentException("Unknown service");
        }

        final Service svc = (Service) provider.eGet(svcFeature);
        final EStructuralFeature rcFeature = svc.eClass().getEStructuralFeature(resourceName);
        if (rcFeature == null) {
            throw new IllegalArgumentException("Unknown resource");
        }

        Metadata metadata = svc.getMetadata().get(rcFeature);
        if (metadata == null) {
            throw new IllegalStateException("No existing metadata for resource");
        }

        // Remove current value, if any
        final List<FeatureCustomMetadata> extra = metadata.getExtra();
        extra.stream().filter(e -> metadataKey.equals(e.getName())).findFirst().ifPresent(extra::remove);
        ;

        // Store new metadata
        final FeatureCustomMetadata customMetadata = sensinactPackage.getSensiNactFactory()
                .createFeatureCustomMetadata();
        customMetadata.setName(metadataKey);
        customMetadata.setTimestamp(timestamp);
        customMetadata.setValue(value);
        extra.add(customMetadata);
    }

    public void addEPackage(EPackage ePackage) {
        // TODO Auto-generated method stub

    }

    public void removeEPakcage(EPackage ePackage) {
        // TODO Auto-generated method stub

    }

}
