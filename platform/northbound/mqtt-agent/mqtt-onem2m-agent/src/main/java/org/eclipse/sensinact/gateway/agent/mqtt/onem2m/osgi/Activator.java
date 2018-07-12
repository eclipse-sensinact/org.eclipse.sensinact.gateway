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
package org.eclipse.sensinact.gateway.agent.mqtt.onem2m.osgi;

import org.eclipse.sensinact.gateway.agent.mqtt.generic.osgi.AbstractMqttActivator;
import org.eclipse.sensinact.gateway.agent.mqtt.onem2m.internal.SnaEventOneM2MMqttHandler;
import org.eclipse.sensinact.gateway.common.annotation.Property;
import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;

/**
 * Extended {@link AbstractActivator}
 */
public class Activator extends AbstractMqttActivator {
    @Property(name = "org.eclipse.sensinact.gateway.northbound.mqtt.onem2m.csebase")
    public String cseBase;

    /**
     * @inheritDoc
     * @see AbstractActivator#doStart()
     */
    @Override
    public void doStart() throws Exception {
        super.doStart(new SnaEventOneM2MMqttHandler(cseBase));
    }
}
