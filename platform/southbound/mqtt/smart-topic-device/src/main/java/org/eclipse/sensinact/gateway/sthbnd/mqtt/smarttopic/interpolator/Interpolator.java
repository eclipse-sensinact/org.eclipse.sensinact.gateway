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
package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.interpolator;

import org.eclipse.sensinact.gateway.common.annotation.Property;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.interpolator.exception.IncompleteDataException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.interpolator.exception.InterpolationException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.interpolator.exception.InvalidValueException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.interpolator.exception.ObjectInstantiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Dictionary;
import java.util.regex.Pattern;

/**
 * Creates instance (or re-use an already existant instance) annotated with {@link org.eclipse.sensinact.gateway.common.annotation.Property} and interpolates with a dictionary of properties
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class Interpolator {

    private static final Logger LOG = LoggerFactory.getLogger(Interpolator.class);
    private final Dictionary propertyValues;

    public Interpolator(Dictionary propertyValues) {
        this.propertyValues = propertyValues;
    }

    public <E> E getInstance(E object) throws InterpolationException{

        try {

            for(Field field : object.getClass().getDeclaredFields()) {
                field.setAccessible(true);

                Property annotation=field.getAnnotation(Property.class);

                if(annotation!=null) {
                    String propertyName;

                    if(annotation.name().trim().equals("")) {
                        propertyName = field.getName();
                    } else {
                        propertyName = annotation.name();
                    }

                    Object value=null;

                    if(!annotation.defaultValue().trim().equals("")) {
                        value=annotation.defaultValue();
                    }

                    if(propertyValues.get(propertyName)!=null) {
                        value = propertyValues.get(propertyName);
                    }

                    if(annotation.mandatory() && value == null) {
                        throw new IncompleteDataException(
                                String.format("mandatory field \"%s\" is missing in property table",field.getName()));
                    }

                    if(!annotation.validationRegex().trim().equals("") && value!=null) {
                        Pattern patternValidation=Pattern.compile(annotation.validationRegex());

                        if(!patternValidation.matcher(value.toString()).matches()) {
                            LOG.error("field value {}, on field {}, is invalid with respect to the regex {} required",
                                    value.toString(), field.getName(), annotation.validationRegex());

                            throw new InvalidValueException(
                                    String.format("field value \"%s\", on field \"%s\", is invalid with respect to the regex \"%s\" required",
                                            value.toString(),field.getName(),annotation.validationRegex()));
                        }
                    }

                    if(value != null) {
                        try {
                            String typeClass=field.getType().getCanonicalName();

                            if (typeClass.equals("java.lang.String")) {
                                field.set(object,value);
                            } else if(typeClass.equals("java.lang.Integer")) {
                                field.set(object,Integer.parseInt(value.toString()));
                            } else if(typeClass.equals("java.lang.Long")) {
                                field.set(object,Long.parseLong(value.toString()));
                            } else if(typeClass.equals("java.lang.Float")) {
                                field.set(object, Float.parseFloat(value.toString()));
                            } else if(typeClass.equals("java.lang.Boolean")) {
                                field.set(object, Boolean.parseBoolean(value.toString()));
                            }
                        } catch(Exception e) {
                            LOG.error("value {} could not be inject on field {} of the type {} in POJO {}",
                                    value.toString(),field.getName(),field.getType().getCanonicalName(), object.getClass().toString(), e);

                            throw new InvalidValueException(String.format("value \"%s\" could not be inject on field \"%s\" of the type \"%s\" in POJO \"%s\"",
                                    value.toString(), field.getName(), field.getType().getCanonicalName(), object.getClass().toString()));
                        }
                    }
                }
            }

            return object;

        } catch (Exception e) {
            LOG.error("Failed to create instance of object to receive interpolation", e);
            throw new InterpolationException(e);
        }

    }

    public <T>T getNewInstance(Class<T> clazz) throws InterpolationException {

        try {
            T object = clazz.newInstance();

            return getInstance(object);

        } catch (InstantiationException e) {
            LOG.error("Failed to create instance of object to receive interpolation", e);
            throw new ObjectInstantiationException(e);
        } catch (IllegalAccessException e) {
            LOG.error("Failed to create instance of object to receive interpolation", e);
            throw new ObjectInstantiationException(e);
        }
    }
}
