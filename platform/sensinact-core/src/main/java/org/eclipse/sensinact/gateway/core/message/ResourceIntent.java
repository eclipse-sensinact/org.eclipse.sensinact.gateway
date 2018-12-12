/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.message;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.core.remote.RemoteCore;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.eclipse.sensinact.gateway.util.stack.AbstractStackEngineHandler;
import org.osgi.framework.ServiceRegistration;

/**
 * A ResourceIntent provides a way to trigger a specific process when 
 * some {@link Resource}(s) become(s) available or unavailable
 */
public abstract class ResourceIntent extends AbstractStackEngineHandler<SnaMessage<?>>  implements LocalAgent,Nameable {
	
	private class Namespace implements Nameable {
		
		final String namespace;
		
		Namespace(String namespace){
			this.namespace = namespace;
		}

		@Override
		public String getName() {
			return this.namespace;
		}
		
		@Override
		public boolean equals(Object o) {
			if(String.class == o.getClass()) {
				return this.namespace.equals(o);
			}
			if(Nameable.class.isAssignableFrom(o.getClass())) {
				return this.namespace.equals(((Nameable)o).getName());
			}
			if(Namespace.class.isAssignableFrom(o.getClass())) {
				return this.namespace.equals(((Namespace)o).getName());
			}
			if(ResolvedPath.class.isAssignableFrom(o.getClass())) {
				return this.namespace.equals(((ResolvedPath)o).getNamespace().getName());
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return this.namespace.hashCode();
		}
	}  
	
	private final class ResolvedPath implements Nameable{
		
		final Namespace namespace;
		final String path;
		
		ResolvedPath(String path){
			String[] uriElements = UriUtils.getUriElements(path);
			String[] providerElements = uriElements[0].split(":");
			
			if(providerElements.length == 1) {
				this.namespace = new Namespace(LOCAL_NAMESPACE);
				this.path = path;
			} else {
				if(providerElements[0].equals(namespace())) {
					this.namespace = new Namespace(LOCAL_NAMESPACE);
					this.path = UriUtils.getUri(new String[]{providerElements[1],
						UriUtils.getUri(Arrays.copyOfRange(uriElements, 1, 
								uriElements.length))});
				} else {
					this.namespace = new Namespace(providerElements[0]);
					this.path = path;
				}
			}
		}

		@Override
		public String getName() {
			return this.path;
		}

		Namespace getNamespace() {
			return this.namespace;
		}
		
		@Override
		public boolean equals(Object o) {
			if(String.class == o.getClass()) {
				return this.path.equals(o);
			}
			if(Nameable.class.isAssignableFrom(o.getClass())) {
				return this.path.equals(((Nameable)o).getName());
			}
			if(Namespace.class.isAssignableFrom(o.getClass())) {
				return this.namespace.equals(o);
			}
			if(ResolvedPath.class.isAssignableFrom(o.getClass())) {
				return this.path.equals(((ResolvedPath)o).getName());
			}
			return false;
		}

		@Override
		public int hashCode() {
			return this.namespace.hashCode()+this.path.hashCode();
		}
	}
	
	/**
	 * Returns true if the {@link Resource} targeted by the
	 * String path passed as parameter exists and is 
	 * accessible to this ResourceIntent according to its 
	 * access rights. Returns false if the {@link Resource} does
	 * not exist or is not accessible to this ResourceIntent
	 * 
	 * @param path the String path of the {@link Resource} for
	 * which to define the accessibility status
	 * 
	 * @return 
	 * <ul>
	 * 		<li>true if the specified {@link Resource} exists and
	 * 			is accessible to this ResourceIntent</li>
	 * 		<li>false otherwise</li>
	 * </ul>
	 */
	public abstract boolean isAccessible(String path);
	
	/**
	 * Returns the String namespace of the sensiNact instance
	 * hosting this ResourceIntent

	 * @return the String namespace of the local sensiNact 
	 * instance
	 */
	public abstract String namespace();
	
	private static final String LOCAL_NAMESPACE = "#LOCAL#";
	private String publicKey;
	private String identifier;
	
	private ExecutorService executor;
	private SnaFilter filter;
	private Executable<Boolean,Void> onAccessible;

	private Map<ResolvedPath,Boolean> accessibility;
	private Map<String,String> filters;
	
	private Mediator mediator;
	private boolean accessible;
	private String commonPath;
	private ServiceRegistration<?> registration;
	
	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing the ResourceIntent
	 * to be instantiated to interact with the OSGi host environment
	 * @param publicKey the String public key allowing to define the access 
	 * rights of the ResourceIntent to be instantiated 
	 * @param onAccessible the {@link Executable} to be executed when the 
	 * availability status of the targeted {@link Resource} changes
	 * @param resourcePath the String path of the {@link Resource} to be
	 * observed
	 */
	public ResourceIntent(Mediator mediator, String publicKey, Executable<Boolean,Void> onAccessible, 
		String... resourcePath) {
		super();
		if(resourcePath == null ||resourcePath.length==0) {
			throw new NullPointerException("Nothing to observed");
		}
		this.mediator = mediator;
		this.accessibility = new HashMap<ResolvedPath,Boolean>();
		this.filters = new HashMap<String,String>();
		int length = resourcePath.length;
		boolean pattern = length > 1;
		boolean complement = false;
		
		StringBuilder filterBuilder = new StringBuilder();
		if(pattern) {
			filterBuilder.append("(");
		}
		ResolvedPath path = null;
		for(int index = 0; index <length; index++){
			path = new ResolvedPath(resourcePath[index]);
			this.accessibility.put(path, false);
			if(index > 0) {
				filterBuilder.append("|");
			}
			if(pattern) {
				filterBuilder.append("(");
			}
			filterBuilder.append(path.getName());
			if(pattern) {
				filterBuilder.append(")");
			}
			if(LOCAL_NAMESPACE.equals(path.getNamespace().getName())) {
				continue;
			}
			String fltr = this.filters.get(path.getNamespace().getName());
			if(fltr == null) {
				fltr = path.getName();
			} else if(fltr.endsWith("))")) {
				fltr = new StringBuilder().append(fltr.substring(0, fltr.length()-1)
					).append("(").append(path.getName()).append("))").toString();
			} else {
				fltr = new StringBuilder().append("(|(").append(fltr).append(")("
						).append(path.getName()).append("))").toString();
			}
			this.filters.put(path.getNamespace().getName(), fltr);
		}
		if(pattern) {
			filterBuilder.append(")");
		}
		if(this.accessibility.size()==1){
			this.commonPath = path.getName();
		} else {
			this.commonPath = UriUtils.PATH_SEPARATOR;
		} 
		this.filter = new SnaFilter(mediator, filterBuilder.toString(), pattern, complement);
		filter.addHandledType(SnaMessage.Type.LIFECYCLE);
		filter.addHandledType(SnaMessage.Type.REMOTE);
		
		this.identifier = new StringBuilder().append("INT").append(
		System.currentTimeMillis()).append(this.accessibility.hashCode()+this.hashCode()
			    ).toString();
		
		this.publicKey = publicKey;
		this.onAccessible = onAccessible;		
		executor = Executors.newCachedThreadPool();
	}
	
	public String getCommonPath() {
		return this.commonPath;
	}
	
	private void setAccessible() {
		boolean accessibleAll = true;		
		synchronized(this.accessibility) {
			Iterator<Boolean> iterator = this.accessibility.values().iterator();			
			while(iterator.hasNext()) {
				accessibleAll &= iterator.next().booleanValue();
			}
		}
		this.setAccessible(accessibleAll);
	}

	private synchronized void setAccessible (final boolean accessible) {
		if((this.accessible && accessible) || (!this.accessible && !accessible)) {
			return;
		}
		this.accessible = accessible;
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					ResourceIntent.this.onAccessible.execute(accessible);
				} catch (Exception e) {
					ResourceIntent.this.mediator.error(e);
				}
			}
		});
	}
	
	private boolean setAccessible(String path, boolean accessible) {
		synchronized(this.accessibility) {
			this.accessibility.put(new ResolvedPath(path), accessible);		
			if(!accessible){
				return false;
			}
			Iterator<Boolean> iterator = this.accessibility.values().iterator();
			while(iterator.hasNext()) {
				if(!iterator.next().booleanValue()) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.MessageRegisterer#register(org.eclipse.sensinact.gateway.core.message.SnaMessage)
	 */
	@Override
	public void register(final SnaMessage<?> message) {
		super.eventEngine.push(message);
	}
	
	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.util.stack.StackEngineHandler#doHandle(java.lang.Object)
	 */
	@Override
	public void doHandle(final SnaMessage<?> message) {
		switch(((SnaMessageSubType)message.getType()).getSnaMessageType()){		
			case LIFECYCLE:
				if(!this.filter.matches(message)) {
					return;
				}
				switch(((SnaLifecycleMessage.Lifecycle)message.getType())) {				
					case RESOURCE_APPEARING:
							setAccessible(setAccessible(message.getPath(),true));
						break;
					case RESOURCE_DISAPPEARING:
							setAccessible(setAccessible(message.getPath(),false));
						break;
					case SERVICE_APPEARING:
					case SERVICE_DISAPPEARING:
					case PROVIDER_APPEARING:	
					case PROVIDER_DISAPPEARING:
					default:
						break;
				}
				break;
			case REMOTE:
				if(this.filters.isEmpty()) {
					return;
				}
				String namespace = ((SnaNotificationMessageImpl<?>)message
					).getNotification(String.class, SnaConstants.NAMESPACE);
				
				if(!this.filters.containsKey(namespace)) {
					return;
				}
				ResolvedPath[] paths = null;
				synchronized(this.accessibility) {
					paths = this.accessibility.keySet().toArray(new ResolvedPath[this.filters.size()]);					
				}
				for(ResolvedPath path:paths) {						
					if(!namespace.equals(path.getNamespace().getName())) {
						continue;
					}
					switch(((SnaRemoteMessage.Remote)message.getType())) {
						case CONNECTED:
							setAccessible(path.getName(), this.isAccessible(path.getName()));
							break;
						case DISCONNECTED:
							setAccessible(path.getName(), false);
							break;
						default:
							break;
					}
				}
				setAccessible();
				break;
			case RESPONSE:
			case UPDATE:
			case ERROR:
			default:
				break;
		}
	}
	
	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.common.primitive.Nameable#getName()
	 */
	@Override
	public String getName() {
		return this.identifier;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.message.SnaAgent#getPublicKey()
	 */
	public String getPublicKey() {
		return this.publicKey;
	}

	/**
	 * Registers this ResourceIntent into the appropriate {@link RemoteCore}
	 */
	protected void registerRemote() {		
		if(this.filters.size() == 0) {
			return;
		}
		this.mediator.callServices(RemoteCore.class, new Executable<RemoteCore, Void>() {
			@Override
			public Void execute(RemoteCore remoteCore) throws Exception {
				ResourceIntent.this.registerRemote(remoteCore);
				return null;
			}
		});
	}

	/**
	 * Registers this ResourceIntent into the {@link RemoteCore} passed as parameter
	 * if its namespace if the one of the targeted {@link Resource}
	 * 
	 * @param remoteCore the {@link RemoteCore} into which register this ResourceIntent
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.LocalAgent#registerRemote(org.eclipse.sensinact.gateway.core.remote.RemoteCore)
	 */
	public void registerRemote(RemoteCore remoteCore) {		
		if (remoteCore != null && this.filters.size()>0 && this.filters.containsKey(remoteCore.endpoint().namespace())) {		
			String sender = this.filters.get(remoteCore.endpoint().namespace());
			boolean pattern = sender.endsWith("))");
			boolean complement = false;
			SnaFilter filter = new SnaFilter(mediator,sender,pattern,complement);
			filter.addHandledType(SnaMessage.Type.LIFECYCLE);
			remoteCore.endpoint().registerAgent(identifier,filter , publicKey);
		}
	}
	
	/**
	 * Starts this ResourceIntent, registers it into the registry of the OSGi host
	 * environment, and in the appropriate {@link RemoteCore} if it is registered
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.SnaAgent#start()
	 */
	public void start() {
		Dictionary properties = new Hashtable();
		properties.put("org.eclipse.sensinact.gateway.agent.id", this.identifier); 
		try {
				this.registration = this.mediator.getContext().registerService(
				new String[] {SnaAgent.class.getName(),LocalAgent.class.getName()},
				this, properties);
				registerRemote();
		} catch (IllegalStateException e) {
			this.mediator.error("The agent is not registered ", e);
		} catch(Exception e) {
			e.printStackTrace();
		}
		synchronized(this.accessibility) {
			Iterator<Map.Entry<ResolvedPath,Boolean>> iterator = this.accessibility.entrySet().iterator();			
			while(iterator.hasNext()) {
				Map.Entry<ResolvedPath,Boolean> entry = iterator.next();
				boolean acc = this.isAccessible(entry.getKey().getName());
				entry.setValue(acc);
			}
		}
		setAccessible();
	}
 	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.message.SnaAgent#stop()
	 */
	@Override
	public void stop() {
		super.stop();
		if(this.filters.size() > 0) {
			StringBuilder filterBuilder = new StringBuilder();
			Iterator<String> iterator = this.filters.keySet().iterator();
			while(iterator.hasNext()) {
				filterBuilder.append("(namespace=").append(iterator.next()).append(")");
			}
			if(this.filters.size() > 1) {
				filterBuilder.insert(0,"(|");
				filterBuilder.append(")");
			}
			this.mediator.callService(RemoteCore.class, filterBuilder.toString(),
				new Executable<RemoteCore, Void>() {
				@Override
				public Void execute(RemoteCore remoteCore) throws Exception {
					remoteCore.endpoint().unregisterAgent(ResourceIntent.this.identifier);
					return null;
				}
			});
		}
		if (this.registration != null) {
			try {
				this.registration.unregister();
				this.registration = null;
			} catch (IllegalStateException e) {
				this.mediator.error(e);
			}
		}
	}
}
