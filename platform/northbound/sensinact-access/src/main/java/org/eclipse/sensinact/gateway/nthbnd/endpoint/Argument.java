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
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Argument
{
	//********************************************************************//
	//						NESTED DECLARATIONS			  			      //
	//********************************************************************//

	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//

	//********************************************************************//
	//						STATIC DECLARATIONS							  //
	//********************************************************************//

	public static Object[] getParameters(Argument[] arguments)
	{
		int index = 0;
		int length = arguments==null?0:arguments.length;
		
		if(length == 0)
		{
			return null;
		}
		Object[] parameters = new Object[length];
		for(;index < length; index++)
		{
			parameters[index] = arguments[index].value;
		}
		return parameters;
	}

	public static Class<?>[] getParameterTypes(Argument[] arguments)
	{
		int index = 0;
		int length = arguments==null?0:arguments.length;
		
		if(length == 0)
		{
			return null;
		}
		Class<?>[] parameterTypes = new Class<?>[length];
		for(;index < length; index++)
		{
			parameterTypes[index] = arguments[index].clazz;
		}
		return parameterTypes;
	}
	
	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//
	
	public final Class<?> clazz;
	public final Object value;
	
	Argument(Class<?> clazz, Object value)
	{
		this.clazz = clazz;
		this.value = value;
	}
}
