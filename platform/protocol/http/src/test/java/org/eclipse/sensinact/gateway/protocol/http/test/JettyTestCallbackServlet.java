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
package org.eclipse.sensinact.gateway.protocol.http.test;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class JettyTestCallbackServlet extends HttpServlet {
    /**
     */
    private CallbackCollection callbackCollection;

    /**
     * @param jettyTestServer
     */
    public JettyTestCallbackServlet(CallbackCollection callbackCollection) {
        this.callbackCollection = callbackCollection;
    }

    /**
     * @inheritDoc
     * @see javax.servlet.http.HttpServlet#
     * doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getRequestURI().equals("/error")) {
            response.setStatus(520);
            return;
        }
        this.doHandle(request, response, this.callbackCollection.getdoGetCallbacks());
    }

    /**
     * @inheritDoc
     * @see javax.servlet.http.HttpServlet#
     * doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getRequestURI().equals("/error")) {
            response.setStatus(520);
            return;
        }
        this.doHandle(request, response, this.callbackCollection.getdoPostCallbacks());
    }

    /**
     * @param request
     * @param response
     * @param callbackList
     * @throws IOException
     */
    private void doHandle(HttpServletRequest request, HttpServletResponse response, List<Callback> callbackList) throws IOException {
        int index = 0;
        int length = callbackList == null ? 0 : callbackList.size();

        for (; index < length; index++) {
            Callback callback = callbackList.get(index);
            Class<?>[] parameterTypes = callback.method.getParameterTypes();

            int parametersIndex = 0;
            int parametersLength = parameterTypes == null ? 0 : parameterTypes.length;

            Object[] parameters = new Object[parametersLength];

            for (; parametersIndex < parametersLength; parametersIndex++) {
                Class<?> parameterClass = parameterTypes[parametersIndex];
                if (ServletRequest.class.isAssignableFrom(parameterClass)) {
                    parameters[parametersIndex] = request;
                    continue;
                }
                if (ServletResponse.class.isAssignableFrom(parameterClass)) {
                    parameters[parametersIndex] = response;
                    continue;
                }
                if (ServletContext.class.isAssignableFrom(parameterClass)) {
                    parameters[parametersIndex] = super.getServletContext();
                    continue;
                }
                if (ServletConfig.class.isAssignableFrom(parameterClass)) {
                    parameters[parametersIndex] = super.getServletConfig();
                    continue;
                }
                parameters[parametersIndex] = super.getServletContext().getAttribute(parameterClass.getCanonicalName());
            }
            try {
                callback.invoke(parameters);

            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }
}