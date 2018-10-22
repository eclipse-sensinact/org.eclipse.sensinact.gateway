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
package org.eclipse.sensinact.gateway.simulated.slider.osgi;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.simulated.slider.api.SliderSetterItf;
import org.eclipse.sensinact.gateway.simulated.slider.internal.SliderAdapter;
import org.eclipse.sensinact.gateway.simulated.slider.internal.SliderGUI;
import org.eclipse.sensinact.gateway.simulated.slider.internal.SliderPacket;
import org.eclipse.sensinact.gateway.simulated.slider.internal.SliderSetter;
import org.json.JSONArray;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class Activator extends AbstractActivator<Mediator> {

    private static final String SLIDERS_DEFAULT = "[\"slider\"]";
    private static final String GUI_ENABLED = "org.eclipse.sensinact.simulated.gui.enabled";
    
    private LocalProtocolStackEndpoint<SliderPacket> connector;
    
    private List<SliderSetterItf> sliderPanels;
    private List<ServiceRegistration<SliderSetterItf>> sliderRegistrations;

    public void doStart() throws Exception {

        sliderPanels = new ArrayList<SliderSetterItf>();
        sliderRegistrations = new ArrayList<ServiceRegistration<SliderSetterItf>>();
        
        ExtModelConfiguration<SliderPacket> manager = ExtModelConfigurationBuilder.instance(
        		mediator, SliderPacket.class
        	).withStartAtInitializationTime(true
        	).build("slider-resource.xml", Collections.<String, String>emptyMap());

        connector = new LocalProtocolStackEndpoint<SliderPacket>(super.mediator);
        connector.connect(manager);
        
        String sliders = (String) super.mediator.getProperty(
        		"org.eclipse.sensinact.simulated.sliders");

        if (sliders == null) {
            sliders = SLIDERS_DEFAULT;
        }

        JSONArray slidersArray = new JSONArray(sliders);

        for (int i = 0; i < slidersArray.length(); i++) {
            final String id = slidersArray.getString(i);
            SliderSetterItf sliderPanel = null;

            if (mediator.getContext().getProperty(GUI_ENABLED).equals("true")) {
                sliderPanel = new SliderGUI(new SliderAdapter(id, connector));
                this.sliderPanels.add(sliderPanel);
            } else {
                sliderPanel = new SliderSetter(new SliderAdapter(id, connector));

                Dictionary props = new Hashtable();
                props.put("slider.id", id);

                ServiceRegistration<SliderSetterItf> sliderRegistration = mediator.getContext(
                	).registerService(SliderSetterItf.class, sliderPanel, props);
                this.sliderPanels.add(sliderPanel);
                this.sliderRegistrations.add(sliderRegistration);
            }
            sliderPanel.move(0);
        }
    }

    public void doStop() throws Exception {
        while (!this.sliderRegistrations.isEmpty()) {
            try {
                this.sliderRegistrations.remove(0).unregister();
            } catch (IllegalStateException e) {
                continue;
            }
        }
        while (!this.sliderPanels.isEmpty()) {
            this.sliderPanels.remove(0).stop();
        }
        connector.stop();
        connector = null;
    }

    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
