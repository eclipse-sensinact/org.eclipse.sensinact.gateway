/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestHandler.NorthboundResponseBuildError;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 *
 */
public abstract class NorthboundAccess<W extends NorthboundRequestWrapper> {
	
	private static final Logger LOG = LoggerFactory.getLogger(NorthboundAccess.class);
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
    protected abstract boolean respond(NorthboundMediator mediator, NorthboundRequestBuilder builder) throws IOException;

    /**
     * @param i
     * @param string
     * @throws IOException
     */
    protected abstract void sendError(int i, String string) throws IOException;


    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//

    /**
     * Compresses a string stream into a byte array compressed with gzip algorithm
     *
     * @param stringContent to content to compress
     * @return th compressed content
     * @throws IOException
     */
    public static byte[] compress(final String stringContent) throws IOException {
        if ((stringContent != null) && (stringContent.length() > 0)) {
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

    protected W request;
    protected NorthboundMediator mediator;

    /**
     * @param request
     * @throws IOException
     */
    public NorthboundAccess(W request) throws IOException {
        this.request = request;
        this.mediator = request.getMediator();
        if (mediator == null) {
            sendError(500, "Unable to process the request");
            return;
        }
    }

    /**
     * @throws IOException
     */
    public void proceed() throws IOException {
        NorthboundResponseBuildError buildError = null;
        NorthboundRequestBuilder builder = null;

        DefaultNorthboundRequestHandler dnrh = new DefaultNorthboundRequestHandler();
        dnrh.init(request);
        if (dnrh.processRequestURI()) {
            builder = dnrh.handle();
            if (builder == null) 
                buildError = dnrh.getBuildError();            
        } else {
            Collection<ServiceReference<NorthboundRequestHandler>> references;
            try {
                references = mediator.getContext().getServiceReferences(NorthboundRequestHandler.class, null);

                for (ServiceReference<NorthboundRequestHandler> reference : references) {
                    NorthboundRequestHandler handler = null;

                    if (reference != null && (handler = mediator.getContext().getService(reference)) != null) {
                        try {
                        	handler.init(request, Arrays.stream(AccessMethod.Type.values()).collect(HashSet::new,Set::add,Set::addAll));                       	
                            if (handler.processRequestURI()) {
                                builder = handler.handle();
                                if (builder == null) {
                                    buildError = handler.getBuildError();
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            LOG.error(e.getMessage(), e);
                        } finally {
                            mediator.getContext().ungetService(reference);
                        }
                    }
                    if (builder != null)
                        break;
                }
            } catch (InvalidSyntaxException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        if (builder == null) {
            if (buildError == null) 
                this.sendError(400, "Invalid request");
            else
                this.sendError(buildError.status, buildError.message);
            return;
        }
        this.respond(mediator, builder);
    }

    /**
     *
     */
    public void destroy() {
        LOG.debug("Destroying NorthboundAccess '%s'", request.getRequestURI());
    }
}
