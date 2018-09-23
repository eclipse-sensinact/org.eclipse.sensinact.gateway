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

package org.eclipse.sensinact.gateway.simulated.billboard.osgi;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.simulated.billboard.internal.BillboardConfig;
import org.eclipse.sensinact.gateway.simulated.billboard.swing.BillboardPanel;
import org.osgi.framework.BundleContext;

import java.util.Collections;

public class Activator extends AbstractActivator<Mediator> {
    private static final String GUI_ENABLED = "org.eclipse.sensinact.simulated.gui.enabled";

    private LocalProtocolStackEndpoint<Packet> connector;
    private ExtModelConfiguration<?> manager;
    private BillboardConfig config;
    private BillboardPanel billboardPanel;

    public void doStart() throws Exception {
        config = new BillboardConfig();

        if (manager == null) {

            manager = ExtModelConfigurationBuilder.instance(super.mediator
            	).withStartAtInitializationTime(true
            	).build("billboard-resource.xml", Collections.<String, String>emptyMap());
            
        }
        if (connector == null) {
            connector = new LocalProtocolStackEndpoint<Packet>(super.mediator);
            connector.addInjectableInstance(BillboardConfig.class, config);
        }
        connector.connect(manager);

        if (mediator.getContext().getProperty(GUI_ENABLED).equals("true")) {
            billboardPanel = new BillboardPanel();
            config.addListener(billboardPanel);
        }
    }

    public void doStop() throws Exception {
        connector.stop();
        config.removeListener(billboardPanel);

        if (billboardPanel != null) {
            billboardPanel.stop();
        }
    }

    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
