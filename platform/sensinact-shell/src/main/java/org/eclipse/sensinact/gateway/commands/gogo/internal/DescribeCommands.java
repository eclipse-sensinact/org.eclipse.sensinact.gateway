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

import org.eclipse.sensinact.gateway.commands.gogo.osgi.CommandServiceMediator;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.Service;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.apache.felix.service.command.Descriptor;

import java.security.InvalidKeyException;

/**
 * @author Rémi Druilhe
 */
public class DescribeCommands {

    private CommandServiceMediator mediator;

    public DescribeCommands(CommandServiceMediator mediator) throws DataStoreException, InvalidKeyException {
        this.mediator = mediator;
    }

    /**
     * Display the information regarding the URL
     */
    @Descriptor("display the information regarding the URL")
    public void describe(@Descriptor("the URL (starting with \"/\"") String url)
            throws DataStoreException, InvalidKeyException {

        if(!url.startsWith("/")) {
            System.out.println("Wrong URL. The URL should start with \"/\"");
            return;
        }

        int counter = 0;
        for(char ch : url.toCharArray()) {
            if(ch == '/' ) {
                counter++;
            }
        }

        String[] uri = url.split("/");

        if(url.endsWith("/")) {
            switch (counter) {
                case 1:
                    new DeviceCommands(mediator).devices();
                    break;
                case 2:
                    new ServiceCommands(mediator).services(uri[1]);
                    break;
                case 3:
                    new ResourceCommands(mediator).resources(uri[1], uri[2]);
                    break;
                default:
                    System.out.println("The requested description does not exist");
                    break;
            }
        } else {
            if (mediator.getSession().getFromUri(url) instanceof ServiceProvider) {
                new DeviceCommands(mediator).device(uri[1]);
            } else if (mediator.getSession().getFromUri(url) instanceof Service) {
                new ServiceCommands(mediator).service(uri[1], uri[2]);
            } else if (mediator.getSession().getFromUri(url) instanceof Resource) {
                new ResourceCommands(mediator).resource(uri[1], uri[2], uri[3]);
            } else {
                System.out.println("The requested description does not exist");
            }
        }
    }
}
