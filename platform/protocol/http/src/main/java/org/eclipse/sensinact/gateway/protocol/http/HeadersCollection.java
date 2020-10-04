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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Http Headers collection implementation
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class HeadersCollection implements Headers {
    /**
     * the Map of header fields
     */
    private Map<String, List<String>> headers;

    /**
     * Constructor
     */
    public HeadersCollection() {
        this.headers = new HashMap<String, List<String>>();
    }

    /**
     * Constructor
     *
     * @param headers the initial Map of header fields
     */
    public HeadersCollection(Map<String, List<String>> headers) {
        this();
        this.addHeaders(headers);
    }

    /**
     * @inheritDoc
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<String> iterator() {
        return this.headers.keySet().iterator();
    }

    /**
     * @inheritDoc
     * @see Headers#addHeaders(java.util.Map)
     */
    @Override
    public void addHeaders(Map<String, List<String>> headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<String, List<String>>> iterator = headers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> entry = iterator.next();
            this.addHeader(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @inheritDoc
     * @see Headers#addHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void addHeader(String header, String value) {
        List<String> headerValues = this.headers.get(header);
        if (headerValues == null) {
            headerValues = new ArrayList<String>();
            this.headers.put(header, headerValues);
        }
        headerValues.add(value);
    }

    /**
     * @inheritDoc
     * @see Headers#addHeader(java.lang.String, java.util.List)
     */
    @Override
    public void addHeader(String header, List<String> values) {
        List<String> headerValues = this.headers.get(header);
        if (headerValues == null) {
            headerValues = new ArrayList<String>();
            this.headers.put(header, headerValues);
        }
        headerValues.addAll(values);
    }

    /**
     * @inheritDoc
     * @see Headers#getHeader(java.lang.String)
     */
    @Override
    public List<String> getHeader(String header) {
        return Collections.unmodifiableList(this.headers.get(header));
    }

    /**
     * @inheritDoc
     * @see Headers#getHeaderAsString(java.lang.String)
     */
    @Override
    public String getHeaderAsString(String header) {
        List<String> list = this.headers.get(header);
        if (list == null) {
            return null;
        }
        int index = 0;
        int length = list.size();

        StringBuilder builder = new StringBuilder();
        for (; index < length; index++) {
            builder.append(index > 0 ? " " : "");
            builder.append(list.get(index));
        }
        return builder.toString();
    }

    /**
     * @inheritDoc
     * @see Headers#getHeaders()
     */
    @Override
    public Map<String, List<String>> getHeaders() {
        return Collections.unmodifiableMap(this.headers);
    }

    /**
     * @inheritDoc
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = this.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            builder.append("\n\t");
            builder.append(key);
            builder.append(":");
            builder.append(this.getHeaderAsString(key));
        }
        return builder.toString();
    }
}
