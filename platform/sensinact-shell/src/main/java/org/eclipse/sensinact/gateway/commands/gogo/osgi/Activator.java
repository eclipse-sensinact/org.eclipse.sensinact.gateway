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
package org.eclipse.sensinact.gateway.commands.gogo.osgi;

import org.eclipse.sensinact.gateway.commands.gogo.internal.*;
import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.osgi.framework.BundleContext;


import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @see AbstractActivator
 */
public class Activator extends AbstractActivator<CommandServiceMediator> {

    /**
     * @inheritDoc
     * @see AbstractActivator#doStart()
     */
    public void doStart() throws Exception {

        Dictionary<String, Object> propsDescribeCommands = new Hashtable<String, Object>();

        propsDescribeCommands.put("osgi.command.scope", "sna");
        propsDescribeCommands.put("osgi.command.function", new String[]{
                "describe"});

        mediator.getContext().registerService(DescribeCommands.class.getName(),
                new DescribeCommands(mediator), propsDescribeCommands);

        Dictionary<String, Object> propsDeviceCommands = new Hashtable<String, Object>();

        propsDeviceCommands.put("osgi.command.scope", "sna");
        propsDeviceCommands.put("osgi.command.function", new String[]{
                "device", "devices"});

        mediator.getContext().registerService(DeviceCommands.class.getName(),
                new DeviceCommands(mediator), propsDeviceCommands);

        Dictionary<String, Object> propsServiceCommands = new Hashtable<String, Object>();

        propsServiceCommands.put("osgi.command.scope", "sna");
        propsServiceCommands.put("osgi.command.function", new String[]{
                "service", "services"});

        mediator.getContext().registerService(ServiceCommands.class.getName(),
                new ServiceCommands(mediator), propsServiceCommands);

        Dictionary<String, Object> propsResourceCommands = new Hashtable<String, Object>();

        propsResourceCommands.put("osgi.command.scope", "sna");
        propsResourceCommands.put("osgi.command.function", new String[]{
                "resources", "resource"});

        mediator.getContext().registerService(ResourceCommands.class.getName(),
                new ResourceCommands(mediator), propsResourceCommands);

        Dictionary<String, Object> propsAccessMethodCommands = new Hashtable<String, Object>();

        propsAccessMethodCommands.put("osgi.command.scope", "sna");
        propsAccessMethodCommands.put("osgi.command.function", new String[]{
                "method", "get", "set", "act", "unsubscribe"});

        mediator.getContext().registerService(AccessMethodCommands.class.getName(),
                new AccessMethodCommands(mediator), propsAccessMethodCommands);

        Dictionary<String, Object> propsUserCommands = new Hashtable<String, Object>();

        propsUserCommands.put("osgi.command.scope", "sna");
        propsUserCommands.put("osgi.command.function", new String[]{
                "su", "info"});

        mediator.getContext().registerService(UserCommands.class.getName(),
                new UserCommands(mediator), propsUserCommands);
    }

    /**
     * @inheritDoc
     * @see AbstractActivator#doStop()
     */
    public void doStop() throws Exception {}

    /**
     * @inheritDoc
     * @see AbstractActivator#doInstantiate(BundleContext)
     */
    public CommandServiceMediator doInstantiate(BundleContext context)
    {
        return new CommandServiceMediator(context);
    }
}
