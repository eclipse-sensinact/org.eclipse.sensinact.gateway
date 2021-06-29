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
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.common.primitive.PathElement;
import org.eclipse.sensinact.gateway.core.FilteringCollection;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class NorthboundRequest implements PathElement, Nameable {
    
	public static final String ROOT = "/sensinact";

    /**
     * Builds a map of parameters according to the query String
     * (HTML query string formated: ?(&lt;key&gt;=&lt;value&gt;)
     * (&&lt;key&gt;=&lt;value&gt;)* ) passed as parameter
     *
     * @param queryString the query String to be converted
     *                    into a map of parameters
     * @throws UnsupportedEncodingException
     */
    public static Map<NorthboundRequestWrapper.QueryKey, List<String>> processRequestQuery(String queryString) throws UnsupportedEncodingException {
        if (queryString == null) 
            return Collections.<NorthboundRequestWrapper.QueryKey, List<String>>emptyMap();
        
        Map<NorthboundRequestWrapper.QueryKey, List<String>> queryMap = new HashMap<NorthboundRequestWrapper.QueryKey, List<String>>();

        char[] characters = queryString.toCharArray();
        int index = 0;
        int pos = 0;
        int length = characters.length;

        boolean escape = false;
        String name = null;
        String value = null;
        StringBuilder element = new StringBuilder();

        for (; index < length; index++) {
            char c = characters[index];
            if (escape) {
                escape = false;
                element.append(c);
                continue;
            }
            switch (c) {
                case '\\':
                    escape = true;
                    break;
                case '=':
                    if (name == null) {
                        name = element.toString();
                        element = new StringBuilder();

                    } else {
                        element.append(c);
                    }
                    break;
                case '&':
                    value = element.toString();
                    addQueryParameter(queryMap, pos++, name, value);
                    name = null;
                    value = null;
                    element = new StringBuilder();
                    break;
                default:
                    element.append(c);
            }
        }
        if(name == null && element.length()>0) {
        	name = element.toString();
            addQueryParameter(queryMap, pos++, name, Boolean.TRUE.toString());
            return queryMap;
        }
        value = element.toString();
        addQueryParameter(queryMap, pos++, name, value);
        return queryMap;
    }

    /**
     * Adds a parameter to the map argument, created using
     * the name and value passed as parameters
     *
     * @param queryMap the map to which to add the
     *                 parameter to be created using the name and
     *                 value arguments
     * @param name     the name of the parameter to be
     *                 added to the map argument
     * @param value    the value of the parameter to be
     *                 added to the map argument
     * @throws UnsupportedEncodingException
     */
    private static void addQueryParameter(Map<NorthboundRequestWrapper.QueryKey, List<String>> queryMap, int pos, String name, String value) throws UnsupportedEncodingException {
        if (name == null || name.length() == 0) 
            name = DefaultNorthboundRequestHandler.RAW_QUERY_PARAMETER;
        else 
            name = URLDecoder.decode(name, "UTF-8");
        NorthboundRequestWrapper.QueryKey key = new NorthboundRequestWrapper.QueryKey();
        key.index = pos;
        key.name = name;
        List<String> values = queryMap.get(key);
        if (values == null) {
            values = new ArrayList<String>();
            queryMap.put(key, values);
        }
        values.add(URLDecoder.decode(value, "UTF-8"));
    }

    /**
     * @return
     */
    protected abstract String getMethod();

    protected FilteringCollection filteringCollection;
    protected NorthboundMediator mediator;
    private String requestIdentifier;

    /**
     * @param mediator
     * @param requestIdentifier
     * @param responseFormat
     * @param authentication
     */
    public NorthboundRequest(NorthboundMediator mediator, String requestIdentifier, FilteringCollection filteringCollection) {
        this.mediator = mediator;
        this.requestIdentifier = requestIdentifier;
        this.filteringCollection = filteringCollection;
    }

    @Override
    public String getPath() {
        return ROOT;
    }

    protected Argument[] getExecutionArguments() {
        Argument[] arguments = new Argument[1];
        arguments[0] = new Argument(String.class, this.requestIdentifier);
        return arguments;
    }
}
