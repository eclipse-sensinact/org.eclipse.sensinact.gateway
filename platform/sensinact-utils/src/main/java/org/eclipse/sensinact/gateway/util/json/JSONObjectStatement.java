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
package org.eclipse.sensinact.gateway.util.json;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * An extended {@link JSONObject} {@link JSONStatement}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class JSONObjectStatement extends JSONObject implements JSONStatement {
    private Map<String, JSONVariable> variables;

    /**
     * Constructor
     *
     * @param json JSON formated string to parse to instantiate
     *             the JSONVariable
     */
    public JSONObjectStatement(String json) {
        this(new JSONTokenerStatement(json));
    }

    /**
     * Constructor
     *
     * @param tokener {@link JSONTokener} used to read the JSON
     *                formated request
     */
    public JSONObjectStatement(JSONTokenerStatement tokener) {
        super();
        this.variables = new HashMap<String, JSONVariable>();

        char c;
        String key;
        if (tokener.nextClean() != '{') {
            throw tokener.syntaxError("A JSONObject text must begin with '{'");
        }
        for (; ; ) {
            c = tokener.nextClean();
            switch (c) {
                case 0:
                    throw tokener.syntaxError("A JSONObject text must end with '}'");
                case '}':
                    return;
                default:
                    tokener.back();
                    key = tokener.nextValue().toString();
            }
            // The key is followed by ':'.
            c = tokener.nextClean();
            if (c != ':') {
                throw tokener.syntaxError("Expected a ':' after a key");
            }
            this.putOnce(key, tokener.nextValue());
            // Pairs are separated by ','.
            switch (tokener.nextClean()) {
                case ';':
                case ',':
                    if (tokener.nextClean() == '}') {
                        return;
                    }
                    tokener.back();
                    break;
                case '}':
                    return;
                default:
                    throw tokener.syntaxError("Expected a ',' or '}'");
            }
        }
    }

    /**
     * @inheritDoc
     * @see org.json.JSONObject#put(java.lang.String, java.lang.Object)
     */
    @Override
    public JSONObject put(String key, Object value) throws JSONException {
        if (key == null) {
            throw new NullPointerException("Null key.");
        }
        if (value != null) {
            //testValidity(value);
            super.put(key, value);

            if (JSONVariable.class.isAssignableFrom(value.getClass())) {
                JSONVariable jsonVariable = (JSONVariable) value;
                this.variables.put(jsonVariable.getName(), jsonVariable);
            }
        } else {
            this.remove(key);
        }
        return this;
    }

    /**
     * @inheritDoc
     * @see org.json.JSONObject#remove(java.lang.String)
     */
    public Object remove(String key) {
        Object value = super.remove(key);
        if (value != null && JSONVariable.class.isAssignableFrom(value.getClass())) {
            JSONVariable jsonVariable = (JSONVariable) value;
            this.variables.remove(jsonVariable.getName());
        }
        return value;
    }

    /**
     * @inheritDoc
     * @see JSONStatement#
     * apply(java.lang.String, java.lang.Object)
     */
    public boolean apply(String variable, Object value) {
        JSONVariable jsonVariable = this.variables.get(variable);
        if (jsonVariable != null) {
            jsonVariable.setValue(value);
            return true;

        } else {
            @SuppressWarnings("unchecked") Iterator<String> iterator = super.keys();
            while (iterator.hasNext()) {
                Object object = super.get(iterator.next());
                if (JSONStatement.class.isAssignableFrom(object.getClass()) && ((JSONStatement) object).apply(variable, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @inheritDoc
     * @see JSONStatement#reset()
     */
    public void reset() {
        Iterator<JSONVariable> variableIterator = this.variables.values().iterator();
        while (variableIterator.hasNext()) {
            variableIterator.next().reset();
        }
        @SuppressWarnings("unchecked") Iterator<String> iterator = super.keys();
        while (iterator.hasNext()) {
            Object object = super.get(iterator.next());
            if (JSONStatement.class.isAssignableFrom(object.getClass())) {
                ((JSONStatement) object).reset();
            }
        }
    }

}
