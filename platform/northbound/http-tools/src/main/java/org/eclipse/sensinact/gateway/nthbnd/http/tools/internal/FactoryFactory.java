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
package org.eclipse.sensinact.gateway.nthbnd.http.tools.internal;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.internal.CallbackFactory;
import org.eclipse.sensinact.gateway.nthbnd.http.forward.internal.ForwardingFactory;

/**
 * A FactoryFactory is in charge of creating {@link CallbackFactory} and
 * {@link ForwardingFactory}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class FactoryFactory {
    private Mediator mediator;
    private ForwardingFactory forwarderFactory;
    private CallbackFactory callbackFactory;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the ForwardingIntallerFactory
     *                 to be instantiated to interact with the OSGi host environment
     */
    public FactoryFactory(Mediator mediator) {
        this.mediator = mediator;
    }

    /**
     *  Starts this FactoryFactory's CallbackFactory and ForwardingFactory
     */
    public void start() throws Exception {
        callbackFactory = new CallbackFactory(mediator);
        callbackFactory.start();

        forwarderFactory = new ForwardingFactory(mediator);
        forwarderFactory.start();
    }

    /**
     * Stops this FactoryFactory's CallbackFactory and ForwardingFactory
     */
    public void stop() throws Exception {
    	callbackFactory.stop();
    	callbackFactory = null;
        forwarderFactory.stop();
        forwarderFactory = null;
    }
}
