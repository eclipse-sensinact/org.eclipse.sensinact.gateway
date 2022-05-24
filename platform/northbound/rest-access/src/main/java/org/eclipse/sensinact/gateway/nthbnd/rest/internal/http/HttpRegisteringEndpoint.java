/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.rest.internal.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequest;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper.QueryKey;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.RegisteringResponse;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.RestAccessConstants;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonObject;

/**
 * This class is the REST interface between each others classes
 * that perform a task and jersey
 */
@SuppressWarnings("serial")
@WebServlet()
public class HttpRegisteringEndpoint extends HttpServlet {
	
	private static final Logger LOG = LoggerFactory.getLogger(HttpRegisteringEndpoint.class);
    private NorthboundMediator mediator;

    /**
     * Constructor
     */
    public HttpRegisteringEndpoint(NorthboundMediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        this.doExecute(request, response);
    }

    /**
     * @param request
     * @param response
     * @throws IOException
     */
    private final void doExecute(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        try {
            String queryString = request.getQueryString();
            if(queryString == null) {
            	response.sendError(400, "'create' or 'renew' request parameter expected");
            }
            String query = null;
            Map<QueryKey,List<String>> map = NorthboundRequest.processRequestQuery(queryString);
            Set<QueryKey> queryKeys = map.keySet();
            Iterator<QueryKey> iterator = queryKeys.iterator();
            while(iterator.hasNext()) {
            	QueryKey queryKey = iterator.next();
            	switch(queryKey.name) {
            	  case "request":
            		List<String> list = map.get(queryKey);
	                if(list != null)
	                	query = list.get(list.size()-1);
	                break;
            	  case "create":
            	  case "renew":
            		  query = queryKey.name;
            		  break;
            	  default:
            		  break;
            	}
            	if(query!=null)
            		break;
            }
            if(query == null) 
            	response.sendError(400, "'create' or 'renew' request parameter expected");
                              
            byte[] content = IOUtils.read(request.getInputStream(),false);
            JsonObject jcontent = JsonProviderFactory.getProvider().createReader(new ByteArrayInputStream(content)).readObject();          

            RegisteringResponse registeringResponse = null;
            
            switch(query) {
                case "create":
                	String login = jcontent.getString("login", null);
                	String password= jcontent.getString("password", null);
                	String account= jcontent.getString("account", null);
                	String accountType= jcontent.getString("accountType", null);
                    registeringResponse = mediator.getAccessingEndpoint().registeringEndpoint(login, password, account, accountType);
                     break;
                case "renew":
                	 account= jcontent.getString("account", null);
                    registeringResponse = mediator.getAccessingEndpoint().passwordRenewingEndpoint(account);
                     break;
				default:
                	response.sendError(400, "'create' or 'renew' request parameter expected");
            }
            byte[] resultBytes = registeringResponse.getJSON().getBytes();
            response.setContentType(RestAccessConstants.JSON_CONTENT_TYPE);
            response.setContentLength(resultBytes.length);
            response.setBufferSize(resultBytes.length);

            ServletOutputStream output = response.getOutputStream();
            output.write(resultBytes);

            response.setStatus(200);

        } catch (ClassCastException e) {
            LOG.error(e.getMessage(), e);
            response.sendError(400, "Invalid parameters type");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            response.sendError(520, "Internal server error");
        } 
    }
}
