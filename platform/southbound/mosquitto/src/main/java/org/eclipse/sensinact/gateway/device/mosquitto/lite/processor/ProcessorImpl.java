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
package org.eclipse.sensinact.gateway.device.mosquitto.lite.processor;

import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.exception.ProcessorException;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.iface.ProcessorFormatIface;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.selector.SelectorIface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processor is the entity that will execute the Selector requests based on the supported Processors.
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class ProcessorImpl {

    private final Map<String,ProcessorFormatIface> processors=new HashMap<String, ProcessorFormatIface>();

    private static final Logger LOG = LoggerFactory.getLogger(ProcessorImpl.class);

    public ProcessorImpl(final List<ProcessorFormatIface> processors){

        for(ProcessorFormatIface processor:processors){
            addProcessorFormatSupport(processor);
        }

    }

    public void addProcessorFormatSupport(ProcessorFormatIface processorFormat){
        this.processors.put(processorFormat.getName(), processorFormat);
    }

    public String execute(final String inData,List<SelectorIface> selectors) throws ProcessorException{

        String incompleteProcessedInData=inData;

        for(SelectorIface selector:selectors){

            try {
                LOG.info("IN {} Selector {} Expression {}",incompleteProcessedInData,selector.getName(),selector.getExpression());
                incompleteProcessedInData=processors.get(selector.getName()).process(incompleteProcessedInData,selector);
            }catch(Exception e){
                throw new ProcessorException("Failed to execute processor "+selector.getName(),e);
            }
        }

        return incompleteProcessedInData;

    }

}
