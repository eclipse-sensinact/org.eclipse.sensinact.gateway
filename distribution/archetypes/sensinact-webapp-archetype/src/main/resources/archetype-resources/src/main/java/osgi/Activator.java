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
package ${package}.osgi;

import java.util.Hashtable;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;

import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackService;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import ${package}.app.component.CallbackServiceImpl;
import ${package}.app.servlet.IndexFilter;
import ${package}.app.servlet.ResourceFilter;
import ${package}.app.servlet.MirrorServlet;
import ${package}.WebAppConstants;

/**
 * Handle the bundle activation / deactivation
 */
public class Activator extends AbstractActivator<Mediator> {

	@Override
    public void doStart() {
    	mediator.register(new Hashtable() {{
            this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN, WebAppConstants.WEBAPP_ROOT);
            this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=default)");
            }
        },new IndexFilter(), 
    	new Class<?>[] {Filter.class});
        super.mediator.info(String.format("%s filter registered", WebAppConstants.WEBAPP_ROOT));
        
        mediator.register(new Hashtable() {{
        	this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN, WebAppConstants.WEBAPP_ALIAS);
            this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=default)");
            }
        }, new ResourceFilter(super.mediator), 
        new Class<?>[] {Filter.class});
        super.mediator.info(String.format("%s filter registered",  WebAppConstants.WEBAPP_ALIAS));
               
	    mediator.register(new Hashtable() {{
        	this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, WebAppConstants.WEBAPP_ALIAS);
            this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=default)");
        	}
	    }, new MirrorServlet(), 
	    new Class<?>[] {Servlet.class});
    }

    @Override
    public void doStop() {
        mediator.info("Swagger API was unregistered from %s context", WebAppConstants.WEBAPP_ALIAS);
    }
    
	@Override
	public Mediator doInstantiate(BundleContext context) {
		return new Mediator(context);
	}

}
