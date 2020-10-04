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
package org.eclipse.sensinact.gateway.system.invoker;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.annotation.TaskCommand;
import org.eclipse.sensinact.gateway.generic.annotation.TaskExecution;
import org.json.JSONArray;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;

@TaskExecution
public class SystemInvoker {
    private Mediator mediator;

    public SystemInvoker(Mediator mediator) {
        this.mediator = mediator;
    }

    @TaskCommand(target = "/sensiNact/system/name", method = CommandType.GET)
    public String getName(String uri, String attributeName) {
        try {
            Properties properties=new Properties();
            properties.load(new FileInputStream("cfgs/sensinact.config"));
            String namespace=properties.getProperty("namespace").toString();
            return namespace;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (String) mediator.getProperty(Core.NAMESPACE_PROP);
    }

    @TaskCommand(target = "/sensiNact/system/datetime", method = CommandType.GET)
    public long getDateTime(String uri, String attributeName) {
        return System.currentTimeMillis();
    }

    @TaskCommand(target = "/sensiNact/system/address", method = CommandType.GET)
    public JSONArray getIpAddress(String uri, String attributeName) {
        JSONArray array = new JSONArray();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        array.put(addr.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return array;
    }
}
