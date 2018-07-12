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
import org.json.JSONTokener;

/**
 * An extended JSONTokener which is able to identify a {@link JSONStatement.JSONVariable}
 * in the  parsed JSON stream
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class JSONTokenerStatement extends JSONTokener {
    /**
     * Constructor
     *
     * @param s the JSON formated string to parse
     */
    public JSONTokenerStatement(String json) {
        super(json);
    }

    /**
     * Get the next value. The value can be a Boolean, Double, Integer,
     * JSONVariable , JSONArrayStatement, JSONObjectStatement, Long, or
     * String, or the JSONObject.NULL object.
     *
     * @return An object.
     * @throws JSONException If syntax error.
     */
    public Object nextValue() throws JSONException {
        char c = this.nextClean();
        switch (c) {
            case '"':
            case '\'':
                return super.nextString(c);
            case '$':
                return this.nextVariable();
            case '{':
                super.back();
                return new JSONObjectStatement(this);
            case '[':
                super.back();
                return new JSONArrayStatement(this);
        }
        /*
         * Handle unquoted text. This could be the values true, false, or
         * null, or it can be a number. An implementation (such as this one)
         * is allowed to also accept non-standard forms.
         *
         * Accumulate characters until we reach the end of the text or a
         * formatting character.
         */
        StringBuffer sb = new StringBuffer();
        while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
            sb.append(c);
            c = super.next();
        }
        super.back();
        String string = sb.toString().trim();
        if ("".equals(string)) {
            throw super.syntaxError("Missing value");
        }
        return JSONObject.stringToValue(string);
    }

    /**
     * Returns the JSONVariable whose name is composed of the
     * characters up to the next close parenthesis character ;
     * otherwise returns null. Backslash processing is not done.
     *
     * @return A JSONVariable.
     * @throws JSONException Unterminated string.
     */
    public JSONStatement.JSONVariable nextVariable() throws JSONException {
        char c = this.next();
        if (c != JSONObjectStatement.OPEN_PARENTHESIS) {
            throw this.syntaxError("Variable name pattern : ^\\$\\([^\\(\\)\\\\]+\\$)");
        }
        StringBuffer sb = new StringBuffer();
        for (; ; ) {
            c = this.next();
            switch (c) {
                case 0:
                case '\n':
                case '\r':
                    throw this.syntaxError("Unterminated string");
                case '\\':
                case '(':
                    throw this.syntaxError("Variable name pattern : ^\\$\\([^\\(\\)\\\\]+\\$)");
                case ')':
                    return new JSONStatement.JSONVariable(sb.toString());
                default:
                    sb.append(c);
            }
        }
    }
}
