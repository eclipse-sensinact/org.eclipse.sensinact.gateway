/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation 
**********************************************************************/
package org.eclipse.sensinact.prototype;

import java.util.List;

import org.eclipse.sensinact.prototype.model.ResourceType;
import org.eclipse.sensinact.prototype.model.ValueType;

public class ResourceShortDescription {

    public String name;

    public ValueType valueType;

    public ResourceType resourceType;
    
    public Class<?> contentType;
    
    public List<Class<?>> actMethodArgumentsTypes;
}
