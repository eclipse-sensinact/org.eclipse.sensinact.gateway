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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/** 
 * Implementation of a {@link ServiceTrackerCustomizer} allowing to associate the execution of {@link 
 * Executable}s to the appearance, the disappearance, and the modification of &lt;S&gt; typed services
 * 
 * @param <S> the type of the tracked services
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
final class TrackerCustomizer<S> implements ServiceTrackerCustomizer<S,S>
{
	private static <E> void execute(Collection<List<Executable<E,Void>>> executables, E instance)
	{
		if(executables.isEmpty()) 
		{
			return;
		}
		Iterator<List<Executable<E,Void>>> iterator = executables.iterator();
		while(iterator.hasNext()) 
		{
			List<Executable<E,Void>> execs = iterator.next();
			if(execs.isEmpty()) 
			{
				continue;
			}
			int index = 0;
			int length = execs == null?0:execs.size();
			
			for(;index < length; index++)
			{
				try
				{
					execs.get(index).execute(instance);
					
				} catch(Exception e)
				{
					e.printStackTrace();
					continue;
				}
			}
		}		
	}
	
	private static final <E> String add(String prefix, 
			Map<String, List<Executable<E,Void>>> map, 
			List<Executable<E,Void>> executables ) 
	{
		if(executables == null|| executables.isEmpty())
		{
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(prefix);
		builder.append("_");
		builder.append(executables.hashCode());
		builder.append(System.currentTimeMillis());
		String key= builder.toString();
		map.put(key, executables);
		return key;
	}
	
	private Mediator mediator;
	
	private Map<String, List<Executable<S,Void>>> addingExecutables;
	private Map<String, List<Executable<S,Void>>> modifyingExecutables;
	private Map<String, List<Executable<S,Void>>> removingExecutables;

	/**
	 * Constructor 
	 * 
	 * @param mediator the {@link Mediator} allowing to interact with
	 * the OSGi host environment
	 */
	TrackerCustomizer(Mediator mediator)
	{
		this.mediator = mediator;
		this.addingExecutables = new HashMap<String,List<Executable<S,Void>>>();
		this.modifyingExecutables = new HashMap<String,List<Executable<S,Void>>>();
		this.removingExecutables = new HashMap<String,List<Executable<S,Void>>>();
	}
	
	/**
	 * Links the event of a &lt;S&gt; typed service appearance to the execution of 
	 * the specified {@link Executable} passed as parameter
	 * 
	 * @param executable the {@link Executable} to be executed when a service of the
	 * appropriate type appears
	 * 
	 * @return the String identifier of the created link
	 */
	public String attachOnAdding(Executable<S,Void> executable)
	{
		return this.attachOnAdding(Collections.singletonList(executable));
	}
	
	/**
	 * Links the event of a &lt;S&gt; typed service appearance to the execution of 
	 * the specified list of {@link Executable}s passed as parameter
	 * 
	 * @param executables the list of {@link Executable}s to be executed when a service
	 * of the appropriate type appears
	 * 
	 * @return the String identifier of the created link
	 */
	public String attachOnAdding(List<Executable<S,Void>> executables)
	{
		synchronized(this.addingExecutables)
		{
			return add("onAdding", this.addingExecutables, executables);
		}
	}
	
	/**
	 * Unlinks the event of a &lt;S&gt; typed service appearance from the execution of 
	 * the list of {@link Executable}s identified by the String key passed as parameter
	 * 
	 * @param key the String identifier of the list of {@link Executable}s to be unregistered
	 */
	public void detachOnAdding(String key)
	{
		synchronized(this.addingExecutables)
		{
			this.addingExecutables.remove(key);
		}
	}

	/**
	 * Links the event of a &lt;S&gt; typed service modification to the execution of 
	 * the specified {@link Executable} passed as parameter
	 * 
	 * @param executables the {@link Executable} to be executed when a service
	 * of the appropriate type is modified
	 * 
	 * @return the String identifier of the created link
	 */
	public String attachOnModifying(Executable<S,Void> executable)
	{
		return this.attachOnModifying(Collections.singletonList(executable));
	}

	/**
	 * Links the event of a &lt;S&gt; typed service modification to the execution of 
	 * the specified list of {@link Executable}s passed as parameter
	 * 
	 * @param executables the list of {@link Executable}s to be executed when a service
	 * of the appropriate type is modified
	 * 
	 * @return the String identifier of the created link
	 */
	public String attachOnModifying(List<Executable<S,Void>> executables)
	{
		synchronized(this.modifyingExecutables)
		{
			return add("onModifying", this.modifyingExecutables, executables);
		}
	}

	/**
	 * Unlinks the event of a &lt;S&gt; typed service modification from the execution of 
	 * the list of {@link Executable}s identified by the String key passed as parameter
	 * 
	 * @param key the String identifier of the list of {@link Executable}s to be unregistered
	 */
	public void detachOnModifying(String key)
	{
		synchronized(this.modifyingExecutables)
		{
			this.modifyingExecutables.remove(key);
		}
	}
	
	/**
	 * Links the event of a &lt;S&gt; typed service disappearance to the execution of 
	 * the specified {@link Executable} passed as parameter
	 * 
	 * @param executables the {@link Executable} to be executed when a service
	 * of the appropriate type disappears
	 * 
	 * @return the String identifier of the created link
	 */
	public String attachOnRemoving(Executable<S,Void> executable)
	{
		return this.attachOnRemoving(Collections.singletonList(executable));
	}

	/**
	 * Links the event of a &lt;S&gt; typed service disappearance to the execution of 
	 * the specified list of {@link Executable}s passed as parameter
	 * 
	 * @param executables the list of {@link Executable}s to be executed when a service
	 * of the appropriate type disappears
	 * 
	 * @return the String identifier of the created link
	 */
	public String attachOnRemoving(List<Executable<S,Void>> executables)
	{
		synchronized(this.removingExecutables)
		{
			return add("onRemoving", this.removingExecutables, executables);
		}
	}
	
	/**
	 * Unlinks the event of a &lt;S&gt; typed service disappearance from the execution of 
	 * the list of {@link Executable}s identified by the String key passed as parameter
	 * 
	 * @param key the String identifier of the list of {@link Executable}s to be unregistered
	 */
	public void detachOnRemoving(String key)
	{
		synchronized(this.removingExecutables)
		{
			this.removingExecutables.remove(key);
		}
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#
	 * addingService(org.osgi.framework.ServiceReference)
	 */
	@Override
	public S addingService(ServiceReference<S> reference)
	{
		S instance = this.mediator.getContext().getService(reference);
		synchronized(this.addingExecutables)
		{
			execute(this.addingExecutables.values(), instance);
		}
		return instance;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer
	 * #modifiedService(org.osgi.framework.ServiceReference, java.lang.Object)
	 */
	@Override
	public void modifiedService(ServiceReference<S> reference, S instance)
	{
		synchronized(this.modifyingExecutables)
		{
			execute(this.modifyingExecutables.values(), instance);
		}
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#
	 * removedService(org.osgi.framework.ServiceReference, java.lang.Object)
	 */
	@Override
	public void removedService(ServiceReference<S> reference, S instance)
	{
		synchronized(this.removingExecutables)
		{
			execute(this.removingExecutables.values(), instance);
		}
	}
}