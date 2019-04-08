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
package org.eclipse.sensinact.gateway.agent.storage.internal;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfiguration;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.protocol.http.client.Response;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.eclipse.sensinact.gateway.util.crypto.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * HTTP Agent dedicated to storage service
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
class StorageConnection {
	private static final Logger LOG = LoggerFactory.getLogger(StorageConnection.class);

	private String authorization;
	private String uri;
	private Mediator mediator;
	Stack stack;
	private boolean running = false;

	/**
	 * Constructor
	 *
	 * @param mediator the associated {@link Mediator}
	 * @param uri      the string URI of the storage server
	 * @param login    the user login
	 * @param password the user password
	 * @throws IOException Exception on connection problem
	 */
	StorageConnection(Mediator mediator, String uri, String login, String password) throws IOException {
		this.mediator = mediator;
		this.uri = uri;
		this.authorization = Base64.encodeBytes((login + ":" + password).getBytes());
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
							if (! shortSleep(200)) {
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

	/**
	 * Executes the HTTP request defined by the method, target, headers and entity
	 * arguments
	 */
	private void sendRequest(JSONObject object) {
		ConnectionConfiguration<SimpleResponse, SimpleRequest> configuration = new ConnectionConfigurationImpl<>();
		try {
			configuration.setContentType("application/json");
			configuration.setAccept("application/json");
			configuration.setUri(this.uri);
			configuration.setContent(object.toString());
			configuration.setHttpMethod("POST");
			configuration.addHeader("Authorization", "Basic " + authorization);
			configuration.addHeader("User-Agent", "java/sensiNact-storage");
			Request request = new SimpleRequest(configuration);
			Response response = request.send();
			if (mediator.isDebugLoggable()) {
				this.mediator.debug(" >> response status code: " + response.getStatusCode());
			}
			Iterator<Map.Entry<String, List<String>>> iterator = response.getHeaders().entrySet().iterator();
			if (!iterator.hasNext()) {
				return;
			}
			Map.Entry<String, List<String>> entry = iterator.next();
			for (; iterator.hasNext(); entry = iterator.next()) {
				this.mediator.debug(entry.getKey() + " :: " + (entry.getValue() == null ? "null"
						: Arrays.toString(entry.getValue().toArray(new String[0]))));
			}
		} catch (Exception e) {
			LOG.error("Can't send request", e);
			if (this.mediator.isErrorLoggable()) {
				this.mediator.error(e.getMessage(), e);
			}
		}
	}

	void close() {
		for (int i=0; i< 10_000 && !this.stack.isEmpty(); i++) {
			if (! shortSleep(200)) {
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
