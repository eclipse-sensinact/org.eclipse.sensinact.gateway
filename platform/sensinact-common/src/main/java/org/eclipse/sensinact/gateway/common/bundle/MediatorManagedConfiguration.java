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
package org.eclipse.sensinact.gateway.common.bundle;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

class MediatorManagedConfiguration implements ManagedService
{
	//********************************************************************//
	//						NESTED DECLARATIONS			  			      //
	//********************************************************************//
	
	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//

	//********************************************************************//
	//						STATIC DECLARATIONS							  //
	//********************************************************************//
	public static final String MANAGED_SENSINACT_MODULE = "org.eclipse.sensinact.gateway.managed";
	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//
	
	private Mediator mediator;
	private ServiceRegistration<ManagedService> registration;
	private List<ManagedConfigurationListener> listeners;
	private String pid;

	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing to interact
	 * with the OSGi host environment
	 * @param pid the string identifier of the {@link ManagedService}
	 * to be instantiated
	 */
	MediatorManagedConfiguration(Mediator mediator,String pid)
	{
		this.pid = pid;
		this.mediator = mediator;
		this.listeners = new ArrayList<ManagedConfigurationListener>();
	}

	/**
	 * Adds a {@link ManagedConfigurationListener} to be notified
	 * when the properties of this {@link ManagedService} are
	 * updated
	 * 
	 * @param listener the {@link ManagedConfigurationListener} to add
	 */
	public void addListener(ManagedConfigurationListener listener)
	{
		if(listener != null)
		{
			synchronized(this.listeners)
			{
				this.listeners.add(listener);
			}
		}
	}

	/**
	 * Removes the {@link ManagedConfigurationListener} to be removed from
	 * the list of those to be notified when the properties of this {@link 
	 * ManagedService} are updated
	 * 
	 * @param listener the {@link ManagedConfigurationListener} to remove
	 */
	public void deleteListener(ManagedConfigurationListener listener)
	{
		if(listener != null)
		{
			synchronized(this.listeners)
			{
				this.listeners.remove(listener);
			}
		}
	}
	
	/**
	 * Returns the default configuration properties set
	 * 
	 * @return the default set of configuration properties
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Hashtable getDefaults()
	{
		Hashtable defaults = new Hashtable();
	    defaults.put(Constants.SERVICE_PID, pid);
		return defaults;
	}
	
	/**
	 * Registers this MediatorManagedService in the OSGi
	 * host environment
	 */
	@SuppressWarnings("unchecked")
	public void register()
	{	       
		this.registration = mediator.getContext(
		).<ManagedService>registerService(
		ManagedService.class, this, getDefaults());
	}

	/**
	 * Unregisters this MediatorManagedService from the OSGi
	 * host environment
	 */
	public void unregister()
	{
		try
		{
			this.registration.unregister();
			
		}catch(IllegalStateException e)
		{
			this.mediator.error(e.getMessage());
		}
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.osgi.service.cm.ManagedService#
	 * updated(java.util.Dictionary)
	 */
	@Override
	public void updated(Dictionary<String, ?> properties)
	        throws ConfigurationException
	{
		Dictionary<String,?> props = properties==null
		?new Hashtable<String,Object>():properties;	

		if(registration == null)
		{
			return;
		}
		try
		{
			if(properties != null)
			{
				registration.setProperties(properties);
				
			} else
			{
				registration.setProperties(getDefaults());
			}
			synchronized(this.listeners)
			{
				Iterator<ManagedConfigurationListener> iterator =
						this.listeners.iterator();
				
				while(iterator.hasNext())
				{
					ManagedConfigurationListener listener = 
							iterator.next();
					listener.updated(properties);
				}
			}
		} catch(Exception e)
		{
			throw new ConfigurationException(null, e.getMessage(), e);
		}
	}
}