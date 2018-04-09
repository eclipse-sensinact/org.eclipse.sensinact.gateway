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
package org.eclipse.sensinact.gateway.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 * Manages {@link Task}s to execute
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class TaskManager
{    
	public static final char IDENTIFIER_SEP_CHAR = '#';

    /**
     * Locker object
     */
    private final Object lock = new Object();
    
    /**
     * tasks storage attempting for free token events
     */
    protected final Deque<Task> waitingTasks;
    
    /**
     * executed tasks storage mapped to requirer 
     * ServiceProvider's identifier
     */
    protected final Map<String, List<Task>> executedTasks;   

    /**
     * the associated {@link TokenEventProvider}
     * */
    protected TaskDesynchronizer desynchronizer;
    
	/**
	 * the {@link TaskTranslator} to which 
	 * to ask for Task creation and transmission 
	 */
	protected final TaskTranslator connector;

    /**
     * the associated {@link Mediator}
     */
    protected Mediator mediator;


    /**
     * Constructor
     * @param requireTokenEventProvider 
     * 
     * @param desynchronizer
     *            the associated token provider
     */
    public TaskManager(Mediator mediator,
    		TaskTranslator connector, boolean initialLockState, 
    		boolean isDesynchronized)
    {
        this.mediator = mediator;
        this.connector = connector;
        this.waitingTasks = new LinkedList<Task>();
        this.executedTasks = new HashMap<String,List<Task>>();
       
        if (isDesynchronized)
        {
        	this.desynchronizer = new TaskDesynchronizer(mediator);
            this.desynchronizer.setLocked(initialLockState);
            new Thread(this.desynchronizer).start();
        }
    }

	/**
     * @inheritDoc
     * 
     * @see org.eclipse.sensinact.gateway.generic.core.Desynchronizable#
     *      nextTask()
     */
    public void nextTask()
    {
        Task task = null;  
        
        synchronized(this.waitingTasks)
        {
            if (!this.waitingTasks.isEmpty())
            {
                 task = this.waitingTasks.poll();
            }
        }
        if (task != null)
        {            
            if(!task.isResultAvailable())
            {   
            	String taskIdentifier = task.getTaskIdentifier();
            	
            	 if(taskIdentifier != null)
            	 {
            		 this.add(UriUtils.getRoot(task.getPath()).substring(1), task);
            	 }
            	 task.execute();
            	 if(taskIdentifier == null)
            	 {
	                //if no identifier avoid a task running in the
	                //background for a result that will never come
	                task.abort(AccessMethod.EMPTY);	                
            	 }
            }            
        } else if(this.mediator.isDebugLoggable())
        {
            this.mediator.debug("No task left to execute");
        }
        synchronized(lock)
        {
            if (this.desynchronizer != null)
            {
               this.desynchronizer.freeingToken();
            }
        }
    }

    
    /**
     * Returns the {@link Task} whose identifier is 
     * passed as parameter
     * 
     * @param taskIdentifier
     *      the string identifier of the {@link Task}
     * 		to return
     * @return
     *      the {@link Task} with the specified identifier
     */
    protected List<Task> remove(String taskIdentifier)
    {
    	if(taskIdentifier == null)
    	{
    		return Collections.<Task>emptyList();
    	}
    	int index = taskIdentifier.indexOf(IDENTIFIER_SEP_CHAR);
    	if(index == -1)
    	{
    		return Collections.<Task>emptyList();
    	}
    	String serviceProviderIdentifier = 
    			taskIdentifier.substring(0,index);
    	
    	List<Task> tasks = new ArrayList<Task>();
    	synchronized(this.executedTasks)
    	{
	        List<Task> executeds = this.executedTasks.get(
	        		serviceProviderIdentifier);
	        if(executeds == null)
	        {
	        	return tasks;
	        }
	        Iterator<Task> iterator = executeds.iterator();        
	        while(iterator.hasNext())
	        {
	            Task task = iterator.next();
	            if(taskIdentifier.equals(task.getTaskIdentifier()))
	            {
	            	iterator.remove();
	            	tasks.add(task);
	            }
	        }
    	}
        return tasks;
    }
    
    /**
     * Adds the {@link Task} passed as parameter to the list of
     * ones mapped to the specified identifier of the {@link 
     * ServiceProvider} requirer
     * 
     * @param task
     *      the {@link Task} to add
     * @param serviceProviderIdentifier
     *      the string identifier of the {@link ServiceProvider}
     * 		requirer
     */
    private void add(String serviceProviderIdentifier, Task task)
    {
    	List<Task> tasks = null;      
    	synchronized(this.executedTasks)
    	{
	        tasks = this.executedTasks.get(serviceProviderIdentifier);
	        if(tasks == null)
	    	{
	    		tasks = new ArrayList<Task>();
	    		this.executedTasks.put(serviceProviderIdentifier, tasks);
	    	}
	        tasks.add(task);	        
	        task.registerCallBack(new TaskCallBack(
				new Executable<Task,Void>()
				{
					@Override
					public Void execute(Task task) throws Exception
					{
						TaskManager.this.remove(task.getTaskIdentifier());
						return null;
					}
				}));
	        if(task.isResultAvailable())
	        {
	        	remove(task.getTaskIdentifier());
	        }
    	}
    }    
    
    /**
     * Asks for an asynchronous task execution. Builds a {@link Task} object and
     * stores it in the list of waiting ones
     * 
     * @param identifier
     *      the requirer {@link PacketProcessor} string identifier 
     * @param command
     *            CommandType : GET, SET, ACT, SUBSCRIBE, 
     *            UNSUBSCRIBE OR SERVICES_ENUMERATION
     * @param profileId 
     * @param resourceConfig
     *            the {@link ExtResourceConfig} mapped to the resource on which 
     *            applies the method
     * @param parameters
     *            the objects array parameterizing the method invocation
     *            
     * @return 
     *      the {@link Future} object associated to the result of the 
     *      task execution
     */
    public Task execute(Task.CommandType command,
    		String path, String profileId, ResourceConfig resourceConfig, 
    		Object[] parameters)
	{
        Task task = null;
        switch(command)
        {
            case GET: 
                task = this.connector.createTask(
                		this.mediator, Task.CommandType.GET, path, profileId,
                        resourceConfig, parameters);
                break;
            case SET:
                task = this.connector.createTask(
                		this.mediator, Task.CommandType.SET, path, profileId,
                        resourceConfig, parameters);
                break;
            case ACT:
                task = this.connector.createTask(
                		this.mediator, Task.CommandType.ACT, path, profileId,
                        resourceConfig, parameters);
                break;
            case SUBSCRIBE:
                task = this.connector.createTask(
                		this.mediator, Task.CommandType.SUBSCRIBE, path, profileId,
                        resourceConfig, parameters);
                break;
            case UNSUBSCRIBE:
                task = this.connector.createTask(
                		this.mediator, Task.CommandType.UNSUBSCRIBE,path, profileId,
                        resourceConfig, parameters);
                break;
            case SERVICES_ENUMERATION:
                task = this.connector.createTask(
                		this.mediator, Task.CommandType.SERVICES_ENUMERATION, path, profileId,
                		resourceConfig,parameters);
                break;
            default:;
        }
        if(task != null)
        {
            return this.execute(task);
        }
        return null;
	}

    /**
     * Asks for an asynchronous task execution
     * 
     * @param task
     *            the task to treat
     */
    public Task execute(Task task)
    {
       synchronized(this.waitingTasks)
       {
            this.waitingTasks.offer(task);
       }
       synchronized(lock)
       {
            if (this.desynchronizer == null)
            {
                this.nextTask();
    
            } else
            {    
                this.desynchronizer.require(this);
            }
        }
        return task;
    }

    /**
     * Stops the associated {@link TokenEventProvider} and
     * {@link ExecutorService} if not null
     */
    public void stop()
    {
        synchronized(lock)
        {
            if (this.desynchronizer != null)
            {
                this.desynchronizer.stop();
                this.desynchronizer = null;
            }
        }
        synchronized(this.waitingTasks)
        {
            this.waitingTasks.clear();
        }
    }

    /**
     * Calls the <code>freeingToken</code> method of the 
     * associated {@link TokenEventProvider} to unlock it
     */
    protected void unlock()
    {
        synchronized(lock)
        {
            if(this.desynchronizer != null)
            {
                this.desynchronizer.freeingToken();
            }
        }
    }
}
