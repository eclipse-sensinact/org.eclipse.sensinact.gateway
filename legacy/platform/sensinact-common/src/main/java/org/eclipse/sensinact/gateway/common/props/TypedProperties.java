/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.common.props;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.JSONUtils;

/**
 * A typed set of properties
 *
 * @param <T> extended Enum type
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class TypedProperties<T extends Enum<T> & KeysCollection> implements JSONable {
	
	private static class TypedKeyValue implements Entry<TypedKey<?>, Object>{
		private final TypedKey<?> key;
		private final Object value;
		
		public TypedKeyValue(TypedKey<?> key, Object value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public TypedKey<?> getKey() {
			return key;
		}

		@Override
		public Object getValue() {
			return value;
		}

		@Override
		public Object setValue(Object value) {
			throw new UnsupportedOperationException("Changing the value via the iterator is not supported");
		}
	}
	
    protected static final String TYPE = "type";

    private final Map<String,Entry<TypedKey<?>, Object>> properties;

    private final T type;

    /**
     * Constructor
     *
     * @param type      the type of this event
     */
    public TypedProperties(T type) {
        this.properties = new HashMap<>();
        this.type = type;
        putValue(TYPE, type);
    }

    /**
     * @inheritDoc
     * @see JSONable#getJSON()
     */
    @Override
    public String getJSON() {
        StringBuilder builder = new StringBuilder();
        Iterator<Entry<TypedKey<?>, Object>> iterator = this.properties.values().iterator();

        builder.append(JSONUtils.OPEN_BRACE);
        int index = 0;

        while (iterator.hasNext()) {
            Map.Entry<TypedKey<?>, Object> entry = iterator.next();
            TypedKey<?> typedKey = entry.getKey();

            if (typedKey.isHidden()) {
                continue;
            }
            builder.append(index > 0 ? JSONUtils.COMMA : "");
            builder.append(JSONUtils.QUOTE);
            builder.append(typedKey.getName());
            builder.append(JSONUtils.QUOTE);
            builder.append(JSONUtils.COLON);
            builder.append(JSONUtils.toJSONFormat(entry.getValue()));
            index++;
        }
        builder.append(JSONUtils.CLOSE_BRACE);
        return builder.toString();
    }

    /**
     * Associates the key - value pair passed as parameter to this
     * PrimitiveEvent. For the predefined keys, the value is set
     * only if it has not been already defined
     *
     * @param key   the key of the property to set
     * @param value the value to set
     */
    protected void putValue(TypedKey<?> key, Object value) {
        this.properties.put(key.getName(), new TypedKeyValue(key, value));
    }

    /**
     * Associates the key - value pair passed as parameter to this
     * PrimitiveEvent. For the predefined keys, the value is set
     * only if it has not been already defined
     *
     * @param key   the key of the property to set
     * @param value the value to set
     */
    protected void putValue(String key, Object value) {
        this.putValue(key, value, false);
    }

    /**
     * Associates the key - value pair passed as parameter to this
     * PrimitiveEvent. For the predefined keys, the value is set
     * only if it has not been already defined
     *
     * @param key    the key of the property to set
     * @param value  the value to set
     * @param hidden the hidden status of the TypedKey to be
     *               created if relevant
     */
    protected void putValue(String key, Object value, boolean hidden) {
        TypedKey<?> typedKey = this.getType().key(key);
        if (typedKey == null) {
            typedKey = new TypedKey<Object>(key, Object.class, hidden);
        }
        this.putValue(typedKey, value);
    }

    /**
     * Associates the key - value pair passed as parameter to this
     * PrimitiveEvent. For the predefined keys, the value is set
     * only if it has not been already defined
     *
     * @param key   the key of the property to set
     * @param value the value to set
     */
    public void put(String key, Object value) {
        this.put(key, value, false);
    }

    /**
     * Associates the key - value pair passed as parameter to this
     * PrimitiveEvent. For the predefined keys, the value is set
     * only if it has not been already defined
     *
     * @param key    the key of the property to set
     * @param value  the value to set
     * @param hidden the hidden status of the TypedKey to be
     *               created if relevant
     */
    public void put(String key, Object value, boolean hidden) {
        if (notPresentInType(key)) {
            this.putValue(key, value, hidden);
        }
    }

	private boolean notPresentInType(String key) {
		return this.getType().keys().stream().noneMatch(k -> k.getName().equals(key));
	}

    /**
     * Removes from the key (and its associated value) from
     * this TypedProperties
     *
     * @param key the key of the property to remove
     */
    public Object remove(String key) {
    	Object value = null;
        if (notPresentInType(key)) {
        	Entry<TypedKey<?>, Object> e = this.properties.remove(key);
            value = e == null ? null : e.getValue(); 
        }
        return value;
    }

    /**
     * Returns the value object of the property whose
     * name is passed as parameter
     *
     * @return the value object for the specified
     * property
     */
    @SuppressWarnings("unchecked")
	public <V> V get(String key) {

    	Entry<TypedKey<?>, Object> e = this.properties.get(key);
    	
        if (e == null) {
        	return null;
        }
        
        Class<?> type = e.getKey().getType();
        Object value = e.getValue();
        
        if (!type.isInstance(value)) {
        	return (V) CastUtils.cast(type, value);
        }
        return (V) e.getValue();
    }

    /**
     * Returns this TypedProperties type
     *
     * @return this TypedProperties type
     */
    public T getType() {
        return type;
    }
    
    protected Iterator<Map.Entry<TypedKey<?>, Object>> typedKeyValues() {
    	return ((Map<String, Entry<TypedKey<?>, Object>>)properties).values().iterator();
    }
}
