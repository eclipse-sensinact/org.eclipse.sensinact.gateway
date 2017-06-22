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

import java.net.NetworkInterface;

public abstract class SSDPAbstractListenerThread implements Runnable {

    protected SSDPDiscoveryNotifier notifier;
    protected NetworkInterface networkInterface;
    protected boolean running;

    /**
     * Constructor.
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
