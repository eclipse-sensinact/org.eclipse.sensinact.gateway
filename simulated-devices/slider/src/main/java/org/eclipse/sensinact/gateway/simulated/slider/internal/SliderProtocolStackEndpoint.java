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
package org.eclipse.sensinact.gateway.simulated.slider.internal;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.InvalidProtocolStackException;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.simulated.slider.api.SliderSetterItf;
import org.json.JSONArray;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SliderProtocolStackEndpoint extends LocalProtocolStackEndpoint<SliderPacket> {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    private static final String SLIDERS_DEFAULT = "[\"slider\"]";
    private static final String GUI_ENABLED = "org.eclipse.sensinact.simulated.gui.enabled";

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//
    private List<SliderSetterItf> sliderPanels;
    private List<ServiceRegistration<SliderSetterItf>> sliderRegistrations;

    /**
     * @param mediator
     */
    public SliderProtocolStackEndpoint(Mediator mediator) {
        super(mediator);
        sliderPanels = new ArrayList<SliderSetterItf>();
        sliderRegistrations = new ArrayList<ServiceRegistration<SliderSetterItf>>();
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint#
     * execute(org.eclipse.sensinact.gateway.generic.Task)
     */
    @Override
    protected Object execute(Task task) throws Exception {
        switch (task.getCommand()) {
            case SERVICES_ENUMERATION:
                task.setResult(new String[]{"cursor"});
                return null;
            case ACT:
            case GET:
            case SET:
            case SUBSCRIBE:
            case UNSUBSCRIBE:
            default:
                return super.execute(task);
        }
    }

    /**
     * @throws InvalidProtocolStackException
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint#
     * connect(org.eclipse.sensinact.gateway.generic.ExtModelConfiguration)
     */
    @Override
    public void connect(ExtModelConfiguration manager) throws InvalidProtocolStackException {
        super.connect(manager);
        String sliders = (String) super.mediator.getProperty("org.eclipse.sensinact.simulated.sliders");

        if (sliders == null) {
            sliders = SLIDERS_DEFAULT;
        }

        JSONArray slidersArray = new JSONArray(sliders);

        for (int i = 0; i < slidersArray.length(); i++) {
            final String id = slidersArray.getString(i);
            SliderSetterItf sliderPanel = null;

            if (mediator.getContext().getProperty(GUI_ENABLED).equals("true")) {
                sliderPanel = new SliderGUI(new SliderAdapter(id, this));
                this.sliderPanels.add(sliderPanel);
            } else {
                sliderPanel = new SliderSetter(new SliderAdapter(id, this));

                Dictionary props = new Hashtable();
                props.put("slider.id", id);

                ServiceRegistration<SliderSetterItf> sliderRegistration = mediator.getContext().registerService(SliderSetterItf.class, sliderPanel, props);
                this.sliderPanels.add(sliderPanel);
                this.sliderRegistrations.add(sliderRegistration);
            }
            sliderPanel.move(0);
        }
    }

    public void stop() {
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
        super.stop();
    }
}
