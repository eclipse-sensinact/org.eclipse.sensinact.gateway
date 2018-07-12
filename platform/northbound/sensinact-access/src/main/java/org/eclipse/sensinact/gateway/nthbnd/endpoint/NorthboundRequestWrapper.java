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

import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.security.Authentication;

import java.util.List;
import java.util.Map;

/**
 * Northbound request wrapper service.
 * <p>
 * It defines the methods allowing to retrieve the relevant
 * characteristics of the request, allowing to treat it
 */
public interface NorthboundRequestWrapper {
    /**
     * Returns the {@link NorthboundMediator} of this
     * NorthboundAccessWrapper, allowing to interact with
     * the OSGi host environment
     *
     * @return the {@link NorthboundMediator} of this
     * NorthboundAccessWrapper
     */
    NorthboundMediator getMediator();

    /**
     * Returns the String uri of the request wrapped
     * by this NorthboundAccessWrapper.
     *
     * @return the wrapped request's String uri
     */
    String getRequestURI();

    /**
     * Returns the String identifier of the request wrapped
     * by this NorthboundAccessWrapper if it exists. Otherwise
     * returns null
     *
     * @param parameters the Set of {@link Parameter}s held
     *                   by the wrapped request
     * @return the wrapped request's String identifier
     */
    String getRequestID(Parameter[] parameters);

    /**
     * Returns the map of parameters built using the query String
     * (HTML query string formated) of the wrapped request if
     * it exists
     *
     * @return the query String of the wrapped request as a
     * map
     */
    Map<String, List<String>> getQueryMap();

    /**
     * Returns the String content the wrapped request
     *
     * @return the content of the request
     */
    String getContent();

    /**
     * Returns a new {@link NorthboundRecipient} created using the
     * array of {@link Parameter}s passed as parameter
     *
     * @param parameters the array of {@link Parameter}s used to
     *                   parameterized the recipient to be returned
     * @return a newly created {@link NorthboundRecipient}
     */
    NorthboundRecipient createRecipient(Parameter[] parameters);

    /**
     * Returns the {@link Authentication} containing the identification
     * material to be associated with the wrapped request
     *
     * @return the {@link Authentication} associated with the
     * wrapped request
     */
    Authentication<?> getAuthentication();
}
