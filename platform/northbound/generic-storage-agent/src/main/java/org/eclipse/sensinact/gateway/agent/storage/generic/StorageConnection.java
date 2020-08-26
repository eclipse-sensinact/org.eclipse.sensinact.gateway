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
package org.eclipse.sensinact.gateway.agent.storage.generic;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * HTTP Agent dedicated to storage service
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class StorageConnection {

    private static final Logger LOG = LoggerFactory.getLogger(StorageConnection.class);

    protected Mediator mediator;
    protected String login, password;
    protected Stack stack;
    
    private boolean running = false;

    /**
     * Send the request described by the {@link JSONObject} passed as 
     * parameter 
     *
     * @param object the {@link JSONObject} describing the request to be sent
     */
    protected abstract void sendRequest(JSONObject object);

    /**
     * Constructor
     *
     * @param mediator the associated {@link Mediator}
     * @param uri the string URI of the storage server
     * @param login the user login
     * @param password the user password
     * @throws IOException Exception on connection problem
     */
    public StorageConnection(Mediator mediator, String login, String password) throws IOException {
        this.mediator = mediator;
        this.login = login;
        this.password = password;
        this.stack = new Stack();
        this.running = true;

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                LOG.info("POP thread started");
                while (running) {
                    try {
                        JSONObject element = stack.pop();
                        if (element != null) {
                            sendRequest(element);
                        } else {
                            if (!shortSleep(200)) {
                                running = false;
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("POP thread error", e);
                    }
                }
                LOG.info("POP thread terminated");
            }
        };
        new Thread(runner).start();
    }

    protected void close() {
        for (int i = 0; i < 10_000 && !this.stack.isEmpty(); i++) {
            if (!shortSleep(200)) {
                return;
            }
        }
        LOG.info("close operation ended");
        this.running = false;
    }

    private boolean shortSleep(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            LOG.error("Sleep operation error", e);
            Thread.interrupted();
            return false;
        }
    }
}
