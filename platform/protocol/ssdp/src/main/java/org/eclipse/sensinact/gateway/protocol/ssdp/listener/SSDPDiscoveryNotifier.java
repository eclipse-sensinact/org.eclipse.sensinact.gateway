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
package org.eclipse.sensinact.gateway.protocol.ssdp.listener;

import org.eclipse.sensinact.gateway.protocol.ssdp.api.SSDPDiscoveryListenerItf;
import org.eclipse.sensinact.gateway.protocol.ssdp.api.SSDPDiscoveryNotifierItf;
import org.eclipse.sensinact.gateway.protocol.ssdp.description.SSDPDescriptionRequest;
import org.eclipse.sensinact.gateway.protocol.ssdp.model.SSDPDescriptionPacket;
import org.eclipse.sensinact.gateway.protocol.ssdp.model.SSDPMessage;
import org.eclipse.sensinact.gateway.protocol.ssdp.model.SSDPReceivedMessage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class receives the SSDP events from {@link SSDPDiscoveryListenerThread} and
 * {@link SSDPMulticastListenerThread} before notifying the registered listeners
 */
public class SSDPDiscoveryNotifier implements SSDPDiscoveryNotifierItf {
    private ServiceReference registration;
    private Map<SSDPDiscoveryListenerItf, String> listeners;
    private Map<String, SSDPDescriptionPacket> discoveredDevices;

    /**
     * Constructor.
     * Registers itself to the OSGi registry.
     *
     * @param context the bundle context
     */
    public SSDPDiscoveryNotifier(BundleContext context) {
        this.listeners = new HashMap<SSDPDiscoveryListenerItf, String>();
        this.discoveredDevices = new HashMap<String, SSDPDescriptionPacket>();
        this.registration = context.registerService(SSDPDiscoveryNotifierItf.class, this, null).getReference();
    }

    /**
     * Unregister the notifier from the OSGi registry
     */
    public void stop(BundleContext context) {
        context.ungetService(registration);
    }

    /**
     * @inheritDoc
     */
    public void addListener(SSDPDiscoveryListenerItf listener, String filter) {
        listeners.put(listener, filter);
    }

    /**
     * @inheritDoc
     */
    public void removeListener(SSDPDiscoveryListenerItf listener) {
        listeners.remove(listener);
    }

    /**
     * @inheritDoc
     */
    public List<SSDPDescriptionPacket> getDescriptions(String filter) {
        List<SSDPDescriptionPacket> descriptionPackets = new ArrayList<SSDPDescriptionPacket>();
        Pattern pattern = Pattern.compile(filter);
        for (Map.Entry<String, SSDPDescriptionPacket> map : discoveredDevices.entrySet()) {
            Matcher matcher = pattern.matcher(map.getKey());
            if (matcher.find()) {
                descriptionPackets.add(map.getValue());
            }
        }
        return descriptionPackets;
    }

    /**
     * Notify listeners when a new SSDP message is received
     *
     * @param message the received message
     */
    public void newSSDPPacket(SSDPMessage message) {
        if (message instanceof SSDPReceivedMessage) {
            if (((SSDPReceivedMessage) message).getLocation() != null) {
                SSDPDescriptionPacket descriptionPacket = SSDPDescriptionRequest.getDescription((SSDPReceivedMessage) message);
                for (Map.Entry<SSDPDiscoveryListenerItf, String> map : listeners.entrySet()) {
                    discoveredDevices.put(descriptionPacket.getFriendlyName(), descriptionPacket);
                    if (map.getValue() != null) {
                        Pattern pattern = Pattern.compile(map.getValue());
                        Matcher matcher = pattern.matcher(descriptionPacket.getFriendlyName());
                        if (matcher.find()) {
                            map.getKey().eventSSDP(message.getEvent(), descriptionPacket);
                        }
                    } else {
                        map.getKey().eventSSDP(message.getEvent(), descriptionPacket);
                    }
                }
            }
        }
    }
}
