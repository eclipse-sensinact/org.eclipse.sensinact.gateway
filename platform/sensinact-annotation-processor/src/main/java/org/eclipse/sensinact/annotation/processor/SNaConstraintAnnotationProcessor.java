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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
 
@SupportedAnnotationTypes(value = { "*" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class SNaConstraintAnnotationProcessor extends AbstractProcessor 
{
 
  private static final String VALIDATION = "validation";
  
  /** 
  * @inheritDoc
  *
  * @see javax.annotation.processing.AbstractProcessor#
  * process(java.util.Set, javax.annotation.processing.RoundEnvironment)
  */
  @Override
  public boolean process( 
		  Set<? extends TypeElement> annotations, 
		  RoundEnvironment roundEnv)
  {
      if (roundEnv.processingOver())
      {
          return true;
      }
      Iterator<? extends TypeElement> iterator = annotations.iterator();
        
      Element constraintElement = processingEnv.getElementUtils()
                .getTypeElement(SNaAnnotationConstraint.class.getName());
        
      if (constraintElement == null)
      {
          return true;
      }
      TypeMirror constraintType = constraintElement.asType();
        
      while (iterator.hasNext())
      {
          TypeElement typeElement = iterator.next();
          
          List<? extends AnnotationMirror> annotationMirrors = 
                  typeElement.getAnnotationMirrors();
          
          TypeMirror validatorTypeMirror = null;
          
          for (AnnotationMirror annotationMirror : annotationMirrors)
          {
              TypeMirror mirror = annotationMirror.getAnnotationType().asElement().asType();
              if (!mirror.equals(constraintType))
              { 
                  continue;
              }                 
              Map<? extends ExecutableElement, ? extends AnnotationValue> values = 
                      annotationMirror.getElementValues();
              
              for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> 
              entry : values.entrySet())
              {                      
                  if (VALIDATION.equals(entry.getKey().getSimpleName().toString()))
                  {
                      validatorTypeMirror = (TypeMirror) 
                              entry.getValue().getValue();
                      break;
                  }
              }
          }
          if (validatorTypeMirror == null)
          {
              continue;
          }
          String validatorClassName = ((TypeMirror) validatorTypeMirror).toString();
          SNaAnnotationValidator validator = SNaAnnotationValidatorFactory.validator(
                  validatorClassName);
          
          if (validator == null)
          {
              processingEnv.getMessager().printMessage(Kind.ERROR,
                      "Invalid validator : " + validatorClassName);
              break;
          }
          Set<? extends Element> annotatedElements = 
                  roundEnv.getElementsAnnotatedWith(
                  typeElement);
         
          Iterator<? extends Element> annotatedIterator = 
                  annotatedElements.iterator();
         
          while (annotatedIterator.hasNext())
          {
              Element annotated = annotatedIterator.next();
              if (!annotated.accept(validator, processingEnv))
              {
                  processingEnv.getMessager().printMessage(Kind.ERROR,
                          validator.getValidationErrorMessage());
              }
          }
      }
      return true;
    }
  }
