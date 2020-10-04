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
