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
package org.eclipse.sensinact.gateway.util.stack;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Stack of <code>&lt;E&gt;</code> elements connected to an
 * handler to which elements are one by one transmitted.
 * The purpose of this class is asynchronous treatment
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class StackEngine<E, H extends StackEngineHandler<E>> implements Runnable {
    private final int UNLIMITED_SIZE = -1;
    private final Object lock = new Object();
    private int maxStackSize;

    private final Deque<E> elements;
    private final H handler;
    private final AtomicBoolean running;
    private final AtomicBoolean closing;
    private final AtomicBoolean locked;
    private TimerTask timer;


    /**
     * Constructor
     */
    public StackEngine(H handler) {
        this.handler = handler;
        this.elements = new LinkedList<E>();
        this.running = new AtomicBoolean(false);
        this.closing = new AtomicBoolean(false);
        this.locked = new AtomicBoolean(false);
        this.maxStackSize = UNLIMITED_SIZE;
    }

    /**
     * @inheritDoc
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        synchronized (lock) {
            this.running.set(true);
        }
        while (running()) {
            E element = pop();
            try {
                if (element != null) {
                    handler.doHandle(element);

                } else {
                    if (closing.get()) {
                        stop();
                        break;
                    }
                    try {
                        Thread.sleep(150);

                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        stop();
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                stop();
                break;
            }
        }
        synchronized (lock) {
            this.elements.clear();
        }
    }

    /**
     * Puts the element  passed as
     * parameter to the tail of the events list
     *
     * @param event the element to store
     */
    public void push(E element) {
        if (this.closing.get()) {
            return;
        }
        if (element == null) {
            return;
        }
        int maxSize = UNLIMITED_SIZE;

        if ((maxSize = this.getMaxStackSize()) != UNLIMITED_SIZE && this.length() == maxSize) {
            return;
        }
        synchronized (lock) {
            this.elements.addLast(element);
        }
    }

    /**
     * Puts the element  passed as
     * parameter to the tail of the events list
     *
     * @param event the element to store
     */
    public void pushFirst(E element) {
        if (this.closing.get()) {
            return;
        }
        if (element == null) {
            return;
        }
        int maxSize = UNLIMITED_SIZE;

        if ((maxSize = this.getMaxStackSize()) != UNLIMITED_SIZE && this.length() == maxSize) {
            return;
        }
        synchronized (lock) {
            this.elements.addFirst(element);
        }
    }

    /**
     * Defines the maximum size of the Stack of <code>&lt;E&gt;</code>
     * typed elements of this StackEngine. If a new <code>&lt;E&gt;</code>
     * typed element is received while the stack has reached
     * its maximum size the oldest one is removed to allow the
     * last received to be put over the stack
     *
     * @param stackSize the maximum size of the Stack of
     *                  <code>&lt;E&gt;</code> typed elements
     *                  of this StackEngine
     */
    public void setMaxStackSize(int stackSize) {
        if (stackSize <= 0) {
            this.maxStackSize = UNLIMITED_SIZE;
            return;
        }
        this.maxStackSize = stackSize;
    }

    /**
     * Returns the maximum size of the Stack of <code>&lt;E&gt;</code>
     * typed elements of this StackEngine.
     *
     * @return the maximum size of the Stack of
     * <code>&lt;E&gt;</code> typed elements
     * of this StackEngine
     */
    public int getMaxStackSize() {
        return this.maxStackSize;
    }

    /**
     * Returns the number of <code>&lt;E&gt;</code> elements
     * stored in the stack
     *
     * @return the length of the stack
     */
    public int length() {
        int length = 0;
        synchronized (lock) {
            length = this.elements.size();
        }
        return length;
    }

    /**
     * Removes and returns the element
     * from the head of the events list
     *
     * @return the first of stored elements
     */
    E pop() {
        E element = null;
        if (locked()) {
            return element;
        }
        synchronized (lock) {
            if (!this.elements.isEmpty()) {
                element = this.elements.removeFirst();
            }
        }
        return element;
    }

    /**
     * Stops this StackEngine
     */
    public void stop() {
        synchronized (lock) {
            this.running.set(false);
        }
    }

    /**
     * Returns this StackEngine's running
     * state
     *
     * @return the running state of this
     * StackEngine
     */
    public boolean running() {
        boolean running = false;
        synchronized (lock) {
            running = this.running.get();
        }
        return running;
    }

    /**
     * Returns this StackEngine's lock
     * state
     *
     * @return the lock state of this
     * StackEngine
     */
    public boolean locked() {
        boolean locked = false;
        synchronized (lock) {
            locked = this.locked.get();
        }
        return locked;
    }

    /**
     * Sets this StackEngine's lock
     * state
     *
     * @param locked the lock state of this
     *               StackEngine
     */
    protected void locked(boolean locked) {
        synchronized (lock) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            this.locked.set(locked);
        }
    }

    /**
     * Defines this StackEngine's lock state as
     * true for the delay specified as parameter
     *
     * @param delay
     * 		the lock delay of this StackEngine
     */
    /**
     * @param delay
     */
    public void locked(long delay) {
        synchronized (lock) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            timer = new TimerTask() {
                @Override
                public void run() {
                    StackEngine.this.locked(false);
                }
            };
            new Timer().schedule(timer, delay);
            this.locked.set(true);
        }
    }

    /**
     * Waits until the stack is empty for closing
     * it
     */
    public void closeWhenEmpty() {
        this.closing.set(true);
    }
}