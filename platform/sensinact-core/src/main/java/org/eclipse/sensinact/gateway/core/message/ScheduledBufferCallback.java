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
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;

/**
 * Extended {@link BufferCallback} allowing to
 * schedule the transmission of the associated 
 * buffer's content independently of its filling 
 * state
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ScheduledBufferCallback 
extends BufferCallback 
{
	protected final int delay;
	private Timer timer;
	
	/**
	 * Constructor
	 * 
	 * @param delay
	 * 		the delay between two triggered event
	 */
    public ScheduledBufferCallback(Mediator mediator, 
    		String identifier, 
    		ErrorHandler errorHandler,
    		Recipient recipient, long timeout, 
    		int delay, int bufferSize) 
	{
    	super(mediator, identifier, errorHandler, 
    			recipient, timeout, bufferSize);
    	if(delay < 10000)
    	{
    		this.delay = 10000;
    		
    	} else
    	{
    		this.delay = delay; 
    	}
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
				synchronized(ScheduledBufferCallback.this.buffer)
				{
					int index = 0;
					int length = ScheduledBufferCallback.this.length;
					if(length > 0)
					{
						SnaMessage[] buffer = new SnaMessage[length];
						
						for(;index < length; index++)
						{
							buffer[index] = ScheduledBufferCallback.this.buffer[index];
						}
						try
			    		{
							ScheduledBufferCallback.this.recipient.callback(
									ScheduledBufferCallback.this.getName(),buffer);		
							ScheduledBufferCallback.this.status = Status.SUCCESS;
							
			    		} catch(Exception e)
			    		{
	
			    			ScheduledBufferCallback.this.status = Status.ERROR;
						    ErrorHandler errorHandler = 
						    	ScheduledBufferCallback.this.getCallbackErrorHandler();					    
						    if(errorHandler!=null )
						    {
						    	errorHandler.register(e);
						    }
			    		}
						ScheduledBufferCallback.this.length = 0;		    	
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
}
