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
package org.eclipse.sensinact.gateway.device.mosquitto.lite.interpolator;

import org.eclipse.sensinact.gateway.common.annotation.Property;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.interpolator.exception.IncompleteDataException;

import java.lang.reflect.Field;
import java.util.Dictionary;

/**
 * Creates instance of manage service pojo object interpolated with a dictionary of properties
 * @param <T>
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class MosquittoManagedServiceIterpolate<T> {

    private final Dictionary propertyValues;
    private final Class<T> clazz;

    public MosquittoManagedServiceIterpolate(Class<T> clazz,Dictionary propertyValues){
        this.clazz=clazz;
        this.propertyValues=propertyValues;
    }

    public Object getInstance() throws IncompleteDataException {

        try {
            Object object=clazz.newInstance();

            for(Field field:object.getClass().getDeclaredFields()){
                field.setAccessible(true);
                Property annotation=field.getAnnotation(Property.class);
                if(annotation!=null){

                    String propertyName;

                    if(annotation.name().trim().equals("")){
                        propertyName=field.getName();
                    }else {
                        propertyName=annotation.name();
                    }

                    String typeClass=field.getType().getCanonicalName();

                    Object value=null;

                    if(!annotation.defaultValue().trim().equals("")){
                        value=annotation.defaultValue();
                    }

                    if(propertyValues.get(propertyName)!=null){
                            value=propertyValues.get(propertyName);
                    }

                    if(annotation.mandatory() && value==null){
                        throw new IncompleteDataException(String.format("Mandatory field \"%s\" is missing in property table",field.getName()));
                    }

                    if(value!=null){
                        if (typeClass.equals("java.lang.String")){
                            field.set(object,value);
                        }else if(typeClass.equals("java.lang.Integer")){
                            field.set(object,Integer.parseInt(value.toString()));
                        }else if(typeClass.equals("java.lang.Long")){
                            field.set(object,Long.parseLong(value.toString()));
                        }else if(typeClass.equals("java.lang.Float")){
                            field.set(object, Float.parseFloat(value.toString()));
                        }else if(typeClass.equals("java.lang.Boolean")){
                            field.set(object, Boolean.parseBoolean(value.toString()));
                        }
                    }

                }

            }

            return object;

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;

    }
}
