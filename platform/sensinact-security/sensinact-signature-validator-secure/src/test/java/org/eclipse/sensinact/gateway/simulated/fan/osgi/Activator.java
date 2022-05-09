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
import org.eclipse.sensinact.gateway.simulated.fan.internal.FanConfig;
import org.eclipse.sensinact.gateway.simulated.fan.swing.FanPanel;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends AbstractActivator<Mediator> {
    private static final String GUI_ENABLED = "org.eclipse.sensinact.simulated.gui.enabled";
    private FanConfig config;
    private FanPanel fanPanel;

    public void doStart() throws Exception {
        config = new FanConfig();
        if (mediator.getContext().getProperty(GUI_ENABLED).equals("true")) {
            fanPanel = new FanPanel(super.mediator);
            config.addListener(fanPanel);
        }
    }

    public void doStop() throws Exception {
        if (fanPanel != null) {
            config.removeListener(fanPanel);
            fanPanel.stop();
        }
    }

    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
