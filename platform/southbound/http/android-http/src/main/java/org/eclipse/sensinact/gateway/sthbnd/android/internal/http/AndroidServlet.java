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
package org.eclipse.sensinact.gateway.sthbnd.android.internal.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.sensinact.gateway.sthbnd.android.internal.AndroidPacket;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;

public class AndroidServlet extends HttpServlet
{

	private Mediator mediator;
	private LocalProtocolStackEndpoint<AndroidPacket> handler;


    public AndroidServlet(LocalProtocolStackEndpoint<AndroidPacket> handler, 
    		Mediator mediator) 
    {
    	this.mediator = mediator;
    	this.handler = handler;
	}

    /**
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
    throws ServletException, IOException 
    { 
    	try
    	{
    		notified(req, resp); 
    		resp.flushBuffer();
        
    	} catch(Exception e)
    	{
    		resp.setStatus(520);
    		resp.flushBuffer();
    		return;
    	}
    }
    
	private void notified(ServletRequest servletRequest, 
			ServletResponse response) throws Exception 
    {
	   byte[] content = IOUtils.read(servletRequest.getInputStream());
	   this.handler.process(new AndroidPacket(content));	   
    }
    
}
