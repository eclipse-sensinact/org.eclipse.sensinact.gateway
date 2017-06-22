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
package org.eclipse.sensinact.gateway.device.openhab.sensinact;

import org.eclipse.sensinact.gateway.device.openhab.OpenHabDevice;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.annotation.TaskCommand;
import org.eclipse.sensinact.gateway.generic.annotation.TaskExecution;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * @Author Jander Nascimento<Jander.BotelhodoNascimento@cea.fr>
 */
@TaskExecution
public class OpenHabInvoker
{
	private Mediator mediator;

    public OpenHabInvoker(Mediator mediator)
    {
    	this.mediator = mediator;
    }

    private OpenHabDevice getOpenHabDevice(String id){
        String filter = String.format("(name=%s)",id);
        try
        {
            BundleContext context=this.mediator.getContext();
            ServiceReference sr[]=context.getServiceReferences(
                    OpenHabDevice.class.getCanonicalName(),filter);

            if(sr.length>0)
            {
                if(sr.length>1)
                {
                    this.mediator.warn(
                            "More than one service for the same lamp was found, t"
                                    + "his can be an error ({} to be precise)");
                }
                return (OpenHabDevice)context.getService(sr[0]);

            }else
            {
                this.mediator.info("No OpenHab hue service found");
            }

        } catch (InvalidSyntaxException e)
        {
            this.mediator.error(
                    "Invalid filter to look for OpenHab service. Filter: {}",e);
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
        OpenHabDevice lamp = getOpenHabDevice(UriUtils.getRoot(uri).substring(1));
        lamp.act(UriUtils.getLeaf(uri).toUpperCase(), actParameters);
    }

    @TaskCommand(method = CommandType.GET)
    public Boolean get(Object...parameters){
        String uri = (String) parameters[0];
        String name=UriUtils.getRoot(uri).substring(1);
        String value=getOpenHabDevice(name).getValue();
        if(value.equals("ON")){
            return true;
        }else{
            return false;
        }

    }

}
