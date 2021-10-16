/*
* Copyright (c) 2020-2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.http;

import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.InvalidProtocolStackException;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.Task.RequestType;
import org.eclipse.sensinact.gateway.protocol.http.Headers;
import org.eclipse.sensinact.gateway.protocol.http.HeadersCollection;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpDiscoveryTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.xml.sax.SAXException;

/**
 * Extended abstract {@link ProtocolStackEndpoint} dedicated to devices using
 * the HTTP protocol
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class HttpProtocolStackEndpoint extends ProtocolStackEndpoint<HttpPacket> {
	
    /**
     * permanent header fields added to each request
     */
    protected Headers permanentHeaders;

    /**
     * the {@link HttpDiscoveryTask}s executed at connection time
     */
    protected Deque<HttpDiscoveryTask<?, ?>> discovery;

    /**
     * the {@link HttpDiscoveryTask}s executed at connection time
     */
    protected Deque<HttpTask<?, ?>> disconnexion;

    /**
     * the extended handled {@link HttpPacket}
     */
    protected Class<? extends HttpPacket> packetType;
    
    /**
     * this HttpProtocolStackEndpoint's stopping status
     */
    private boolean stopping;

    /**
     * Constructor
     * 
     * @param mediator
     * 
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public HttpProtocolStackEndpoint(Mediator mediator) 
    	throws ParserConfigurationException, SAXException, IOException {
        super(mediator);
        this.stopping = false;

        this.permanentHeaders = new HeadersCollection();
        this.discovery = new LinkedList<HttpDiscoveryTask<?, ?>>();
        this.disconnexion = new LinkedList<HttpTask<?, ?>>();
    }

    @Override
    public void send(Task task) {
    	HttpTask<?,?> _task =  (HttpTask<?,?>)task;        
        _task.addHeaders(this.permanentHeaders.getHeaders());
        
        if (_task.getPacketType() == null)
            _task.setPacketType(packetType);
        
        try {
            Request<HttpResponse> request = (Request<HttpResponse>) _task.build();
            HttpResponse response = request.send();
            if (response == null) {
                mediator.error("Unable to connect");
                return;
            }
            if (!_task.isDirect()) {
                HttpPacket packet = response.createPacket();
                this.process(packet);
            } else 
            	_task.setResult(new String(response.getContent()));            
        } catch (Exception e) {
        	e.printStackTrace();
            super.mediator.error(e);
        }
    }

    @Override
    public void connect(ExtModelConfiguration<HttpPacket> manager) throws InvalidProtocolStackException {
        this.packetType = (Class<? extends HttpPacket>) manager.getPacketType();

        super.connect(manager);

        Iterator<HttpDiscoveryTask<?, ?>> iterator = this.discovery.iterator();

        while (iterator.hasNext()) {
            HttpDiscoveryTask<?, ?> discoveryTask = iterator.next();
            if (discoveryTask != null) {
                super.connector.execute(discoveryTask);
                long wait = discoveryTask.getTimeout();
                while (!discoveryTask.isResultAvailable() && wait > 0) {
                    try {
                        Thread.sleep(150);
                        wait -= 150;
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        if (this.mediator.isErrorLoggable()) {
                            this.mediator.error(e.getMessage(), e);
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Registers a permanent header field value that will be added to
     * each request build by this HttpProtocolStackEndpoint
     *
     * @param header header field name for which to add a permanent value
     * @param value  the permanent header field value to add
     */
    public void registerPermanentHeader(String header, String value) {
        if (header != null && value != null) {
            this.permanentHeaders.addHeader(header, value);
        }
    }

    /**
     * Registers an {@link HttpDiscoveryTask} to this HttpProtocolStackEndpoint
     * the registered {@link HttpDiscoveryTask}s are the first executed at
     * connection time
     *
     * @param task the {@link HttpDiscoveryTask} to register
     */
    public void registerDiscoveryTask(HttpDiscoveryTask<?, ?> task) {
        if (task != null) {
            this.discovery.add(task);
        }
    }    
    
    /**
     * Registers an {@link HttpTask} to this HttpProtocolStackEndpoint
     * the registered {@link HttpTask}s are the last executed at
     * stopping time
     *
     * @param task the {@link HttpTask} to register
     */
    public void registerDisconnexionTask(HttpTask<?, ?> task) {
        if (task != null) {
            this.disconnexion.add(task);
        }
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.URI;
    }

    /**
     * Stops this {@link ProtocolStackEndpoint} and its
     * associated {@link Connector}
     */
    public void stop() {
        this.stopping = true;
        Iterator<HttpTask<?, ?>> iterator = this.disconnexion.iterator();
        while (iterator.hasNext()) {
            HttpTask<?, ?> disconnexionTask = iterator.next();
            if (disconnexionTask != null) {
                super.connector.execute(disconnexionTask);
                long wait = disconnexionTask.getTimeout();
                while (!disconnexionTask.isResultAvailable() && wait > 0) {
                    try {
                        Thread.sleep(150);
                        wait -= 150;
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        if (this.mediator.isErrorLoggable()) {
                            this.mediator.error(e.getMessage(), e);
                        }
                        break;
                    }
                }
            }
        }
        if (super.connector != null) {
            super.connector.stop();
        }
    }
}
