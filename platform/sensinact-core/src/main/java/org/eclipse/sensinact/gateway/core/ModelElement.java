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
package org.eclipse.sensinact.gateway.core;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.Elements;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.Name;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.common.primitive.ProcessableData;
import org.eclipse.sensinact.gateway.core.message.SnaConstants;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage.Lifecycle;
import org.eclipse.sensinact.gateway.core.message.SnaNotificationMessageImpl;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.security.AccessLevel;
import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.AccessNode;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;
import org.eclipse.sensinact.gateway.core.security.SessionKey;
import org.json.JSONObject;

/**
 * Abstract sensiNact resource model element (service provider, service 
 * & resource) implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class ModelElement<I extends ModelInstance<?>, 
P extends ProcessableData, E extends Nameable, R extends Nameable> 
extends Elements<E> implements SensiNactResourceModelElement<R>
{		
	/**
	 * Returns the {@link Lifecycle} event associated
	 * to the registration of this ModelElement 
	 * 
	 * @return 
	 * 		the {@link Lifecycle} event for this
	 * 		ModelElement registration
	 */
	protected abstract Lifecycle getRegisteredEvent();
	
	/**
	 * Returns the {@link Lifecycle} event associated
	 * to the unregistration of this ModelElement 
	 * 
	 * @return 
	 * 		the {@link Lifecycle} event for this
	 * 		ModelElement unregistration
	 */
	protected abstract Lifecycle getUnregisteredEvent();
	
	/**
	 * Returns the interface type implemented by a proxy
	 * instance of this AbstractModelElement
	 * 
	 * @return the interface type of a proxy instance of 
	 * this AbstractModelElement
	 */
	protected abstract Class<?> getProxyType();

	/** 
     * Processes the {@link ProcessableData} passed as 
     * parameter
     *  
     * @param data the {@link ProcessableData} to process
     */
	public abstract void process(P data);
	
	/**
	 * Creates and returns an  element of an {@link 
	 * ModelElementProxy} of this ModelElement for the 
	 * element of this collection  passed as parameter
	 * 
	 * @param accessLevelOption
	 * 		the long unique identifier for which to create the
	 * 		proxy instance of the specified element
	 * @param element
	 * 		the element for which to create the proxy counterpart
	 * @return
	 * 		the proxy for the specified element and the specified 
	 * 		user
	 * 
	 * @throws ModelElementProxyBuildException 
	 */
	protected abstract R getElementProxy(
			AccessLevelOption accessLevelOption,
			E element) throws ModelElementProxyBuildException;
	
	/**
	 * this ModelElement's parent 
	 */
	protected final ModelElement<I,?,?,?> parent;
	
	/**
	 * the {@link SensiNactResourceModel} to which this 
	 * extended {@link ModelElement} belongs to
	 */
	protected I modelInstance;
	
	/**
	 * the proxies of this AbstractResourceModelElement mapped to 
	 * the profile identifiers for which they have been created
	 */
	protected Map<String, AccessLevelOption> sessions;

	/**
	 * the proxies of this AbstractResourceModelElement mapped to 
	 * the {@link AccessLevel} for which they have been created
	 */
	protected EnumMap<AccessLevelOption, ElementsProxy<R>> proxies; 
	
	/**
	 * Started status of this AbstractModelElement
	 */
	protected AtomicBoolean started;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * 		the {@link Mediator} that will allow the {@link 
	 * 		ModelElementOld} to instantiate to interact
	 * 		with the OSGi host environment
	 * @param uri
	 * 		the string uri path of the {@link ModelElementProxy}
	 * 		to instantiate
	 */
	protected ModelElement(I modelInstance, 
		ModelElement<I,?,?,?> parent, String uri) 
	{
		super(uri);
		if(parent != null && parent.getModelInstance() != modelInstance)
		{
			throw new RuntimeException("Inconsistent hierarchy");
		}
		this.parent = parent;
		this.modelInstance = modelInstance;
		this.started = new AtomicBoolean(false);
		this.sessions = new HashMap<String, AccessLevelOption>();
		this.proxies = new EnumMap<AccessLevelOption, ElementsProxy<R>>(
				AccessLevelOption.class);
	}
    
    /**
     * Returns the {@link SensiNactResourceModel} to which 
     * this extended {@link ModelElement} belongs to
     * 
     * @return
     * 		the {@link SensiNactResourceModel} to which 
     * 		this extended {@link ModelElement} belongs 
     * 		to
     */
    public I getModelInstance()
    {
    	return this.modelInstance;
    }
    
    /**
     * Creates and returns a proxy of this AbstractModelElement
     * 
     * @param uid
     * 		the long unique identifier of the user for who to 
     * 		create the appropriate proxy
     * @return
     * 		a new proxy object based on this ModelElement
     * 
     * @throws ModelElementProxyBuildException 
     */
	public <S extends  ElementsProxy<R>> S getProxy(final String publicKey) 
    		throws ModelElementProxyBuildException 
    {
    	if(!this.started.get())
    	{
    		throw new ModelElementProxyBuildException(
    				"this model element must be started first");
    	}
    	AccessLevelOption accessLevelOption = this.sessions.get(publicKey);
    	if(accessLevelOption == null)
    	{
    		accessLevelOption = 
    			this.modelInstance.getAccessLevelOption(this, publicKey);
    		this.sessions.put(publicKey, accessLevelOption);
    	}
    	return this.getProxy(accessLevelOption);
    }

    /**
     * Creates and returns a proxy of this AbstractModelElement
     * 
     * @param uid
     * 		the long unique identifier of the user for who to 
     * 		create the appropriate proxy
     * @return
     * 		a new proxy object based on this ModelElement
     * 
     * @throws ModelElementProxyBuildException 
     */
	public <S extends  ElementsProxy<R>> S getProxy(SessionKey key) 
    		throws ModelElementProxyBuildException 
    {
    	if(!this.started.get())
    	{
    		throw new ModelElementProxyBuildException(
    				"this model element must be started first");
    	}
    	AccessTree<? extends AccessNode> accessTree = key.getAccessTree();
    	if(accessTree == null || accessTree.getRoot()==null)
    	{
    		throw new ModelElementProxyBuildException(
    				"A valid access tree was expected");
    	}
		AccessNode node = accessTree.getRoot().get(super.getPath());
		//AccessLevelOption will be the same for all methods, just check
		//what is the one associated with the DESCRIBE one
		AccessLevelOption accessLevelOption = 
			node.getAccessLevelOption(AccessMethod.Type.valueOf(
					AccessMethod.DESCRIBE));		
    	return this.getProxy(accessLevelOption);
    }

    /**
     * Creates and returns a proxy of this AbstractModelElement
     * 
     * @param uid
     * 		the long unique identifier of the user for who to 
     * 		create the appropriate proxy
     * @return
     * 		a new proxy object based on this ModelElement
     * 
     * @throws ModelElementProxyBuildException 
     */
	@SuppressWarnings("unchecked")
	protected final <S extends  ElementsProxy<R>> S getProxy(
			final AccessLevelOption accessLevelOption) 
    		throws ModelElementProxyBuildException 
    {
    	if(!this.started.get())
    	{
    		throw new ModelElementProxyBuildException(
    				"this model element must be started first");
    	}
    	if(accessLevelOption == null)
    	{
    		throw new ModelElementProxyBuildException(
    				"Access level option expected");
    	}
		Class<?> proxied = this.getProxyType();
    	ElementsProxy<R> proxy = this.proxies.get(accessLevelOption);
    	
    	if(proxy == null)
    	{	   
	    	List<MethodAccessibility> methodAccessibilities = 
	    			this.modelInstance.getAuthorizations(
	    			this, accessLevelOption);
	    	
	    	int index = -1;
	    	
	    	//if the describe method does not exists it means 
	    	//that the user is not allowed to access this ModelElement
	    	if((index = methodAccessibilities.indexOf(new Name<
	    		MethodAccessibility>(AccessMethod.DESCRIBE)))==-1 || 
	    		!methodAccessibilities.get(index).isAccessible())
	    	{
	    		proxy = new UnaccessibleModelElement<R>(
	    			this.modelInstance.mediator(), proxied, 
	    			this.getPath());
	    		
	    	} else
	    	{
		    	//instantiate the proxy element
	        	final List<R> proxies = new ArrayList<R>(); 
	        	try
	        	{
		        	forEach(new Executable<E, Void>()
		        	{
						@Override
						public Void execute(E element) throws Exception
						{
							R proxy = ModelElement.this.getElementProxy(
									accessLevelOption, element);							
							if(proxy != null)
							{
								proxies.add(proxy); 
							}
							return null;
						}
		        	});
	        	} catch(Exception e)
	        	{
	        		throw new ModelElementProxyBuildException(e);
	        	}
	        	proxy = this.getProxy(methodAccessibilities, proxies);	
	        	if(proxy == null)
	        	{
	        		return null;
	        	}
	        	this.proxies.put(accessLevelOption, proxy);
	    	}
    	}
        //creates the java proxy based on the created ModelElementProxy 
        //and returns it
        return  (S) Proxy.newProxyInstance(ModelElement.class.getClassLoader(),
        		new Class<?>[]{ proxied }, proxy);
    }
	
    /**
	 * Passes on the invocation of an {@link AccessMethod}
	 * whose type is passed as parameter, for the specified
	 * resource and parameterized with the arguments array
	 *  
	 * @param type
	 * 		the type of the invoked method
	 * @param uri
	 * 		the String uri of the ModelElement
	 * 		targeted by the call
	 * @param parameters
	 * 		the objects array parameterizing the call
	 * @return
	 * 		the <code>&lt;TASK&gt;</code> typed result
	 * 		object of the execution chain 
	 * @throws Exception 
	 */
	protected <TASK> TASK passOn(String type, String uri, Object[] parameters) 
			throws Exception 
	{
		if(this.parent != null)
		{
			return this.parent.<TASK>passOn(type, uri, parameters);
		}
		return null;
	}
	
	/**
	 * @InheritedDoc
	 *
	 * @see Elements#
	 * addElement(Nameable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean addElement(final E element) 
	{
		if (!super.addElement(element))
		{
			return false;
		}			
		if(this.started.get() && 
				ModelElement.class.isAssignableFrom(
					element.getClass()))
		{
			((ModelElement<I, ?, ?, ?>)element).start();
		}
		synchronized (this.proxies)
		{	
			Iterator<Map.Entry<AccessLevelOption, ElementsProxy<R>>> 
			iterator = this.proxies.entrySet().iterator();
			
			while (iterator.hasNext())
			{
				final Map.Entry<AccessLevelOption, ElementsProxy<R>> 
				entry = iterator.next();
				try 
				{
					R provided = this.getElementProxy(
						entry.getKey(), element);
					
					if (provided != null)
					{
						entry.getValue().addElement(provided);
					}					
				} catch (Exception e) 
				{
					super.removeElement(element.getName());
					this.modelInstance.mediator().error(e);
					return false;
				}
			}	
		}
		return true;
	}
        
	/**
	 * @inheritDoc
	 *
	 * @see Elements#
	 * removeElement(java.lang.String)
	 */
    @SuppressWarnings("unchecked")
	@Override
	public E removeElement(String name)
	{
       E element = null;
	   if((element = super.removeElement(name))!=null)
	   {	
			if(this.started.get() && 
					ModelElement.class.isAssignableFrom(
					element.getClass()))
		   {
				((ModelElement<I, ?, ?, ?>)element).stop();
		   }
		   if(!this.proxies.isEmpty())
	       {    		
		    	synchronized(this.proxies)
		    	{
		    		Iterator<ElementsProxy<R>> iterator = 
		    				this.proxies.values().iterator();
		    		
		    		while(iterator.hasNext())
		    		{
		    			iterator.next().removeElement(name);
		    		}
		    	}
	       }
		   return element;   
	   }
	   return null;
	}
    
	/**
     * @inheritDoc
     *
     * @see org.eclipse.sensinact.gateway.core.ModelElementOld#
     * registered(org.eclipse.sensinact.gateway.core.model.message.SnaMessageHandler)
     */
	protected void start()
    {  	
    	if(!this.modelInstance.isRegistered())
    	{
    		this.modelInstance.mediator().error("%s not registered", 
    				this.modelInstance.getName());
    		return;
    	}    
    	if(this.started.get())
    	{
    		this.modelInstance.mediator().debug("%s already started", 
    				this.getName());
    		return;
    	}    	
    	this.started.set(true);
    	String path = super.getPath();
    	Lifecycle event = this.getRegisteredEvent();
    	
    	SnaLifecycleMessage notification = 
    	SnaNotificationMessageImpl.Builder.<SnaLifecycleMessage>notification(
    		this.modelInstance.mediator(), event , path);
    	
    	JSONObject notificationObject = new JSONObject();    	
    	notificationObject.put(SnaConstants.ADDED_OR_REMOVED, event.name());
    	notification.setNotification(notificationObject); 
    	
    	this.modelInstance.postMessage(notification);
    	try
		{
			forEach(new Executable<E,Void>()
			{
				@SuppressWarnings("unchecked")
				@Override
				public Void execute(E element) throws Exception
				{
					if(ModelElement.class.isAssignableFrom(
							element.getClass()))
					{
						((ModelElement<I, ?, ?, ?>)element).start();
					}
					return null;
				}});
		}
		catch (Exception e)
		{
			this.getModelInstance().mediator().error(e);
		}
    }
		
	/**
     * Stops this extended {@link ModelElementOld}
     */
    protected void stop()
    {   
    	if(!this.started.get())
    	{
    		this.modelInstance.mediator().debug("%s not started", 
    				this.getName());
    		return;
    	}    	
    	this.started.set(false);
    	this.proxies.clear();
    	try
		{
			forEach(new Executable<E,Void>()
			{
				@SuppressWarnings("unchecked")
				@Override
				public Void execute(E element) throws Exception
				{
					if(ModelElement.class.isAssignableFrom(
							element.getClass()))
					{
						((ModelElement<I, ?, ?, ?>)element).stop();
					}
					return null;
				}});
		}
		catch (Exception e)
		{
			this.getModelInstance().mediator().error(e);
		}
    	Lifecycle event = this.getUnregisteredEvent();
    	String path = super.getPath();
    	
    	SnaLifecycleMessage notification = SnaNotificationMessageImpl.Builder.<
    		SnaLifecycleMessage>notification(this.modelInstance.mediator(), 
    			event, path);
    	
    	JSONObject notificationObject = new JSONObject();
    	notificationObject.put(SnaConstants.ADDED_OR_REMOVED,event.name());    	
    	notification.setNotification(notificationObject);
    	
    	this.modelInstance.postMessage(notification); 
    }
}
