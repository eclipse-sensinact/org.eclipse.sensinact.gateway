/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.http.forward;

import org.eclipse.jetty.server.Request;
import org.eclipse.sensinact.gateway.common.execution.Executable;

import java.util.Dictionary;

import javax.servlet.http.HttpServletRequest;

/**
 * A ForwardingService provides the information allowing to create a
 * {@link ForwardingFilter}
 *
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public interface ForwardingService {
	
    /**
     * Returns the String pattern of a {@link ForwardingFilter}
     * based on this ForwardingService
     *
     * @return the String pattern of this ForwardingService
     */
    String getPattern();

    /**
     * Returns the initial set of properties of a {@link ForwardingFilter}
     * based on this ForwardingService
     *
     * @return the set of properties of this ForwardingService
     */
    Dictionary<String, Object> getProperties();
    
    /**
     * Returns the {@link Executable} in charge of building the query to
     * be forwarded by the {@link ForwardingFilter} based on this ForwardingService,
     * and according to the {@link Request} parameterizing its execution
     *
     * @return the forwarding query of this ForwardingService
     */
	String getQuery(HttpServletRequest baseRequest);

    /**
     * Returns the {@link Executable} in charge of building the forwarding
     * URI of a {@link ForwardingFilter} based on this ForwardingService,
     * according to the {@link Request} parameterizing its execution
     *
     * @return the forwarding URI builder of this ForwardingService
     */
	String getUri(HttpServletRequest baseRequest);
	
	/**
     * Returns the {@link Executable} in charge of building the forwarding
     * URI of a {@link ForwardingFilter} based on this ForwardingService,
     * according to the {@link Request} parameterizing its execution
     *
     * @return the forwarding URI builder of this ForwardingService
     */
	String getParam(HttpServletRequest baseRequest);
	
	/**
     * Returns the {@link Executable} in charge of building the forwarding
     * URI of a {@link ForwardingFilter} based on this ForwardingService,
     * according to the {@link Request} parameterizing its execution
     *
     * @return the forwarding URI builder of this ForwardingService
     */
	String getFragment(HttpServletRequest baseRequest);
}
