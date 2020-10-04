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
package org.eclipse.sensinact.gateway.agent.storage.http.osgi;

import org.eclipse.sensinact.gateway.agent.storage.http.internal.HttpStorageConnection;
import org.eclipse.sensinact.gateway.agent.storage.generic.StorageAgent;
import org.eclipse.sensinact.gateway.common.annotation.Property;
import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Core;
import org.osgi.framework.BundleContext;

/**
 * Extended {@link AbstractActivator}
 */
public class Activator extends AbstractActivator<Mediator> {
    @Property
    private String login;
    @Property
    private String password;
    @Property(validationRegex = "^http[s]*://.*/write/measure$")
    private String broker;
    private StorageAgent handler;
    private String registration;

    @Override
    public void doStart() throws Exception {
        try {
            if (super.mediator.isDebugLoggable()) 
                super.mediator.debug("Starting storage agent.");            
            this.handler = new StorageAgent(new HttpStorageConnection(super.mediator, broker, login, password));
            this.registration = mediator.callService(Core.class, new Executable<Core, String>() {
                @Override
                public String execute(Core service) throws Exception {
                    return service.registerAgent(mediator, Activator.this.handler, null);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doStop() throws Exception {
        if (super.mediator.isDebugLoggable()) {
            super.mediator.debug("Stopping storage agent.");
        }        
        if(this.handler!=null){
            this.handler.stop();
        }
        this.registration = null;
        this.handler = null;
    }

    @Override
    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
