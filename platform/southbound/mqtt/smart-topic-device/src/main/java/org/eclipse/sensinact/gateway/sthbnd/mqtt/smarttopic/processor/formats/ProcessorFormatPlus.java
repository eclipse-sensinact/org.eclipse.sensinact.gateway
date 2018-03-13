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
package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats;

import org.eclipse.sensinact.gateway.sthbnd.mqtt.MqttActivator;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.exception.ProcessorFormatException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.iface.ProcessorFormatIface;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.selector.SelectorIface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLDecoder;

public class ProcessorFormatPlus implements ProcessorFormatIface {

    private static final Logger LOG = LoggerFactory.getLogger(MqttActivator.class);

    @Override
    public String getName() {
        return "plus";
    }

    @Override
    public String process(String inData,SelectorIface selector) throws ProcessorFormatException {
        try {
            Float value1=new Float(inData);
            Float value2=new Float(selector.getExpression());
            return Float.valueOf(value1+value2).toString();
        } catch (Exception e) {
            LOG.error("Failed to apply {} filter. Bypassing filter",getName(),e);
            return inData;
        }
    }
}
