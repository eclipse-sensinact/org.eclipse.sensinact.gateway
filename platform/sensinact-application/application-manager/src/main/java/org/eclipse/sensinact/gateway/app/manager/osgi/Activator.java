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

package org.eclipse.sensinact.gateway.app.manager.osgi;

import org.eclipse.sensinact.gateway.app.manager.internal.AppManagerFactory;
import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.osgi.framework.BundleContext;


/**
 * @see AbstractActivator
 *
 * @author Remi Druilhe
 */
public class Activator extends AbstractActivator<AppServiceMediator> {

    private AppManagerFactory appManagerFactory;

    /**
     * @see AbstractActivator#doStart()
     */
    public void doStart() throws Exception {
        this.appManagerFactory = new AppManagerFactory(mediator);
    }

    /**
     * @see AbstractActivator#doStop()
     */
    public void doStop() throws Exception {
        this.appManagerFactory.deleteAppManager();
    }

    /**
     * @see AbstractActivator#doInstantiate(BundleContext)
     */
    public AppServiceMediator doInstantiate(BundleContext context)
    {
        return new AppServiceMediator(context);
    }
}
