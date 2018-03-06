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
package org.eclipse.sensinact.gateway.commands.gogo.internal.shell;

import java.io.IOException;

import org.eclipse.sensinact.gateway.commands.gogo.osgi.CommandServiceMediator;
import org.eclipse.sensinact.gateway.core.ResultHolder;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccess;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundEndpoint;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequest;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestBuilder;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.format.JSONResponseFormat;
import org.json.JSONObject;

/**
 * Extended {@link NorthboundAccess} dedicated to shell access request
 * processing
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ShellAccess extends NorthboundAccess<ShellAccessRequest>
{	
	public static void proceed(CommandServiceMediator mediator, 
			JSONObject object) 
	{
    	try 
    	{
    		ShellAccessRequest request = new ShellAccessRequest(
    				mediator, object);
			new ShellAccess(request).proceed();
			
		} catch (InvalidCredentialException | IOException e) 
    	{
			mediator.getOutput().outputError(
					520, e.getMessage());
		}    	
	}
	
	protected NorthboundEndpoint endpoint;

	/**
	 * Constructor
	 * 
	 * @param request the {@link ShellAccessRequest} that will be treated
	 * by the ShellAccess to be instantiated
	 * 
	 * @throws IOException
	 * @throws InvalidCredentialException
	 */
	public ShellAccess(ShellAccessRequest request)
		throws IOException, InvalidCredentialException
	{
		super(request);
		this.endpoint = ((CommandServiceMediator)super.mediator
				).getEndpoint();
	}
	
	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccess#
	 * respond(org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator, org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestBuilder)
	 */
	@Override
	protected boolean respond(
			NorthboundMediator mediator,
			NorthboundRequestBuilder builder) 
			throws IOException 
	{
		NorthboundRequest nthbndRequest = builder.build();
		if(nthbndRequest == null)
		{
			this.sendError(500, "Internal server error");
			return false;
		}
		ResultHolder<?> cap = this.endpoint.execute(nthbndRequest);
		JSONObject result = new JSONResponseFormat(mediator
			).format(cap.getResult());
		
		if(result == null)
		{
			this.sendError(500, "Internal server error");
			return false;
		}
		((CommandServiceMediator)super.mediator
				).getOutput().output(result,0);
		return true;
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccess#
	 * sendError(int, java.lang.String)
	 */
	@Override
	protected void sendError(int i, String message)
			throws IOException
	{
		((CommandServiceMediator)super.mediator
				).getOutput().outputError(i, message);
	}
}
