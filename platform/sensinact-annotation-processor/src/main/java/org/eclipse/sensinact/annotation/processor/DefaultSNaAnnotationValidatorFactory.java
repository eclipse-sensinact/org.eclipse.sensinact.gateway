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

/**
 * Default extended {@link SNaAnnotationValidatorFactory} instantiating
 * {@link SNaAnnotationValidator}s owning a constructor without 
 * parameter
 */
public class DefaultSNaAnnotationValidatorFactory 
extends SNaAnnotationValidatorFactory
{   

    /** 
     * @inheritDoc
     *
     * @see SNaAnnotationValidatorFactory#
     * validator(java.lang.Class)
     */
    @Override
    protected <V extends SNaAnnotationValidator> V validator(
            Class<V> validatorClass)
    {
        try
        {
            return validatorClass.newInstance();
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /** 
     * @inheritDoc
     *
     * @see SNaAnnotationValidatorFactory#
     * handle(java.lang.Class)
     */
    @Override
    protected <V extends SNaAnnotationValidator> boolean 
    handle(Class<V> validatorClass)
    {
    	try
    	{
			validatorClass.getDeclaredConstructor() ;
			return true;
			
		} catch (Exception e) 
		{
			//e.printStackTrace();
			return false;
		}
    }
}
