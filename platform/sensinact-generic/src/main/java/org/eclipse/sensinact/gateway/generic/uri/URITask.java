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
package org.eclipse.sensinact.gateway.generic.uri;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.generic.Task;

/**
 * Extended {@link Task} dedicated to URI typed tasks
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface URITask extends Task
{   
    public static final Task.RequestType REQUEST_TYPE = Task.RequestType.URI;
    
    /**
     * Returns the payload of the frame command to send as 
     * a bytes array  
     * 
     * @return
     *      the payload of the frame command to send as 
     *      a bytes array  
     */
    Object getContent();
    
    /**
     * Returns the string formated URI of the targeted remote 
     * service endpoint  
     * 
     * @return
     *      the string formated URI of the targeted remote 
     *      service endpoint  
     */
    String getUri();
    
    /**
     * Returns a set options parameterizing the request to 
     * build  
     * 
     * @return
     *      a set options parameterizing the request to 
     *      build  
     */
    Map<String,List<String>> getOptions();
}
