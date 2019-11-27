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
package org.eclipse.sensinact.gateway.security.oauth2.osgi;

import java.util.Hashtable;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.security.oauth2.OpenIDServer;
import org.eclipse.sensinact.gateway.security.oauth2.filter.SecurityFilter;
import org.eclipse.sensinact.gateway.security.oauth2.servlet.SecurityServlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;


/**
 * @see AbstractActivator
 */
public class Activator extends AbstractActivator<Mediator> {

	public static final String AUTH_SECURITY_CONFIG = "org.eclipse.sensinact.security.oauth2.config";
	     
	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doStart()
	 */
	@Override
	public void doStart() throws Exception {
		String configfile = (String)super.mediator.getProperty(AUTH_SECURITY_CONFIG);		
		OpenIDServer oidcServer = new OpenIDServer(super.mediator.getContext(), configfile);		
		super.mediator.register(
			new SecurityFilter(oidcServer, oidcServer),
			Filter.class,
			new Hashtable() {{
				this.put(Constants.SERVICE_RANKING, 0);
				this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN, "/*");	
				this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=org.eclipse.sensinact)");
				this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_ASYNC_SUPPORTED, true);}}
			);		
	    super.mediator.register(
	    	new SecurityServlet(oidcServer, oidcServer), 
	    	Servlet.class,
	    	new Hashtable() {{
	    	    this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, "/auth");
				this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=org.eclipse.sensinact)");
	    	    this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_ASYNC_SUPPORTED,true);}}
	    	);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doStop()
	 */
	@Override
    public void doStop() throws Exception {}

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doInstantiate(org.osgi.framework.BundleContext)
     */
    @Override
    public Mediator doInstantiate(BundleContext context) {
        Mediator mediator = new Mediator(context);
        return mediator;
    }
}
