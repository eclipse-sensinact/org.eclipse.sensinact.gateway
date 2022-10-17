/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

/**
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public class Argument {

    public static Object[] getParameters(Argument[] arguments) {
        int index = 0;
        int length = arguments == null ? 0 : arguments.length;
        if (length == 0) 
            return null;
        Object[] parameters = new Object[length];        
        for (; index < length; index++) 
            parameters[index] = arguments[index].value;
        return parameters;
    }

    public static Class<?>[] getParameterTypes(Argument[] arguments) {
        int index = 0;
        int length = arguments == null ? 0 : arguments.length;

        if (length == 0) {
            return null; 
        }
        Class<?>[] parameterTypes = new Class<?>[length];
        for (; index < length; index++) {
            parameterTypes[index] = arguments[index].clazz;
        }
        return parameterTypes;
    }

    public final Class<?> clazz;
    public final Object value;

    Argument(Class<?> clazz, Object value) {
        this.clazz = clazz;
        this.value = value;
    }
    
    @Override
    public String toString() {
    	return String.format("{\"type\":\"%s\",\"value\":\"%s\"}", this.clazz.getName(), String.valueOf(value));
    }
}
