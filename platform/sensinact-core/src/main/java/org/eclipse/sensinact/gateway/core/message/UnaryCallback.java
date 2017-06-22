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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.DefaultCallbackErrorHandler;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.eclipse.sensinact.gateway.util.stack.AbstractStackEngineHandler;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.util.stack.StackEngineHandler;

/**
 * Abstract {@link SnaCallback} implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UnaryCallback extends AbstractStackEngineHandler<SnaMessage<?>> 
implements SnaCallback
{	
	private final String identifier;
	
	private ErrorHandler errorHandler;

	protected Status status;

	protected Recipient recipient;
	
	protected final long timeout; 
	
//	protected boolean handleUnchanged;

	/**
	 * Mediator used to interact with the OSGi host
	 * environment 
	 */
	protected final Mediator mediator;

	/**
	 * Constructor
	 * 
	 * @param identifier
	 * 		the identifier of the {@link SnaCallback}
	 * 		to instantiate
	 * @param errorHandler
	 * 		the {@link SnaCallbackErrorHandler} of the 
	 * 		{@link SnaCallback} to instantiate
	 * @param lifetime 
	 */
	public UnaryCallback(Mediator mediator, 
			String identifier, 
			ErrorHandler errorHandler,
			Recipient recipient, long lifetime)
	{
		this.mediator = mediator;
		this.identifier = identifier;
		
		this.errorHandler = errorHandler == null
				?new DefaultCallbackErrorHandler():errorHandler;
				
		this.recipient = recipient;
		this.timeout = lifetime==ENDLESS?ENDLESS
				:(System.currentTimeMillis()+lifetime);
//		this.handleUnchanged(this.recipient.handleUnchanged());
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
	
	/**
	 * @inheritDoc
	 *
	 * @see StackEngineHandler#doHandle(java.lang.Object)
	 */
	@Override
	public void doHandle(SnaMessage<?> message)
	{
	   try
	   {
		   this.recipient.callback(this.getName(), 
				   new SnaMessage[]{message});
		   
		   this.status = Status.SUCCESS;
	   }
	   catch (Exception e)
	   {
		   if(this.mediator.isDebugLoggable())
		   {
			   this.mediator.error(e, e.getMessage());
		   }
		   this.status = Status.ERROR;
		   if(this.errorHandler != null)
		   {
			   this.errorHandler.register(e);
		   }
	   }
	}

	/**
	 * @inheritDoc
	 *
	 * @see MessageRegisterer#
	 * register(SnaMessage)
	 */
	@Override
	public void register(SnaMessage message)
	{
		super.eventEngine.push(message);
	}
}
