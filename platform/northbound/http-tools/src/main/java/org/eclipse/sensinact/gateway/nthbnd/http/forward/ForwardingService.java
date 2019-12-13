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
package org.eclipse.sensinact.gateway.nthbnd.http.forward;

import java.util.Dictionary;

import javax.servlet.http.HttpServletRequest;

/**
 * A ForwardingService provides the information allowing to create a
 * {@link ForwardingFilter}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
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
    Dictionary getProperties();
    
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
