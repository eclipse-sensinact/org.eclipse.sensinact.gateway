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
package org.eclipse.sensinact.gateway.agent.storage.generic;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes the effective storage in the linked data store
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class StorageConnection {

    private static final Logger LOG = LoggerFactory.getLogger(StorageConnection.class);

    protected Mediator mediator;
    protected Stack stack;
    
    private boolean running = false;

    /**
     * Store the JSON formated data passed as parameter 
     *
     * @param object the {@link JSONObject} wrapping the data to be stored
     */
    protected abstract void store(JSONObject object);

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the StorageConnection to be 
     * instantiated to interact with the OSGi host environment 
     */
    public StorageConnection(Mediator mediator) {
        this.mediator = mediator;
        this.stack = new Stack();
        this.running = true;

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                while (running) {
                    try {
                        JSONObject element = stack.pop();
                        if (element != null) {
                            store(element);
                        } else {
                            if (!shortSleep(200)) {
                                running = false;
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("POP thread error", e);
                    }
                }
            }
        };
        new Thread(runner).start();
    }

    protected void close() {
        for (int i = 0; i < 10_000 && !this.stack.isEmpty(); i++) {
            if (!shortSleep(200))
                break;
        }
        this.running = false;
        LOG.info("close operation ended");
    }

    private boolean shortSleep(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            Thread.interrupted();
            return false;
        }
    }
}
