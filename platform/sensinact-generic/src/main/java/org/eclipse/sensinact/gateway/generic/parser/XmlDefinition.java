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
package org.eclipse.sensinact.gateway.generic.parser;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * Abstract xml element definition 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class XmlDefinition
{    
    /**
     * the associated {@link Mediator}
     */
    protected Mediator mediator;

    /**
     * Setting {@link Method} of an xml element content
     */
    protected Method tagged;
    
    
    /**
     * Constructor
     * 
     * @param mediator
     *      the associated Mediator
     * @param atts
     *      the set of attributes data structure for the 
     *      xml element
     */
    public XmlDefinition (Mediator mediator, Attributes atts)
    {
        this.mediator = mediator;        
        XmlElement element = this.getClass().getAnnotation(XmlElement.class);
       
        if(element != null)
        {            
            String name = element.tag();
            String field = element.field();

            if(!name.equals(XmlElement.DEFAULT_ELEMENT_VALUE)
                   && !field.equals(XmlElement.DEFAULT_ELEMENT_VALUE))
            {
               char[] fieldChars = field.toCharArray();
               fieldChars[0] = Character.toUpperCase(fieldChars[0]);
               
               String methodName = new StringBuilder("set").append(
            		   new String(fieldChars)).toString();
                
               this.tagged = this.getMethod(methodName);
            }
        }
        if(atts != null)
        {            
            for(XmlAttribute annotation : this.buildAttributeAnnotationsSet())
            {
            	String name = annotation.attribute();
            	String field = annotation.field();
            	String value = null;
             
                if(name.equals(XmlAttribute.DEFAULT_ATTRIBUTE_VALUE)
                        || field.equals(XmlAttribute.DEFAULT_ATTRIBUTE_VALUE)|| 
                        (value = atts.getValue(name)) == null || value.length() == 0)
                { 
                    continue;
                }
                char[] fieldChars = field.toCharArray();
                fieldChars[0] = Character.toUpperCase(fieldChars[0]);
                
                String methodName = new StringBuilder("set").append(
                        new String(fieldChars)).toString();
                
                Method method = this.getMethod(methodName);
                
                if(method != null)
                {
                   this.invoke(method, value);   
                } 
            }
        }
    }
    
    /**
     * Sets the value of the field mapped to the xml tag name 
     * passed as parameter
     *   
     * @param tag
     *      the name of the xml tag
     * @param value
     *      the string value to set
     */
    public void mapTag(String value)
    {
        this.invoke(tagged,value);
    }
    
    /**
     * Invoke the {@link Method} object passed as parameter
     * using the String value argument to parameterize
     * the call 
     * 
     * @param method
     *      the method to invoke
     * @param value
     *      the string value used to parameterize
     *      the invocation
     */
    private void invoke(Method method, String value)
    {
        if(method != null)
        {        
            method.setAccessible(true);
            try
              {
                method.invoke(this, value);
                
              } catch (Exception e)
              {
                if(this.mediator.isErrorLoggable())
                {
                    this.mediator.error(e, e.getMessage());
                }
              } 
        }
    }    

    /**
     * Builds and returns an array of all declared XmlAttribute 
     * annotations wrapped in XmlAttributes ones for this 
     * XmlDefinition and its super classes until the Object 
     * one
     * 
     * @return
     *      an array of all declared XmlAttribute annotations 
     *      wrapped in XmlAttributes ones for this XmlDefinition 
     *      and its super classes until the Object one
     */
    private XmlAttribute[] buildAttributeAnnotationsSet()
    {
        List<XmlAttribute> tags = new ArrayList<XmlAttribute>();
        Class<?> currentClass = this.getClass();
        
        while(currentClass != null)
        {
            XmlAttributes attributes = currentClass.getAnnotation(
                    XmlAttributes.class);
            
            if(attributes != null)
            {
                for(XmlAttribute attribute : attributes.value())
                {
                    tags.add(attribute);
                }
            }
            currentClass = currentClass.getSuperclass(); 
        }
        return tags.toArray(new XmlAttribute[tags.size()]);
    }
    
    /**
     * @param methodName
     * @param parameterTypes
     * @return
     */
    private Method getMethod(String methodName)
    {
        Class<?> currentClass = this.getClass();
        Method method = null;
        
        while(XmlDefinition.class.isAssignableFrom(currentClass))
        {
            try
            {
                method = currentClass.getDeclaredMethod(methodName,String.class);
                break;
            
            } catch(Exception e)
            {
                currentClass = currentClass.getSuperclass(); 
            }
        }
        return method;
    }
}
