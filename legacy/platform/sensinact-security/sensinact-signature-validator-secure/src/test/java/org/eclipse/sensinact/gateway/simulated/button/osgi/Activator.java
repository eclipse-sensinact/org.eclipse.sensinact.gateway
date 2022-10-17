/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.simulated.button.osgi;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.simulated.button.api.ButtonSetterItf;
import org.eclipse.sensinact.gateway.simulated.button.internal.ButtonAdapter;
import org.eclipse.sensinact.gateway.simulated.button.internal.ButtonGUI;
import org.eclipse.sensinact.gateway.simulated.button.internal.ButtonSetter;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends AbstractActivator<Mediator> {
    private static final String GUI_ENABLED = "org.eclipse.sensinact.simulated.gui.enabled";

    private ButtonSetterItf buttonPanel;
    private JFrame jFrame;
    private ServiceRegistration<ButtonSetterItf> buttonRegistration;

    public void doStart() throws Exception {
       
        if (mediator.getContext().getProperty(GUI_ENABLED).equals("true")) {
            buttonPanel = new ButtonGUI(mediator, new ButtonAdapter());
            jFrame = new JFrame();
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createAndShowGUI((JLabel) buttonPanel);
                }
            });
        } else {
            buttonPanel = new ButtonSetter(new ButtonAdapter());
            buttonRegistration = mediator.getContext().registerService(ButtonSetterItf.class, buttonPanel, null);
        }
    }

    public void doStop() throws Exception {
    	buttonPanel.stop();
        if (jFrame != null) {
            jFrame.dispose();
        }
        if (buttonRegistration != null) {
            buttonRegistration.unregister();
        }
    }

    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }

    private void createAndShowGUI(JLabel gui) {
        jFrame.add(gui, BorderLayout.CENTER);
        jFrame.setSize(136, 156);
        jFrame.setTitle("button");
        jFrame.setVisible(true);
    }
}
