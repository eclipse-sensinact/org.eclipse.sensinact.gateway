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
package org.eclipse.sensinact.gateway.agent.storage.osgi;

import org.eclipse.sensinact.gateway.agent.storage.internal.StorageAgent;
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

    /**
     * @inheritDoc
     * @see AbstractActivator#doStart() (org.osgi.framework.BundleContext)
     */
    @Override
    public void doStart() throws Exception {
        try {
            if (super.mediator.isDebugLoggable()) {
                super.mediator.debug("Starting storage agent.");
            }
            this.handler = new StorageAgent(login, password, broker, super.mediator);
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

    /**
     * @inheritDoc
     * @see AbstractActivator#doStop()
     */
    @Override
    public void doStop() throws Exception {
        if (super.mediator.isDebugLoggable()) {
            super.mediator.debug("Stopping storage agent.");
        }
        this.registration = null;
        this.handler.stop();
        this.handler = null;
    }

    /**
     * @inheritDoc
     * @see AbstractActivator#doInstantiate(BundleContext)
     */
    @Override
    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
