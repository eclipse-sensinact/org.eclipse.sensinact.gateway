/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.util.json.JSONValidator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON Helpers
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class JSONUtils {
    protected static final Logger LOGGER = LoggerFactory.getLogger(JSONUtils.class);

    public static final int DEFAULT_MAX_DEEP = 3;

    /**
     * Returns the JSON formated string representation
     * of the {@link Object} passed as parameter, according
     * to the DEFAULT_MAX_DEEP constant value possible
     * iterations through the object hierarchy
     *
     * @param object the {@link Object} to describe as a JSON
     *               formated string
     * @return the JSON formated string representation
     */
    public static String toJSONFormat(Object object) {
        return JSONUtils.toJSONFormat(object, new JSONUtils.JSONWriterContext(DEFAULT_MAX_DEEP, object));
    }

    /**
     * Returns the JSON formated string representation
     * of the {@link Object} passed as parameter according
     * to the number passed as parameter of possible
     * iterations through the object hierarchy
     *
     * @param object the {@link Object} to describe as a JSON
     *               formated string
     * @param deep   number of possible iterations through the
     *               object hierarchy
     * @return the JSON formated string representation
     */
    public static String toJSONFormat(Object object, int deep) {
        return JSONUtils.toJSONFormat(object, new JSONUtils.JSONWriterContext(deep, object));
    }

    /**
     * Returns the JSON formated string representation
     * of the {@link Object} passed as parameter
     *
     * @param object  the {@link Object} to return a JSON
     *                formated string representation of
     * @param context state of the process
     * @return the JSON formated string representation
     */
    public static String toJSONFormat(Object object, JSONUtils.JSONWriterContext context) {
        if (object == null) {
            return JSON_NULL;
        }
        if (JSONObject.class.isAssignableFrom(object.getClass())) {
            return ((JSONObject) object).toString();
        }
        if (JSONArray.class.isAssignableFrom(object.getClass())) {
            return ((JSONArray) object).toString();
        }
        if (object.getClass().isArray()) {
            return JSONUtils.arrayToJSONFormat(object, context);
        }
        if (List.class.isAssignableFrom(object.getClass())) {
            return toJSONFormat(((List<?>) object).toArray(new Object[0]), context);
        }
        if (Map.class.isAssignableFrom(object.getClass())) {
            Iterator<Map.Entry<?,?>> iterator = ((Map) object).entrySet().iterator();
            int index = 0;
            StringBuilder builder = new StringBuilder();
            builder.append(JSONUtils.OPEN_BRACE);

            while (iterator.hasNext()) {
                Map.Entry<?, ?> entry = iterator.next();
                String key = toJSONFormat(entry.getKey());
                if (key == null) {
                    continue;
                }
                builder.append(index > 0 ? JSONUtils.COMMA : JSONUtils.EMPTY);
                if (!key.startsWith("\"")) {
                    builder.append(JSONUtils.QUOTE);
                    builder.append(key);
                    builder.append(JSONUtils.QUOTE);

                } else {
                    builder.append(key);
                }
                builder.append(JSONUtils.COLON);
                builder.append(toJSONFormat(entry.getValue()));
                index++;
            }
            builder.append(JSONUtils.CLOSE_BRACE);
            return builder.toString();
        }
        if (String.class == object.getClass() /*&& new JSONValidator((String)object).valid()*/) {
        	    try {
	        	 	if(new JSONValidator((String)object).valid()) {
	        	 		return (String)object;
	        	 	}
        	    }catch(Exception e) {
        	    	LOGGER.error(e.getMessage(),e);
        	    }
        }
        if (CastUtils.isPrimitive(object.getClass())) {
            return JSONUtils.primitiveToJSONFormat(object);

        } else if (object.getClass().isEnum()) {
            return JSONUtils.primitiveToJSONFormat(((Enum) object).name());
        } else {
            try {
                Method method = object.getClass().getDeclaredMethod("getJSON");

                if (method.getReturnType() == String.class) {
                    return (String) method.invoke(object);
                }
            } catch (Exception e) {
                LOGGER.debug("Object to cast is not a JSONable instance");
            }
        }
        if (context.tooDeep() || context.circularReference()) {
            return new StringBuilder().append(QUOTE).append(object.toString()).append(QUOTE).toString();
        }
        StringBuilder builder = new StringBuilder();
        builder.append(OPEN_BRACE);
        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.isAccessible()) {
                String fieldJSON = null;
                try {
                    context.decrementAndGet();
                    fieldJSON = toJSONFormat(field.get(object), context);

                } catch (Exception e) {
                    continue;
                }
                if (fieldJSON != null && fieldJSON.length() > 0) {
                    builder.append(QUOTE);
                    builder.append(field.getName());
                    builder.append(QUOTE);
                    builder.append(COLON);
                    builder.append(fieldJSON);
                }
            }
        }
        builder.append(CLOSE_BRACE);
        return builder.toString();
    }

    /**
     * Returns the JSON formated string representation
     * of the {@link Array} passed as parameter
     *
     * @param array   the {@link array} to represent as a JSON
     *                formated string
     * @param context the state of the process
     * @return the JSON formated string representation
     */
    private static String arrayToJSONFormat(Object array, JSONWriterContext context) {
        StringBuilder builder = new StringBuilder();
        builder.append(OPEN_BRACKET);

        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            context.decrementAndGet();
            String json = JSONUtils.toJSONFormat(Array.get(array, i), context);

            if (json != null && json.length() > 0) {
                builder.append(builder.length() > 1 ? COMMA : EMPTY);
                builder.append(json);
            }
        }
        builder.append(CLOSE_BRACKET);
        return builder.toString();
    }

    /**
     * Returns the JSON formated string representation of
     * the Primitive object passed as parameter
     *
     * @param primitive the Primitive object to return the JSON formated
     *                  string representation of
     * @return the JSON formated string representation of
     * the Primitive object passed as parameter
     */
    private static String primitiveToJSONFormat(Object primitive) {
        StringBuilder builder = new StringBuilder();
        Class<?> argClass = primitive.getClass();

        if (CastUtils.isPrimitive(argClass)) {
            if (String.class.equals(argClass)) {
                builder.append(QUOTE);
                builder.append(((String)primitive).replace("\"", "\\\""));
                builder.append(QUOTE);
            } else if (Character.class.equals(argClass) || char.class.equals(argClass)) {
                    builder.append(QUOTE);
                    builder.append(primitive);
                    builder.append(QUOTE);
           } else {
                builder.append(String.valueOf(primitive));
            }
        } else {
            return JSON_NULL;
        }
        return builder.toString();
    }

    /**
     * JSON writing process context
     * TODO : handle circular reference
     */
    private static final class JSONWriterContext {
        public final Object jsonable;
        private int deep = 0;

        JSONWriterContext(int deep, Object jsonable) {
            this.jsonable = jsonable;
            this.deep = deep;
        }

        public boolean circularReference() {
            //TODO : implement
            return false;
        }

        int decrementAndGet() {
            return --deep;
        }

        boolean tooDeep() {
            return deep < 0;
        }
    }

    public static final String EMPTY = "";

    //JSON-RPC GRAMMAR
    public static final String JSON_RPC_NAME = "jsonrpc";
    public static final String JSON_RPC_VERSION = "2.0";
    public static final String JSON_RPC_ID = "id";
    public static final String JSON_RPC_FUNCTION = "function";
    public static final String JSON_RPC_PROCEDURE = "procedure";
    public static final String JSON_RPC_METHOD = "method";
    public static final String JSON_RPC_PARAMS = "params";
    public static final String JSON_RPC_RESULT = "result";
    public static final String JSON_RPC_ERROR = "error";
    public static final String JSON_RPC_MESSAGE = "message";
    public static final String JSON_RPC_CODE = "code";

    //JSON KEY-WORDS
    public static final String JSON_TRUE = "true";
    public static final String JSON_FALSE = "false";
    public static final String JSON_NULL = "null";

    //JSON DELIMITERS
    public static final char OPEN_BRACE = '{';
    public static final char CLOSE_BRACE = '}';
    public static final char OPEN_BRACKET = '[';
    public static final char CLOSE_BRACKET = ']';
    public static final char COMMA = ',';
    public static final char QUOTE = '\"';
    public static final char APOSTROPHE = '\'';
    public static final char COLON = ':';
    public static final char SEMICOLON = ';';
}
