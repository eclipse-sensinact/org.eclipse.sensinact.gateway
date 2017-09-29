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
package org.eclipse.sensinact.gateway.commands.gogo.internal;

import java.security.InvalidKeyException;

import org.apache.felix.service.command.Descriptor;
import org.eclipse.sensinact.gateway.commands.gogo.osgi.CommandServiceMediator;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 * @author Rémi Druilhe
 */
public class DescribeCommands {

    private CommandServiceMediator mediator;

    public DescribeCommands(CommandServiceMediator mediator) 
    		throws DataStoreException, InvalidKeyException 
    {
        this.mediator = mediator;
    }

    /**
     * Display the information regarding the URL
     */
    @Descriptor("display the information regarding the URL")
    public void describe(@Descriptor("the URL (starting with \"/\"") String url)
            throws DataStoreException, InvalidKeyException 
    {
        String[] uriElements = UriUtils.getUriElements(url);
        switch (uriElements.length)
        {
            case 1:
		        if(uriElements[0].equals("providers")) 
		        {
		        	new DeviceCommands(mediator).devices();
		        	
		        } else
		        {
		        	new DeviceCommands(mediator).device(
		        		uriElements[0]);
		        }
                break;
            case 2:
		        if(uriElements[1].equals("services")) 
		        {
		        	new ServiceCommands(mediator).services(
		        		uriElements[0]);
		        	
		        } else
		        {
		        	 new ServiceCommands(mediator).service(
		        		uriElements[0], uriElements[1]);
		        }
                break;
            case 3:
		        if(uriElements[2].equals("resources")) 
		        {
		        	new ResourceCommands(mediator).resources(
		        		uriElements[0], uriElements[1]);
		        	
		        } else
		        {
		        	new ResourceCommands(mediator).resource(
		        		uriElements[0], uriElements[1], uriElements[2]);
		        }
                break;
            default:
                System.out.println("Invalid URL parameter");
                break;
        }
    }
}
