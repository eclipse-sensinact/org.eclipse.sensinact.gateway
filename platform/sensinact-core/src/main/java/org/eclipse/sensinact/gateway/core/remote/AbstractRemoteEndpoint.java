/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.remote;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.Sessions.SessionObserver;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

/**
 * Abstract implementation of a {@link RemoteEndpoint} service.
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractRemoteEndpoint implements RemoteEndpoint, SessionObserver {
	
	private static final Logger LOG=LoggerFactory.getLogger(AbstractRemoteEndpoint.class);

	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	/**
	 * Runnable in charge of maintaining the connection of the
	 * {@link RemoteEndpoint} to which it belongs to and the one held by a remote
	 * sensiNact instance
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
		 *
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			running = true;
			while (running) {
				boolean connected = AbstractRemoteEndpoint.this.getConnected();
				if(!connected) {
					AbstractRemoteEndpoint.this.doOpen();
				}
				try {						
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					Thread.interrupted();
					break;
				}
			}
		}

	}
	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	/**
	 * Transmits the subscription request to the remote connected
	 * {@link RemoteEndpoint}
	 * 
	 * @param publicKey
	 *            the String public key of the {@link Session} requiring for the
	 *            subscription to be created
	 * @param serviceProviderId
	 *            the service provider String identifier
	 * @param serviceId
	 *            the service String identifier
	 * @param resourceId
	 *            the resource String identifier
	 * @param conditions
	 *            the JSON formated list of constraints applying on the subscription
	 *            to be created
	 * 
	 * @return the JSON formated subscription response
	 */
	protected abstract JsonObject doSubscribe(String publicKey, String serviceProviderId, String serviceId,
			String resourceId, JsonArray conditions);

	/**
	 * Asks for the close of the remote {@link Session} whose String public key is
	 * passed as parameter
	 * 
	 * @param publicKey
	 *            the String public key of the remote {@link Session} to be closed
	 */
	protected abstract void closeSession(String publicKey);

	/**
	 * Connects this {@link RemoteEndpoint} to a remote sensiNact gateway instance
	 * 
	 * @return true if the connection has been made properly; false otherwise
	 */
	protected abstract void doOpen();

	/**
	 * Disconnects the {@link RemoteCore} previously connected from a remote
	 * sensiNact gateway instance it is connected to
	 */
	protected abstract void doClose();

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	private final Object lock = new Object();
	
	private volatile boolean connected;
	
	protected final boolean automaticReconnection;
	protected final Mediator mediator;

	protected RemoteCore remoteCore;

	protected Map<String, Recipient> recipients;
	private ConnectionThread connectionThread;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing the {@link RemoteEndpoint} to be
	 *            instantiated to interact with the OSGi host environment
	 */
	public AbstractRemoteEndpoint(Mediator mediator) {
		this(mediator, true);
	}

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing the {@link RemoteEndpoint} to be
	 *            instantiated to interact with the OSGi host environment
	 * @param automaticReconnection
	 *            defines whether the {@link RemoteEndpoint} to be instantiated will
	 *            try to reconnect when the connection is closed unexpectedly
	 */
	public AbstractRemoteEndpoint(Mediator mediator, boolean automaticReconnection) {
		this.mediator = mediator;
		this.recipients = new HashMap<String, Recipient>();
		this.connected = false;
		this.connectionThread = null;
		this.automaticReconnection = automaticReconnection;
	}

	/**
	 * @return
	 */
	public boolean getConnected() {
		boolean connected = false;
		synchronized(lock) {			
			connected = this.connected;
		}
		return connected;
	}

	/**
	 * @param connected
	 */
	private void setConnected(boolean connected) {
		synchronized(lock) {			
			this.connected = connected;
		}
	}
	
	@Override
	public void open(RemoteCore remoteCore) {
		if (this.getConnected()) {
			LOG.debug("Endpoint already connected");
			return;
		}
		this.remoteCore = remoteCore;
		if (this.automaticReconnection && 
			(this.connectionThread==null || !this.connectionThread.running)) {
			this.connectionThread = new ConnectionThread();
			new Thread(connectionThread).start();

		} else {
			this.doOpen();
		}
	}

	/**
	 * This method is called when this {@link RemoteEndpoint} is connected to the
	 * remote one
	 */
	protected void connected() {
		this.setConnected(true);
		this.remoteCore.connect(this.namespace());
	}

	@Override
	public void close() {
		if(this.connectionThread != null) {
			this.connectionThread.stop();
		}
		this.doClose();
		this.setConnected(false);
	}

	/**
	 * This method is called when this AbstractRemoteEndpoint is disconnected from
	 * the remote one
	 */
	protected void disconnected() {
		if (!this.getConnected()) {
			return;
		}
		this.setConnected(false);
		this.remoteCore.disconnect();
	}

	@Override
	public JsonObject subscribe(String publicKey, String serviceProviderId, String serviceId, String resourceId,
			Recipient recipient, JsonArray conditions) {
		if (!this.getConnected()) {
			return null;
		}
		JsonObject response = this.doSubscribe(publicKey, serviceProviderId, serviceId, resourceId, conditions);
		try {
			this.recipients.put(response.getJsonObject("response").getString("subscriptionId"), recipient);

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return response;
	}

	/**
	 * Returns the String namespace of the local sensiNact instance this
	 * {@link RemoteEndpoint} is connected to, through the {@link RemoteCore}
	 * 
	 * @return the local sensiNact gateway's String namespace
	 */
	public String getLocalNamespace() {
		if (this.remoteCore == null) {
			return null;
		}
		return this.remoteCore.namespace();
	}

	@Override
	public void disappearing(String publicKey) {
		if (!this.getConnected()) {
			return;
		}
		this.closeSession(publicKey);
	}
}