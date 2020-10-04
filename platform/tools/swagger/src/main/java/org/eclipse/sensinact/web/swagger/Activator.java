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
package org.eclipse.sensinact.web.swagger;

import java.util.Hashtable;

import javax.servlet.Filter;
//import javax.servlet.Servlet;
import javax.servlet.Servlet;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

/**
 * Service that published studio-lite on Jetty server
 *
 * @author Jander Nascimento
 */
public class Activator extends AbstractActivator<Mediator> {

    private static final String SWAGGER_ALIAS = "/swagger-api";

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doStart()
     */
    @SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	@Override
    public void doStart() {    	

        mediator.register(new IndexFilter(SWAGGER_ALIAS), Filter.class, new Hashtable() {{
        	this.put(Constants.SERVICE_RANKING, 3);
            this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN, SWAGGER_ALIAS);
            this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=default)");
            }
        });
        super.mediator.info(String.format("%s filter registered", SWAGGER_ALIAS));
        
        mediator.register(new ResourceFilter(super.mediator, SWAGGER_ALIAS), Filter.class, new Hashtable() {{
        	this.put(Constants.SERVICE_RANKING, 2);
            this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN, SWAGGER_ALIAS+"/*");
            this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=default)");
            }
        });
        super.mediator.info(String.format("%s filter registered", SWAGGER_ALIAS+"/*"));
        
	    mediator.register(new Hashtable() {{
        	this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, SWAGGER_ALIAS+"/*");
            this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=default)");
        	}
	    }, new MirrorServlet(), 
	    new Class<?>[] {Servlet.class});
	    
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doStop()
     */
    @Override
    public void doStop() {
        mediator.info("Swagger API was unregistered from %s context", SWAGGER_ALIAS);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doInstantiate(org.osgi.framework.BundleContext)
	 */
	@Override
	public Mediator doInstantiate(BundleContext context) {
		return new Mediator(context);
	}

}
