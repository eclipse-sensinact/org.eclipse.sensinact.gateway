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
package org.eclipse.sensinact.gateway.common.primitive;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A Primitive is a data structure mapping a name to a value whose type is
 * specified
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class Primitive implements Nameable {
    /**
     * Returns the {@link Modifiable} policy defining the modality
     * which applies to changes targeting this Primitive
     *
     * @return this Primitive's {@link Modifiable} policy
     */
    public abstract Modifiable getModifiable();

    /**
     * Called before the value of this Primitive has
     * changed, but after the validation of its type -
     * this method is called even if the value is equals
     * to the current one
     *
     * @param value the value being set to this Primitive
     */
    protected abstract void beforeChange(Object value) throws InvalidValueException;

    /**
     * Called when the value of this Primitive has
     * changed - this method is not called if the set
     * value is equal to the previous one
     *
     * @param value the updated value of the primitive
     * @throws InvalidValueException
     */
    protected abstract void afterChange(Object value) throws InvalidValueException;

    /**
     * for synchronization purpose when accessing the value object
     */
    protected final Object lock = new Object();
    /**
     * the type of this Primitive's value
     */
    protected final Class type;

    /**
     * the name of this {@link Primitive}
     */
    protected final String name;
    /**
     * the value of this Primitive
     */
    protected Object value;
    protected Mediator mediator;

    /**
     * Constructor
     *
     * @param name the name of the Primitive to instantiate
     * @param type the type of the Primitive to instantiate
     */
    protected Primitive(Mediator mediator, String name, Class type) throws InvalidValueException {
        checkType(type);
        this.mediator = mediator;
        this.name = name;
        this.type = type;
    }

    /**
     * Constructor
     *
     * @param name the name of the Primitive to instantiate
     * @param type the canonical name of the type of the
     *             Primitive to instantiate
     */
    protected Primitive(Mediator mediator, String name, String type) throws InvalidValueException {
        if (name == null || type == null) {
            throw new InvalidValueException("name and type are required");
        }
        this.mediator = mediator;
        Class<?> clazz = null;
        try {
            clazz = CastUtils.loadClass(mediator.getClassLoader(), type);

        } catch (ClassNotFoundException e) {
            throw new InvalidValueException(new StringBuilder().append("Invalid type :").append(type).toString());
        }
        checkType(clazz);
        this.name = name;
        this.type = clazz;
    }

    /**
     * Constructor
     *
     * @param name  the name of the Primitive to instantiate
     * @param type  the type of the Primi  n  tive to instantiate
     * @param value the value of the Primitive to instantiate
     * @throws InvalidConstraintDefinitionException
     */
    protected Primitive(Mediator mediator, String name, Class type, Object value) throws InvalidValueException {
        this(mediator, name, type);
        if (value != null) {
            this.setValue(value);
        }
    }

    /**
     * Constructor
     *
     * @param jsonObject the JSONObject describing the Primitive
     *                   to instantiate
     */
    protected Primitive(Mediator mediator, JSONObject jsonObject) throws InvalidValueException {
        this(mediator, jsonObject == null ? null : jsonObject.optString(PrimitiveDescription.NAME_KEY), jsonObject == null ? null : jsonObject.optString(PrimitiveDescription.TYPE_KEY));

        // set the value if defined in the JSONObject
        Object ovalue = jsonObject.opt(PrimitiveDescription.VALUE_KEY);
        if (ovalue != null) {
            this.setValue(CastUtils.getObjectFromJSON(this.mediator.getClassLoader(), this.getType(), ovalue));
        }
    }

    /**
     * @inheritDoc
     * @see Nameable#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the type of this Primitive or the type
     * of the linked one
     *
     * @return the type of this Primitive or the type
     * of the linked one
     */
    public Class<?> getType() {
        return this.type;
    }

    /**
     * Returns the copy of the value of this Primitive or
     * a copy of the value of the linked one
     *
     * @return the value of this Primitive or a copy of
     * the value of the linked one
     */
    public Object getValue() {
        Object value = null;
        synchronized (lock) {
            value = CastUtils.copy(this.type, this.value);
        }
        return value;
    }

    /**
     * Sets this Primitive's value
     *
     * @param value the value to set
     * @throws InvalidValueException if the value cannot be set
     */
    public Object setValue(Object value) throws InvalidValueException {
        if (Modifiable.FIXED.equals(this.getModifiable())) {
            throw new InvalidValueException("the value cannot be modified");
        }
        Object valueObject = cast(value);
        Object copy = CastUtils.copy(this.type, valueObject);

        synchronized (lock) {
            boolean hasChanged = ((valueObject == null && this.value != null) || (valueObject != null && !valueObject.equals(this.value)));

            this.beforeChange(copy);
            this.value = valueObject;
            if (hasChanged) {
                this.afterChange(copy);
            }
        }
        return copy;
    }

    /**
     * Validates the type of this Primitive
     *
     * @param type the type to validate
     */
    protected void checkType(Class<?> type) throws InvalidValueTypeException {
        if (!CastUtils.isPrimitive(type) && !JSONObject.class.isAssignableFrom(type) && !JSONArray.class.isAssignableFrom(type) && !type.isEnum() && (!type.isArray() || !CastUtils.isPrimitive(type.getComponentType()))) {
            throw new InvalidValueTypeException("Invalid type : " + type.getCanonicalName());
        }
    }

    /**
     * Casts the value object passed as parameter into the type
     * of this Primitive and returns the resulting casted object
     *
     * @param value the value object to cast
     * @return the casted object value
     * @throws InvalidValueException if the value object cannot be cast into the type
     *                               of this Primitive
     */
    protected Object cast(Object value) throws InvalidValueException {
        Object valueObject = null;
        try {
            valueObject = CastUtils.cast(this.mediator.getClassLoader(), this.getType(), value);

        } catch (ClassCastException e) {
            throw new InvalidValueException(e);
        }
        return valueObject;
    }
}
