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
package org.eclipse.sensinact.gateway.device.mosquitto.lite.sensinact;

import org.eclipse.sensinact.gateway.device.mosquitto.lite.device.MQTTBusClient;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.annotation.TaskCommand;
import org.eclipse.sensinact.gateway.generic.annotation.TaskExecution;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Sensinact class
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Nascimento</a>
 */
@TaskExecution
public class MQTTInvoker
{
	private Mediator mediator;

    public MQTTInvoker(Mediator mediator)
    {
    	this.mediator = mediator;
    }

    private MQTTBusClient getLamp(String id){
        String filter = String.format("(host=%s)",id);
        try
        {
            BundleContext context=this.mediator.getContext();
            ServiceReference sr[]=context.getServiceReferences(
                    MQTTBusClient.class.getCanonicalName(),filter);

            if(sr.length>0)
            {
                if(sr.length>1)
                {
                    this.mediator.warn(
                            "More than one service for the same lamp was found, t"
                                    + "his can be an error ({} to be precise)");
                }
                return (MQTTBusClient)context.getService(sr[0]);

            }else
            {
                this.mediator.info("No PhilipsLamp hue service found");
            }

        } catch (InvalidSyntaxException e)
        {
            this.mediator.error(
                    "Invalid filter to look for PhilipsHueLamp service. Filter: {}",e);
        }

        return null;
    }

	@TaskCommand(method = CommandType.ACT)
    public void act(Object...parameters)
    {
    	String uri = (String) parameters[0];
    	Object[] actParameters = null;
    	if(parameters.length > 1)
    	{
    		actParameters = new Object[parameters.length-1];
    		System.arraycopy(parameters, 1, actParameters, 0, parameters.length-1);
    	}
        MQTTBusClient lamp = getLamp(UriUtils.getRoot(uri).substring(1));
        lamp.act(UriUtils.getLeaf(uri).toUpperCase(), actParameters);
    }
}
