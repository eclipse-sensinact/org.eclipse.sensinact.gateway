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

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;

/**
 * Extended {@link BufferMidCallback} allowing to schedule the transmission of
 * the associated buffer's content independently of its filling state
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ScheduledBufferMidCallback extends BufferMidCallback {
	
	protected final int delay;
	private Timer timer;

	/**
	 * Constructor
	 * 
	 * @param delay
	 *            the delay between two triggered event
	 */
	public ScheduledBufferMidCallback(Mediator mediator, ErrorHandler errorHandler,
			Recipient recipient, long timeout, int delay, int bufferSize) {
		super(mediator, errorHandler, recipient, timeout, bufferSize);
		if (delay < 10000) {
			this.delay = 10000;

		} else {
			this.delay = delay;
		}
	}

	/**
	 * Starts this extended {@link MidCallback}
	 */
	public void start() {
		TimerTask task = new TimerTask() {
			/**
			 * @inheritDoc
			 *
			 * @see java.util.TimerTask#run()
			 */
			@Override
			public void run() {
				synchronized (ScheduledBufferMidCallback.this.buffer) {
					int index = 0;
					int length = ScheduledBufferMidCallback.this.length;
					if (length > 0) {
						SnaMessage[] buffer = new SnaMessage[length];

						for (; index < length; index++) {
							buffer[index] = ScheduledBufferMidCallback.this.buffer[index];
						}
						try {
							ScheduledBufferMidCallback.this.recipient.callback(getName(), buffer);
						} catch (Exception e) {
							int continuation = ScheduledBufferMidCallback.this.getCallbackErrorHandler().handle(e);
							switch(continuation){
								case ErrorHandler.Policy.CONTINUE :
								case ErrorHandler.Policy.IGNORE:
								case ErrorHandler.Policy.ROLLBACK:
									break;
								case ErrorHandler.Policy.STOP:
									ScheduledBufferMidCallback.this.stop();
								default:
									break;		       
							}
						}
						ScheduledBufferMidCallback.this.length = 0;
					}
				}
			}
		};
		this.timer = new Timer(true);
		this.timer.scheduleAtFixedRate(task, 0, delay);
	}
	
	/**
	 * Stops this {@link MidCallback} and frees the associated {@link Timer}
	 */
	public void stop() {
		super.stop();
		this.timer.cancel();
		this.timer.purge();
		this.timer = null;
	}
}
