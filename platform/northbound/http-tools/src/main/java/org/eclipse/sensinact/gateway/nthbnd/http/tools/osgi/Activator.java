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
package org.eclipse.sensinact.gateway.nthbnd.http.tools.osgi;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.nthbnd.http.tools.internal.FactoryFactory;
import org.osgi.framework.BundleContext;

/**
 * Extended {@link AbstractActivator}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Activator extends AbstractActivator<Mediator> {
    private FactoryFactory factoryFactory = null;

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#
     * doStart()
     */
    public void doStart() throws Exception {
        this.factoryFactory = new FactoryFactory(mediator);
        this.factoryFactory.start();
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#
     * doStop()
     */
    public void doStop() throws Exception {
        if (this.factoryFactory != null) {
            this.factoryFactory.stop();
        }
        this.factoryFactory = null;
    }

    /**
     * @inheritDoc
     * @see AbstractActivator#
     * doInstantiate(org.osgi.framework.BundleContext, int, java.io.FileOutputStream)
     */
    @Override
    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
