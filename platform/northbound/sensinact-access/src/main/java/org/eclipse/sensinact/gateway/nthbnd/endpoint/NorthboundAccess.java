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

package org.eclipse.sensinact.gateway.nthbnd.endpoint;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;

import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestHandler.NorthboundResponseBuildError;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * 
 */
public abstract class NorthboundAccess<F, W extends NorthboundRequestWrapper>
{
	//********************************************************************//
	//						NESTED DECLARATIONS			  			      //
	//********************************************************************//

	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//

    /**
     * @param mediator
     * @param builder
     * @return
     * @throws IOException
     */
    protected abstract boolean respond(
    	NorthboundMediator mediator, NorthboundRequestBuilder<F> builder) 
    	throws IOException;

	/**
	 * @param i
	 * @param string
	 * @throws IOException 
	 */
	protected abstract void sendError(int i, String string) 
		throws IOException;
	
	
	//********************************************************************//
	//						STATIC DECLARATIONS							  //
	//********************************************************************//

    /**
     * Compresses a string stream into a byte array compressed with gzip algorithm
     * @param stringContent to content to compress
     * @return th compressed content
     * @throws IOException
     */
    public static byte[] compress(final String stringContent) 
    		throws IOException 
    {
        if ((stringContent != null) && (stringContent.length() > 0)) 
        {
            ByteArrayOutputStream byteArrayOutputstream = new ByteArrayOutputStream();
            GZIPOutputStream compressContent = new GZIPOutputStream(byteArrayOutputstream);
            compressContent.write(stringContent.getBytes("UTF-8"));
            compressContent.close();
            return byteArrayOutputstream.toByteArray();
        }
        return null;
    }
    
	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//
    
	protected NorthboundEndpoint endpoint;
	public W request;
	
	/**
	 * @param request
	 */
	public NorthboundAccess(W request)
	{
		this.request = request;
	}

	/**
	 * @throws IOException
	 */
	public void proceed() throws IOException
	{
		NorthboundMediator mediator = request.getMediator();
		if(mediator == null)
		{
			sendError(500, "Unable to process the request");
			return;
		}
		Authentication<?> authentication = request.getAuthentication();
		try
		{
			this.endpoint = new NorthboundEndpoint(mediator, authentication);
			
		} catch (InvalidCredentialException e)
		{
			sendError(401, "Unauthorized");
			return;
		}	
		NorthboundResponseBuildError buildError = null;
		NorthboundRequestBuilder<F> builder = null;
		
		DefaultNorthboundRequestHandler dnrh = new DefaultNorthboundRequestHandler();
		dnrh.init(request);

		if(dnrh.processRequestURI())
		{
			builder = dnrh.<F>handle();			
			if(builder == null)
			{
				buildError = dnrh.getBuildError();
			}
		} else
		{
			Collection<ServiceReference<NorthboundRequestHandler>> 
			references;
			try
			{
				references = mediator.getContext().getServiceReferences(
					NorthboundRequestHandler.class, null);
				
				for (ServiceReference<NorthboundRequestHandler> reference 
						: references)
				{
					NorthboundRequestHandler handler = null;
					
					if(reference != null && (handler = mediator.getContext(
							).getService(reference))!=null)
					{
						try
						{
							handler.init(request);
							if(handler.processRequestURI())
							{
								builder = handler.<F>handle();
								if(builder == null)
								{
									buildError = handler.getBuildError();
									break;
								}
							}
						} catch(IOException e)
						{
							mediator.error(e);
							
						} finally
						{
							mediator.getContext().ungetService(reference);
						}
					}
					if(builder != null)
					{
						break;
					}					
				}
			}
			catch (InvalidSyntaxException e)
			{
				mediator.error(e);
			}
		}
		if(builder == null)
		{
			if(buildError == null)
			{
				this.sendError(400, "Invalid request");
			} else
			{
				this.sendError(buildError.status, buildError.message);
			}
			return;
		}
		this.respond(mediator, builder);
	}

	/**
	 * 
	 */
	public void destroy()
	{	
		this.endpoint = null;
	}
}
