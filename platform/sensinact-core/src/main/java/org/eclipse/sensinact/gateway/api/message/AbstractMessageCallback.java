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
package org.eclipse.sensinact.gateway.api.message;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.gateway.common.execution.DefaultErrorHandler;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.core.message.MidCallbackException;
import org.eclipse.sensinact.gateway.util.stack.AbstractStackEngineHandler;

/**
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public abstract class AbstractMessageCallback implements MessageCallback {
	
	class MessageRegistererBufferDelegate extends AbstractStackEngineHandler<SnaMessage<?>>
	implements MessageRegisterer {
		
		MessageRegistererBufferDelegate() {
			super();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.util.stack.StackEngineHandler#doHandle(java.lang.Object)
		 */
		@Override
		public void doHandle(SnaMessage<?> message) {
			try {
				AbstractMessageCallback.this.doCallback(message);
			} catch (MidCallbackException e) {
				int continuation = AbstractMessageCallback.this.getCallbackErrorHandler().handle(e);
				switch(continuation){
					case ErrorHandler.Policy.CONTINUE :
					case ErrorHandler.Policy.IGNORE:
					case ErrorHandler.Policy.ROLLBACK:
						break;
					case ErrorHandler.Policy.STOP:
						AbstractMessageCallback.this.stop();
					default:
						break;		        
				}
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.core.message.MessageRegisterer#register(org.eclipse.sensinact.gateway.core.message.SnaMessage)
		 */
		@Override
		public void register(SnaMessage<?> message) {
			long timeout = AbstractMessageCallback.this.getTimeout();
			if(timeout > 0 && System.currentTimeMillis() > timeout) {
				AbstractMessageCallback.this.stop();
				return;
			}
			super.eventEngine.push(message);
		}
	}

	class MessageRegistererDirectDelegate implements MessageRegisterer {
		/* (non-Javadoc)
		 * @see org.eclipse.sensinact.gateway.core.message.MessageRegisterer#register(org.eclipse.sensinact.gateway.core.message.SnaMessage)
		 */
		@Override
		public void register(SnaMessage<?> message) {
			long timeout = AbstractMessageCallback.this.getTimeout();
			if(timeout > 0 && System.currentTimeMillis() > timeout) {
				AbstractMessageCallback.this.stop();
				return;
			}
			try {
				AbstractMessageCallback.this.doCallback(message);
			} catch (MidCallbackException e) {
				int continuation = AbstractMessageCallback.this.getCallbackErrorHandler().handle(e);
				switch(continuation){
					case ErrorHandler.Policy.CONTINUE :
					case ErrorHandler.Policy.IGNORE:
					case ErrorHandler.Policy.ROLLBACK:
						break;
					case ErrorHandler.Policy.STOP:
						AbstractMessageCallback.this.stop();
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
	 * the {@link MessageRegisterer} of this {@link MessageCallback}
	 */
	protected MessageRegisterer registerer;

	/**
	 * this {@link MessageCallback}'s String identifier
	 */
	protected String identifier;

	/**
	 * the active status of this {@link MessageCallback}
	 */
	protected AtomicBoolean isActive;

	/**
	 * Constructor
	 * 
	 * @param identifier
	 */
	protected AbstractMessageCallback() {
		this(false, null);
	}
	
	/**
	 * Constructor
	 * 
	 * @param identifier the String identifier of the {@link MessageCallback}
	 * to be instantiated 
	 */
	protected AbstractMessageCallback(String identifier) {
		this(false, identifier);
	}

	/**
	 * Constructor
	 * 
	 * @param buffer Defines whether the {@link MessageCallback} to be instantiated will
	 * use a fifo buffer to store received messages before treating them
	 */
	protected AbstractMessageCallback(boolean buffer) {
		this(buffer,null);
	}


	/**
	 * Constructor
	 * 
	 * @param buffer Defines whether the {@link MessageCallback} to be instantiated 
	 * will use a fifo buffer to store received messages before treating them
	 * @param identifier the String identifier of the {@link MessageCallback}
	 * to be instantiated 
	 */
	protected AbstractMessageCallback(boolean buffer, String identifier) {
		this.setTimeout(MessageCallback.ENDLESS);
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
	 * @see org.eclipse.sensinact.gateway.api.message.MessageCallback#
	 *      getCallbackErrorHandler()
	 */
	public ErrorHandler getCallbackErrorHandler() {
		if (this.errorHandler == null) {
			this.errorHandler = new DefaultErrorHandler();
		}
		return this.errorHandler;
	}

	/**
	 * Defines the {@link ErrorHandler} of this {@link MessageCallback}
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
	 * @see org.eclipse.sensinact.gateway.api.message.MessageCallback#getTimeout()
	 */
	public long getTimeout() {
		return this.timeout;
	}

	/**
	 * Defines this {@link MessageCallback}'s timeout
	 * 
	 * @param timeout
	 *            the timeout long value of this {@link MessageCallback}
	 */
	protected void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.api.message.MessageCallback#
	 *      getMessageRegisterer()
	 */
	public MessageRegisterer getMessageRegisterer() {
		return this.registerer;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.api.message.MessageCallback#isActive()
	 */
	@Override
	public boolean isActive(){
		synchronized(this.isActive) {
			return this.isActive.get();
		}
	}
	
	/**
	 * Stops this {@link MessageCallback} and all its associated processes
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