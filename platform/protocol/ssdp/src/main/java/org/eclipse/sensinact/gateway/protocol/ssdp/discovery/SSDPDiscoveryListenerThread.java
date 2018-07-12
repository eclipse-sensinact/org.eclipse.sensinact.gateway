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

import org.eclipse.sensinact.gateway.protocol.ssdp.exception.InvalidParameterException;
import org.eclipse.sensinact.gateway.protocol.ssdp.listener.SSDPDiscoveryNotifier;
import org.eclipse.sensinact.gateway.protocol.ssdp.model.MSearchMessage;
import org.eclipse.sensinact.gateway.protocol.ssdp.model.SSDPConstant;
import org.eclipse.sensinact.gateway.protocol.ssdp.parser.SSDPDiscoveryParser;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Enumeration;

/**
 * Creates a thread that listen for responses from the multicast request it sends
 */
public class SSDPDiscoveryListenerThread extends SSDPAbstractListenerThread {
    private DatagramChannel channel;
    private Thread thread;

    /**
     * Constructor.
     *
     * @param notifier the notifier that will receive the discovery messages from the thread
     */
    public SSDPDiscoveryListenerThread(SSDPDiscoveryNotifier notifier, NetworkInterface networkInterface) {
        super(notifier, networkInterface);
        try {
            InetAddress addr = null;
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (!(address instanceof Inet6Address)) {
                    addr = address;
                    break;
                }
            }
            channel = DatagramChannel.open();
            channel.socket().bind(new InetSocketAddress(addr, 9654));
            this.sendDiscovery();
        } catch (IOException e) {
            e.printStackTrace();
        }
        thread = new Thread(this);
        thread.start();
    }

    /**
     * @inheritDoc
     */
    public void run() {
        ByteBuffer in = ByteBuffer.allocate(8000);
        while (running) {
            try {
                in.clear();
                if (channel.isOpen()) {
                    channel.receive(in);
                    in.flip();
                    byte[] buf = new byte[in.limit()];
                    in.get(buf, 0, in.limit());
                    notifier.newSSDPPacket(SSDPDiscoveryParser.parse(new String(buf)));
                } else {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @inheritDoc
     */
    public void stop() {
        this.running = false;
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Construct the M-SEARCH request and send it through the channel on the multicast address
     *
     * @throws IOException
     */
    public void sendDiscovery() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8000);
        try {
            buffer.put(MSearchMessage.createMessage("upnp:rootdevice", 3).getBytes());
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        }
        buffer.flip();
        channel.send(buffer, new InetSocketAddress(SSDPConstant.MULTICAST_IP, SSDPConstant.MULTICAST_PORT));
    }
}
