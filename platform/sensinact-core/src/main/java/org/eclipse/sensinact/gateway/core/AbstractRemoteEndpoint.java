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
package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Sessions.SessionObserver;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Abstract implementation of a {@link RemoteEndpoint} service.
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractRemoteEndpoint implements RemoteEndpoint, SessionObserver {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//

    /**
     * Runnable in charge of maintaining the connection of
     * the {@link RemoteEndpoint} to which it belongs to and
     * the one held by a remote sensiNact instance
     */
    class ConnectionThread implements Runnable {
        boolean running = false;

        /**
         * Stops this Runnable process
         */
        void stop() {
            this.running = false;
        }

        /**
         * @inheritDoc
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            running = true;
            while (running) {
                if (AbstractRemoteEndpoint.this.connected) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        break;
                    }
                } else {
                    AbstractRemoteEndpoint.this.doOpen();
                }

            }
        }

    }
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//

    /**
     * Transmits the subscription request to the remote connected
     * {@link RemoteEndpoint}
     *
     * @param publicKey         the String public key of the {@link Session} requiring
     *                          for the subscription to be created
     * @param serviceProviderId the service provider String identifier
     * @param serviceId         the service String identifier
     * @param resourceId        the resource String identifier
     * @param conditions        the JSON formated list of constraints applying
     *                          on the subscription to be created
     * @return the JSON formated subscription response
     */
    protected abstract JSONObject doSubscribe(String publicKey, String serviceProviderId, String serviceId, String resourceId, JSONArray conditions);

    /**
     * Asks for the close of the remote {@link Session} whose String public
     * key is passed as parameter
     *
     * @param publicKey the String public key of the remote {@link Session}
     *                  to be closed
     */
    protected abstract void closeSession(String publicKey);

    /**
     * Connects this {@link RemoteEndpoint} to a remote sensiNact
     * gateway instance
     *
     * @return true if the connection has been made properly; false
     * otherwise
     */
    protected abstract void doOpen();

    /**
     * Disconnects the {@link RemoteCore} previously connected from
     * a remote sensiNact gateway instance it is connected to
     */
    protected abstract void doClose();

    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    protected final boolean automaticReconnection;
    protected final Mediator mediator;

    protected RemoteCore remoteCore;
    protected boolean connected;

    protected Map<String, Recipient> recipients;
    private ConnectionThread connectionThread;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the {@link
     *                 RemoteEndpoint} to be instantiated to interact with the
     *                 OSGi host environment
     */
    public AbstractRemoteEndpoint(Mediator mediator) {
        this(mediator, true);
    }

    /**
     * Constructor
     *
     * @param mediator              the {@link Mediator} allowing the {@link
     *                              RemoteEndpoint} to be instantiated to interact with the
     *                              OSGi host environment
     * @param automaticReconnection defines whether the  {@link
     *                              RemoteEndpoint} to be instantiated will try to reconnect
     *                              when the connection is closed unexpectedly
     */
    public AbstractRemoteEndpoint(Mediator mediator, boolean automaticReconnection) {
        this.mediator = mediator;
        this.recipients = new HashMap<String, Recipient>();
        this.connected = false;
        this.connectionThread = null;
        this.automaticReconnection = automaticReconnection;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.RemoteEndpoint#
     * open(org.eclipse.sensinact.gateway.core.RemoteCore)
     */
    @Override
    public void open(RemoteCore remoteCore) {
        if (this.connected) {
            mediator.debug("Endpoint already connected");
            return;
        }
        this.remoteCore = remoteCore;
        if (this.automaticReconnection) {
            this.connectionThread = new ConnectionThread();
            new Thread(connectionThread).start();

        } else {
            this.doOpen();
        }
    }

    /**
     * This method is called when this {@link RemoteEndpoint}
     * is connected to the remote one
     */
    protected void connected() {
        if (this.connected) {
            return;
        }
        this.connected = true;
        this.remoteCore.connect(this.namespace());
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.RemoteEndpoint#close()
     */
    @Override
    public void close() {
        this.connectionThread.stop();
        this.doClose();
        this.connected = false;
    }

    /**
     * This method is called when this AbstractRemoteEndpoint
     * is disconnected from the remote one
     */
    protected void disconnected() {
        if (!this.connected) {
            return;
        }
        this.remoteCore.disconnect();
        this.connected = false;
    }

    /**
     * @inheritDoc
     * @see fr.cea.sna.gateway.core.RemoteEndpoint#
     * jsonSubscribe(java.lang.String, java.lang.String, java.lang.String,
     * fr.cea.sna.gateway.core.model.message.Recipient, org.json.JSONArray)
     */
    @Override
    public JSONObject subscribe(String publicKey, String serviceProviderId, String serviceId, String resourceId, Recipient recipient, JSONArray conditions) {
        if (!this.connected) {
            return null;
        }
        JSONObject response = this.doSubscribe(publicKey, serviceProviderId, serviceId, resourceId, conditions);
        try {
            this.recipients.put(response.getJSONObject("response").getString("subscriptionId"), recipient);

        } catch (Exception e) {
            mediator.error(e.getMessage(), e);
        }
        return response;
    }

    /**
     * Returns the String namespace of the local sensiNact instance
     * this {@link RemoteEndpoint} is connected to, through the {@link
     * RemoteCore}
     *
     * @return the local sensiNact gateway's String namespace
     */
    public String getLocalNamespace() {
        if (this.remoteCore == null) {
            return null;
        }
        return this.remoteCore.namespace();
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Sessions.SessionObserver#
     * disappearing(java.lang.String)
     */
    @Override
    public void disappearing(String publicKey) {
        if (!this.connected) {
            return;
        }
        this.closeSession(publicKey);
    }
}

