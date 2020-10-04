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
package org.eclipse.sensinact.gateway.nthbnd.http.callback;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

/**
 * The response wrapper defines the methods allowing to format it and 
 * to complete it before to send it back to the requirer
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public interface ResponseWrapper {
      
    /**
     * Defines the Map of attributes applying on the underlying wrapped response
     *
     * @return the Map of attributes applying
     */
    void setAttributes( Map<String, List<String>> attributes);

    /**
     * Set the String content of the underlying wrapped response
     *
     * @return the content of the request
     */
    void setContent(String content);

    /**
     * Set the bytes array content of the underlying wrapped response
     *
     * @param bytes the bytes array content of the underlying request
     */
    void setContent(byte[] bytes);
    
    /**
     * Returns the String content the wrapped request
     *
     * @return the content of the request
     */
    void setError(Throwable t);

    /**
     * Set the error status and message of the {@link HttpServletResponse} of
     * this CallbackContext's {@link AsyncContext}
     *
     * @param status  the int error status
     * @param message the String error message
     * 
     * @throws IOException if an input or output exception occurred
     */
    void setError(int status, String message);
    
    /**
     * Set the bytes array content of the {@link HttpServletResponse} of
     * this CallbackContext's {@link AsyncContext}
     *
     * @param bytes the bytes array content to be set as content of the
     *              {@link HttpServletResponse}
     * @throws IOException if an input or output exception occurred
     */
    void setResponseStatus(int status) ;
    
    /**
     * Send back the underlying wrapped response
     */
    void flush();

}
