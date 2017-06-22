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
package org.eclipse.sensinact.gateway.core.security.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field annotation identifying immutable TABLE, 
 * meaning that it is not possible to UPDATE an 
 * existing entity or to CREATE a new one
 */
@Inherited
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE})
public @interface Immutable
{
	enum Operation
	{
		CREATE,
		READ,
		UPDATE,
		DELETE;		
	}
	
	Operation[] operation() default {
		Operation.CREATE,
		Operation.UPDATE,
		Operation.DELETE};
}