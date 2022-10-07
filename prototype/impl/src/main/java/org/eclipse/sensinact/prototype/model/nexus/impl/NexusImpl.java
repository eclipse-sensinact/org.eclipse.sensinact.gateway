/**
 * Copyright (c) 2012 - 2022 Data In Motion and others.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sensinact.model.core.Provider;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.model.core.Service;
import org.eclipse.sensinact.prototype.dto.impl.DataUpdateDto;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


/**
 * 
 * @author Juergen Albert
 * @since 26 Sep 2022
 */
@Component
public class NexusImpl {

	/** HTTPS_ECLIPSE_ORG_SENSINACT_BASE */
	private static final String DEFAULT_URI = "https://eclipse.org/sensinact/base";
	private static final URI DEFAULT_URI_OBJECT = URI.createURI(DEFAULT_URI);

	@Reference
	private ResourceSet resourceSet;
	
	@Reference
	private SensiNactPackage sensinactPackage;
	
	private Map<URI, ProviderTypeWrapper> providerCache = new ConcurrentHashMap<>();
	private Map<URI, EPackage> packageCache = new ConcurrentHashMap<>();
	private ReentrantReadWriteLock modelLock;
	private EPackage defaultPackage;
	
	
	private static class ProviderTypeWrapper {
		
		private EClass provider;
		private ReentrantReadWriteLock lock;
		private Map<URI, Provider> instances = new ConcurrentHashMap<>();

		public ProviderTypeWrapper(EClass provider) {
			this.provider = provider;
			lock = new ReentrantReadWriteLock();
		}
		
		/**
		 * Returns the lock.
		 * @return the lock
		 */
		public ReentrantReadWriteLock getLock() {
			return lock;
		}
		
		/**
		 * Returns the provider.
		 * @return the provider
		 */
		public EClass getProviderType() {
			return provider;
		}
		
		/**
		 * Returns the instances.
		 * @return the instances
		 */
		public Map<URI, Provider> getInstances() {
			return instances;
		}
	}
	
	enum ModelTransactionState {
		NONE,
		NEW
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
		 * @return the featurePath
		 */
		public List<EStructuralFeature> getFeaturePath() {
			return featurePath;
		}

		/**
		 * Returns the service.
		 * @return the service
		 */
		public EClass getService() {
			return service;
		}

		/**
		 * Sets the service.
		 * @param service the service to set
		 */
		public void setService(EClass service) {
			this.service = service;
		}

		/**
		 * Returns the serviceState.
		 * @return the serviceState
		 */
		public ModelTransactionState getServiceState() {
			return serviceState;
		}

		/**
		 * Sets the serviceState.
		 * @param serviceState the serviceState to set
		 */
		public void setServiceState(ModelTransactionState serviceState) {
			this.serviceState = serviceState;
		}

		/**
		 * Returns the resourceState.
		 * @return the resourceState
		 */
		public ModelTransactionState getResourceState() {
			return resourceState;
		}

		/**
		 * Sets the resourceState.
		 * @param resourceState the resourceState to set
		 */
		public void setResourceState(ModelTransactionState resourceState) {
			this.resourceState = resourceState;
		}
	}
	
	@Activate
	void activate() {
		defaultPackage = EMFUtil.createPackage("base", DEFAULT_URI, "sensinactBase", resourceSet);
		packageCache.put(DEFAULT_URI_OBJECT, defaultPackage);
	}
	
	public void handleDataUpdateDto(DataUpdateDto dto) {
		String model = dto.model;
		if(model == null) {
			model = dto.provider; 
		}
		
		ProviderTypeWrapper wrapper = getOrCreateProvider(model, DEFAULT_URI);
		String service = dto.service;
		ModelTransaction transaction = getOrCreateService(service, wrapper);
		String resource = dto.resource;
		Class<?> type = dto.type;
		transaction = getOrCreateResource(wrapper, resource, type, transaction);
		updateInstances(wrapper, transaction);
		setData(wrapper, transaction, dto);
	}
	
	/**
	 * @param transaction
	 * @param data
	 */
	private void setData(ProviderTypeWrapper wrapper, ModelTransaction transaction, DataUpdateDto dto) {
		URI instanceUri = createURI(dto);
		wrapper.getLock().readLock().lock();
		Provider provider = wrapper.getInstances().get(instanceUri);
		wrapper.getLock().readLock().unlock();
		if(provider == null) {
			wrapper.getLock().writeLock().lock();
			provider = wrapper.getInstances().get(instanceUri);
			if(provider == null) {
				provider = (Provider) EcoreUtil.create(wrapper.getProviderType());
				provider.setAdmin(sensinactPackage.getSensiNactFactory().createAdmin());
				wrapper.getInstances().put(instanceUri, provider);
			}
			wrapper.getLock().writeLock().unlock();
		}
		
		EStructuralFeature serviceFeature = transaction.getFeaturePath().get(0);
		
		wrapper.getLock().readLock().lock();
		Service service = (Service) provider.eGet(serviceFeature);
		wrapper.getLock().readLock().unlock();
		if(service == null) {
			wrapper.getLock().writeLock().lock();
			service = (Service) provider.eGet(serviceFeature);
			if(service == null) {
				service = (Service) EcoreUtil.create((EClass) serviceFeature.getEType());
				provider.eSet(serviceFeature, service);
			}
			wrapper.getLock().writeLock().unlock();
		}
		
		service.eSet(transaction.getFeaturePath().get(1), dto.data);
		
		//TODO: Handle Metadata
	}

	/**
	 * @param dto
	 * @return
	 */
	private URI createURI(DataUpdateDto dto) {
		String model = dto.model;
		if(model == null) {
			model = dto.provider; 
		}
		URI uri = URI.createURI(model).appendSegment(dto.provider).appendFragment(dto.provider);
		return uri;
	}

	/**
	 * @param wrapper
	 * @param transaction
	 */
	private void updateInstances(ProviderTypeWrapper wrapper, ModelTransaction transaction) {
		if(transaction.getServiceState() != ModelTransactionState.NONE && transaction.getResourceState() != ModelTransactionState.NONE) {
			wrapper.getLock().writeLock().lock();
			try {
				Set<Entry<URI,Provider>> entrySet = wrapper.getInstances().entrySet();
				for (Iterator<Entry<URI, Provider>> iterator = entrySet.iterator(); iterator.hasNext();) {
					Entry<URI, Provider> entry = iterator.next();
					Provider provider = entry.getValue();
					//we have to create a copy, so the instances know about the updated model
					Provider updatedProvider = provider;
					if(transaction.getServiceState() == ModelTransactionState.NEW) {
						updatedProvider = EcoreUtil.copy(provider);
						entry.setValue(updatedProvider);
					}
					if(transaction.getResourceState() == ModelTransactionState.NEW && updatedProvider.eIsSet(transaction.getFeaturePath().get(0))) {
						Service service = (Service) updatedProvider.eGet(transaction.getFeaturePath().get(0));
						Service updatedService = EcoreUtil.copy(service);
						updatedProvider.eSet(transaction.getFeaturePath().get(0), updatedService);
					}
				}
			} finally {
				wrapper.getLock().writeLock().unlock();
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
			ModelTransaction transaction) {
		wrapper.getLock().readLock().lock();
		EClass service = transaction.getService();
		EStructuralFeature feature = service.getEStructuralFeature(resource);
		wrapper.getLock().readLock().unlock();
		if(feature == null) {
			wrapper.getLock().writeLock().lock();
			feature = service.getEStructuralFeature(resource);
			//The EClass does not have a reference to the service. This means we need to create an EClass representing the Service and add a reference to our provider EClass 
			if(feature == null) {
				if(transaction.getServiceState() != ModelTransactionState.NEW) {
					transaction.setResourceState(ModelTransactionState.NEW);
				}
				feature = EMFUtil.createEAttribute(service, resource, type);
			}
			wrapper.getLock().writeLock().unlock();
		}
		transaction.addFeature(feature);
		return transaction;
	}

	private ProviderTypeWrapper getOrCreateProvider(String rawProviderName, String thePackageUri) {
		String providerName = firstToUpper(rawProviderName);
		URI packageUri = URI.createURI(thePackageUri);
		URI providerUri = packageUri.appendFragment(providerName);
		modelLock.readLock().lock();
		ProviderTypeWrapper wrapper = providerCache.get(providerUri);
		modelLock.readLock().unlock();
		if(wrapper == null) {
			modelLock.writeLock().lock();
			wrapper = providerCache.get(providerUri);
			if(wrapper == null) {
				EPackage ePackage = packageCache.get(packageUri);
				EClass providerClass = (EClass) ePackage.getEClassifier(providerName);
				if(providerClass == null) {
					providerClass = EMFUtil.createEClass(providerName, ePackage, sensinactPackage.getProvider());
					wrapper = new ProviderTypeWrapper(providerClass);
					providerCache.put(providerUri, wrapper);
				}
			}
			modelLock.writeLock().unlock();
		}
		return wrapper;
	}
	
	private ModelTransaction getOrCreateService(String serviceName, ProviderTypeWrapper wrapper) {
		wrapper.getLock().readLock().lock();
		ModelTransaction transaction = new ModelTransaction();
		EClass provider = wrapper.getProviderType();
		EStructuralFeature feature = provider.getEStructuralFeature(serviceName);
		wrapper.getLock().readLock().unlock();
		if(feature == null) {
			wrapper.getLock().writeLock().lock();
			feature = provider.getEStructuralFeature(serviceName);
			//The EClass does not have a reference to the service. This means we need to create an EClass representing the Service and add a reference to our provider EClass 
			if(feature == null) {
				transaction.setServiceState(ModelTransactionState.NEW);
				EPackage ePackage = wrapper.getProviderType().getEPackage();
				EClass service = EMFUtil.createEClass(constructServiceEClassName(provider.getName(), serviceName), ePackage, sensinactPackage.getService());
				feature = EMFUtil.createEReference(provider, serviceName, service, true);
				transaction.setService(service);
			}
			wrapper.getLock().writeLock().unlock();
		}
		transaction.addFeature(feature);
		return transaction;
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
