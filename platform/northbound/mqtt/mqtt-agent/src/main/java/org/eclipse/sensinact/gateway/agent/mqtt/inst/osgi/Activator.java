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
    @Property(defaultValue = "humanreadable",mandatory = false)
    public String payloadType;
    @Property(mandatory = false)
    String username;
    @Property(mandatory = false)
    String password;
    /**
     * @inheritDoc
     * @see AbstractActivator#doStart()
     */
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
        doStart(new SnaEventEventHandler(broker,new Integer(qos),prefix,payloadType));
    }
}
