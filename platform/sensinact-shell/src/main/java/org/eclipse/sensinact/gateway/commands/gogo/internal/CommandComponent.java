/*
* Copyright (c) 2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
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
