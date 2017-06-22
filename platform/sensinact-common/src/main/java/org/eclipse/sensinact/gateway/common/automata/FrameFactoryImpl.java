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
package org.eclipse.sensinact.gateway.common.automata;


import java.lang.reflect.Constructor;

/**
 * Basis implementation of a {@link FrameFactory} service 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class FrameFactoryImpl implements FrameFactory 
{
	/**
	 * the ClassLoader of the factory
	 */
	private ClassLoader classLoader;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * 		the {@link ServiceMediator} to use
	 */
	public FrameFactoryImpl() 
	{
		this.classLoader = FrameFactoryImpl.class.getClassLoader();
	}

	/**
	 * Defines the ClassLoader of the factory
	 * 
	 * @param classLoader
	 * 		the ClassLoader of the factory
	 */
	public void setClassLoader(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}
	
	/**
	 * @inheritDoc
	 * 
	 * @see FrameFactory.box.services.api.frame.model.FrameFactoryItf#
	 * newInstance(java.lang.String, byte[])
	 */
	public Frame newInstance(FrameModel model) throws FrameException
	{
		Frame frameObject = null;
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		try
		{			
			Thread.currentThread().setContextClassLoader(this.classLoader);
			
			@SuppressWarnings("unchecked")
			Class<? extends Frame> frameClass = (Class<? extends Frame>) 
					this.classLoader.loadClass(model.getClassName());
			
			Constructor<? extends Frame> constructor = frameClass.getConstructor();
				frameObject = constructor.newInstance(new Object[]{});
				frameObject.setLength(model.length());
				frameObject.setOffset(model.offset());
				
				FrameModel[] childModels = model.children();
				int index = 0;
				
				for(;index<childModels.length;index++)
				{				
					FrameModel subModel  = childModels[index];
					Frame child = newInstance(subModel);
					if(child!=null)
					{
						frameObject.addChild(child);
					}
				}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new FrameException(e.getMessage(),e);
			
		} finally
		{
			Thread.currentThread().setContextClassLoader(current);
		}
		return frameObject;
	}

}
