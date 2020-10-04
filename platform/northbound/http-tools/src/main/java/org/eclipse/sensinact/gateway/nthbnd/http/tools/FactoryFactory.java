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
package org.eclipse.sensinact.gateway.nthbnd.http.tools;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.internal.CallbackFactory;
import org.eclipse.sensinact.gateway.nthbnd.http.forward.internal.ForwardingFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * A FactoryFactory is in charge of creating {@link CallbackFactory} and {@link ForwardingFactory}
 *
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
@Component(immediate=true)
public class FactoryFactory {
	
    private Mediator mediator;
    private ForwardingFactory forwarderFactory;
    private CallbackFactory callbackFactory;
    
    /**
     *  Starts this FactoryFactory's CallbackFactory and ForwardingFactory
     */
    @Activate
    public void activate(ComponentContext context) throws Exception {
    	this.mediator = new Mediator(context.getBundleContext());
        callbackFactory = new CallbackFactory(mediator);
        callbackFactory.start();

        forwarderFactory = new ForwardingFactory(mediator);
        forwarderFactory.start();
    }

    /**
     * Stops this FactoryFactory's CallbackFactory and ForwardingFactory
     */
    @Deactivate
    public void deactivate() throws Exception {
    	callbackFactory.stop();
    	callbackFactory = null;
        forwarderFactory.stop();
        forwarderFactory = null;
        this.mediator = null;
    }
}
