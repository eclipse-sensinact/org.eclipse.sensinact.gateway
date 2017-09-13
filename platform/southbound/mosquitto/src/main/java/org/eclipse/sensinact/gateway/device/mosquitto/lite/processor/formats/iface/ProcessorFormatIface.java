package org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.iface;

import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.exception.ProcessorFormatException;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.selector.SelectorIface;

/**
 * Created by nj246216 on 15/06/17.
 */
public interface ProcessorFormatIface {

    String getName();
    String process(String inData,SelectorIface selector) throws ProcessorFormatException;

}
