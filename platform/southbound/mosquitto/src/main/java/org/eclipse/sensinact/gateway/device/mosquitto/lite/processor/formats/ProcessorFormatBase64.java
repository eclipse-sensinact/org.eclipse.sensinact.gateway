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

/**
 * Stateless class that is capable of interprete a given format. e.g.
 * using inData [1,7,16] and the selector expression '1' will return 7
 * using inData [1,7,16] and the selector expression '0' will return 1
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class ProcessorFormatBase64 implements ProcessorFormatIface {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    @Override
    public String getName() {
        return "base64";
    }

    @Override
    public String process(String inData,SelectorIface selector) throws ProcessorFormatException {
        try {
            return new String(Base64.decode(inData.getBytes()));
        } catch (IOException e) {
            LOG.error("Failed to apply {} filter. Bypassing filter",getName(),e);
            return inData;
        }
    }
}
