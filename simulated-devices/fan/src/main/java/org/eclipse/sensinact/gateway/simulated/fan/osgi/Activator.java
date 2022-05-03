/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.simulated.fan.osgi;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.simulated.fan.internal.FanConfig;
import org.eclipse.sensinact.gateway.simulated.fan.swing.FanPanel;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import java.util.Collections;

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends AbstractActivator<Mediator> {
    private static final String GUI_ENABLED = "org.eclipse.sensinact.simulated.gui.enabled";
    private LocalProtocolStackEndpoint<Packet> connector;
    private ExtModelConfiguration<Packet> manager;
    private FanConfig config;
    private FanPanel fanPanel;

    public void doStart() throws Exception {
        config = new FanConfig();
        if (manager == null) {
            manager = ExtModelConfigurationBuilder.instance(super.mediator
            	).withStartAtInitializationTime(true
            	).build("fan-resource.xml", Collections.<String, String>emptyMap());
        }
        if (connector == null) {
            connector = new LocalProtocolStackEndpoint<Packet>(super.mediator);
            connector.addInjectableInstance(FanConfig.class, config);
        }
        connector.connect(manager);
        if ("true".equals(mediator.getProperty(GUI_ENABLED))) {
            fanPanel = new FanPanel(super.mediator);
            config.addListener(fanPanel);
        }
    }

    public void doStop() throws Exception {
        connector.stop();
        if (fanPanel != null) {
            config.removeListener(fanPanel);
            fanPanel.stop();
        }
    }

    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
