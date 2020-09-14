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

import java.util.List;
import java.util.Map;

/**
 * the request wrapper defines the methods allowing to retrieve the relevant
 * characteristics permitting to treat it
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public interface RequestWrapper {
    	
    /**
     * Returns the String uri of the underlying wrapped request
     *
     * @return the uri of the request
     */
    String getRequestURI();
    
    /**
     * Returns the map of parameters built using the query String
     * of the underlying wrapped request if any
     *
     * @return the query String of the request as a map
     */
    Map<String, List<String>> getQueryMap();    

    /**
     * Returns the map of attributes of the underlying wrapped request
     * including the headers if it is an Http one
     *
     * @return the map of attributes of the request
     */
    Map<String, List<String>> getAttributes();

    /**
     * Returns the String content the underlying  wrapped request
     *
     * @return the content of the request
     */
    String getContent();

}
