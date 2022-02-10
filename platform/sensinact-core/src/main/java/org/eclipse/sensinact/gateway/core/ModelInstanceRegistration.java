/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.ServiceProvider.LifecycleStatus;
import org.eclipse.sensinact.gateway.core.message.AbstractMidCallback;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage.Lifecycle;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.message.SnaMessageSubType;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessage;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.MutableAccessNode;
import org.eclipse.sensinact.gateway.util.GeoJsonUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.eclipse.sensinact.gateway.util.location.Point;
import org.json.JSONObject;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the {@link ServiceRegistration} of a {@link SensiNactResourceModel}
 * instance and updates the properties of its associated {@link ServiceReference}.
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ModelInstanceRegistration extends AbstractMidCallback {
	private static final Logger LOG=LoggerFactory.getLogger(ModelInstanceRegistration.class);

	private boolean registered;
	private ServiceRegistration<?> instanceRegistration;
	private ModelConfiguration configuration;
	private Map<String, List<String>> observed;

	/**
	 * Constructor
	 * 
	 * @param path
	 *            the String uri of the {@link SensiNactResourceModel} registered
	 * @param observed
	 *            the list of observed String paths
	 * @param registration
	 *            the {@link ServiceRegistration} of the {@link SensiNactResourceModel}
	 * @param configuration
	 *            the {@link ModelConfiguration} of the {@link ModelInstance} whose
	 *            registration will be wrapped by the ModelInstanceRegistration to
	 *            be instantiated
	 */
	public ModelInstanceRegistration(String path, List<String> observed, ServiceRegistration<?> registration,
			ModelConfiguration configuration) {
		super(false);
		this.observed = new HashMap<String, List<String>>();

		if (observed != null && !observed.isEmpty()) {
			Iterator<String> it = observed.iterator();
			while (it.hasNext()) {
				String obs = it.next();
				String[] obsEls = UriUtils.getUriElements(obs);
				int length = obsEls == null ? 0 : obsEls.length;

				String attribute = null;
				switch (length) {
				case 0:
				case 1:
					continue;
				case 2:
					attribute = DataResource.VALUE;
					break;
				case 3:
					attribute = obsEls[2];
					break;
				default:
					continue;
				}
				String key = new StringBuilder().append(obsEls[0]).append(".").append(obsEls[1]).toString();
				List<String> list = this.observed.get(key);
				if (list == null) {
					list = new ArrayList<String>();
					this.observed.put(key, list);
				}
				if (!list.contains(attribute)) 
					list.add(attribute);
			}
		}		
		Arrays.asList(ModelInstance.LOCATION_PROPERTY, 
			ModelInstance.ICON_PROPERTY, 
			ModelInstance.FRIENDLY_NAME_PROPERTY, 
			ModelInstance.BRIDGE_PROPERTY
			).stream().forEach( p -> {
				List<String> list = ModelInstanceRegistration.this.observed.get(p);
				if (list == null) {
					list = new ArrayList<String>();
					ModelInstanceRegistration.this.observed.put(p, list);
				}
				if (!list.contains(DataResource.VALUE)) 
					list.add(DataResource.VALUE);
			}
		);
		
		this.instanceRegistration = registration;
		this.configuration = configuration;
		this.registered = true;
	}

	/**
	 * Unregisters the {@link ModelInstance} service registration of this
	 * ModelInstanceRegistration
	 */
	public void unregister() {
		this.registered = false;
		if (this.instanceRegistration != null) {
			this.instanceRegistration.unregister();
		}
	}

	void update(final Dictionary<String, Object> properties) {
		if (!registered || properties == null || properties.size() == 0 || this.instanceRegistration == null) 
			return;
		synchronized (this.instanceRegistration) {
			AccessController.<Void>doPrivileged(new PrivilegedAction<Void>() {
				@Override
				public Void run() {
					try {
						ModelInstanceRegistration.this.instanceRegistration.setProperties(properties);

					} catch (IllegalArgumentException e) {
						// if it is a duplicate service property
						// try to retrieve it and to remove it
						String message = e.getMessage();
						String duplicateMessage = "Duplicate service property: ";
						String duplicateProperty = null;
						
						if (message.startsWith(duplicateMessage)) 
							duplicateProperty = message.substring(duplicateMessage.length());
						
						if (duplicateProperty != null && properties.remove(duplicateProperty) != null) 
							update(properties);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			});
		}
	}

	private Dictionary<String, Object> properties() {
		final Hashtable<String, Object> properties = new Hashtable<String, Object>();

		if (this.instanceRegistration == null) {
			return properties;
		}
		synchronized (this.instanceRegistration) {
			AccessController.<Void>doPrivileged(new PrivilegedAction<Void>() {
				@Override
				public Void run() {
					ServiceReference<?> ref = null;
					if ((ref = ModelInstanceRegistration.this.instanceRegistration.getReference()) != null) {
						String[] keys = ref.getPropertyKeys();
						for (String key : keys) {
							properties.put(key, ref.getProperty(key));
						}
					}
					return null;
				}
			});
		}
		return properties;
	}

	protected void updateLifecycle(LifecycleStatus status) {
		if (!registered) {
			return;
		}
		Dictionary<String, Object> properties = properties();
		properties.put("lifecycle.status", status.name());
		this.update(properties);
	}

	private final void updateObserved(String observed, Object value) {
		if (!registered || observed == null) 
			return;
		Dictionary<String, Object> properties = properties();
		properties.remove(observed);
		if (observed.equals(ModelInstance.LOCATION_PROPERTY.concat(".value"))) {
			properties.remove("latitude");
			properties.remove("longitude");
			try {
					Point p = GeoJsonUtils.getFirstPointFromGeoJsonPoint(String.valueOf(value));
					properties.put("latitude", p.latitude);
					properties.put("longitude", p.longitude);					
				} catch (Exception e) {
					LOG.debug(e.getMessage());
				}
		}
		if (value != null) {
			properties.put(observed, value);
			this.update(properties);
		}
	}

	/**
	 */
	public void updateContent(SnaLifecycleMessage.Lifecycle lifecycle, String uri, JSONObject initial, String type) {
		if (!registered) {
			return;
		}
		String[] uriElements = UriUtils.getUriElements(uri);
		int length = uriElements == null ? 0 : uriElements.length;
		String service = (length > 1) ? uriElements[1] : null;
		if (service == null) {
			return;
		}
		MutableAccessNode root = this.configuration.getAccessTree().getRoot();
		MutableAccessNode node = (MutableAccessNode) root.get(uri);
		if (node == null) 
			node = root;
		
		String resource = (length > 2) ? uriElements[2] : null;
		boolean added = !lifecycle.equals(Lifecycle.RESOURCE_DISAPPEARING)
				&& !lifecycle.equals(Lifecycle.SERVICE_DISAPPEARING);

		Dictionary<String, Object> properties = properties();

		if (resource != null) {
			if (added) {
				updateResourceAppearing(service, resource, type, initial, node, properties);
			} else {
				updateResourceDisappearing(service, resource, node, properties);
			}
		} else {
			updateService(service, node, added, properties);
		}
		this.update(properties);
	}

	private final void updateResourceAppearing(String service, String resource, String type, JSONObject initial,
			MutableAccessNode node, Dictionary<String, Object> properties) {
		if (!registered || service == null || resource == null) {
			return;
		}
		AccessMethod.Type[] accessMethodTypes = AccessMethod.Type.values();
		int typesLength = accessMethodTypes == null ? 0 : accessMethodTypes.length;

		String serviceKey = service.concat(".resources");
		String resourceKey = new StringBuilder().append(service).append(".").append(resource).toString();

		List<String> resources = (List<String>) properties.get(serviceKey);
		if (resources == null) {
			resources = new ArrayList<String>();
			properties.put(serviceKey, resources);
		}
		List<String> attributes = this.observed.get(resourceKey);
		if (attributes != null && !attributes.isEmpty()) {
			Iterator<String> it = attributes.iterator();
			String name = initial == null ? null : initial.optString(Resource.NAME);

			while (it.hasNext()) {
				Object value = null;
				String attribute = it.next();

				if (attribute.equals(name) || (attribute.equals(DataResource.VALUE) && resource.equals(name))) 
					value = initial.opt(DataResource.VALUE);
				
				if (ModelInstance.LOCATION_PROPERTY.equals(resourceKey)) {
					try {
							Point p = GeoJsonUtils.getFirstPointFromGeoJsonPoint(String.valueOf(value));
							properties.put("latitude", p.latitude);
							properties.put("longitude", p.longitude);					
						} catch (Exception e) {
							LOG.debug(e.getMessage());
						}
				}
				if (value != null) {
					properties.put(new StringBuilder(
						).append(resourceKey
						).append("."
						).append(attribute
						).toString(), value);
				}
			}
		}
		resources.add(resource);
		properties.put(resourceKey.concat(".type"), type);

		int index = 0;
		for (; index < typesLength; index++) {
			AccessLevelOption accessLevelOption = node.getAccessLevelOption(accessMethodTypes[index]);

			properties.put(new StringBuilder().append(resourceKey).append(".").append(accessMethodTypes[index].name())
					.toString(), accessLevelOption.getAccessLevel().getLevel());
		}
	}

	private final void updateResourceDisappearing(String service, String resource, MutableAccessNode node,
			Dictionary<String, Object> properties) {
		if (!registered || service == null || resource == null) {
			return;
		}
		AccessMethod.Type[] accessMethodTypes = AccessMethod.Type.values();
		int typesLength = accessMethodTypes == null ? 0 : accessMethodTypes.length;

		String serviceKey = service.concat(".resources");
		String resourceKey = new StringBuilder().append(service).append(".").append(resource).toString();

		List<String> resources = (List<String>) properties.get(serviceKey);
		if (resources != null) 
			resources.remove(resource);
		
		properties.remove(resourceKey.concat(".type"));
		int index = 0;
		for (; index < typesLength; index++) {
			properties.remove(new StringBuilder().append(resourceKey).append(".")
					.append(accessMethodTypes[index].name()).toString());
		}
	}

	private final void updateService(String service, MutableAccessNode node, boolean added,
			Dictionary<String, Object> properties) {
		if (!registered || service == null) {
			return;
		}
		List<String> services = (List<String>) properties.get("services");
		if (services == null) {
			services = new ArrayList<String>();
		}
		if (added) {
			AccessMethod.Type[] accessMethodTypes = AccessMethod.Type.values();
			int typesLength = accessMethodTypes == null ? 0 : accessMethodTypes.length;

			int index = 0;
			for (; index < typesLength; index++) {
				AccessLevelOption accessLevelOption = node.getAccessLevelOption(accessMethodTypes[index]);

				properties.put(new StringBuilder().append(service).append(".").append(accessMethodTypes[index].name())
						.toString(), accessLevelOption.getAccessLevel().getLevel());
			}
			services.add(service);
		} else {
			services.remove(service);
			List<String> tobeRemoved = new ArrayList<>();
			Enumeration<String> enumeration = properties.keys();
			while (enumeration.hasMoreElements()) {
				String key = enumeration.nextElement();
				if (key != null && key.startsWith(service.concat("."))) {
					tobeRemoved.add(key);
				}
			}
			Iterator<String> iterator = tobeRemoved.iterator();
			while (iterator.hasNext()) {
				properties.remove(iterator.next());
			}
		}
		properties.put("services", services);
	}

	@Override
	public synchronized void doCallback(SnaMessage<?> message) {
		String uri = message.getPath();
		String[] uriElements = UriUtils.getUriElements(uri);
		switch (((SnaMessageSubType) message.getType()).getSnaMessageType()) {
		case UPDATE:
			SnaUpdateMessage m = (SnaUpdateMessage) message;
			JSONObject notification = m.getNotification();
			String key = new StringBuilder().append(uriElements[1]).append(".").append(uriElements[2]).toString();
			switch(m.getType()) {
				case ATTRIBUTE_VALUE_UPDATED:
					List<String> obs = this.observed.get(key);
					if (obs != null && !obs.isEmpty() && obs.contains(uriElements[3])) {
						Object value = notification.opt(DataResource.VALUE);
						this.updateObserved(new StringBuilder().append(key).append("."
							).append(uriElements[3]).toString(), value);
					}
					break;
				case METADATA_VALUE_UPDATED:
					Object value = notification.opt(DataResource.VALUE);				
					this.updateObserved(new StringBuilder().append(key).append("."
						).append(uriElements[3]).append(".").append(uriElements[4]).toString(), value);					
					break;
				case ACTUATED: 
					break;
				}
			break;
		case LIFECYCLE:
			SnaLifecycleMessage l = (SnaLifecycleMessage) message;
			String type = null;
			JSONObject initial = null;
			switch (l.getType()) {
				case RESOURCE_APPEARING:
					key = new StringBuilder().append(uriElements[1]).append(".").append(uriElements[2]).toString();
					initial = (JSONObject) ((SnaLifecycleMessageImpl) l).get("initial");
									
					type = ((SnaLifecycleMessageImpl) l).getNotification().optString("type");
					ResourceConfig config = configuration.getResourceConfig(new ResourceDescriptor(
							).withResourceName(uriElements[2]
							).withServiceName(uriElements[1]));
					List<String> observeds = null;
					if(config!=null)
						observeds = config.getObserveds(uriElements[1]);
					if (observeds != null && !observeds.isEmpty()) {
						Iterator<String> it = observeds.iterator();
						while (it.hasNext()) {			
							String attr = null;
							String s = it.next();
							String[] obsEls = UriUtils.getUriElements(s);
							int length = obsEls == null ? 0 : obsEls.length;
							switch (length) {
							case 0:
							case 1:
								continue;
							case 2:
								attr = DataResource.VALUE;
								break;
							case 3:
								attr = obsEls[2];
								break;
							default:
								continue;
							}
							String observedKey = new StringBuilder().append(obsEls[0]).append(".").append(obsEls[1]).toString();
							List<String> list = this.observed.get(observedKey);
							if (list == null) {
								list = new ArrayList<>();
								this.observed.put(observedKey, list);
							}
							if (!list.contains(attr)) 
								list.add(attr);
						}
					}		
					String modifiable = initial==null?null:String.valueOf(initial.opt(Metadata.MODIFIABLE));
					if(modifiable!=null) 
					    this.updateObserved(new StringBuilder().append(key).append("."
							).append(DataResource.VALUE).append(".").append(Metadata.MODIFIABLE).toString(),
					    		modifiable);			
				case SERVICE_APPEARING:
				case PROVIDER_DISAPPEARING:
				case RESOURCE_DISAPPEARING:
				case SERVICE_DISAPPEARING:
					this.updateContent(l.getType(), uri, initial, type);
				case PROVIDER_APPEARING:
				default:
					break;
			}
			break;
		case ERROR:
		case REMOTE:
		case RESPONSE:
		default:
			break;
		}
	}
}