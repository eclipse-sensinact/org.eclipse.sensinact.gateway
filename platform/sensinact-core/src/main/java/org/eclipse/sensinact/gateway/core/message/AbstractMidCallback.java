/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.message;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.gateway.common.execution.DefaultErrorHandler;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.util.stack.AbstractStackEngineHandler;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractMidCallback implements MidCallback {
	
	class MessageRegistererBufferDelegate extends AbstractStackEngineHandler<SnaMessage<?>>
			implements MessageRegisterer {
		
		/**
		 * Constructor
		 */
		MessageRegistererBufferDelegate() {
			super();
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.util.stack.StackEngineHandler#doHandle(java.lang.Object)
		 */
		@Override
		public void doHandle(SnaMessage<?> message) {
			try {
				AbstractMidCallback.this.doCallback(message);
			} catch (MidCallbackException e) {
				int continuation = AbstractMidCallback.this.getCallbackErrorHandler().handle(e);
				switch(continuation){
					case ErrorHandler.Policy.CONTINUE :
					case ErrorHandler.Policy.IGNORE:
					case ErrorHandler.Policy.ROLLBACK:
						break;
					case ErrorHandler.Policy.STOP:
						AbstractMidCallback.this.stop();
					default:
						break;		        
				}
			}
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.message.MessageRegisterer#register(org.eclipse.sensinact.gateway.core.message.SnaMessage)
		 */
		public void register(SnaMessage<?> message) {
			super.eventEngine.push(message);
		}
	}

	class MessageRegistererDirectDelegate implements MessageRegisterer {
		/**
		 * @throws MidCallbackException 
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.message.MessageRegisterer#register(org.eclipse.sensinact.gateway.core.message.SnaMessage)
		 */
		public void register(SnaMessage<?> message) {
			try {
				AbstractMidCallback.this.doCallback(message);
			} catch (MidCallbackException e) {
				int continuation = AbstractMidCallback.this.getCallbackErrorHandler().handle(e);
				switch(continuation){
					case ErrorHandler.Policy.CONTINUE :
					case ErrorHandler.Policy.IGNORE:
					case ErrorHandler.Policy.ROLLBACK:
						break;
					case ErrorHandler.Policy.STOP:
						AbstractMidCallback.this.stop();
					default:
						break;		     
				}
			}
		}
	}

	/**
	 * Transmits the {@link SnaMessage} passed as parameter to the final recipient
	 * of the callback process
	 * 
	 * @param message
	 *            the {@link SnaMessage} to be sent
	 * @throws MidCallbackException 
	 * 			  if an error occurs when transmitting the specified message
	 */
	protected abstract void doCallback(SnaMessage<?> message) throws MidCallbackException;

	/**
	 * this callback's error handler
	 */
	protected ErrorHandler errorHandler;

	/**
	 * this callback life timeout
	 */
	protected long timeout;

	/**
	 * the {@link MessageRegisterer} of this {@link MidCallback}
	 */
	protected MessageRegisterer registerer;

	/**
	 * this {@link MidCallback}'s String identifier
	 */
	protected String identifier;

	/**
	 * the active status of this {@link MidCallback}
	 */
	protected AtomicBoolean isActive;

	/**
	 * Constructor
	 * 
	 * @param identifier
	 */
	protected AbstractMidCallback() {
		this(false, null);
	}
	
	/**
	 * Constructor
	 * 
	 * @param identifier the String identifier of the {@link MidCallback}
	 * to be instantiated 
	 */
	protected AbstractMidCallback(String identifier) {
		this(false, identifier);
	}

	/**
	 * Constructor
	 * 
	 * @param buffer Defines whether the {@link MidCallback} to be instantiated will
	 * use a fifo buffer to store received messages before treating them
	 */
	protected AbstractMidCallback(boolean buffer) {
		this(buffer,null);
	}


	/**
	 * Constructor
	 * 
	 * @param buffer Defines whether the {@link MidCallback} to be instantiated 
	 * will use a fifo buffer to store received messages before treating them
	 * @param identifier the String identifier of the {@link MidCallback}
	 * to be instantiated 
	 */
	protected AbstractMidCallback(boolean buffer, String identifier) {
		this.setTimeout(MidCallback.ENDLESS);
		this.registerer = buffer ? new MessageRegistererBufferDelegate() : new MessageRegistererDirectDelegate();
		this.isActive = new AtomicBoolean(true);
		if(identifier == null) {
			this.identifier = new StringBuilder().append("MID").append(
				System.currentTimeMillis()).append(this.hashCode()).toString();
		} else {
			this.identifier = identifier;
		}
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.primitive.Nameable#getName()
	 */
	public String getName() {
		return this.identifier;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.message.MidCallback#
	 *      getCallbackErrorHandler()
	 */
	public ErrorHandler getCallbackErrorHandler() {
		if (this.errorHandler == null) {
			this.errorHandler = new DefaultErrorHandler();
		}
		return this.errorHandler;
	}

	/**
	 * Defines the {@link ErrorHandler} of this {@link MidCallback}
	 * 
	 * @param errorHandler
	 *            the {@link ErrorHandler} to be set
	 */
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.message.MidCallback#getTimeout()
	 */
	public long getTimeout() {
		return this.timeout;
	}

	/**
	 * Defines this {@link MidCallback}'s timeout
	 * 
	 * @param timeout
	 *            the timeout long value of this {@link MidCallback}
	 */
	protected void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.message.MidCallback#
	 *      getMessageRegisterer()
	 */
	public MessageRegisterer getMessageRegisterer() {
		return this.registerer;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.message.MidCallback#isActive()
	 */
	@Override
	public boolean isActive(){
		synchronized(this.isActive) {
			return this.isActive.get();
		}
	}
	
	/**
	 * Stops this {@link MidCallback} and all its associated processes
	 */
	public void stop() {
		synchronized(this.isActive) {
			this.isActive.set(false);
		}		
		if (MessageRegistererBufferDelegate.class.isAssignableFrom(this.registerer.getClass())) {
			((MessageRegistererBufferDelegate) this.registerer).stop();
		}
	}
}