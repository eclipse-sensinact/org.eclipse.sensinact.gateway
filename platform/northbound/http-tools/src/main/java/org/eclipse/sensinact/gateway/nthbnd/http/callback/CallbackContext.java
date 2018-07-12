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
package org.eclipse.sensinact.gateway.nthbnd.http.callback;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * Holds data structures and helper methods to process a {@link ServletRequest}
 * and to build the appropriate {@link ServletResponse}
 */
public class CallbackContext {

    private static final int BUFFER_SIZE = 64 * 1024;

    private Mediator mediator;
    private AsyncContext asyncContext;

    private String method;
    private byte[] content;

    /**
     * Constructor
     *
     * @param mediator     the {@link Mediator} allowing the CallbackContext
     *                     to be instantiated to interact with the OSGi host environment
     * @param method       the String name of the HTTP method
     * @param asyncContext the {@link AsyncContext}
     */
    public CallbackContext(Mediator mediator, String method, AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
        this.method = method;
        if (this.asyncContext == null) {
            throw new NullPointerException("Null AsyncContext");
        }
    }

    /**
     * Returns the String name of the HTTP method of the call that
     * is at the origin of this CallbackContext birth
     *
     * @return the HTTP method String name
     */
    public String getMethod() {
        return this.method;
    }

    /**
     * Returns the {@link AsyncContext} of this CallbackContext
     *
     * @return this CallbackContext's {@link AsyncContext}
     */
    public AsyncContext getAsyncContext() {
        return this.asyncContext;
    }

    /**
     * Returns the {@link HttpServletRequest} of the {@link AsyncContext}
     * of this CallbackContext
     *
     * @return the {@link HttpServletRequest} of this CallbackContext's
     * {@link AsyncContext}
     */
    public HttpServletRequest getRequest() {
        return (HttpServletRequest) getAsyncContext().getRequest();
    }

    /**
     * Returns the {@link HttpServletResponse} of the {@link AsyncContext}
     * of this CallbackContext
     *
     * @return the {@link HttpServletResponse} of this CallbackContext's
     * {@link AsyncContext}
     */
    public HttpServletResponse getResponse() {
        return (HttpServletResponse) getAsyncContext().getResponse();
    }

    /**
     * Returns the bytes array content of the {@link HttpServletRequest} attached
     * to the {@link AsyncContext} of this CallbackContext
     *
     * @return the bytes array content of the {@link HttpServletRequest} processed
     * @throws IOException if an input or output exception occurred
     */
    public byte[] getRequestContent() throws IOException {
        if (this.content == null) {
            int read = 0;
            int pos = 0;
            byte[] buffer = new byte[BUFFER_SIZE];

            int length = getRequest().getContentLength();
            byte[] content = new byte[pos];
            try {
                InputStream input = getRequest().getInputStream();
                while (true) {
                    if (length > -1 && pos >= length) {
                        break;
                    }
                    read = input.read(buffer);
                    if (read == -1) {
                        break;
                    }
                    byte[] newContent = new byte[pos + read];
                    if (pos > 0) {
                        System.arraycopy(content, 0, newContent, 0, pos);
                    }
                    System.arraycopy(buffer, 0, newContent, pos, read);
                    content = newContent;
                    newContent = null;
                    pos += read;
                }
            } catch (IOException e) {
                mediator.error(e);
                throw e;
            }
            this.content = content;
        }
        return this.content;
    }

    /**
     * Set the bytes array content of the {@link HttpServletResponse} attached
     * to the {@link AsyncContext} of this CallbackContext
     *
     * @param bytes the bytes array content of to the built {@link HttpServletResponse}
     * @throws IOException if an input or output exception occurred
     */
    public void setResponseContent(byte[] bytes) throws IOException {
        int length = bytes == null ? 0 : bytes.length;
        if (length > 0) {
            HttpServletResponse response = getResponse();
            response.setContentLength(length);
            response.setBufferSize(length);
            ServletOutputStream output = response.getOutputStream();
            output.write(bytes);
        }
    }

    /**
     * Set the bytes array content of the {@link HttpServletResponse} of
     * this CallbackContext's {@link AsyncContext}
     *
     * @param bytes the bytes array content to be set as content of the
     *              {@link HttpServletResponse}
     * @throws IOException if an input or output exception occurred
     */
    public void setResponseStatus(int status) {
        getResponse().setStatus(status);
    }

    /**
     * Set the error status and message of the {@link HttpServletResponse} of
     * this CallbackContext's {@link AsyncContext}
     *
     * @param status  the int error status
     * @param message the String error message
     * @throws IOException if an input or output exception occurred
     */
    public void setResponseError(int status, String message) throws IOException {
        getResponse().sendError(status, message);
    }
}
