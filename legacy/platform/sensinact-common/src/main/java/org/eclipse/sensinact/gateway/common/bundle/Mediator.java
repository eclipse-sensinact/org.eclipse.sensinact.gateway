/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/

package org.eclipse.sensinact.gateway.common.bundle;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.util.PropertyUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mediator Pattern Architecture purpose
 */
public class Mediator {
	private static final Logger LOG=LoggerFactory.getLogger(Mediator.class);
	public static final String DEFAULT_BUNDLE_PROPERTY_FILEDIR = "felix.fileinstall.dir";

	public static final String SENSINACT_CONFIG_FILE = "sensiNact-conf.xml";

	/**
	 * ThreadLocal {@link ServiceCaller} used to interact with the OSGi host
	 * environment by calling and registering services
	 */
	public final ThreadLocal<ServiceCaller> CALLERS = new ThreadLocal<ServiceCaller>() {
		@Override
		public ServiceCaller initialValue() {
			return new ServiceCaller(Mediator.this.getContext());
		}
	};

	protected Properties properties;
	protected BundleContext context;
	private Map<String, TrackerCustomizer<?>> customizers;
	private List<ServiceTracker<?, ?>> trackers;
	private List<ServiceRegistration<?>> registrations;
	private MediatorManagedConfiguration configuration;

	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public Mediator(BundleContext context) {
		this.context = context;
		this.properties = new Properties();
		this.customizers = new HashMap<String, TrackerCustomizer<?>>();
		this.trackers = new ArrayList<ServiceTracker<?, ?>>();
		this.registrations = new ArrayList<ServiceRegistration<?>>();
		try {
			URL config = context.getBundle().getResource(SENSINACT_CONFIG_FILE);

			if (config != null) {
				InputStream input;
				input = config.openStream();
				properties.loadFromXML(input);
				input.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadBundleProperties();

		String managed = (String) this.getProperty(MediatorManagedConfiguration.MANAGED_SENSINACT_MODULE);

		if (managed != null && Boolean.parseBoolean(managed)) {
			this.configuration = new MediatorManagedConfiguration(this,
					context.getBundle().getSymbolicName().replace('-', '.'));
			this.configuration.register();
		}
	}

	private void loadBundleProperties() {

		final String fileInstallDir = context.getProperty(DEFAULT_BUNDLE_PROPERTY_FILEDIR);

		if (fileInstallDir == null) {
			return;
		}
		LOG.info("Configuration directory {}", fileInstallDir);

		final String symbolicName = context.getBundle().getSymbolicName();
		LOG.info("Bundle symbolic name {}", symbolicName);

		final String bundlePropertyFileName = String.format("%s/%s.config", fileInstallDir, symbolicName);
		Boolean propertiesLoaded = Boolean.FALSE;
		Properties bundleProperties = new Properties();

		/**
		 * Looks for property files put into config directory
		 */
		try {
			bundleProperties.load(new FileInputStream(bundlePropertyFileName));
			LOG.debug("File {} loaded successfully", bundlePropertyFileName);
			logBundleProperties(symbolicName, bundlePropertyFileName, bundleProperties);
			propertiesLoaded = true;
		} catch (IOException e) {
			// Log message will be aggregated with the fallback result and given later
		}

		// If not even the fallback didnt manage to get loaded, display message in the
		// log
		if (!propertiesLoaded) {
			LOG.debug("bundle {} does not have custom configuration {}, using default values.", 
					symbolicName, bundlePropertyFileName);
		}

		for (Map.Entry<Object, Object> name : bundleProperties.entrySet()) {
			this.properties.put(name.getKey().toString(), name.getValue().toString());
		}
	}

	private void logBundleProperties(String bundleName, String propertyFile, Properties properties) {
		LOG.debug("Loading properties for bundle {} located in {}", bundleName, propertyFile);
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			LOG.info("{}:{}", entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Adds a {@link ManagedConfigurationListener} to be notified when the
	 * properties of this this Mediator's {@link MediatorManagedConfiguration} are
	 * updated
	 * 
	 * @param listener
	 *            the {@link ManagedConfigurationListener} to add
	 */
	public void addListener(ManagedConfigurationListener listener) {
		if (configuration != null) {
			this.configuration.addListener(listener);
		}
	}

	/**
	 * Removes the {@link ManagedConfigurationListener} to be removed from the list
	 * of those to be notified when the properties of this Mediator's
	 * {@link MediatorManagedConfiguration} are updated
	 * 
	 * @param listener
	 *            the {@link ManagedConfigurationListener} to remove
	 */
	public void deleteListener(ManagedConfigurationListener listener) {
		if (configuration != null) {
			this.configuration.deleteListener(listener);
		}
	}

	/**
	 * Registers the service passed as parameter in the OSGi host environment using
	 * the specified type and Dictionary
	 * 
	 * @param service
	 *            the service to be registered
	 * @param serviceType
	 *            the type of the service to be registered
	 * @param properties
	 *            the set of properties associated to the service registration
	 */
	public <S> void register(S service, Class<S> serviceType, Dictionary<String, ?> properties) {
		synchronized (this.registrations) {
			this.registrations.add(this.context.registerService(serviceType, service, properties));
		}
	}

	/**
	 * Registers the service passed as parameter in the OSGi host environment using
	 * the specified type and Dictionary
	 * 
	 * @param service
	 *            the service to be registered
	 * @param serviceType
	 *            the type of the service to be registered
	 * @param properties
	 *            the set of properties associated to the service registration
	 */
	public void register(Dictionary<String, ?> properties, Object service, Class<?>[] serviceTypes) {
		int length = serviceTypes == null ? 0 : serviceTypes.length;
		int index = 0;
		String[] types = new String[length];
		for (; index < length; index++) {
			types[index] = serviceTypes[index].getName();
		}
		synchronized (this.registrations) {
			this.registrations.add(this.context.registerService(types, service, properties));
		}
	}

	/**
	 * @param serviceType
	 * @param executable
	 * @return
	 */
	public <S, R> R callService(Class<S> serviceType, Executable<S, R> executable) {
		return this.callService(serviceType, null, executable);
	}

	/**
	 * @param serviceType
	 * @param filter
	 * @param executable
	 * @return
	 */
	public <S, R> R callService(Class<S> serviceType, String filter, Executable<S, R> executable) {
		ServiceCaller caller = CALLERS.get();
		caller.attach();
		try {
			return caller.callService(serviceType, filter, executable);

		} catch (Exception e) {
			LOG.error(e.getMessage(),e);

		} finally {
			if (caller.release() == 0) {
				CALLERS.remove();
			}
		}
		return null;
	}

	/**
	 * @param serviceType
	 * @param executable
	 */
	public <S> void callServices(Class<S> serviceType, Executable<S, Void> executable) {
		this.callServices(serviceType, null, executable);
	}

	/**
	 * @param serviceType
	 * @param filter
	 * @param executable
	 */
	public <S> void callServices(Class<S> serviceType, String filter, Executable<S, Void> executable) {
		ServiceCaller caller = CALLERS.get();
		caller.attach();
		try {
			caller.callServices(serviceType, filter, executable);

		} catch (Exception e) {
			LOG.error(e.getMessage(),e);

		} finally {
			if (caller.release() == 0) {
				CALLERS.remove();
			}
		}
	}

	/**
	 * @param serviceType
	 * @param returnType
	 * @param filter
	 * @param executable
	 * @return
	 */
	public <S, R> Collection<R> callServices(Class<S> serviceType, Class<R> returnType, String filter,
			Executable<S, R> executable) {
		ServiceCaller caller = CALLERS.get();
		caller.attach();
		try {
			return caller.callServices(serviceType, returnType, filter, executable);

		} catch (Exception e) {
			LOG.error(e.getMessage(),e);

		} finally {
			if (caller.release() == 0) {
				CALLERS.remove();
			}
		}
		return Collections.<R>emptyList();
	}

	/**
	 * Attaches the {@link Executable} passed as parameter to the event of a service
	 * appearance if this last one is of the specified Type, and compliant with the
	 * specified String filter
	 * 
	 * @param serviceType
	 *            the Type of service for which to execute the specified
	 *            {@link Executable}
	 * @param filter
	 *            the String filter defining the properties of the service for which
	 *            to execute the specified {@link Executable}
	 * @param executables
	 *            the list of {@link Executable}s to be executed when the
	 *            appropriate service appears
	 * 
	 * @return the String identifier of the created attachment, that can be used
	 *         later to delete it
	 */
	public <S> String attachOnServiceAppearing(Class<S> serviceType, String filter,
			List<Executable<S, Void>> executables) {
		String key = null;
		String trackingFilter = createFilter(serviceType, filter);

		@SuppressWarnings("unchecked")
		TrackerCustomizer<S> customizer = (TrackerCustomizer<S>) this.customizers.get(trackingFilter);

		if (customizer == null) {
			customizer = new TrackerCustomizer<S>(this);
			this.customizers.put(trackingFilter, customizer);

			key = customizer.attachOnAdding(executables);
			ServiceTracker<S, S> tracker = new ServiceTracker<S, S>(context, serviceType.getCanonicalName(),
					customizer);

			synchronized (this.trackers) {
				trackers.add(tracker);
			}
			tracker.open();
		} else {
			key = customizer.attachOnAdding(executables);
		}
		return key;
	}

	/**
	 * Attaches the {@link Executable} passed as parameter to the event of a service
	 * appearance if this last one is of the specified Type, and compliant with the
	 * specified String filter
	 * 
	 * @param serviceType
	 *            the Type of service for which to execute the specified
	 *            {@link Executable}
	 * @param filter
	 *            the String filter defining the properties of the service for which
	 *            to execute the specified {@link Executable}
	 * @param executable
	 *            the {@link Executable} to be executed when the appropriate service
	 *            appears
	 * 
	 * @return the String identifier of the created attachment, that can be used
	 *         later to delete it
	 */
	public <S> String attachOnServiceAppearing(Class<S> serviceType, String filter, Executable<S, Void> executable) {
		return this.attachOnServiceAppearing(serviceType, filter, Collections.singletonList(executable));
	}

	/**
	 * @param serviceType
	 * @param filter
	 * @param key
	 */
	public <S> void detachOnServiceAppearing(Class<S> serviceType, String filter, String key) {
		String trackingFilter = createFilter(serviceType, filter);
		@SuppressWarnings("unchecked")
		TrackerCustomizer<S> customizer = (TrackerCustomizer<S>) this.customizers.get(trackingFilter);
		customizer.detachOnAdding(key);
	}

	/**
	 * Attaches the {@link Executable} passed as parameter to the event of a service
	 * disappearance if this last one is of the specified Type, and compliant with
	 * the specified String filter
	 * 
	 * @param serviceType
	 *            the Type of service for which to execute the specified
	 *            {@link Executable}
	 * @param filter
	 *            the String filter defining the properties of the service for which
	 *            to execute the specified {@link Executable}
	 * @param executables
	 *            the list of {@link Executable}s to be executed when the
	 *            appropriate service disappears
	 * 
	 * @return the String identifier of the created attachment, that can be used
	 *         later to delete it
	 */
	public <S> String attachOnServiceDisappearing(Class<S> serviceType, String filter,
			List<Executable<S, Void>> executables) {
		String key = null;
		String trackingFilter = createFilter(serviceType, filter);
		@SuppressWarnings("unchecked")
		TrackerCustomizer<S> customizer = (TrackerCustomizer<S>) this.customizers.get(trackingFilter);

		if (customizer == null) {
			customizer = new TrackerCustomizer<S>(this);
			this.customizers.put(trackingFilter, customizer);
			key = customizer.attachOnRemoving(executables);

			ServiceTracker<S, S> tracker = new ServiceTracker<S, S>(context, trackingFilter, customizer);

			synchronized (this.trackers) {
				trackers.add(tracker);
			}
			tracker.open();
		} else {
			key = customizer.attachOnRemoving(executables);
		}
		return key;
	}

	/**
	 * Attaches the {@link Executable} passed as parameter to the event of a service
	 * disappearance if this last one is of the specified Type, and compliant with
	 * the specified String filter
	 * 
	 * @param serviceType
	 *            the Type of service for which to execute the specified
	 *            {@link Executable}
	 * @param filter
	 *            the String filter defining the properties of the service for which
	 *            to execute the specified {@link Executable}
	 * @param executable
	 *            the {@link Executable}s to be executed when the appropriate
	 *            service disappears
	 * 
	 * @return the String identifier of the created attachment, that can be used
	 *         later to delete it
	 */
	public <S> String attachOnServiceDisappearing(Class<S> serviceType, String filter, Executable<S, Void> executable) {
		return this.attachOnServiceDisappearing(serviceType, filter, Collections.singletonList(executable));
	}

	/**
	 * @param serviceType
	 * @param filter
	 * @param key
	 */
	public <S> void detachOnServiceDisappearing(Class<S> serviceType, String filter, String key) {
		String trackingFilter = createFilter(serviceType, filter);
		@SuppressWarnings("unchecked")
		TrackerCustomizer<S> customizer = (TrackerCustomizer<S>) this.customizers.get(trackingFilter);
		customizer.detachOnRemoving(key);
	}

	/**
	 * Attaches the {@link Executable} passed as parameter to the event of a service
	 * modification if this last one is of the specified Type, and compliant with
	 * the specified String filter
	 * 
	 * @param serviceType
	 *            the Type of service for which to execute the specified
	 *            {@link Executable}
	 * @param filter
	 *            the String filter defining the properties of the service for which
	 *            to execute the specified {@link Executable}
	 * @param executables
	 *            the list of {@link Executable}s to be executed when the
	 *            appropriate service is modified
	 * 
	 * @return the String identifier of the created attachment, that can be used
	 *         later to delete it
	 */
	public <S> String attachOnServiceUpdating(Class<S> serviceType, String filter,
			List<Executable<S, Void>> executables) {
		String key = null;
		String trackingFilter = createFilter(serviceType, filter);

		@SuppressWarnings("unchecked")
		TrackerCustomizer<S> customizer = (TrackerCustomizer<S>) this.customizers.get(trackingFilter);

		if (customizer == null) {
			customizer = new TrackerCustomizer<S>(this);
			this.customizers.put(trackingFilter, customizer);

			key = customizer.attachOnModifying(executables);
			ServiceTracker<S, S> tracker = new ServiceTracker<S, S>(context, trackingFilter, customizer);
			synchronized (this.trackers) {
				trackers.add(tracker);
			}
			tracker.open();
		} else {
			key = customizer.attachOnModifying(executables);
		}
		return key;
	}

	/**
	 * Attaches the {@link Executable} passed as parameter to the event of a service
	 * modification if this last one is of the specified Type, and compliant with
	 * the specified String filter
	 * 
	 * @param serviceType
	 *            the Type of service for which to execute the specified
	 *            {@link Executable}
	 * @param filter
	 *            the String filter defining the properties of the service for which
	 *            to execute the specified {@link Executable}
	 * @param executable
	 *            the {@link Executable} to be executed when the appropriate service
	 *            is modified
	 * 
	 * @return the String identifier of the created attachment, that can be used
	 *         later to delete it
	 */
	public <S> String attachOnServiceUpdating(Class<S> serviceType, String filter, Executable<S, Void> executable) {
		return this.attachOnServiceUpdating(serviceType, filter, Collections.singletonList(executable));
	}

	/**
	 * @param serviceType
	 * @param filter
	 * @param key
	 */
	public <S> void detachOnServiceUpdating(Class<S> serviceType, String filter, String key) {
		String trackingFilter = createFilter(serviceType, filter);
		@SuppressWarnings("unchecked")
		TrackerCustomizer<S> customizer = (TrackerCustomizer<S>) this.customizers.get(trackingFilter);
		customizer.detachOnModifying(key);
	}

	/**
	 * @param serviceType
	 * @param filter
	 * @return
	 */
	public String createFilter(Class<?> serviceType, String filter) {
		StringBuilder classBuilder = null;
		StringBuilder filterBuilder = null;

		if (serviceType != null) {
			classBuilder = new StringBuilder();
			classBuilder.append("(objectClass=");
			classBuilder.append(serviceType.getCanonicalName());
			classBuilder.append(")");
		}
		if (filter != null) {
			filterBuilder = new StringBuilder();

			if (classBuilder != null) {
				filterBuilder.append("(&");
				filterBuilder.append(classBuilder.toString());
			}
			if (!filter.startsWith("(")) {
				filterBuilder.append("(");
			}
			filterBuilder.append(filter);
			if (!filter.endsWith(")")) {
				filterBuilder.append(")");
			}
			if (classBuilder != null) {
				filterBuilder.append(")");
			}
		} else {
			filterBuilder = classBuilder;
		}
		if (filterBuilder != null) {
			return filterBuilder.toString();
		}
		return null;
	}


	/**
	 * the mediator is deactivated before bundle stopping
	 */
	public void deactivate() {
		synchronized (this.trackers) {
			int index = 0;
			int length = trackers == null ? 0 : trackers.size();
			for (; index < length; index++) {
				trackers.remove(0).close();
			}
		}
		synchronized (this.registrations) {
			int index = 0;
			int length = this.registrations == null ? 0 : this.registrations.size();
			for (; index < length; index++) {
				try {
					this.registrations.remove(0).unregister();
				} catch (IllegalStateException e) {
				}
			}
		}
		this.customizers.clear();
		if (this.configuration != null) {
			this.configuration.unregister();
		}
	}

	/**
	 * Returns the associated {@link BundleContext}
	 * 
	 * @return the associated {@link BundleContext}
	 */
	public BundleContext getContext() {
		return this.context;
	}

	/**
	 * @return
	 */
	public ClassLoader getClassLoader() {
		ClassLoader classloader = null;

		try {
			classloader = this.getContext().getBundle().adapt(BundleWiring.class).getClassLoader();

		} catch (NullPointerException e) {
			Enumeration<URL> entries = getContext().getBundle().findEntries("/", "*.class", true);

			if (entries != null && entries.hasMoreElements()) {
				String classname = entries.nextElement().getPath();
				int startIndex = 0;
				int endIndex = classname.length() - ".class".length();
				startIndex += classname.startsWith("/") ? 1 : 0;

				classname = classname.substring(startIndex, endIndex);
				classname = classname.replace('/', '.');
				try {
					Class<?> clazz = getContext().getBundle().loadClass(classname);
					classloader = clazz.getClassLoader();

				} catch (ClassNotFoundException ex) {
					LOG.error(ex.getMessage(),ex);
				}
			}
		}
		if (classloader == null) {
			classloader = Thread.currentThread().getContextClassLoader();
		}
		return classloader;
	}

	/**
	 * Returns the value object of the property whose key is passed as parameter
	 * 
	 * @param property
	 *            the property key to return the value of
	 * @return the value object of the property for the specified key
	 */
	public Object getProperty(final String property) {
		return PropertyUtils.getProperty(this.context, this.properties, property);
	}

	/**
	 * Adds a property entry to this Mediator
	 * 
	 * @param property
	 *            the property key to set the value of
	 * @param value
	 *            the object value of the property
	 */
	public void setProperty(String property, Object value) {
		if (property == null) {
			return;
		}
		this.properties.put(property, value);
	}


	public Map<?, ?> getProperties() {
		return Collections.unmodifiableMap(properties);
	}
}
