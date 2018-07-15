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

import org.eclipse.sensinact.gateway.common.execution.DefaultCallbackErrorHandler;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.eclipse.sensinact.gateway.util.stack.AbstractStackEngineHandler;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractMidCallback implements MidCallback {
	class MessageRegistererBufferDelegate extends AbstractStackEngineHandler<SnaMessage<?>>
			implements MessageRegisterer {
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
			AbstractMidCallback.this.doCallback(message);
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
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.message.MessageRegisterer#register(org.eclipse.sensinact.gateway.core.message.SnaMessage)
		 */
		public void register(SnaMessage<?> message) {
			AbstractMidCallback.this.doCallback(message);
		}
	}

	/**
	 * Transmits the {@link SnaMessage} passed as parameter to the final recipient
	 * of the callback process
	 * 
	 * @param message
	 *            the {@link SnaMessage} to be sent
	 */
	protected abstract void doCallback(SnaMessage<?> message);

	/**
	 * last callback status
	 */
	protected Status status;

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
	 * Constructor
	 * 
	 * @param identifier
	 */
	protected AbstractMidCallback() {
		this(false);
	}

	/**
	 * Constructor
	 * 
	 * @param identifier
	 *            the String identifier of the {@link MidCallback} to be
	 *            instantiated
	 * 
	 * @param buffer
	 *            Defines whether the {@link MidCallback} to be instantiated will
	 *            use a fifo buffer to store received messages before treating them
	 */
	protected AbstractMidCallback(boolean buffer) {
		this.setTimeout(MidCallback.ENDLESS);
		this.registerer = buffer ? new MessageRegistererBufferDelegate() : new MessageRegistererDirectDelegate();
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
	 *      setIdentifier(java.lang.String)
	 */
	public void setIdentifier(String identifier) {
		if (this.identifier != null) {
			return;
		}
		this.identifier = identifier;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.message.MidCallback#
	 *      getCallbackErrorHandler()
	 */
	public ErrorHandler getCallbackErrorHandler() {
		if (this.errorHandler == null) {
			this.errorHandler = new DefaultCallbackErrorHandler();
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
	 * @see org.eclipse.sensinact.gateway.core.message.MidCallback#getStatus()
	 */
	public AccessMethodResponse.Status getStatus() {
		return this.status;
	}

	/**
	 * Defines this callback's {@link AccessMethodResponse.Status}
	 * 
	 * @param status
	 *            the {@link AccessMethodResponse.Status} of this callback
	 */
	protected void setStatus(Status status) {
		this.status = status;
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
	 * Stops this {@link MidCallback} and all its associated processes
	 */
	public void stop() {
		if (MessageRegistererBufferDelegate.class.isAssignableFrom(this.registerer.getClass())) {
			((MessageRegistererBufferDelegate) this.registerer).stop();
		}
	}
}