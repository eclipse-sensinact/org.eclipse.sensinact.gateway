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
package org.eclipse.sensinact.gateway.commands.gogo.internal.shell;

import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONArray;
import org.json.JSONObject;

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
    public void output(JSONObject object, int offset) {
        String[] names = JSONObject.getNames(object);
        int index = 0;
        int length = names == null ? 0 : names.length;
        for (; index < length; index++) {
            String name = names[index];
            if (offset == 0 && (name.equals("type") || name.equals("statusCode"))) {
                continue;
            }
            Object value = object.get(name);

            if (CastUtils.isPrimitive(value.getClass())) {
                StringBuilder builder = new StringBuilder();
                builder.append(name);
                builder.append(" : ");
                builder.append(value);
                output(builder.toString(), offset);

            } else if (value.getClass() == JSONObject.class) {
                outputUnderlined(name, offset + 4);
                output((JSONObject) value, offset + 4);

            } else if (value.getClass() == JSONArray.class) {
                outputUnderlined(name, offset + 4);
                output((JSONArray) value, offset + 4);
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
    public void output(JSONArray object, int offset) {
        int index = 0;
        int length = object == null ? 0 : object.length();
        for (; index < length; index++) {
            Object value = object.get(index);
            if (CastUtils.isPrimitive(value.getClass())) {
                output(String.valueOf(value), offset);

            } else if (value.getClass() == JSONObject.class) {
                output((JSONObject) value, offset + 4);

            } else if (value.getClass() == JSONArray.class) {
                output((JSONArray) value, offset + 4);
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
