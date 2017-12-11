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
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.eclipse.sensinact.gateway.util.stack.StackEngineHandler;

/**
 * Abstract {@link MidCallback} implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UnaryMidCallback extends AbstractMidCallback
{		
	/**
	 * Mediator used to interact with the OSGi host
	 * environment 
	 */
	protected final Mediator mediator;
	protected Recipient recipient;


	/**
	 * Constructor
	 * 
	 * @param identifier
	 * 		the identifier of the {@link MidCallback}
	 * 		to instantiate
	 * @param errorHandler
	 * 		the {@link SnaCallbackErrorHandler} of the 
	 * 		{@link MidCallback} to instantiate
	 * @param lifetime 
	 */
	public UnaryMidCallback(Mediator mediator, 
			String identifier, 
			ErrorHandler errorHandler,
			Recipient recipient, long lifetime)
	{
		super(true);
		this.mediator = mediator;
		
		this.recipient = recipient;
		super.setTimeout(lifetime<=10000?ENDLESS
				:(System.currentTimeMillis()+lifetime));
		super.setErrorHandler(errorHandler);
		super.setIdentifier(identifier);
	}

	/**
	 * @inheritDoc
	 *
	 * @see StackEngineHandler#doHandle(java.lang.Object)
	 */
	@Override
	public void doCallback(SnaMessage<?> message)
	{
	   try
	   {
		   this.recipient.callback(this.getName(), 
				   new SnaMessage[]{message});

			setStatus(Status.SUCCESS);
	   }
	   catch (Exception e)
	   {
		   if(this.mediator.isDebugLoggable())
		   {
			   this.mediator.error(e, e.getMessage());
		   }
		   setStatus(Status.SUCCESS);
		   super.getCallbackErrorHandler().register(e);
	   }
	}
}
