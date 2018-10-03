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
package org.eclipse.sensinact.gateway.remote.http.sample;

import org.eclipse.sensinact.gateway.common.bundle.ManagedConfigurationListener;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.Name;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.remote.http.HttpEndpoint;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class HttpEndpointManager implements ManagedConfigurationListener {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//

    class HttpEndpointDescriptor implements Nameable {
        private final String prefix;
        String remoteAddress = null;
        int remotePort = -1;
        String remotePath = null;
        String remoteToken = null;
        
        String localAddress = null;
        int localPort = -1;
        String localPath = null;
        String localToken = null;

        HttpEndpointDescriptor(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String getName() {
            return this.prefix;
        }

        public boolean complete() {
            return (remoteAddress != null && localAddress != null
            	&& remotePort > 0 && localPort > 0
            	&& localPath != null && remotePath != null
            	&& localToken != null && remoteToken != null);
        }
    }

    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//
    private Map<HttpEndpointDescriptor, HttpEndpoint> map;
    private Mediator mediator;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the HttpEndpointManager
     *                 to be instantiated to interact with the OSGi host environment
     */
    public HttpEndpointManager(Mediator mediator) {
        this.mediator = mediator;
        this.map = new HashMap<>();
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.bundle.ManagedConfigurationListener#
     * updated(java.util.Dictionary)
     */
    @Override
    public void updated(Dictionary<String, ?> properties) {
        synchronized (this.map) {
            List<HttpEndpointDescriptor> descriptors = new ArrayList<HttpEndpointDescriptor>();

            Enumeration<String> e = properties.keys();
            while (e.hasMoreElements()) {
                try {
                    String key = e.nextElement();
                    String prefix = key.substring(0, key.lastIndexOf('.'));

                    HttpEndpointDescriptor descriptor = null;
                    int index = -1;

                    if ((index = descriptors.indexOf(new Name<HttpEndpointDescriptor>(prefix))) > -1) {
                        descriptor = descriptors.get(index);

                    } else {
                        descriptor = new HttpEndpointDescriptor(prefix);
                        descriptors.add(descriptor);
                    }
                    String value = key.substring(prefix.length() + 1);
                    switch (value) {
                        case "remoteAddress":
                            descriptor.remoteAddress = (String) properties.get(key);
                            break;
                        case "remotePort":
                            descriptor.remotePort = Integer.parseInt((String) properties.get(key));
                            break;
                        case "remotePath":
                            descriptor.remotePath = (String) properties.get(key);
                            break;
                        case "remoteToken":
                            descriptor.remoteToken = (String) properties.get(key);
                            break;
                        case "localAddress":
                            descriptor.localAddress = (String) properties.get(key);
                            break;
                        case "localPort":
                            descriptor.localPort = Integer.parseInt((String) properties.get(key));
                            break;
                        case "localPath":
                            descriptor.localPath = (String) properties.get(key);
                            break;
                        case "localToken":
                            descriptor.localToken = (String) properties.get(key);
                            break;
                        default:
                            break;
                    }
                } catch (IndexOutOfBoundsException ex) {
                    continue;
                }
            }
            Iterator<Entry<HttpEndpointDescriptor, HttpEndpoint>> entryIterator = 
            		this.map.entrySet().iterator();

            while (entryIterator.hasNext()) {
                Entry<HttpEndpointDescriptor, HttpEndpoint> entry = entryIterator.next();
                HttpEndpointDescriptor descriptor = entry.getKey();

                int index = -1;
                if ((index = descriptors.indexOf(new Name<HttpEndpointDescriptor>(descriptor.prefix))) == -1 || !descriptors.remove(index).complete()) {
                    entry.getValue().close();
                    entryIterator.remove();
                }
            }
            Iterator<HttpEndpointDescriptor> iterator = descriptors.iterator();

            while (iterator.hasNext()) {
                HttpEndpointDescriptor descriptor = iterator.next();                
                if (!descriptor.complete()) {
                    continue;
                }
                final HttpEndpoint endpoint = new HttpEndpoint(mediator, 
                descriptor.localPath, descriptor.localToken, descriptor.remoteAddress, 
                descriptor.remotePort, descriptor.remotePath, descriptor.remoteToken);
                
                if (endpoint != null) {
                    this.map.put(descriptor, endpoint);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            registerEndpoint(endpoint);
                        }
                    }).start();
                }
            }
        }
    }

    private void registerEndpoint(final HttpEndpoint endpoint) {
        mediator.callService(Core.class, new Executable<Core, Void>() {
            @Override
            public Void execute(final Core core) throws Exception {
                core.createRemoteCore(endpoint);
                return null;
            }
        });
    }

    /**
     * Stops this HttpEndpointManager and disconnect all the managed
     * {@link HttpEndpoint}s
     */
    public void stop() {
        synchronized (this.map) {
            Iterator<Map.Entry<HttpEndpointDescriptor, HttpEndpoint>> iterator = 
            		this.map.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<HttpEndpointDescriptor, HttpEndpoint> entry = iterator.next();
                HttpEndpoint endpoint = entry.getValue();
                endpoint.close();
            }
            this.map.clear();
        }
    }
}
