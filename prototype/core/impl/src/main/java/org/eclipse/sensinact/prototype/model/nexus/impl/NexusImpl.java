/**
 * Copyright (c) 2022 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.sensinact.prototype.model.nexus.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sensinact.model.core.Metadata;
import org.eclipse.sensinact.model.core.ModelMetadata;
import org.eclipse.sensinact.model.core.Provider;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.model.core.Service;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;

/**
 * 
 * @author Juergen Albert
 * @since 26 Sep 2022
 */
public class NexusImpl {

    /** HTTPS_ECLIPSE_ORG_SENSINACT_BASE */
    private static final String DEFAULT_URI = "https://eclipse.org/sensinact/base";
    private static final URI DEFAULT_URI_OBJECT = URI.createURI(DEFAULT_URI);

    private final ResourceSet resourceSet;
    private final SensiNactPackage sensinactPackage;
    private final Supplier<NotificationAccumulator> notificationAccumulator;

    private Map<URI, ProviderTypeWrapper> providerCache = new ConcurrentHashMap<>();
    private Map<URI, EPackage> packageCache = new ConcurrentHashMap<>();
    private EPackage defaultPackage;

    public NexusImpl(ResourceSet resourceSet, SensiNactPackage sensinactPackage,
            Supplier<NotificationAccumulator> accumulator) {
        this.resourceSet = resourceSet;
        this.sensinactPackage = sensinactPackage;
        this.notificationAccumulator = accumulator;

        defaultPackage = EMFUtil.createPackage("base", DEFAULT_URI, "sensinactBase", resourceSet);
        packageCache.put(DEFAULT_URI_OBJECT, defaultPackage);
    }

    private static class ProviderTypeWrapper {

        private EClass provider;
        private Map<URI, Provider> instances = new ConcurrentHashMap<>();

        public ProviderTypeWrapper(EClass provider) {
            this.provider = provider;
        }

        /**
         * Returns the provider.
         * 
         * @return the provider
         */
        public EClass getProviderType() {
            return provider;
        }

        /**
         * Returns the instances.
         * 
         * @return the instances
         */
        public Map<URI, Provider> getInstances() {
            return instances;
        }
    }

    enum ModelTransactionState {
        NONE, NEW
    }

    private static class ModelTransaction {

        private List<EStructuralFeature> featurePath = new ArrayList<>();
        private ModelTransactionState serviceState = ModelTransactionState.NONE;
        private ModelTransactionState resourceState = ModelTransactionState.NONE;
        private EClass service;

        public void addFeature(EStructuralFeature feature) {
            featurePath.add(feature);
        }

        /**
         * Returns the featurePath.
         * 
         * @return the featurePath
         */
        public List<EStructuralFeature> getFeaturePath() {
            return featurePath;
        }

        /**
         * Returns the service.
         * 
         * @return the service
         */
        public EClass getService() {
            return service;
        }

        /**
         * Sets the service.
         * 
         * @param service the service to set
         */
        public void setService(EClass service) {
            this.service = service;
        }

        /**
         * Returns the serviceState.
         * 
         * @return the serviceState
         */
        public ModelTransactionState getServiceState() {
            return serviceState;
        }

        /**
         * Sets the serviceState.
         * 
         * @param serviceState the serviceState to set
         */
        public void setServiceState(ModelTransactionState serviceState) {
            this.serviceState = serviceState;
        }

        /**
         * Returns the resourceState.
         * 
         * @return the resourceState
         */
        public ModelTransactionState getResourceState() {
            return resourceState;
        }

        /**
         * Sets the resourceState.
         * 
         * @param resourceState the resourceState to set
         */
        public void setResourceState(ModelTransactionState resourceState) {
            this.resourceState = resourceState;
        }
    }

    public void handleDataUpdate(String model, String provider, String service, String resource, Class<?> type,
            Object data, Instant timestamp) {
        if (model == null) {
            model = provider;
        }

        Instant metaTimestamp = timestamp == null ? Instant.now() : timestamp;

        NotificationAccumulator accumulator = notificationAccumulator.get();

        ProviderTypeWrapper wrapper = getOrCreateProvider(model, DEFAULT_URI, metaTimestamp, accumulator);
        ModelTransaction transaction = getOrCreateService(service, wrapper, metaTimestamp, accumulator);
        transaction = getOrCreateResource(wrapper, resource, type, transaction, metaTimestamp, accumulator);
        updateInstances(wrapper, transaction);
        setData(wrapper, transaction, model, provider, data, metaTimestamp, accumulator);
    }

    /**
     * @param transaction
     * @param data
     */
    private void setData(ProviderTypeWrapper wrapper, ModelTransaction transaction, String modelName,
            String providerName, Object data, Instant timestamp, NotificationAccumulator accumulator) {
        URI instanceUri = createURI(modelName, providerName);
        Provider provider = wrapper.getInstances().get(instanceUri);
        if (provider == null) {
            provider = (Provider) EcoreUtil.create(wrapper.getProviderType());
            provider.setId(providerName);
            provider.setAdmin(sensinactPackage.getSensiNactFactory().createAdmin());
            provider.getAdmin().setFriendlyName(providerName);
            wrapper.getInstances().put(instanceUri, provider);
            accumulator.addProvider(providerName);
        }

        EStructuralFeature serviceFeature = transaction.getFeaturePath().get(0);

        Service service = (Service) provider.eGet(serviceFeature);
        if (service == null) {
            service = (Service) EcoreUtil.create((EClass) serviceFeature.getEType());
            provider.eSet(serviceFeature, service);
            accumulator.addService(providerName, serviceFeature.getName());
        }

        EStructuralFeature resourceFeature = transaction.getFeaturePath().get(1);

        // Handle Metadata

        Date tStamp = Date.from(timestamp);
        Metadata metadata = service.getMetadata().get(resourceFeature);

        Map<String, Object> oldMetaData = null;
        Object oldValue = service.eGet(resourceFeature);
        if (metadata != null) {
            oldMetaData = EMFUtil.toEObjectAttributesToMap(metadata);
            oldMetaData.put("value", oldValue);
        }

        if (metadata == null || metadata.getTimestamp().before(tStamp)) {
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
        metadata.setTimestamp(tStamp);

        Map<String, Object> newMetaData = EMFUtil.toEObjectAttributesToMap(metadata);
        newMetaData.put("value", data);

        accumulator.metadataValueUpdate(providerName, serviceFeature.getName(), resourceFeature.getName(), oldMetaData,
                newMetaData, timestamp);
    }

    public Provider getProvider(String providerName) {
        return getProvider(providerName, providerName);
    }

    public Provider getProvider(String model, String providerName) {
        URI packageUri = URI.createURI(DEFAULT_URI);
        URI providerUri = packageUri.appendFragment(model);
        ProviderTypeWrapper wrapper = providerCache.get(providerUri);
        return wrapper == null ? null : wrapper.getInstances().get(createURI(model, providerName));
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

    /**
     * @param wrapper
     * @param transaction
     */
    private void updateInstances(ProviderTypeWrapper wrapper, ModelTransaction transaction) {
        if (transaction.getServiceState() != ModelTransactionState.NONE
                || transaction.getResourceState() != ModelTransactionState.NONE) {
            Set<Entry<URI, Provider>> entrySet = wrapper.getInstances().entrySet();
            for (Iterator<Entry<URI, Provider>> iterator = entrySet.iterator(); iterator.hasNext();) {
                Entry<URI, Provider> entry = iterator.next();
                Provider provider = entry.getValue();
                // we have to create a copy, so the instances know about the updated model
                Provider updatedProvider = provider;
                if (transaction.getServiceState() == ModelTransactionState.NEW) {
                    updatedProvider = SensinactCopier.copySelective(provider);
                    entry.setValue(updatedProvider);
                }
                if (transaction.getResourceState() == ModelTransactionState.NEW
                        && updatedProvider.eIsSet(transaction.getFeaturePath().get(0))) {
                    Service service = (Service) updatedProvider.eGet(transaction.getFeaturePath().get(0));
                    Service updatedService = SensinactCopier.copySelective(service);
                    updatedProvider.eSet(transaction.getFeaturePath().get(0), updatedService);
                }
            }
        }
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

    private ProviderTypeWrapper getOrCreateProvider(String rawProviderName, String thePackageUri, Instant timestamp,
            NotificationAccumulator accumulator) {
        String providerName = firstToUpper(rawProviderName);
        URI packageUri = URI.createURI(thePackageUri);
        URI providerUri = packageUri.appendFragment(providerName);
        ProviderTypeWrapper wrapper = providerCache.get(providerUri);
        if (wrapper == null) {
            EPackage ePackage = packageCache.get(packageUri);
            EClass providerClass = (EClass) ePackage.getEClassifier(providerName);
            if (providerClass == null) {
                providerClass = EMFUtil.createEClass(providerName, ePackage, (ec) -> createEClassAnnotations(timestamp),
                        sensinactPackage.getProvider());
                wrapper = new ProviderTypeWrapper(providerClass);
                providerCache.put(providerUri, wrapper);
                // TODO - do we need a notification here, and if so what notification?
                // It's definitely not the creation of a provider instance
//				notificationAccumulator.get().addProvider(providerName);
            }
        }
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

    /**
     * @param provider
     */
    private void updateMetadata(EClass provider) {
        EAnnotation metadata = provider.getEAnnotation("metadata");
        ModelMetadata meta = (ModelMetadata) metadata.getContents().get(0);
        meta.setVersion(meta.getVersion() + 1);
    }

    private List<EAnnotation> createEClassAnnotations(Instant timestamp) {
        ModelMetadata meta = sensinactPackage.getSensiNactFactory().createModelMetadata();
        meta.setTimestamp(Date.from(timestamp));
        meta.setVersion(1);
        EAnnotation annotation = EMFUtil.createEAnnotation("metadata", Collections.singletonList(meta));
        return Collections.singletonList(annotation);
    }

    private List<EAnnotation> createEFeatureAnnotation(EStructuralFeature feature, Instant timestamp) {
        ModelMetadata meta = sensinactPackage.getSensiNactFactory().createModelMetadata();
        meta.setTimestamp(Date.from(timestamp));
        meta.setVersion(EMFUtil.getContainerVersion(feature));
        EAnnotation annotation = EMFUtil.createEAnnotation("metadata", Collections.singletonList(meta));
        return Collections.singletonList(annotation);
    }

    /**
     * @param name
     * @param serviceName
     * @return
     */
    private String constructServiceEClassName(String providerName, String serviceName) {
        return firstToUpper(providerName) + firstToUpper(serviceName);
    }

    private String firstToUpper(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}
