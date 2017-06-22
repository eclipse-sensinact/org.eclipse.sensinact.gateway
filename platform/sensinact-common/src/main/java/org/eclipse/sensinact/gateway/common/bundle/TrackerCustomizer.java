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
import java.util.List;

import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import org.eclipse.sensinact.gateway.common.execution.Executable;

final class TrackerCustomizer<S> 
implements ServiceTrackerCustomizer<S,S>
{
	private Mediator mediator;
	
	private List<Executable<S,Void>> addingExecutors;
	private List<Executable<S,Void>> modifyingExecutors;
	private List<Executable<S,Void>> removingExecutors;

	TrackerCustomizer(Mediator mediator)
	{
		this.mediator = mediator;
		this.addingExecutors = new ArrayList<Executable<S,Void>>();
		this.modifyingExecutors = new ArrayList<Executable<S,Void>>();
		this.removingExecutors = new ArrayList<Executable<S,Void>>();
	}

	/**
	 * @param executor
	 */
	public void onAdding(List<Executable<S,Void>> executors)
	{
		synchronized(this.addingExecutors)
		{
			this.addingExecutors.addAll(executors);
		}
	}

	/**
	 * @param executor
	 */
	public void onModifying(List<Executable<S,Void>> executors)
	{
		synchronized(this.modifyingExecutors)
		{
			this.modifyingExecutors.addAll(executors);
		}
	}
	
	/**
	 * @param executor
	 */
	public void onRemoving(List<Executable<S,Void>> executors)
	{
		synchronized(this.removingExecutors)
		{
			this.removingExecutors.addAll(executors);
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
		synchronized(this.addingExecutors)
		{
			execute(this.addingExecutors, instance);
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
		synchronized(this.modifyingExecutors)
		{
			execute(this.modifyingExecutors, instance);
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
		synchronized(this.removingExecutors)
		{
			execute(this.removingExecutors, instance);
		}
	}
	
	/**
	 * @param executors
	 * @param instance
	 */
	private void execute(List<Executable<S,Void>> executors, S instance)
	{
		int index = 0;
		int length = executors == null?0:executors.size();
		for(;index < length; index++)
		{
			try
			{
				executors.get(index).execute(instance);
				
			} catch(Exception e)
			{
				e.printStackTrace();
				continue;
			}
		}
		
	}
}