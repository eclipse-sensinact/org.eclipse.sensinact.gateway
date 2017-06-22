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

package org.eclipse.sensinact.annotation.processor;

import java.lang.annotation.Annotation;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementVisitor;

/**
 * Validation service of an SNaAnnotationContraint 
 * applied on an other {@link Annotation}
 * 
 * the visited (and validated ) Element is the one on which 
 * applies the constrained {@link Annotation}
 */
public abstract class SNaAnnotationValidator
implements ElementVisitor<Boolean,ProcessingEnvironment>
{    
	/**
	 * Returns the validation error message
	 * 
	 * @return
	 * 		the validation error message
	 */
	public abstract String getValidationErrorMessage();
}
