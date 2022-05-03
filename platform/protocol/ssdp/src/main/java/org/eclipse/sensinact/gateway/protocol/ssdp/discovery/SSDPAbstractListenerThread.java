/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.protocol.ssdp.discovery;

import org.eclipse.sensinact.gateway.protocol.ssdp.listener.SSDPDiscoveryNotifier;

import java.net.NetworkInterface;

public abstract class SSDPAbstractListenerThread implements Runnable {
    protected SSDPDiscoveryNotifier notifier;
    protected NetworkInterface networkInterface;
    protected boolean running;

    /**
     * Constructor.
     *
     * @param notifier the notifier that will receive the discovery messages from the thread
     */
    public SSDPAbstractListenerThread(SSDPDiscoveryNotifier notifier, NetworkInterface networkInterface) {
        this.notifier = notifier;
        this.networkInterface = networkInterface;
        this.running = true;
    }

    /**
     * Stop the current thread
     */
    public abstract void stop();
}
