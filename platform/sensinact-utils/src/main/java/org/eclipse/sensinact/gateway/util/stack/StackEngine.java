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
package org.eclipse.sensinact.gateway.util.stack;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Stack of <code>&lt;E&gt;</code> elements connected to an
 * handler to which elements are one by one transmitted.
 * The purpose of this class is asynchronous treatment
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class StackEngine<E, H extends StackEngineHandler<E>> {
    private final int UNLIMITED_SIZE = -1;
    private final Object lock = new Object();
    private int maxStackSize;

    private final Deque<E> elements;
    private final H handler;
    private final AtomicBoolean locked;

    private final Semaphore semaphore;
    private final CountDownLatch completionLatch;
    private final AtomicBoolean running;
    private final AtomicBoolean closing;
    
    private final ScheduledExecutorService worker;
    
    private ScheduledFuture<?> unlockTask;
    
    /**
     * Constructor
     */
    StackEngine(H handler, ScheduledExecutorService worker) {
        this.handler = handler;
        this.elements = new LinkedList<E>();
        this.semaphore = new Semaphore(1);
        this.completionLatch = new CountDownLatch(1);
        this.running = new AtomicBoolean(true);
        this.closing = new AtomicBoolean(false);
        this.locked = new AtomicBoolean(false);
        this.worker = worker;
        this.maxStackSize = UNLIMITED_SIZE;
        requestProcessingIfNeeded();
    }
    
    void requestProcessingIfNeeded() {
        worker.schedule(this::_requestProcessingIfNeeded, 100, TimeUnit.MILLISECONDS);
    }
    
    void _requestProcessingIfNeeded() {
        if(running.get() && semaphore.tryAcquire()) {
            worker.execute(this::dequeue);
        }
    }

    /**
     * Dequeue and handle up to 5 items before releasing the
     * thread so other StackEngine instances may do their work
     * too
     */
    void dequeue() {
        for(int i = 0; i < 5; i++) {
            E element = pop();
            try {
                if (element != null) {
                    handler.doHandle(element);
                } else {
                    if (closing.get()) {
                        stop();
                    }
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                stop();
                break;
            }
        }
        
        boolean runAgain;
        synchronized (lock) {
            runAgain = !elements.isEmpty();
        }
        
        if(runAgain) {
            worker.execute(this::dequeue);
        } else {
            semaphore.release();
        }
        
        if(!running()) {
            completionLatch.countDown();
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
        requestProcessingIfNeeded();
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
        requestProcessingIfNeeded();
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
            element = this.elements.pollFirst();
        }
        return element;
    }

    /**
     * Stops this StackEngine
     */
    void stop() {
        synchronized (lock) {
            this.running.set(false);
            elements.clear();
        }
        if(semaphore.tryAcquire()) {
            completionLatch.countDown();
        }
    }

    /**
     * Returns this StackEngine's running
     * state
     *
     * @return the running state of this
     * StackEngine
     */
    boolean running() {
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
    void unlock() {
        synchronized (lock) {
            if(Thread.interrupted()) {
                return;
            }
            
            if (unlockTask != null) {
                unlockTask.cancel(false);
                unlockTask = null;
            }
            locked.set(false);
        }
    }

    /**
     * Defines this StackEngine's lock state as
     * true for the delay specified as parameter
     *
     * @param delay the lock delay of this StackEngine
     */
    public void locked(long delay) {
        synchronized (lock) {
            if (unlockTask != null) {
                unlockTask.cancel(true);
                unlockTask = null;
            }
            unlockTask = worker.schedule(this::unlock, delay, TimeUnit.MILLISECONDS);
            this.locked.set(true);
        }
    }

    /**
     * Waits until the stack is empty for closing
     * it
     */
    void closeWhenEmpty() {
        this.closing.set(true);
        synchronized (lock) {
            if(elements.isEmpty()) {
                this.running.set(false);
            }
        }
        
        if(semaphore.tryAcquire()) {
            completionLatch.countDown();
        }
    }
    
    void awaitTermination() throws InterruptedException {
        completionLatch.await(100, TimeUnit.MILLISECONDS);
    }

    void awaitTermination(long time, TimeUnit unit) throws InterruptedException {
        completionLatch.await(time, unit);
    }
}