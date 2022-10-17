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
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

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
        JsonObjectBuilder description = getJsonObject();
        description.add(VALUE_KEY, this.getJsonValue());
        return description.build().toString();
    }

    protected JsonValue getJsonValue() {
    	
    	JsonValue jv = CastUtils.cast(JsonValue.class, this.getValue());
    	return jv == null ? JsonValue.NULL : jv;
    }

    protected JsonValue getJsonValue(PrimitiveDescription pd) {
    	return pd.getJsonValue();
    }
    
    /**
     * Returns the JsonObject from which are based
     * the JSON formated string descriptions of this
     * PrimitiveDescription
     *
     * @return the basis JsonObject describing this
     * PrimitiveDescription
     */
    protected final JsonObjectBuilder getJsonObject() {
        JsonObjectBuilder description = JsonProviderFactory.getProvider().createObjectBuilder();
        description.add(NAME_KEY, name);
        String typeName = CastUtils.writeClass(this.getType());

        description.add(TYPE_KEY, typeName);
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
            return JsonObject.NULL;
        }
        if (String.class == type || type.isPrimitive() || JsonObject.class.isAssignableFrom(type) || JsonArray.class.isAssignableFrom(type)) {
            return value;

        } else if (type.isEnum()) {
            return ((Enum) value).name();

        } else {
            return JSONUtils.toJSONFormat(value);
        }
    }
}
