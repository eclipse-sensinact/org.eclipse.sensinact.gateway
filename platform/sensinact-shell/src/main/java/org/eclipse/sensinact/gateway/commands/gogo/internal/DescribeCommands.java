/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.commands.gogo.internal;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.annotations.GogoCommand;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.security.InvalidKeyException;

/**
 * @author Rémi Druilhe
 */
@Component(service = DescribeCommands.class)
@GogoCommand(
		scope = "sna", 
		function = {"describe"}
	)
public class DescribeCommands {
    
	@Reference
	private DeviceCommands deviceCommands;

	@Reference
	private ServiceCommands serviceCommands;

	@Reference
	private ResourceCommands resourceCommands;

    /**
     * Display the information regarding the URL
     */
    @Descriptor("display the information regarding the URL")
    public void describe(@Descriptor("the URL (starting with \"/\"") String url) throws DataStoreException, InvalidKeyException {
        String[] uriElements = UriUtils.getUriElements(url);
        switch (uriElements.length) {
            case 1:
                if (uriElements[0].equals("providers")) {
                	deviceCommands.devices();
                } else {
                	deviceCommands.device(uriElements[0]);
                }
                break;
            case 2:
                if (uriElements[1].equals("services")) {
                    serviceCommands.services(uriElements[0]);
                } else {
                    serviceCommands.service(uriElements[0], uriElements[1]);
                }
                break;
            case 3:
                if (uriElements[2].equals("resources")) {
                    resourceCommands.resources(uriElements[0], uriElements[1]);
                } else {
                    resourceCommands.resource(uriElements[0], uriElements[1], uriElements[2]);
                }
                break;
            default:
                System.out.println("Invalid URL parameter");
                break;
        }
    }
}
