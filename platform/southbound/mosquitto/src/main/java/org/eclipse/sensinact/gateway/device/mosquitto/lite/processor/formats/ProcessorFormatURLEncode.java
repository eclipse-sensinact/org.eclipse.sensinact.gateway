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
package org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats;

import org.eclipse.sensinact.gateway.device.mosquitto.lite.Activator;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.exception.ProcessorFormatException;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.iface.ProcessorFormatIface;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.selector.SelectorIface;
import org.eclipse.sensinact.gateway.util.crypto.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLDecoder;

public class ProcessorFormatURLEncode implements ProcessorFormatIface {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    @Override
    public String getName() {
        return "urlencode";
    }

    @Override
    public String process(String inData,SelectorIface selector) throws ProcessorFormatException {
        try {
            return URLDecoder.decode(inData,"utf-8").toString();
        } catch (IOException e) {
            LOG.error("Failed to apply {} filter. Bypassing filter",getName(),e);
            return inData;
        }
    }
}
