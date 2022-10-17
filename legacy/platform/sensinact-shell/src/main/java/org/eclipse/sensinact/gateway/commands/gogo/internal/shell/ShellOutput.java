/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.commands.gogo.internal.shell;

import java.util.Map.Entry;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 * Output writer of the response to shell requests
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ShellOutput {
    /**
     * Prints out the {@link JSONObject} passed as
     * parameter with the specified offset
     *
     * @param object the {@link JSONObject} to be
     *               written
     * @param offset the output offset
     */
    public void output(JsonObject object, int offset) {
        for (Entry<String, JsonValue> e : object.entrySet()) {
            String name = e.getKey();
            if (offset == 0 && (name.equals("type") || name.equals("statusCode"))) {
                continue;
            }
            JsonValue value = e.getValue();

            if (value.getValueType() == ValueType.OBJECT) {
                outputUnderlined(name, offset + 4);
                output(value.asJsonObject(), offset + 4);
            } else if (value.getValueType() == ValueType.ARRAY) {
                outputUnderlined(name, offset + 4);
                output(value.asJsonArray(), offset + 4);
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append(name);
                builder.append(" : ");
                builder.append(value.toString());
                output(builder.toString(), offset);
            }
        }
    }

    /**
     * Prints out the {@link JSONArray} passed as
     * parameter with the specified offset
     *
     * @param object the {@link JSONArray} to be
     *               written
     * @param offset the output offset
     */
    public void output(JsonArray object, int offset) {
        for (JsonValue value : object) {
            if (value.getValueType() == ValueType.OBJECT) {

            } else if (value.getValueType() == ValueType.ARRAY) {
                output(value.asJsonArray(), offset + 4);
            } else {
                output(value.toString(), offset);
            }
        }
    }

    /**
     * Prints out the String passed as
     * parameter with the specified offset
     *
     * @param s      the String to be written
     * @param offset the output offset
     */
    public void output(String s, int offset) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (; index < offset; index++) {
            builder.append(' ');
        }
        builder.append(s);
        System.out.println(builder.toString());
    }

    /**
     * Prints out the String passed as
     * parameter, underlined and with the
     * specified offset
     *
     * @param s      the String to be written underlined
     * @param offset the output offset
     */
    public void outputUnderlined(String s, int offset) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (; index < s.length(); index++) {
            builder.append('-');
        }
        output(s, offset);
        output(builder.toString(), offset);
    }

    /**
     * Prints out the String passed as
     * parameter, quoted and with the
     * specified offset
     *
     * @param s      the String to be written quoted
     * @param offset the output offset
     */
    public void outputQuoted(String s, int offset) {
        StringBuilder builder = new StringBuilder();
        builder.append('"');
        builder.append(s);
        builder.append('"');
        output(builder.toString(), offset);
    }

    /**
     * Prints out the error String message passed as
     * parameter and the associated integer status code
     *
     * @param statusCode the integer error status code
     * @param s          the String error message
     */
    public void outputError(int statusCode, String s) {
        StringBuilder builder = new StringBuilder();
        builder.append("Error [");
        builder.append(statusCode);
        builder.append("] :");
        builder.append('"');
        builder.append(s);
        builder.append('"');
        output(builder.toString(), 0);
    }
}
