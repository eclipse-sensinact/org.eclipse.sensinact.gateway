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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Abstract implementation of a {@link StackEnginetHandler}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractStackEngineHandler<E> implements StackEngineHandler<E> {
    /**
     * the {@link StackEngine} stacking the <code>&lt;E&gt;</code>
     * typed elements
     */
    protected final StackEngine<E, StackEngineHandler<E>> eventEngine;

    /**
     * Constructor
     */
    //TODO : allow restart by defining a separated start method
    public AbstractStackEngineHandler() {
        //instantiate the engine
        this.eventEngine = new StackEngine<E, StackEngineHandler<E>>(this, getWorker());
    }

    /**
     * Stops
     */
    public void stop() {
        //stop the engine
        this.eventEngine.stop();
        try {
            this.eventEngine.awaitTermination();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            releaseWorker();
        }
    }

    /**
     * Stops
     *
     * @throws InterruptedException
     */
    public void close() {
        //wait for the stack emptiness for stopping
        this.eventEngine.closeWhenEmpty();
        try {
            this.eventEngine.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            releaseWorker();
        }
    }
    
    private static ScheduledExecutorService sharedExecutor;
    private static long referenceCount = 0;
    
    private static ScheduledExecutorService getWorker() {
        synchronized (AbstractStackEngineHandler.class) {
            referenceCount++;
            
            if(sharedExecutor == null) {
                ScheduledThreadPoolExecutor worker = new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "Stack Engine Worker Thread"));
                worker.setMaximumPoolSize(8);
                sharedExecutor = worker;
            }
            return sharedExecutor;
        }
    }

    private static void releaseWorker() {
        synchronized (AbstractStackEngineHandler.class) {
            referenceCount--;
            
            if(referenceCount == 0) {
                sharedExecutor.shutdownNow();
                sharedExecutor = null;
            }
        }
    }
    
}
