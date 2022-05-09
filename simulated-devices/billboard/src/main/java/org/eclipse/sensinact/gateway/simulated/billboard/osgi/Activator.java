/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/

package org.eclipse.sensinact.gateway.simulated.billboard.osgi;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.simulated.billboard.internal.BillboardConfig;
import org.eclipse.sensinact.gateway.simulated.billboard.swing.BillboardPanel;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import java.util.Collections;

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends AbstractActivator<Mediator> {
    private static final String GUI_ENABLED = "org.eclipse.sensinact.simulated.gui.enabled";

    private LocalProtocolStackEndpoint<Packet> connector;
    private ExtModelConfiguration<Packet> manager;
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

        if ("true".equals(mediator.getProperty(GUI_ENABLED))) {
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
