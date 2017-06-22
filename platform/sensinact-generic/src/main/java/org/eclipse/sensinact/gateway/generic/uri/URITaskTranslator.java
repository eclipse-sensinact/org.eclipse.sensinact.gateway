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
import org.eclipse.sensinact.gateway.generic.TaskTranslator;

/**
 * Service dedicated to data stream transmission
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface URITaskTranslator extends TaskTranslator
{       
    public static final Task.RequestType REQUEST_TYPE = Task.RequestType.URI;
    
    /**
     * Creates an appropriate request and sends it
     * 
     * @param serviceProviderIdentifier
     * 		the String identifier of the requirer {@link ServiceProvider}
     * @param path
     * 		URI targeted by the request to create  
     * @param content
     * 		the request object content
     * @param options
     * 		set of options applying on the request to create
     */
    void send(String serviceProviderIdentifier, String path, Object content, 
    		Map<String,List<String>> options);
}
