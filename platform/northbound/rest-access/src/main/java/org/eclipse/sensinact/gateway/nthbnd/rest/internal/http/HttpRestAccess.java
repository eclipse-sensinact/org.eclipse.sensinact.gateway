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
package org.eclipse.sensinact.gateway.nthbnd.rest.internal.http;

import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.DescribeResponse;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.AuthenticationToken;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccess;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundEndpoint;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequest;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestBuilder;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper.QueryKey;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.RestAccessConstants;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public class HttpRestAccess extends NorthboundAccess<HttpRestAccessRequest> {
    private HttpServletResponseWrapper response;
    private NorthboundEndpoint endpoint;

    /**
     * @param request
     * @param response
     * @throws IOException
     * @throws InvalidCredentialException
     */
    public HttpRestAccess(HttpRestAccessRequest request, HttpServletResponseWrapper response) throws IOException, InvalidCredentialException {
        super(request);
        this.response = response;
        Authentication<?> authentication = request.getAuthentication();
        if (authentication == null) {
            this.endpoint = request.getMediator().getNorthboundEndpoints().getEndpoint();
            response.setHeader("X-Auth-Token", this.endpoint.getSessionToken());
        } else if (AuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            this.endpoint = request.getMediator().getNorthboundEndpoints().getEndpoint((AuthenticationToken) authentication);
        } else {
            throw new InvalidCredentialException("Authentication token was expected");
        }
    }

    @Override
    protected boolean respond(NorthboundMediator mediator, NorthboundRequestBuilder builder) throws IOException {
        String httpMethod = super.request.getMethod();
        String snaMethod = builder.getMethod();

        switch (snaMethod) {
            case "DESCRIBE":
            case "GET":
                if (!"GET".equals(httpMethod)) {
                    sendError(400, "Invalid HTTP method");
                    return false;
                }
                break;
            case "ACT":
            case "UNSUBSCRIBE":
            case "SET":
            case "SUBSCRIBE":
                if (!"POST".equals(httpMethod)) {
                    sendError(400, "Invalid HTTP method");
                    return false;
                }
                break;
            default:
                break;
        }
        NorthboundRequest nthbndRequest = builder.build();
        if (nthbndRequest == null) {
            sendError(500, "Internal server error");
            return false;
        }
        AccessMethodResponse<?> cap = this.endpoint.execute(nthbndRequest);
        if (cap == null) {
            sendError(500, "Internal server error");
            return false;
        }
        String result = null;
        List<String> rawList = null;
        Map<QueryKey, List<String>> queryMap = super.request.getQueryMap();
        Iterator<QueryKey> iterator = queryMap.keySet().iterator();
        while(iterator.hasNext()) {
        	QueryKey queryKey = iterator.next();
        	if("rawDescribe".equals(queryKey.name)) {
        		rawList = queryMap.get(queryKey);
        		break;
        	}
        }
        if (rawList != null && (rawList.contains("true") || rawList.contains("True") || rawList.contains("yes") || rawList.contains("Yes")) && DescribeResponse.class.isAssignableFrom(cap.getClass()))
            result = ((DescribeResponse<?>) cap).getJSON(true);
        else
            result = cap.getJSON();
        byte[] resultBytes;
        String acceptEncoding = super.request.getHeader("Accept-Encoding");
        if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
            resultBytes = NorthboundAccess.compress(result);
            response.setHeader("Content-Encoding", "gzip");

        } else {
            resultBytes = result.getBytes("UTF-8");
        }
        int length = -1;
        if ((length = resultBytes == null ? 0 : resultBytes.length) > 0) {
            response.setContentType(RestAccessConstants.JSON_CONTENT_TYPE);
            response.setContentLength(resultBytes.length);
            response.setBufferSize(resultBytes.length);

            ServletOutputStream output = this.response.getOutputStream();
            output.write(resultBytes);
        }
        response.setStatus(cap.getStatusCode());
        return true;

    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccess#
     * sendError(int, java.lang.String)
     */
    @Override
    protected void sendError(int statusCode, String message) throws IOException {
        this.response.sendError(statusCode, message);
    }
}
