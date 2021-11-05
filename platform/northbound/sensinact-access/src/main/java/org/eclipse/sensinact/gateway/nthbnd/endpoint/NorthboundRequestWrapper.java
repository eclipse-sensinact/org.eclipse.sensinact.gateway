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
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.security.Authentication;

/**
 * Northbound request wrapper service.
 * <br/>
 * It defines the methods allowing to retrieve the relevant
 * characteristics of the request, allowing to treat it
 */
public interface NorthboundRequestWrapper {
	
    class QueryKey {
		public int index;
		public String name;
		
		public int hashCode() {
			return index;
		}
	}

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
     * Returns the String name of the identifier of the request wrapped
     * by this NorthboundAccessWrapper.
     * 
     * @return the String name of the wrapped request identifier
     */
    String getRequestIdProperty();
    
    /**
     * Returns the String identifier of the request wrapped
     * by this NorthboundAccessWrapper.
     * 
     * @return the String identifier of the wrapped request
     */
    String getRequestId();

    /**
     * Returns the map of parameters built using the query String
     * (HTML query string formated) of the wrapped request if
     * it exists
     *
     * @return the query String of the wrapped request as a
     * map
     */
    Map<NorthboundRequestWrapper.QueryKey, List<String>> getQueryMap();

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
     * @param parameters the List of {@link Parameter}s used to
     * parameterized the recipient to be returned
     * 
     * @return a newly created {@link NorthboundRecipient}
     */
    NorthboundRecipient createRecipient(List<Parameter> parameters);

    /**
     * Returns the {@link Authentication} containing the identification
     * material to be associated with the wrapped request
     *
     * @return the {@link Authentication} associated with the
     * wrapped request
     */
    Authentication<?> getAuthentication();
}
