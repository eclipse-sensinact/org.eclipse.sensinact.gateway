/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.common.interpolator;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.eclipse.sensinact.gateway.common.annotation.Property;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.interpolator.exception.IncompleteDataException;
import org.eclipse.sensinact.gateway.common.interpolator.exception.InterpolationException;
import org.eclipse.sensinact.gateway.common.interpolator.exception.InvalidValueException;
import org.eclipse.sensinact.gateway.common.interpolator.exception.ObjectInstantiationException;

/**
 * Creates instance (or re-use an already existant instance) annotated with {@link Property} and interpolates with a dictionary of properties
 *
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class Interpolator {
    private static final Logger LOG = Logger.getLogger(Interpolator.class.getName());
    
    private final Map<String,String> propertiesInjected=new HashMap<>();
	private PropertyBucket propertyBucket;

    public Interpolator(Mediator mediator) {
    	this.propertyBucket = new MediatorPropertyBucket(mediator);
    }

    public Interpolator(Map<?, ?> map) {
    	this.propertyBucket = new MapPropertyBucket(map);
    }    

    public Interpolator(Dictionary<String, ?> dictionary) {
    	this.propertyBucket = new DictionaryPropertyBucket(dictionary);
    }

    
    public <E> E getInstance(E object) throws InterpolationException {
        try {
            for (Field field : object.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Property annotation = field.getAnnotation(Property.class);
                if (annotation != null) {
                    String propertyName;
                    if (annotation.name().trim().equals("")) {
                        propertyName = field.getName();
                    } else {
                        propertyName = annotation.name();
                    }
                    Object value = propertyBucket.get(propertyName);
                    
                    if (!annotation.defaultValue().trim().equals("") && value==null) {
                        value = annotation.defaultValue();
                    }
                    if (annotation.mandatory() && value == null) {
                        throw new IncompleteDataException(String.format("mandatory field \"%s\" is missing in property table", field.getName()));
                    }
                    if (!annotation.validationRegex().trim().equals("") && value != null) {
                        Pattern patternValidation = Pattern.compile(annotation.validationRegex());
                        if (!patternValidation.matcher(value.toString()).matches()) {
                            LOG.log(Level.SEVERE, String.format("field value %s, on field {}, is invalid with respect to the regex %s required", value.toString(), field.getName(), annotation.validationRegex()));
                            throw new InvalidValueException(String.format("field value \"%s\", on field \"%s\", is invalid with respect to the regex \"%s\" required", value.toString(), field.getName(), annotation.validationRegex()));
                        }
                    }
                    if (value != null) {
                        try {
                            propertiesInjected.put(propertyName,value.toString());
                            String typeClass = field.getType().getCanonicalName();
                            if (typeClass.equals("java.lang.String")) {
                                field.set(object, value);
                            } else if (typeClass.equals("java.lang.Integer")) {
                                field.set(object, Integer.parseInt(value.toString()));
                            } else if (typeClass.equals("java.lang.Long")) {
                                field.set(object, Long.parseLong(value.toString()));
                            } else if (typeClass.equals("java.lang.Float")) {
                                field.set(object, Float.parseFloat(value.toString()));
                            } else if (typeClass.equals("java.lang.Boolean")) {
                                field.set(object, Boolean.parseBoolean(value.toString()));
                            }
                        } catch (Exception e) {
                            LOG.log(Level.SEVERE, String.format("value %s could not be inject on field %s of the type %s in POJO %s", value.toString(), field.getName(), field.getType().getCanonicalName(), object.getClass().toString()), e);
                            throw new InvalidValueException(String.format("value \"%s\" could not be inject on field \"%s\" of the type \"%s\" in POJO \"%s\"", value.toString(), field.getName(), field.getType().getCanonicalName(), object.getClass().toString()));
                        }
                    }
                }
            }
            return object;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create instance of object to receive interpolation", e);
            throw new InterpolationException(e);
        }
    }

    public <T> T getNewInstance(Class<T> clazz) throws InterpolationException {
        try {
            T object = clazz.newInstance();
            return getInstance(object);
        } catch (InstantiationException e) {
            LOG.log(Level.SEVERE, "Failed to create instance of object to receive interpolation", e);
            throw new ObjectInstantiationException(e);
        } catch (IllegalAccessException e) {
            LOG.log(Level.SEVERE, "Failed to create instance of object to receive interpolation", e);
            throw new ObjectInstantiationException(e);
        }
    }
    

    public Map<String, String> getPropertiesInjected() {
        return Collections.unmodifiableMap(propertiesInjected);
    }
    
    private interface PropertyBucket {
    	
    	Object get(String name);
    }
    
    private class MediatorPropertyBucket implements PropertyBucket {
    	
    	private final Mediator mediator;
    	
    	MediatorPropertyBucket(Mediator mediator) {
    		this.mediator = mediator;
    	}

		@Override
		public Object get(String property) {
			return this.mediator.getProperty(property);
		}
    }

    private class MapPropertyBucket implements PropertyBucket {
    	
    	private final Map<?, ?> map;
    	
    	MapPropertyBucket(Map<?, ?> map) {
    		this.map = map;
    	}

		@Override
		public Object get(String property) {
			return this.map.get(property);
		}
    }

    private class DictionaryPropertyBucket implements PropertyBucket {
    	
    	private final Dictionary<String, ?> dictionary;
    	
    	DictionaryPropertyBucket(Dictionary<String, ?> dictionary) {
    		this.dictionary = dictionary;
    	}

		@Override
		public Object get(String property) {
			return this.dictionary.get(property);
		}
    }
}
