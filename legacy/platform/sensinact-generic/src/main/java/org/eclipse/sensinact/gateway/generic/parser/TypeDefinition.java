/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.parser;

/**
 * Java Type wrapper
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class TypeDefinition<T>  {
	
    /**
     * the java Type 
     */
    protected T type;

    
    /**
     * Constructor
     * 
     * @param type the Java Type of the TypeDefinition 
     * to be instantiated
     */
    public TypeDefinition(T type) {
    	this.type = type;
    }

    /**
     * Returns the wrapped Java Type 
     *
     * @return the Java Type of this TypeDefinition
     */
    public T getType() {
        return this.type;
    }
}
