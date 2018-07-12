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
package org.eclipse.sensinact.gateway.nthbnd.jsonpath.json;

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.spi.json.AbstractJsonProvider;
import org.eclipse.sensinact.gateway.nthbnd.jsonpath.JsonOrgJson;
import org.eclipse.sensinact.gateway.nthbnd.jsonpath.JsonOrgJsonArray;
import org.eclipse.sensinact.gateway.nthbnd.jsonpath.JsonOrgJsonObject;
import org.eclipse.sensinact.gateway.nthbnd.jsonpath.JsonOrgJsonTokener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class JsonOrgJsonProvider extends AbstractJsonProvider {

    @Override
    public Object parse(String json, boolean strict) throws InvalidJsonException {
        try {
            Object o = new JsonOrgJsonTokener(json).nextValue();
            if (strict && !(o instanceof JsonOrgJson)) {
                throw new InvalidJsonException(json);
            }
            return o;
        } catch (JSONException e) {
            throw new InvalidJsonException(e);
        }
    }

    @Override
    public Object parse(InputStream jsonStream, String charset, boolean strict) throws InvalidJsonException {
        try {
            Object o = new JsonOrgJsonTokener(new InputStreamReader(jsonStream, charset)).nextValue();
            if (strict && !(o instanceof JsonOrgJson)) {
                throw new InvalidJsonException(o.toString());
            }
            return o;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new InvalidJsonException(e);
        } catch (UnsupportedEncodingException e) {
            throw new JsonPathException(e);
        }
    }

    @Override
    public Object unwrap(Object obj) {
        if (JSONObject.NULL.equals(obj)) {
            return null;
        }
        return obj;
    }

    @Override
    public String toJson(Object obj) {
        Class clazz = obj.getClass();
        if (JSONObject.class.isAssignableFrom(clazz) || JSONArray.class.isAssignableFrom(clazz)) {
            return obj.toString();
        }
        if (obj instanceof Map) {
            return new JsonOrgJsonObject((Map) obj).toString();
        } else if (obj instanceof List) {
            return new JsonOrgJsonArray((List) obj).toString();
        } else {
            throw new UnsupportedOperationException(obj.getClass().getName() + " can not be converted to JSON");
        }
    }

    @Override
    public Object createArray() {
        return new JsonOrgJsonArray();
    }

    @Override
    public Object createMap() {
        return new JsonOrgJsonObject();
    }

    @Override
    public boolean isArray(Object obj) {
        return (obj instanceof List);
    }

    @Override
    public Object getArrayIndex(Object obj, int idx) {
        Class clazz = obj.getClass();
        if (JSONArray.class.isAssignableFrom(clazz)) {
            try {
                Object o = ((JSONArray) obj).get(idx);
                return unwrap(o);

            } catch (JSONException e) {
                return UNDEFINED;
            }
        } else if (List.class.isAssignableFrom(clazz)) {
            if (idx >= ((List) obj).size()) {
                return UNDEFINED;
            }
            Object o = ((List) obj).get(idx);
            return unwrap(o);
        }
        return UNDEFINED;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setArrayIndex(Object array, int index, Object newValue) {
        if (!isArray(array)) {
            throw new UnsupportedOperationException();
        } else {
            if (index < 0) {
                throw new IllegalArgumentException("JSONArray[" + index + "] not found.");
            }
            List l = toJsonArray(array);
            if (index < l.size()) {
                l.set(index, createJsonElement(newValue));
            } else {
                while (index != l.size()) {
                    l.add(JSONObject.NULL);
                    index++;
                }
                l.add(createJsonElement(newValue));
            }
        }
    }

    @Override
    public Object getMapValue(Object obj, String key) {
        Class clazz = obj.getClass();
        if (JSONObject.class.isAssignableFrom(clazz)) {
            try {
                Object o = ((JSONObject) obj).get(key);
                return unwrap(o);

            } catch (JSONException e) {
                return UNDEFINED;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            if (!((Map) obj).containsKey(key)) {
                return UNDEFINED;
            }
            Object o = ((Map) obj).get(key);
            return unwrap(o);
        }
        return UNDEFINED;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setProperty(Object obj, Object key, Object value) {
        if (isMap(obj)) toJsonObject(obj).put(key.toString(), createJsonElement(value));
        else {
            List array = toJsonArray(obj);
            int index;
            if (key != null) {
                index = key instanceof Integer ? (Integer) key : Integer.parseInt(key.toString());
            } else {
                index = array.size();
            }
            if (index == array.size()) {
                array.add(createJsonElement(value));
            } else {
                array.set(index, createJsonElement(value));
            }
        }
    }

    public void removeProperty(Object obj, Object key) {
        if (isMap(obj)) toJsonObject(obj).remove(key.toString());
        else {
            List array = toJsonArray(obj);
            int index = key instanceof Integer ? (Integer) key : Integer.parseInt(key.toString());
            array.remove(index);
        }
    }

    @Override
    public boolean isMap(Object obj) {
        return (obj instanceof Map);
    }

    @Override
    public Collection<String> getPropertyKeys(Object obj) {
        List<String> keys = new ArrayList<String>();
        Iterator<?> arr = toJsonObject(obj).keySet().iterator();
        for (; arr.hasNext(); ) {
            keys.add((String) arr.next());
        }
        return keys;
    }

    @Override
    public int length(Object obj) {
        if (isArray(obj)) {
            return toJsonArray(obj).size();
        } else if (isMap(obj)) {
            return toJsonObject(obj).size();
        } else {
            if (obj instanceof String) {
                return ((String) obj).length();
            }
        }
        throw new JsonPathException("length operation can not applied to " + obj != null ? obj.getClass().getName() : "null");
    }

    @Override
    public Iterable<?> toIterable(Object obj) {
        List<Object> values = new ArrayList<Object>();
        if (isArray(obj)) {
            Iterator<?> arr = toJsonArray(obj).iterator();
            for (; arr.hasNext(); ) {
                values.add(unwrap(arr.next()));
            }
            return values;
        } else {
            Iterator arr = toJsonObject(obj).values().iterator();
            for (; arr.hasNext(); ) {
                values.add(unwrap(arr.next()));
            }
            return values;
        }
    }

    private Object createJsonElement(Object o) {
        return o;
    }

    private List toJsonArray(Object o) {
        return (List) o;
    }

    private Map toJsonObject(Object o) {
        return (Map) o;
    }
}
