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
package org.eclipse.sensinact.prototype.model.nexus.impl;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.sensinact.model.core.Action;
import org.eclipse.sensinact.model.core.ActionParameter;
import org.eclipse.sensinact.model.core.Admin;
import org.eclipse.sensinact.model.core.AnnotationMetadata;
import org.eclipse.sensinact.model.core.FeatureCustomMetadata;
import org.eclipse.sensinact.model.core.Provider;
import org.eclipse.sensinact.model.core.ResourceAttribute;
import org.eclipse.sensinact.model.core.ResourceMetadata;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.model.core.Service;
import org.eclipse.sensinact.model.core.ServiceReference;
import org.eclipse.sensinact.prototype.command.impl.ActionHandler;
import org.eclipse.sensinact.prototype.model.ResourceType;
import org.eclipse.sensinact.prototype.model.nexus.impl.emf.EMFUtil;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.osgi.util.promise.Promise;
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

    private Map<URI, EPackage> packageCache = new ConcurrentHashMap<>();
    private EPackage defaultPackage;

    private final Map<String, Provider> providers = new HashMap<>();

    private final Map<String, EClass> models = new HashMap<>();

    private final ActionHandler actionHandler;

    public ModelNexus(ResourceSet resourceSet, SensiNactPackage sensinactPackage,
            Supplier<NotificationAccumulator> accumulator, ActionHandler actionHandler) {
        this.resourceSet = resourceSet;
        this.sensinactPackage = sensinactPackage;
        this.notificationAccumulator = accumulator;
        this.actionHandler = actionHandler;
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
        setupSensinactProvider();
    }

    private Optional<EPackage> loadDefaultPackage(Path fileName) {
        Resource resource = resourceSet.createResource(URI.createFileURI(fileName.toString()));
        if (Files.isRegularFile(fileName)) {
            try {
                resource.load(null);
                if (!resource.getContents().isEmpty()) {
                    EPackage defaultPackage = (EPackage) resource.getContents().get(0);
                    resource.setURI(URI.createURI(defaultPackage.getNsURI()));
                    resourceSet.getResources().remove(resource);
                    resourceSet.getPackageRegistry().put(defaultPackage.getNsURI(), defaultPackage);

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
                    models.putIfAbsent(getModelName(eClass), eClass);
                    providers.put(provider.getId(), provider);
                }
            }
        } catch (IOException e) {
            LOG.error("THIS WILL BE A RUNTIME EXCPETION FOR NOW: Error loading provider from Path: {}", path, e);
            throw new RuntimeException(e);
        }

    }

    private void setupSensinactProvider() {

        Instant now = Instant.now();
        NotificationAccumulator accumulator = notificationAccumulator.get();
        EClass sensiNactModel = getModel("sensinact").orElseGet(() -> createModel("sensinact", now, accumulator));
        EReference svc = Optional.ofNullable(getServiceForModel(sensiNactModel, "system"))
                .orElseGet(() -> createService(sensiNactModel, "system", now, accumulator));
        EClass svcClass = svc.getEReferenceType();
        EStructuralFeature versionResource = Optional.ofNullable(svcClass.getEStructuralFeature("version"))
                .orElseGet(() -> createResource(svcClass, "version", double.class, now, null, accumulator));
        EStructuralFeature startedResource = Optional.ofNullable(svcClass.getEStructuralFeature("started"))
                .orElseGet(() -> createResource(svcClass, "started", Instant.class, now, null, accumulator));

        Provider provider = Optional.ofNullable(getProvider("sensiNact"))
                .orElseGet(() -> doCreateProvider("sensinact", sensiNactModel, "sensiNact", now, accumulator));

        handleDataUpdate("sensinact", provider, svc, versionResource, 0.1D, now);
        handleDataUpdate("sensinact", provider, svc, startedResource, now, now);

    }

    public void shutDown() {
        defaultPackage.eResource().setURI(URI.createFileURI(Path.of(BASIC_BASE_ECORE).toAbsolutePath().toString()));
        try {
            defaultPackage.eResource().save(null);
            providers.values().forEach(this::saveInstance);
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
        parent.getLinkedProviders().add(child);

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

        NotificationAccumulator accumulator = notificationAccumulator.get();

        Provider parent = providers.get(parentProvider);

        Provider child = providers.get(childProvider);

        if (parent == null) {
            throw new IllegalArgumentException("No parent provider " + parentProvider);
        }
        if (child == null) {
            throw new IllegalArgumentException("No child provider " + childProvider);
        }
        parent.getLinkedProviders().remove(child);

        // TODO unlink event
        // accumulator.unlink(...)
    }

    public void handleDataUpdate(String modelName, Provider provider, EStructuralFeature serviceFeature,
            EStructuralFeature resourceFeature, Object data, Instant timestamp) {

        Instant metaTimestamp = timestamp == null ? Instant.now() : timestamp;

        String providerName = provider.getId();

        NotificationAccumulator accumulator = notificationAccumulator.get();

        Service service = (Service) provider.eGet(serviceFeature);
        if (service == null) {
            service = (Service) EcoreUtil.create((EClass) serviceFeature.getEType());
            provider.eSet(serviceFeature, service);
            accumulator.addService(modelName, providerName, serviceFeature.getName());
        }

        // Handle Metadata

        ResourceMetadata metadata = service.getMetadata().get(resourceFeature);

        Map<String, Object> oldMetaData = null;
        Object oldValue = service.eGet(resourceFeature);
        if (metadata != null) {
            oldMetaData = EMFUtil.toEObjectAttributesToMap(metadata);
            oldMetaData.put("value", oldValue);
        }
        if (oldValue == null) {
            accumulator.addResource(modelName, providerName, serviceFeature.getName(), resourceFeature.getName());
        }

        // Allow an update if the resource didn't exist or if the update timestamp is
        // equal to or after the one of the current value
        if (metadata == null || !metadata.getTimestamp().isAfter(timestamp)) {
            EClassifier resourceType = resourceFeature.getEType();
            if (data == null || resourceType.isInstance(data)) {
                service.eSet(resourceFeature, data);
            } else {
                service.eSet(resourceFeature, EMFUtil.convertToTargetType(resourceType, data));
            }
            accumulator.resourceValueUpdate(modelName, providerName, serviceFeature.getName(),
                    resourceFeature.getName(), resourceType.getInstanceClass(), oldValue, data, timestamp);
        } else {
            return;
        }

        if (metadata == null) {
            metadata = sensinactPackage.getSensiNactFactory().createResourceMetadata();
            metadata.setTimestamp(metaTimestamp);
            service.getMetadata().put(resourceFeature, metadata);
        }
        metadata.setTimestamp(timestamp);

        Map<String, Object> newMetaData = EMFUtil.toEObjectAttributesToMap(metadata, true);
        newMetaData.put("value", data);

        accumulator.metadataValueUpdate(modelName, providerName, serviceFeature.getName(), resourceFeature.getName(),
                oldMetaData, newMetaData, timestamp);
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
    private Provider doCreateProvider(String modelName, EClass model, String providerName, Instant timestamp,
            NotificationAccumulator accumulator) {

        Provider provider = (Provider) EcoreUtil.create(model);
        provider.setId(providerName);

        final Admin adminSvc = sensinactPackage.getSensiNactFactory().createAdmin();
        provider.setAdmin(adminSvc);

        // Set a timestamp to admin resources to indicate them as valued
        for (EStructuralFeature resourceFeature : provider.getAdmin().eClass().getEStructuralFeatures()) {
            ResourceMetadata metadata = sensinactPackage.getSensiNactFactory().createResourceMetadata();
            metadata.setTimestamp(Instant.EPOCH);
            metadata.setOriginalName(resourceFeature.getName());
            adminSvc.getMetadata().put(resourceFeature, metadata);
        }

        // Set the friendlyName value
        adminSvc.setFriendlyName(providerName);
        adminSvc.getMetadata().get(SensiNactPackage.Literals.ADMIN__FRIENDLY_NAME).setTimestamp(timestamp);

        providers.put(providerName, provider);
        accumulator.addProvider(modelName, providerName);

        return provider;
    }

    public Provider createProviderInstance(String modelName, String providerName, NotificationAccumulator accumulator) {
        return createProviderInstance(modelName, providerName, Instant.now(), accumulator);
    }

    public Provider createProviderInstance(String modelName, String providerName, Instant timestamp,
            NotificationAccumulator accumulator) {

        Provider provider = getProvider(providerName);
        if (provider != null) {
            String m = getModelName(provider.eClass());
            if (!m.equals(modelName)) {
                throw new IllegalArgumentException(
                        "The provider " + providerName + " already exists with a different model " + m);
            } else {
                throw new IllegalArgumentException(
                        "The provider " + providerName + " already exists with the model " + modelName);
            }
        } else {
            provider = doCreateProvider(modelName, getMandatoryModel(modelName), providerName, timestamp, accumulator);
        }

        return provider;
    }

    public Provider getProvider(String providerName) {
        return providers.get(providerName);
    }

    public String getProviderModel(String providerName) {
        return Optional.ofNullable(providers.get(providerName)).map(p -> getModelName(p.eClass())).orElse(null);
    }

    public Provider getProvider(String model, String providerName) {
        Provider p = providers.get(providerName);
        if (p != null) {
            String m = getModelName(p.eClass());
            if (!m.equals(model)) {
                LOG.debug("Provider {} exists but with model {} not model {}", providerName, m, model);
                p = null;
            }
        }
        return p;
    }

    /**
     * Lists know providers
     */
    public Collection<Provider> getProviders() {
        return List.copyOf(providers.values());
    }

    /**
     * Lists know providers
     */
    public List<Provider> getProviders(String model) {
        EClass m = getMandatoryModel(model);
        // Don't use isInstance as subtypes have a different model
        return providers.values().stream().filter(p -> p.eClass() == m).collect(Collectors.toList());
    }

    public EAttribute createResource(EClass service, String resource, Class<?> type, Instant timestamp,
            Object defaultValue, NotificationAccumulator accumulator) {
        FeatureCustomMetadata resourceType = sensinactPackage.getSensiNactFactory().createFeatureCustomMetadata();
        resourceType.setName("resourceType");
        resourceType.setValue(ResourceType.SENSOR);
        resourceType.setTimestamp(Instant.EPOCH);

        return doCreateResource(service, resource, type, timestamp, defaultValue, List.of(resourceType), accumulator);
    }

    private EAttribute doCreateResource(EClass service, String resource, Class<?> type, Instant timestamp,
            Object defaultValue, List<FeatureCustomMetadata> metadata, NotificationAccumulator accumulator) {
        assertResourceNotExist(service, resource);
        ResourceAttribute feature = EMFUtil.createResourceAttribute(service, resource, type, defaultValue);
        EMFUtil.fillMetadata(feature, timestamp, false, resource, List.of());
        return feature;
    }

    private void assertResourceNotExist(EClass service, String resource) {
        ETypedElement element = service.getEOperations().stream().filter(o -> o.getName().equals(resource))
                .map(ETypedElement.class::cast).findFirst().orElseGet(() -> service.getEStructuralFeature(resource));
        if (element != null) {
            throw new IllegalArgumentException("There is an existing resource with name " + resource + " in service "
                    + service + " in model " + getModelName(service.eContainingFeature().getEContainingClass()));
        }
    }

    private EClass createModel(String modelName, String thePackageUri, Instant timestamp) {
        String modelClassName = firstToUpper(modelName);
        URI packageUri = URI.createURI(thePackageUri);
        EPackage ePackage = packageCache.get(packageUri);
        EClass model = EMFUtil.createEClass(modelClassName, ePackage,
                (ec) -> createEClassAnnotations(modelName, timestamp), sensinactPackage.getProvider());
        models.put(modelName, model);
        return model;
    }

    private EReference createService(EClass model, String name, Instant timestamp) {
        EPackage ePackage = model.getEPackage();
        EClass service = EMFUtil.createEClass(constructServiceEClassName(model.getName(), name), ePackage,
                (ec) -> createEClassAnnotations(timestamp), sensinactPackage.getService());
        ServiceReference ref = EMFUtil.createServiceReference(model, name, service, true);
        EMFUtil.fillMetadata(ref, timestamp, false, name, List.of());
        return ref;
    }

    private List<EAnnotation> createEClassAnnotations(Instant timestamp) {
        AnnotationMetadata meta = sensinactPackage.getSensiNactFactory().createAnnotationMetadata();
        meta.setTimestamp(timestamp);
        EAnnotation annotation = EMFUtil.createEAnnotation("metadata", Collections.singletonList(meta));
        return Collections.singletonList(annotation);
    }

    private List<EAnnotation> createEClassAnnotations(String model, Instant timestamp) {
        // TODO make this part of the ModelMetadata?
        return List.of(createEClassAnnotations(timestamp).get(0),
                EMFUtil.createEAnnotation("model", Map.of("name", model)));
    }

//    private List<EAnnotation> createEFeatureAnnotation(EStructuralFeature feature, Instant timestamp,
//            List<FeatureCustomMetadata> metadata) {
//        ModelMetadata meta = sensinactPackage.getSensiNactFactory().createModelMetadata();
//        meta.setTimestamp(timestamp);
//        meta.getExtra().addAll(metadata);
//        EAnnotation annotation = EMFUtil.createEAnnotation("metadata", Collections.singletonList(meta));
//        return Collections.singletonList(annotation);
//    }

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

    private void saveInstance(Provider p) {
        URI baseUri = URI.createFileURI(Path.of(INSTANCES).toAbsolutePath().toString());
        URI instanceUri = baseUri.appendSegment(p.getId()).appendFileExtension("xmi");
        Resource res = resourceSet.createResource(instanceUri);
        res.getContents().add(p);
        try {
            p.eResource().save(Collections.singletonMap(XMLResource.OPTION_SCHEMA_LOCATION, true));
        } catch (IOException ex) {
            LOG.error("THIS WILL BE A RUNTIME EXCPETION FOR NOW: Error saving provider fro URI: {}", instanceUri, p);
            throw new RuntimeException(ex);
        }
    }

    public Map<String, Object> getResourceMetadata(Provider provider, EStructuralFeature svcFeature,
            final ETypedElement rcFeature) {
        final Service svc = (Service) provider.eGet(svcFeature);
        if (svc == null) {
            return Map.of();
        }

        final ResourceMetadata metadata = svc.getMetadata().get(rcFeature);
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

    public void setResourceMetadata(Provider provider, EStructuralFeature svcFeature, ETypedElement resource,
            String metadataKey, Object value, Instant timestamp) {
        if (metadataKey == null || metadataKey.isEmpty()) {
            throw new IllegalArgumentException("Empty metadata key");
        }

        if (timestamp == null) {
            throw new IllegalArgumentException("Invalid timestamp");
        }

        final Service svc = (Service) provider.eGet(svcFeature);

        ResourceMetadata metadata = svc == null ? null : svc.getMetadata().get(resource);

        if (metadata == null) {
            throw new IllegalStateException("No existing metadata for resource");
        }

        metadata.getExtra().stream().filter(fcm -> fcm.getName().equals(metadataKey)).findFirst().ifPresentOrElse(
                fcm -> handleFeatureCustomMetadata(fcm, metadataKey, timestamp, value),
                () -> metadata.getExtra()
                        .add(handleFeatureCustomMetadata(
                                sensinactPackage.getSensiNactFactory().createFeatureCustomMetadata(), metadataKey,
                                timestamp, value)));

        metadata.setTimestamp(timestamp);
    }

    private FeatureCustomMetadata handleFeatureCustomMetadata(FeatureCustomMetadata customMetadata, String metadataKey,
            Instant timestamp, Object value) {
        customMetadata.setName(metadataKey);
        customMetadata.setTimestamp(timestamp);
        customMetadata.setValue(value);
        return customMetadata;
    }

    public Set<String> getModelNames() {
        return Set.copyOf(models.keySet());
    }

    public Optional<EClass> getModel(String modelName) {
        return Optional.ofNullable(models.get(modelName));
    }

    private EClass getMandatoryModel(String modelName) {
        return getModel(modelName).orElseThrow(() -> new IllegalArgumentException("No model with name " + modelName));
    }

    public EClass createModel(String modelName, Instant timestamp, NotificationAccumulator accumulator) {
        if (getModel(modelName).isPresent()) {
            throw new IllegalArgumentException("There is an existing model with name " + modelName);
        }
        return createModel(modelName, DEFAULT_URI, timestamp);
    }

    public EReference createService(EClass model, String service, Instant creationTimestamp,
            NotificationAccumulator accumulator) {
        if (model.getEStructuralFeature(service) != null) {
            throw new IllegalArgumentException(
                    "There is an existing service with name " + service + " in model " + model);
        }
        return createService(model, service, creationTimestamp);
    }

    public String getModelName(EClass model) {
        return model.getEAnnotation("model").getDetails().get("name");
    }

    public Stream<EReference> getServicesForModel(EClass model) {
        EClass svcClass = sensinactPackage.getService();
        return model.getEAllReferences().stream().filter(r -> svcClass.isSuperTypeOf(r.getEReferenceType()));
    }

    private EReference getServiceForModel(EClass model, String serviceName) {
        EStructuralFeature feature = model.getEStructuralFeature(serviceName);
        EClass serviceEClass = sensinactPackage.getService();
        if (feature != null && (!(feature instanceof EReference)
                || !serviceEClass.isSuperTypeOf(((EReference) feature).getEReferenceType()))) {
            throw new IllegalArgumentException("The field " + serviceName + " exists in the model "
                    + getModelName(model) + " and is not a service");
        }
        return feature == null ? null : (EReference) feature;
    }

    public Stream<ETypedElement> getResourcesForService(EClass svcClass) {
        return Stream.concat(
                svcClass.getEAllAttributes().stream()
                        .filter(o -> o.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE),
                svcClass.getEAllOperations().stream()
                        .filter(o -> o.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE));
    }

    public EOperation createActionResource(EClass serviceEClass, String name, Class<?> type,
            List<Entry<String, Class<?>>> namedParameterTypes) {

        assertResourceNotExist(serviceEClass, name);

        List<ActionParameter> params = namedParameterTypes.stream().map(EMFUtil::createActionParameter)
                .collect(Collectors.toList());

        Action action = EMFUtil.createAction(serviceEClass, name, type, params);

        return action;
    }

    public Promise<Object> act(Provider provider, EStructuralFeature service, ETypedElement resource,
            Map<String, Object> parameters) {
        return actionHandler.act(getModelName(provider.eClass()), provider.getId(), service.getName(),
                resource.getName(), parameters);
    }
}
