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

import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.json.JSONObject;
import org.json.JSONString;

/**
 * A JSONStatement allows to define variables whose
 * value can be set later in a JSONArray or a JSONObject
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface JSONStatement {
    public static final char VARIABLE_PREFIX = '$';
    public static final char OPEN_PARENTHESIS = '(';
    public static final char CLOSE_PARENTHESIS = ')';

    /**
     * Applies the value object passed as parameter to the
     * JSONVariable whose name is passed as parameter and which
     * belongs to this JSONStatement
     *
     * @param variable the name of the {@link JSONVariable} to which to
     *                 apply the specified value
     * @param value    the value object to apply to the specified
     *                 {@link JSONVariable}
     * @return <ul>
     * <li>true if the targeted JSONVariable has been
     * found and its value set properly</li>
     * <li>false otherwise</li>
     * </ul>
     */
    boolean apply(String variable, Object value);

    /**
     * Resets the value of all contained JSONVariables of
     * this JSONStatement
     */
    void reset();

    /**
     * A variable of a JSONStatement
     */
    public static final class JSONVariable implements JSONString {
        static final Object UNSET = new Object();

        private final String variable;
        private Object value = UNSET;

        /**
         * Constructor
         *
         * @param string the name of the JSONVariable to
         *               instantiate
         */
        public JSONVariable(String variable) {
            this.variable = variable;
        }

        /**
         * Sets the value object of this JSONVariable
         *
         * @param value the object value to set
         */
        public void setValue(Object value) {
            this.value = value;
        }

        /**
         * Sets the default object value to this JSONVariable
         */
        public void reset() {
            this.setValue(JSONVariable.UNSET);
        }

        /**
         * Returns the name of this Variable
         *
         * @return this variable's name
         */
        public String getName() {
            return this.variable;
        }

        /**
         * @InheritedDoc
         * @see org.json.JSONString#toJSONString()
         */
        @Override
        public String toJSONString() {
            return value == UNSET ? JSONObject.NULL.toString() : JSONUtils.toJSONFormat(value);
        }
    }
}
