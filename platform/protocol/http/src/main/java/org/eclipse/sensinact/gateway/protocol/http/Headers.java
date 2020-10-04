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
package org.eclipse.sensinact.gateway.protocol.http;

import java.util.List;
import java.util.Map;

/**
 * HTTP headers collection
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Headers extends Iterable<String> {
    /**
     * Adds all entry of the Map passed as parameter to
     * this headers map
     *
     * @param headers the Map of all the headers to add
     */
    void addHeaders(Map<String, List<String>> headers);

    /**
     * Adds the field value passed as parameter to the list
     * of ones mapped to the field name also passed as
     * parameter
     *
     * @param header the field for which to add the field value
     * @param value  the field value to add
     */
    void addHeader(String header, String value);

    /**
     * Adds the list of field values passed as parameter
     * to the field name also passed as parameter
     *
     * @param header the field for which to add the list of field
     *               values
     * @param values the list of field values to add
     */
    void addHeader(String header, List<String> values);

    /**
     * Returns the unmodifiable List of Strings that
     * represents the values for the field name passed
     * as parameter
     *
     * @param header the field name for which to return the list
     *               of values
     * @return the unmodifiable List of Strings that
     * represents the values for the specified
     * field name
     */
    public List<String> getHeader(String header);

    /**
     * Returns the String concatenation of the list of
     * field values for the one whose name is passed
     * as parameter
     *
     * @param header the field name for which to return the list
     *               of values concatenate in a single String
     * @return the String concatenation of the list of
     * field values for the specified one
     */
    String getHeaderAsString(String header);

    /**
     * Returns the unmodifiable Map of all the header fields.
     * The Map keys are Strings that represent the header field names.
     * Each Map value is an unmodifiable List of Strings that represents
     * the corresponding field values.
     *
     * @return the unmodifiable Map of all header fields
     */
    Map<String, List<String>> getHeaders();
}
