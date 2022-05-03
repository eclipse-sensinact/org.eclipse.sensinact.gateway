/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.message;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;

/**
 * Extended {@link MidCallback} managing a buffer to store received events
 * before transmission
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class BufferMidCallback extends UnaryMidCallback {
	/**
	 * current buffer's size
	 */
	protected int length;

	/**
	 * the buffer total capacity
	 */
	protected final int bufferSize;

	/**
	 * the {@link SnaMessage}s buffer
	 */
	protected final SnaMessage<?>[] buffer;

	/**
	 * Constructor
	 * 
	 * @param bufferSize
	 *            the length of the buffer to create
	 */
	public BufferMidCallback(Mediator mediator, ErrorHandler errorHandler, Recipient recipient,
			long timeout, int bufferSize) {
		super(mediator, errorHandler, recipient, timeout);
		if (bufferSize == 0) {
			this.bufferSize = 1;

		} else {
			this.bufferSize = bufferSize;
		}
		this.length = 0;
		buffer = new SnaMessage[this.bufferSize];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see StackEngineHandler#doHandle(java.lang.Object)
	 */
	@Override
	public void doCallback(SnaMessage<?> message) throws MidCallbackException {
		synchronized (this.buffer) {
			this.buffer[length++] = message;

			if (this.length == this.bufferSize) {
				SnaMessage<?>[] msgBuffer = new SnaMessage[length];
				int index = 0;

				for (; index < length; index++) {
					msgBuffer[index] = this.buffer[index];
				}
				try {
					this.recipient.callback(this.getName(), msgBuffer);

				} catch (Exception e) {
					throw new MidCallbackException(e);
				}
				this.length = 0;
			}
		}
	}
}
