/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.commands.gogo.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(service = CommandComponent.class)
public class CommandComponent {
	
    private CommandServiceMediator mediator;

    @Activate
    void start(BundleContext ctx) throws Exception {
    	this.mediator = new CommandServiceMediator(ctx);
    }
    
    public CommandServiceMediator getCommandMediator() {
    	return mediator;
    }
}
