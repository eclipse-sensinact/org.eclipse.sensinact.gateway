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
package org.eclipse.sensinact.gateway.protocol.ssdp.osgi;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.sensinact.gateway.protocol.ssdp.discovery.SSDPAbstractListenerThread;
import org.eclipse.sensinact.gateway.protocol.ssdp.discovery.SSDPDiscoveryListenerThread;
import org.eclipse.sensinact.gateway.protocol.ssdp.discovery.SSDPMulticastListenerThread;
import org.eclipse.sensinact.gateway.protocol.ssdp.listener.SSDPDiscoveryNotifier;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator implements BundleActivator {
    private List<SSDPAbstractListenerThread> discoveryThreads;
    private SSDPDiscoveryNotifier notifier;

    public void start(BundleContext context) throws Exception {
        this.discoveryThreads = new ArrayList<SSDPAbstractListenerThread>();
        this.notifier = new SSDPDiscoveryNotifier(context);
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (!networkInterface.isLoopback() && !networkInterface.isVirtual()) {
                discoveryThreads.add(new SSDPDiscoveryListenerThread(notifier, networkInterface));
                discoveryThreads.add(new SSDPMulticastListenerThread(notifier, networkInterface));
            }
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (SSDPAbstractListenerThread discoveryThread : discoveryThreads) {
            discoveryThread.stop();
        }
        notifier.stop(context);
    }
}
