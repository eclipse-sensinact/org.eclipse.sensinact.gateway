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

package org.eclipse.sensinact.core.annotation.processor.test;


import java.util.List;
import java.util.ListIterator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;

import org.eclipse.sensinact.annotation.processor.SNaAnnotationValidator;

/**
 * If a {@link Name} annotation is used, a {@link Surname} 
 * one is expected - The scope is the class holding the annotated
 * field
 */
public class SNaTestAnnotationValidator extends
		SNaAnnotationValidator
{

	private String message;
	@Override
	public Boolean visit(Element element)
	{
		return new Boolean(false);
	}

	@Override
	public Boolean visit(Element element, 
			ProcessingEnvironment  environment) 
	{
		return new Boolean(false);
	}

	@Override
	public Boolean visitExecutable(ExecutableElement arg0,
			ProcessingEnvironment  environment) 
	{
		return new Boolean(false);
	}

	@Override
	public Boolean visitPackage(PackageElement arg0, 
			ProcessingEnvironment  environment) 
	{
		return new Boolean(false);
	}

	@Override
	public Boolean visitType(TypeElement typeElement,
			ProcessingEnvironment  environment)
	{
		return new Boolean(false);
	}

	@Override
	public Boolean visitTypeParameter(TypeParameterElement parameter,
			ProcessingEnvironment  environment)
	{
		return new Boolean(false);
	}

	@Override
	public Boolean visitUnknown(Element element, 
			ProcessingEnvironment environment) 
	{
		return new Boolean(false);
	}

	@Override
	public Boolean visitVariable(VariableElement variable,
			ProcessingEnvironment  environment)
	{
		TypeElement typeElement = (TypeElement) variable.getEnclosingElement();
		List<? extends Element> elements = typeElement.getEnclosedElements();
		ListIterator<? extends Element> listIterator = elements.listIterator();
		while(listIterator.hasNext())
		{
			Element element = listIterator.next();
			if(element.getAnnotation(Surname.class)!=null)
			{ 
				System.out.println("Surname annotated element found :" +
						element.getSimpleName());
				return new Boolean(true);
			}
		}
		this.message = "Surname annotated element not found ";
		return new Boolean(false);
	}

	@Override
	public String getValidationErrorMessage() 
	{
		return this.message;
	}
   
}
