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
package org.eclipse.sensinact.gateway.agent.mqtt.inst.osgi;

import org.eclipse.sensinact.gateway.agent.mqtt.generic.osgi.AbstractMqttActivator;
import org.eclipse.sensinact.gateway.agent.mqtt.inst.internal.SnaEventEventHandler;
import org.eclipse.sensinact.gateway.common.annotation.Property;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.io.IOException;
import java.util.Dictionary;

/**
 * Extended {@link AbstractActivator}
 */
public class Activator extends AbstractMqttActivator {

    @Property(defaultValue = "127.0.0.1")
    public String host;
    @Property(defaultValue = "1883",validationRegex = Property.INTEGER)
    public String port;
    @Property(defaultValue = "0",validationRegex = Property.INTEGER)
    public String qos;
    @Property(defaultValue = "/",mandatory = false)
    public String prefix;
    @Property(defaultValue = "tcp",mandatory = false)
    public String protocol;
    @Property(mandatory = false)
    String username;
    @Property(mandatory = false)
    String password;
    private BundleContext context;
    /**
     * @inheritDoc
     * @see AbstractActivator#doStart()
     */

    protected Mediator initMediator(BundleContext context){
        this.context=context;
        return super.initMediator(context);
    }

    @Override
    public void doStart() throws Exception {
        final String broker = String.format("%s://%s:%s",protocol,host,port);
        mediator.setProperty("broker",broker);
        mediator.setProperty("qos",qos);
        mediator.setProperty("prefix",prefix);
        if(username!=null){
            mediator.setProperty("username",username);
            mediator.setProperty("password",password);
        }
        //ServiceReference[] ca=mediator.getContext().getServiceReferences(ConfigurationAdmin.class.getCanonicalName(),null);
        ServiceTracker st=new ServiceTracker<ConfigurationAdmin,ConfigurationAdmin>(FrameworkUtil.getBundle(Activator.class).getBundleContext(), ConfigurationAdmin.class.getCanonicalName(), new ServiceTrackerCustomizer<ConfigurationAdmin, ConfigurationAdmin>() {

            @Override
            public ConfigurationAdmin addingService(ServiceReference<ConfigurationAdmin> reference) {
                try {
                    ConfigurationAdmin configAdmin=(ConfigurationAdmin) FrameworkUtil.getBundle(Activator.class).getBundleContext().getService(reference);
                    doStart(new SnaEventEventHandler(broker,new Integer(qos),prefix,configAdmin));
                    return configAdmin;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void modifiedService(ServiceReference<ConfigurationAdmin> reference, ConfigurationAdmin service) {

            }

            @Override
            public void removedService(ServiceReference<ConfigurationAdmin> reference, ConfigurationAdmin service) {

            }
        });
        st.open();
    }
}
