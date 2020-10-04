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
package org.eclipse.sensinact.gateway.nthbnd.http.callback;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * An abstract {@link RequestWrapper} implementation
 *  
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public abstract class AbstractRequestWrapper implements RequestWrapper {
	
	protected static final Logger LOG = Logger.getLogger(RequestWrapper.class.getName());
	
	protected static String RAW_QUERY_PARAMETER = "#RAW#";
	
	/**
     * Builds a map of parameters according to the query String (HTML query string 
     * formated: ?(&lt;key&gt;=&lt;value&gt;)(&&lt;key&gt;=&lt;value&gt;)* ) passed 
     * as parameter
     *
     * @param queryString the query String to be converted into a map of parameters
     * 
     * @throws UnsupportedEncodingException
     */
    protected static Map<String, List<String>> processRequestQuery(String queryString) throws UnsupportedEncodingException {
        if (queryString == null) {
            return Collections.<String, List<String>>emptyMap();
        }
        Map<String, List<String>> queryMap = new HashMap<String, List<String>>();

        char[] characters = queryString.toCharArray();
        int index = 0;
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
                    addParameter(queryMap, name, value);
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
            addParameter(queryMap, name, Boolean.TRUE.toString());
            return queryMap;
        }
        value = element.toString();
        addParameter(queryMap, name, value);
        return queryMap;
    }

    /**
     * Adds an entry to the Map passed as parameter, mapping the specified name to 
     * the value argument
     *
     * @param map the map to add the entry in 
     * @param name the name of the parameter to be added 
     * @param value the value of the parameter to be added
     * 
     * @throws UnsupportedEncodingException
     */
    protected static void addParameter(Map<String, List<String>> map, String name, String value) throws UnsupportedEncodingException {
        if (name == null || name.length() == 0) {
            name = RAW_QUERY_PARAMETER;
        } else {
            name = URLDecoder.decode(name, "UTF-8");
        }
        List<String> values = map.get(name);
        if (values == null) {
            values = new ArrayList<String>();
            map.put(name, values);
        }
        values.add(URLDecoder.decode(value, "UTF-8"));
    }
}
