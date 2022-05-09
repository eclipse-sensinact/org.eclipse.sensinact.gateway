/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.common.primitive;

import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Description of a {@link Primitive}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class PrimitiveDescription implements Description, Nameable {
    /**
     * Indent factor used to display JSON formated description string
     */
    protected static final int INDENT_FACTOR = 3;
    /**
     * URI JSON Key
     */
    protected final static String URI_KEY = "uri";
    /**
     * Name JSON Key
     */
    public final static String NAME_KEY = "name";
    /**
     * MethodType JSON Key
     */
    public final static String TYPE_KEY = "type";
    /**
     * Value JSON Key
     */
    public final static String VALUE_KEY = "value";
    /**
     * Timestamp JSON Key
     */
    protected final String TIMESTAMP_KEY = "timestamp";
    /**
     * the name of the described {@link Primitive}
     */
    protected final String name;
    /**
     * the type of the described {@link Primitive}
     */
    protected final Class<?> type;
    /**
     * a copy of the described {@link Primitive}'s current value
     */
    protected Object value;
    /**
     * the described {@link Primitive}'s modifiable policy
     */
    protected final Modifiable modifiable;

    /**
     * Constructor
     *
     * @param uri        the string uri of the described {@link Primitive}
     * @param name       the name of the described {@link Primitive}
     * @param type       the type of the described {@link Primitive}
     * @param modifiable Is the value of the described {@link Primitive} modifiable ?
     */
    protected PrimitiveDescription(String name, Class<?> type, Modifiable modifiable) {
        this.name = name;
        this.type = type;
        this.modifiable = modifiable;
    }

    /**
     * @param primitive the {@link Primitive} to describe
     */
    protected <P extends Primitive> PrimitiveDescription(P primitive) {
        this.name = primitive.getName();
        this.type = primitive.getType();
        this.modifiable = primitive.getModifiable();
        this.value = primitive.getValue();
    }

    /**
     * @inheritDoc
     * @see JSONable#getJSON()
     */
    @Override
    public String getJSON() {
        JSONObject description = getJSONObject();
        description.put(VALUE_KEY, toJson(this.getType(), this.getValue()));
        return description.toString(INDENT_FACTOR);
    }

    /**
     * Returns the JSONObject from which are based
     * the JSON formated string descriptions of this
     * PrimitiveDescription
     *
     * @return the basis JSONObject describing this
     * PrimitiveDescription
     */
    protected final JSONObject getJSONObject() {
        JSONObject description = new JSONObject();
        description.put(NAME_KEY, name);
        String typeName = CastUtils.writeClass(this.getType());

        description.put(TYPE_KEY, typeName);
        return description;
    }

    /**
     * Return the name of the described
     * {@link Primitive}
     *
     * @return the name of the described
     * {@link Primitive}
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the type of the described
     * {@link Primitive}
     *
     * @return the type of the described
     * {@link Primitive}
     */
    public Class<?> getType() {
        return this.type;
    }

    /**
     * Returns a the value of the
     * described {@link Primitive}
     *
     * @return the value of the described
     * {@link Primitive}
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Returns the {@link Modifiable} policy defining
     * the modality which applies to changes targeting
     * the described {@link Primitive}
     *
     * @return the described Primitive's {@link Modifiable}
     * policy
     */
    public Modifiable getModifiable() {
        return this.modifiable;
    }

    /**
     * This method is called whenever the described
     * {@link Describable} object's value is changed.
     * Update this description's value
     */
    protected void update(Object object) {
        this.value = object;
    }

    /**
     * Converts the specified object value
     * whose type is passed as parameter into
     * a JSON compatible formated one
     *
     * @param type  the type of the value object to cast
     * @param value the object value to convert into a
     *              JSON formated one
     * @return the JSON formated object value
     */
    public static Object toJson(Class<?> type, Object value) {
        if (value == null) {
            return JSONObject.NULL;
        }
        if (String.class == type || type.isPrimitive() || JSONObject.class.isAssignableFrom(type) || JSONArray.class.isAssignableFrom(type)) {
            return value;

        } else if (type.isEnum()) {
            return ((Enum) value).name();

        } else {
            return JSONUtils.toJSONFormat(value);
        }
    }
}
