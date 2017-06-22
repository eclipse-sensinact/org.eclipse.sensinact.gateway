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


import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.DefaultCallbackErrorHandler;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;


/**
 * Extended {@link BufferCallback} allowing to
 * schedule the transmission of the associated buffer's 
 * content independently of its filling state
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ScheduledCallback implements SnaCallback
{
	private final String identifier;
	
	private ErrorHandler errorHandler;

	protected Status status;

	protected Recipient recipient;
	
	protected final long timeout; 

	/**
	 * Mediator used to interact with the OSGi host
	 * environment 
	 */
	protected final Mediator mediator;
	protected final int delay;
	private Timer timer;

	protected SnaMessage lastMessage;

//	protected boolean handleUnchanged;
	
	/**
	 * Constructor
	 * 
	 * @param delay
	 * 		the delay between two triggered event
	 */
    public ScheduledCallback(Mediator mediator, 
			String identifier, 
			ErrorHandler errorHandler,
			Recipient recipient, long lifetime, int delay)
	{
		this.mediator = mediator;
		this.identifier = identifier;
		
		this.errorHandler = errorHandler == null
				?new DefaultCallbackErrorHandler():errorHandler;
				
		this.recipient = recipient;
		this.timeout = lifetime==ENDLESS?ENDLESS
				:(System.currentTimeMillis()+lifetime);
		
    	if(delay < 1000)
    	{
    		this.delay = 1000;
    		
    	} else
    	{
    		this.delay = delay; 
    	}
//    	this.handleUnchanged = false;
	}
     
    /**
     * Starts this extended {@link SnaCallback}
     */
    public void start()
    {
    	TimerTask task = new TimerTask()
		{
			/**
			 * @inheritDoc
			 *
			 * @see java.util.TimerTask#run()
			 */
			@Override
            public void run()
            {
				try
	    		{
					ScheduledCallback.this.recipient.callback(
						ScheduledCallback.this.getName(),new SnaMessage[]{
								ScheduledCallback.this.lastMessage});
					
	    			ScheduledCallback.this.status = Status.SUCCESS;
	    			
	    		} catch(Exception e)
	    		{
	    			ScheduledCallback.this.status = Status.ERROR;
				    ErrorHandler errorHandler = 
				    		ScheduledCallback.this.getCallbackErrorHandler();	
				    
				    if(errorHandler!=null )
				    {
				    	errorHandler.register(e);
				    }
	    		}
            }
		};
    	this.timer = new Timer(true);
        this.timer.scheduleAtFixedRate(task, 0, delay);
    }
    
	/**
	 * Stops this {@link SnaCallback} and frees 
	 * the associated {@link Timer}
	 */
	public void stop()
	{
    	this.timer.cancel();
		this.timer.purge();
		this.timer = null;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see SnaCallback#
	 * register(SnaMessage)
	 */
    @Override
    public void register(SnaMessage message)
    {
	  this.lastMessage = message;
    }

    /**
	 * @inheritDoc
	 *
	 * @see Nameable#getName()
	 */
    @Override
    public String getName()
    {
	    return this.identifier;
    }

	/**
	 * @inheritDoc
	 *
	 * @see SnaCallback#getCallbackErrorHandler()
	 */
    @Override
    public ErrorHandler getCallbackErrorHandler()
    {
	    return this.errorHandler;
    }

	/**
	 * @inheritDoc
	 *
	 * @see SnaCallback#getStatus()
	 */
    @Override
    public Status getStatus()
    {
	    return this.status;
    }
    
    /**
     * @inheritDoc
     *
     * @see SnaCallback#getTimeout()
     */
    @Override
    public long getTimeout()
    {
    	return this.timeout;
    }
    

//	/** 
//	 * Defines whether this callback handles unchanged value notifications, 
//	 * meaning that only the timestamp has been updated. 
//	 * 
//	 * @param handleUnchanged
//	 * 		<ul>
//     * 			<li>true if this callback handles unchanged value
//     * 			notifications</li>
//     * 			<li>false otherwise</li>
//     * 		</ul>
//	 */
//	public void handleUnchanged(boolean handleUnchanged)
//	{
//		this.handleUnchanged = handleUnchanged;
//	}
//	
//	/** 
//	 * @inheritDoc
//	 * 
//	 * @see SnaCallback#handleUnchanged()
//	 */
//	@Override
//	public boolean handleUnchanged()
//	{
//		return this.handleUnchanged;
//	}
}
