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

import java.util.ServiceLoader;

/**
 * Factory service of {@link SNaAnnotationValidator}s
 */
public abstract class SNaAnnotationValidatorFactory
{   
    /**
     * Returns a new {@link SNaAnnotationValidator} whose type is 
     * passed as parameter
     * 
     * @param validatorClass
     *      the extended {@link SNaAnnotationValidator} type 
     *      to create an instance of
     * @return
     *      a new created instance of the {@link SNaAnnotationValidator}
     *      or null if the type is not handle by this factory
     */
    protected abstract <V extends SNaAnnotationValidator> 
    V validator(Class<V> validatorClass);
    
    /**
     * Returns true if the {@link SNaAnnotationValidator} type 
     * passed as parameter is handled by this factory; returns
     * false otherwise
     * 
     * @param validatorClass
     *      the extended {@link SNaAnnotationValidator} type to
     *      check whether it is handled by this factory or not      
     * @return
     *      true if the {@link SNaAnnotationValidator} type 
     *      is handled; false otherwise
     */
    protected abstract <V extends SNaAnnotationValidator> 
    boolean handle(Class<V> validatorClass);
    
    /**
     * Returns a new SNaAnnotationValidatorFactory 
     * handling the {@link SNaAnnotationValidator} 
     * type passed as parameter, or null if no factory 
     * can be found
     * 
     * @param validatorClass
     *      the extended {@link SNaAnnotationValidator} 
     *      type for which to search (and to instantiate) an 
     *      appropriate factory
     * @return
     *      a new created instance of an SNaAnnotationValidatorFactory
     *      handling the specified extended {@link SNaAnnotationValidator} 
     *      type
     */
    private static <F extends SNaAnnotationValidator> 
    SNaAnnotationValidatorFactory factory(Class<F> validatorClass)
    {
       ServiceLoader<SNaAnnotationValidatorFactory> 
        resourceFactoryLoader =  ServiceLoader.load(
                SNaAnnotationValidatorFactory.class,  
                Thread.currentThread().getContextClassLoader());
       
       SNaAnnotationValidatorFactory factory = null;
       
       for (SNaAnnotationValidatorFactory validatorFactory : 
           resourceFactoryLoader) 
       {
           if(validatorFactory.handle(validatorClass))
           {
        	   factory = validatorFactory;
               break;
           }
       }
       return factory;
    }
    
    /**
     * Returns a new created {@link SNaAnnotationValidator}
     * whose type is passed as parameter,
     * 
     * @param validatorClass
     *      the extended {@link SNaAnnotationValidator} 
     *      type to create an instance of
     * @return
     *      a new created instance of the extended 
     *      {@link SNaAnnotationValidator} type
     */
    @SuppressWarnings("unchecked")
    public static <V extends SNaAnnotationValidator> 
    V validator(String validatorClassName)
    {
        ClassLoader loader = Thread.currentThread(
                ).getContextClassLoader();
        Class<V> validatorClass = null;
        try
        {
            validatorClass = (Class<V>) loader.loadClass(
                    validatorClassName);
            SNaAnnotationValidatorFactory factory = 
                    SNaAnnotationValidatorFactory.factory(
                            validatorClass);
            if (factory != null)
            {
                return factory.validator(
                        validatorClass);
            }
        } catch (Exception e) // could be ClassNotFoundException or ClassCastException
        {
            e.printStackTrace();
        }
       return null;
    }
}
