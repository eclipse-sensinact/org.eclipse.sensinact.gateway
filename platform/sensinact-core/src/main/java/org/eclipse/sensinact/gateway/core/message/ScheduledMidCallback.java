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
import org.eclipse.sensinact.gateway.common.primitive.Nameable;

/**
 * Extended {@link BufferMidCallback} allowing to schedule the transmission of
 * the associated buffer's content independently of its filling state
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ScheduledMidCallback extends AbstractMidCallback {
	/**
	 * Mediator used to interact with the OSGi host environment
	 */
	protected final Mediator mediator;
	protected Recipient recipient;
	protected final int delay;
	private Timer timer;

	protected SnaMessage<?> lastMessage;

	/**
	 * Constructor
	 * 
	 * @param delay
	 *            the delay between two triggered event
	 */
	public ScheduledMidCallback(Mediator mediator, ErrorHandler errorHandler, Recipient recipient,
			long lifetime, int delay) {
		super(true);
		this.mediator = mediator;

		this.recipient = recipient;
		super.setErrorHandler(errorHandler);
		super.setTimeout(lifetime == ENDLESS ? ENDLESS : (System.currentTimeMillis() + lifetime));

		if (delay < 1000) {
			this.delay = 1000;

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
				try {
					synchronized (this) {
						ScheduledMidCallback.this.recipient.callback(getName(),
								new SnaMessage[] { ScheduledMidCallback.this.lastMessage });
					}
				} catch (Exception e) {
					int continuation = ScheduledMidCallback.this.getCallbackErrorHandler().handle(e);
					switch(continuation){
						case ErrorHandler.Policy.CONTINUE :
						case ErrorHandler.Policy.IGNORE:
						case ErrorHandler.Policy.ROLLBACK:
							break;
						case ErrorHandler.Policy.STOP:
							ScheduledMidCallback.this.stop();
						default:
							break;		       
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

	/**
	 * @inheritDoc
	 *
	 * @see MidCallback# register(SnaMessage)
	 */
	@Override
	public void doCallback(SnaMessage<?> message) throws MidCallbackException {
		synchronized (this) {
			this.lastMessage = message;
		}
	}

	/**
	 * @inheritDoc
	 *
	 * @see Nameable#getName()
	 */
	@Override
	public String getName() {
		return this.identifier;
	}
}
