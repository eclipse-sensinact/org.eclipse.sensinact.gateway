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
package org.eclipse.sensinact.gateway.core;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.sensinact.gateway.common.primitive.Typable;
import org.eclipse.sensinact.gateway.util.ReflectUtils;

/**
 * Configuration of the implemented type and interfaces of a {@link ResourceImpl}
 * instance
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class TypeConfig implements Typable<TypeConfig.Type> 
{
	private final static Class<? extends ResourceImpl> 
	DEFAULT_IMPLEMENTATION_CLASS = ResourceImpl.class;
	
	public static enum Type
	{
        STATE_VARIABLE(Resource.Type.STATE_VARIABLE), 
        SENSOR(Resource.Type.SENSOR), 
        PROPERTY(Resource.Type.PROPERTY), 
        LOCATION(Resource.Type.PROPERTY), 
        ACTION(Resource.Type.ACTION);
        
        private final Resource.Type type;

		Type(Resource.Type type)
        {
        	this.type = type;
        }
		
		Resource.Type getResourceType()
		{
			return this.type;
		}
	}
	protected final TypeConfig.Type type;

	protected Class<? extends Resource> implementationInterface;
	private Class<? extends ResourceImpl> baseClass;
	private Class<? extends ResourceImpl> implementationClass;

	protected LinkedList<Class<Resource>> list;

	private AttributeBuilder[] attributeBuilders;
	
	/**
	 * Constructor
	 * 
	 * @param implementationInterface
	 * 		the interface implemented by the {@link ResourceImpl} 
	 * 		configured by the TypeConfig to instantiate
	 */
	public TypeConfig(Class<? extends Resource> implementationInterface)
	{
		this.implementationInterface = implementationInterface;
		this.list = ReflectUtils.getOrderedImplementedInterfaces(
					Resource.class, implementationInterface);
		
		Resource.Type type = this.<Resource.Type>getConstantValue(
				Resource.TYPE_PROPERTY, true) ;
		
		String typeName = type.name();
		
		if(type == Resource.Type.PROPERTY 
				&& LocationResource.class.isAssignableFrom(
				implementationInterface))
		{
			this.type = TypeConfig.Type.LOCATION;
			
		} else
		{
			this.type =  TypeConfig.Type.valueOf(typeName);
		}
	}
	
	/**
	 * Constructor
	 * 
	 * @param type
	 * 		the {@link TypeConfig.Type} of the 
	 * 		TypeConfig to instantiate
	 */
	public TypeConfig(Type type) 
	{
		this.type = type;
		switch(type)
		{
		case ACTION:
			this.implementationInterface = ActionResource.class;
			break;
		case LOCATION:
			this.implementationInterface = LocationResource.class;
			break;
		case PROPERTY:
			this.implementationInterface = PropertyResource.class;
			break;
		case SENSOR:
			this.implementationInterface = SensorDataResource.class;
			break;
		case STATE_VARIABLE:
			this.implementationInterface = StateVariableResource.class;
			break;
		default:
			this.implementationInterface = null;
		}
		this.list = ReflectUtils.getOrderedImplementedInterfaces(
					Resource.class, this.implementationInterface);
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see Typable#getType()
	 */
	@Override
	public TypeConfig.Type getType() 
	{
		return this.type;
	}


   /**
    * Returns {@link Resource.Type} of the resources 
    * based on this {@link TypeConfig}
    * 
    * @return 
    * 		{@link Resource.Type} type of the resources 
    * 		based on this {@link TypeConfig}
    */
    public Resource.Type getResourceType()
    {
    	return this.type.getResourceType();
    }

    /**
     * Returns the extended {@link ResourceImpl} base type used
     * to create new instance using reflection
     * 
     * @return 
     * 		the extended {@link ResourceImpl} base type
     */
    public Class<? extends ResourceImpl> getResourceBaseClass()
    {
    	if(this.baseClass == null)
    	{
    		return  this.getDefaultImplementationClass();
    	}
    	return this.baseClass;
    }

    /**
     * Defines the extended {@link ResourceImpl} base type used
     * to create new instance using reflection
     * 
     * @param baseClass 
     * 		the extended {@link ResourceImpl} base type
     */
    public void  setBaseClass(Class<? extends ResourceImpl> baseClass)
    {
    	this.baseClass = baseClass;
    }
    
    /**
     * Returns the extended {@link ResourceImpl} type implemented by the
     * resources based on this {@link ResourceInfo}
     * 
     * @return 
     * 		the extended {@link ResourceImpl} type implemented by the
     *      resources based on this {@link ResourceInfo}
     */
    public Class<? extends ResourceImpl> getResourceImplementedClass()
    {
    	if(this.implementationClass == null)
    	{
    		return  this.getDefaultImplementationClass();
    	}
    	return this.implementationClass;
    }

    /**
     * Returns the extended {@link Resource} interface implemented by the
     * resources based on this {@link TypeConfig}
     * 
     * @return 
     * 		the extended {@link Resource} interface implemented by the
     *      resources based on this {@link TypeConfig}
     */
    public Class<? extends Resource> getResourceImplementedInterface()
    {
    	return this.implementationInterface;
    }

    /**
     * Returns the extended {@link Resource} interface implemented by the
     * resources based on this {@link TypeConfig}
     * 
     * @return 
     * 		the extended {@link Resource} interface implemented by the
     *      resources based on this {@link TypeConfig}
     */
    public void setResourceImplementedInterface(
    		Class<? extends Resource> implementedInterface)
    {
    	if(implementedInterface == null)
    	{
    		return;
    	}
    	switch(this.type)
		{
			case ACTION:
				if(!ActionResource.class.isAssignableFrom(implementedInterface))
				{
					return;
				}
				break;
			case LOCATION:
				if(!LocationResource.class.isAssignableFrom(implementedInterface))
				{
					return;
				}
				break;
			case PROPERTY:
				if(!PropertyResource.class.isAssignableFrom(implementedInterface))
				{
					return;
				}
				break;
			case SENSOR:
				if(!SensorDataResource.class.isAssignableFrom(implementedInterface))
				{
					return;
				}
				break;
			case STATE_VARIABLE:
				if(!StateVariableResource.class.isAssignableFrom(implementedInterface))
				{
					return;
				}
				break;
		}
    	this.implementationInterface = implementedInterface;
		this.list = ReflectUtils.getOrderedImplementedInterfaces(
					Resource.class, this.implementationInterface);
    }

    /**
     * Sets the extended {@link ResourceImpl} type implemented by the
     * resources based on this {@link ResourceInfo}
     * 
     * @param implementationClass 
     * 		the extended {@link ResourceImpl} type implemented by the
     *      resources based on this {@link ResourceInfo}
     */
    public void setImplementationClass(Class<? extends ResourceImpl> 
    implementationClass)
    {
    	this.implementationClass = implementationClass;	
    }
    
    /**
     * Returns the default extended {@link ResourceImpl} 
     * implementation class
     *  
     * @return
     * 		the default implementation class
     */
    protected Class<? extends ResourceImpl> getDefaultImplementationClass()
    {
    	return TypeConfig.DEFAULT_IMPLEMENTATION_CLASS;
    }
    
    /**
     * Returns the value of the constant field whose name
     * is passed as parameter in the list of the implemented
     * interfaces
     * 
     * @param constant
     * 		the constant field name
     * @param fromTop
     * 		search for the constant value from the top
     * 		of the list or from the bottom of the list
     * 		of the implemented interface
     * @return
     * 		the constant field value
     */
    public <T> T getConstantValue(String constant, boolean fromTop)
    {
	    return ReflectUtils.<Resource,T>getConstantValue(
		    this.list, constant, fromTop);
    }
	
	/**
	 * Returns the set of {@link AttributeBuilder}s for
	 * the configured {@link Resource} type
	 * @param service 
	 * 
	 * @return
	 * 		the set of {@link AttributeBuilder}s for the 
	 * 		configured {@link Resource} type 
	 */
	protected List<AttributeBuilder> getAttributeBuilders()
	{
		if(this.attributeBuilders == null)
		{
			this.attributeBuilders = 
				AttributeBuilder.getAttributeBuilders(
							this.list);			
		}
		return Arrays.asList(AttributeBuilder.clone(
			this.attributeBuilders,this.attributeBuilders.length));
	}
}
