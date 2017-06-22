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

package org.eclipse.sensinact.annotation.processor.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Gathering of helper methods 
 */
public abstract class SNaAnnotationProcessorUtils
{   
    /**
     * Returns the map of {@link Method}s of the 
     * {@link Class} passed as parameter mapped to 
     * {@link Annotation} instances whose type is 
     * the same as the one also passed as parameter. 
     * If the method is not annotated but overwrites 
     * an annotated one, it is mapped to the annotation 
     * instance of its overwritten counterpart. 
     * Synthetic and Bridge methods are omitted.
     * 
     * @param annotated
     *      the {@link Class} in which to search
     *      properly annotated methods
     * @param annotationClass
     *      the expected {@link Annotation} type
     * @return
     *      the map of methods of the specified 
     *      class mapped to associated annotations
     */
    public static <A extends Annotation> Map<Method, A> 
    getAnnotatedMethods(Class<?> annotated, Class<A> annotationClass)
    {     
        Map<Method,A> annotatedMethods = null;
        Class<?> superClass = annotated.getSuperclass();
        if(superClass != null)
        {
            annotatedMethods = getAnnotatedMethods(
                    superClass, annotationClass);
        } else
        {
            annotatedMethods = new HashMap<Method,A>();
        }
        int index = 0;
        
        Method[] methods = annotated.getDeclaredMethods();   
        Method method = null;
        A annotation = null;
        
        for(;index<methods.length;index++)
        {
            method = methods[index];
            
            if((annotation = method.getAnnotation(
                    annotationClass))!=null)
            {
                if(!method.isSynthetic()  && !method.isBridge())
                {
                    Method overwritten = containsSignature(
                            method, annotatedMethods.keySet());
                    
                    annotatedMethods.remove(overwritten);
                    annotatedMethods.put(method, annotation);
                }
            } else
            {
                Method overwritten = containsSignature(
                        method, annotatedMethods.keySet());
                
                if(overwritten != null)
                {
                    annotation = annotatedMethods.remove(overwritten);
                    annotatedMethods.put(method, annotation);
                }
            }
        }
        return annotatedMethods;        
    }
    
    /**
     * Returns {@link Method} contained by the {@link Set} passed as 
     * parameter whose signature is the same than the one of
     * the {@link Method} also passed as parameter. Returns
     * null if the {@link Set} does not contain such a {@link
     * Method}
     * 
     * @param method
     *      the {@link Method} whose signature has to be searched
     *      in the {@link Set}
     * @param methods
     *      the {@link Set} of {@link Method} in which search the
     *      signature
     * @return
     *      the {@link Method} contained by the {@link Set} whose
     *      signature is the same than the one of the specified 
     *      {@link Method}
     */
    public static Method containsSignature(
            Method method, Set<Method> methods)
    {
        if(methods==null || methods.isEmpty())
        {
            return null;
        }
        Iterator<Method> iterator = methods.iterator();
        while(iterator.hasNext())
        {
            Method containedMethod = iterator.next();
            if(signatureEquals(method, containedMethod))
            {
                return containedMethod;
            }
        }
        return null;
    }
    
    /**
     * Returns true is the signatures of the two {@link Method}s
     * passed as parameters are the same ; returns false 
     * otherwise
     * 
     * @param first
     *      the first of the two {@link Method}s to compare
     *      the signatures of
     * @param second
     *      the second of the two {@link Method}s to compare
     *      the signatures of
     * @return
     *      true is the signatures of the two {@link Method}s
     *      are the same; <br/>false otherwise
     */
    public static boolean signatureEquals(
            Method first, Method second)
    {
        if(!first.getName().equals(second.getName()))
        {
            return false;
        }
        Class<?>[] firstParameterTypes = first.getParameterTypes();
        Class<?>[] secondParameterTypes = second.getParameterTypes();
        
        if(firstParameterTypes.length != secondParameterTypes.length)
        {
            return false;
        }
        int index = 0;
        for(;((index < firstParameterTypes.length)
            && (firstParameterTypes[index] == secondParameterTypes[index]));
                index++);
        
        return (index==firstParameterTypes.length);
    }
    
    /**
     * Returns the {@link Method} the one passed 
     * as parameter overwrites if it exists; otherwise
     * returns null
     * 
     * @param method
     *      the method to check whether it is an overwritten 
     *      super-class's one
     * @return
     *      the overwritten method or null if the specified
     *      method does not overwrite a super class one
     */
    public static Method overwritten(Method method)
    {
        Method overwritten = null;
        String methodName  = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        
        Class<?> declaringClass  = 
                method.getDeclaringClass().getSuperclass();
        while(declaringClass != null)
        {
           try
           {
               overwritten = declaringClass.getDeclaredMethod(
                       methodName, parameterTypes);
               break;
               
           } catch (Exception e)
           {
               continue;
           }
        }
        return overwritten;
    }
}
