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
package org.eclipse.sensinact.gateway.protocol.ssdp.discovery;

import org.eclipse.sensinact.gateway.protocol.ssdp.listener.SSDPDiscoveryNotifier;
import org.eclipse.sensinact.gateway.protocol.ssdp.model.SSDPConstant;
import org.eclipse.sensinact.gateway.protocol.ssdp.parser.SSDPDiscoveryParser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

/**
 * Creates a thread that listen for multicast request
 */
public class SSDPMulticastListenerThread extends SSDPAbstractListenerThread {
    /**
     * Constructor.
     *
     * @param notifier the notifier that will receive the discovery messages from the thread
     */
    public SSDPMulticastListenerThread(SSDPDiscoveryNotifier notifier, NetworkInterface networkInterface) {
        super(notifier, networkInterface);
        new Thread(this).start();
    }

    /**
     * @inheritDoc
     */
    public void run() {
        MulticastSocket multicastSocket = null;
        try {
            InetAddress group = InetAddress.getByName(SSDPConstant.MULTICAST_IP);
            multicastSocket = new MulticastSocket(SSDPConstant.MULTICAST_PORT);
            multicastSocket.joinGroup(group);
            multicastSocket.setNetworkInterface(networkInterface);
            while (running) {
                if (multicastSocket.isBound()) {
                    byte[] buf = new byte[1000];
                    DatagramPacket recv = new DatagramPacket(buf, buf.length);
                    multicastSocket.receive(recv);
                    notifier.newSSDPPacket(SSDPDiscoveryParser.parse(new String(buf)));
                }
            }
            multicastSocket.leaveGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (multicastSocket != null) {
                multicastSocket.close();
            }
        }
    }

    /**
     * @inheritDoc
     */
    public void stop() {
        this.running = false;
    }
}
