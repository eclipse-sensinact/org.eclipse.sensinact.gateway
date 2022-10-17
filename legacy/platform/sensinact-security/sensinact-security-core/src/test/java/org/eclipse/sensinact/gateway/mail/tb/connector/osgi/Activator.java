/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.mail.tb.connector.osgi;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.AccountConnector;
import org.eclipse.sensinact.gateway.mail.tb.connector.MailAccountConnectorMoke;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import java.util.Dictionary;
import java.util.Hashtable;

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
        props.put("org.eclipse.sensinact.security.account.type", MailAccountConnectorMoke.ACCOUNT_TYPE);
        // Rank higher than the real service
        props.put(Constants.SERVICE_RANKING, Integer.MAX_VALUE);
        mediator.getContext().registerService(AccountConnector.class.getName(), new MailAccountConnectorMoke(mediator), props);
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
