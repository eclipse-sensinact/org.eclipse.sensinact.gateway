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
package org.eclipse.sensinact.gateway.generic.stream;

import org.eclipse.sensinact.gateway.generic.Task;

/**
 * Extended {@link Task} dedicated to STREAM typed tasks
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface StreamTask extends Task
{   
    public static final Task.RequestType REQUEST_TYPE = Task.RequestType.STREAM;
    
    /**
     * Returns the payload of the frame command to send as 
     * a bytes array  
     * 
     * @return
     *      the payload of the frame command to send as 
     *      a bytes array  
     */
    byte[] getPayloadBytesArray();
}
