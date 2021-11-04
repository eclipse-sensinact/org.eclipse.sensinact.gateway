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
package org.eclipse.sensinact.gateway.generic;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allow to desynchronize task processing
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class TaskDesynchronizer implements Runnable {
	
	private static final Logger LOG = LoggerFactory.getLogger(TaskDesynchronizer.class);
    private final LinkedList<TaskManager> listeners;
    private final AtomicInteger leftTasks;
    private boolean running = true;
    /**
     * Constructor
     */
    public TaskDesynchronizer() {
        this.listeners = new LinkedList<TaskManager>();
        // default initial status is unlocked
        this.leftTasks = new AtomicInteger(1);
    }

    /**
     * Defines whether this TokenEventProvider implementation instance processes
     * the next requirement as soon as it has been registered or if it is locked
     * and waits for a freeing event first
     *
     * @param locked <p/>
     *               true if the current TokenEventProvider implementation instance
     *               is locked.
     *               <p/>
     *               false otherwise
     */
    public void setLocked(boolean locked) {
        synchronized (this.leftTasks) {
            this.leftTasks.set(locked ? 0 : 1);
        }
    }

    /**
     * The token is asked by the {@link Desynchronizable} passed as parameter
     * for a single task
     *
     * @param listener the {@link Desynchronizable} asking for the token
     */
    public void require(TaskManager desynchronizedService) {
        this.require(desynchronizedService, 1);
    }

    /**
     * The token is asked by the {@link Desynchronizable} passed as parameter
     * for a batch of "count" task(s) treatment(s)
     *
     * @param count    the number of task reservations
     * @param listener the {@link Desynchronizable} asking for the token
     */
    public void require(TaskManager listener, int count) {
        synchronized (this.leftTasks) {
            for (int i = 0; i < count; i++) {
                this.listeners.offer(listener);
            }
        }
    }

    /**
     * the current TokenEventProvider implementation instance is
     * informed about the freeing of the token by the last {@link
     * Desynchronizable} which was using it
     */
    public void freeingToken() {
        synchronized (this.leftTasks) {
            this.leftTasks.incrementAndGet();
        }
    }

    /**
     * stop the current {@link Runnable} TokenEventProvider
     * implementation instance
     */
    public void stop() {
        this.running = false;
    }

    /**
     * @inheritDoc
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        TaskManager listener = null;
        while (running) {
            listener = null;
            synchronized (this.leftTasks) {
                if (this.leftTasks.get() > 0 && (listener = this.listeners.poll()) != null) {
                    this.leftTasks.decrementAndGet();
                }
            }
            if (listener != null) {
                listener.nextTask();

            } else {
                try {
                    Thread.sleep(150);

                } catch (InterruptedException e) {
                    running = false;
                    if (LOG.isErrorEnabled()) {
                        LOG.error( e.getMessage(), e);
                    }
                    Thread.interrupted();
                }
            }
        }
    }
}
