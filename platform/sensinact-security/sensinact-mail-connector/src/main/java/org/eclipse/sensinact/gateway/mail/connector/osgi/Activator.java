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
package org.eclipse.sensinact.gateway.mail.connector.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.AccountConnector;
import org.eclipse.sensinact.gateway.mail.connector.MailAccountConnector;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

/**
 * @see AbstractActivator
 */
@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends AbstractActivator<Mediator> {
	
    /**
     * @inheritDoc
     * 
     * @see AbstractActivator#doStart()
     */
	@Override
    public void doStart() throws Exception {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("org.eclipse.sensinact.security.account.type", MailAccountConnector.ACCOUNT_TYPE);
        mediator.getContext().registerService(AccountConnector.class.getName(), new MailAccountConnector(mediator), props);
    }

    /**
     * @inheritDoc
     * 
     * @see AbstractActivator#doStop()
     */
	@Override
    public void doStop() throws Exception {
    }

    /**
     * @inheritDoc
     * 
     * @see AbstractActivator#doInstantiate(BundleContext)
     */
	@Override
    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
