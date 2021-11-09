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
package org.eclipse.sensinact.gateway.util.rest;

import org.osgi.framework.ServiceReference;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Rest like URI/Resource mapping report
 */
public class RestLikeMappingReport {
    /**
     * map of string filters according to uri element
     */
    private Map<String, String> filtersMap;
    /**
     * map of ServiceReferences array according to uri element
     */
    private Map<String, List<ServiceReference<?>>> referencesMap;
    /**
     * unparsed part initial index
     */
    private int unparsed;
    /**
     * parsed URI elements
     */
    private String[] uriElements;
    /**
     * {@link StackTraceElement}s list
     */
    private LinkedList<StackTraceElement> trace;
    /**
     * method part of the parsed uri
     */
    private String method;

    /**
     * class name of returned registered services
     */
    private String className;

    /**
     * Constructor
     *
     * @param uriElements elements array of parsed URI
     */
    public RestLikeMappingReport(String[] uriElements) {
        this.uriElements = uriElements;
        this.unparsed = this.uriElements.length;
        this.filtersMap = new HashMap<String, String>();
        this.referencesMap = new HashMap<String, List<ServiceReference<?>>>();
        this.trace = new LinkedList<StackTraceElement>();
    }

    /**
     * Reports the registered {@link ServiceReference}s for
     * a specific string filter
     *
     * @param filter     the string filter
     * @param references the registered {@link ServiceReference}s list
     */
    void reportReferences(String filter, List<ServiceReference<?>> references) {
        this.referencesMap.put(filter, references);
    }

    /**
     * Reports a string filter for a specific uri elements
     * index
     *
     * @param index  the uri elements index
     * @param filter the string filter
     */
    void reportFilter(int index, String filter) {
        this.filtersMap.put(uriElements[index], filter);
    }

    /**
     * Reports the retrieved method referenced in the
     * parsed uri
     *
     * @param method the method referenced in the parsed uri
     */
    void reportMethod(String method) {
        this.method = method;
    }

    /**
     * Reports the last referenced OSGi service's
     * class name
     *
     * @param className the last referenced OSGi service's
     *                  class name
     */
    void reportImplementation(String className) {
        this.className = className;
    }

    /**
     * Reports the unfiltered uri elements
     * index
     *
     * @param index the unfiltered uri elements
     *              index
     */
    void reportIndex(int index) {
        this.unparsed = index;
    }

    /**
     * Reports the exception passed as parameter
     *
     * @param throwable a thrown exception
     */
    void reportError(Throwable throwable) {
        this.trace.addAll(Arrays.asList(throwable.getStackTrace()));
    }

    /**
     * Prints reported stack traces
     *
     * @param output the output stream to print in
     */
    public void printStackTrace(PrintStream output) {
        for (int i = 0; i < trace.size(); i++) {
            StackTraceElement element = trace.get(i);
            output.println(element.toString());
        }
    }

    /**
     * Returns the retrieved method's name in
     * the parsed uri
     *
     * @return the retrieved method's name in
     * the parsed uri
     */
    public String method() {
        return this.method;
    }

    /**
     * Returns the last referenced OSGi service's
     * class name
     *
     * @return the last referenced OSGi service's
     * class name
     */
    public String implementation() {
        return this.className;
    }

    /**
     * Returns the
     *
     * @return
     */
    public String unparsed() {
        StringBuilder builder = new StringBuilder();

        for (int i = this.unparsed + 1; i < this.uriElements.length; i++) {
            if (this.uriElements[i].equals(method)) {
                continue;
            }
            builder.append(uriElements[i]);
            builder.append((i < this.uriElements.length - 1) ? "/" : "");
        }
        return builder.toString();
    }

    /**
     * Returns the set of {@link ServiceReference}s as an array
     * retrieved for the key passed as parameter
     *
     * @return the set of {@link ServiceReference}s as an array
     * retrieved for the key passed as parameter
     */
    public ServiceReference<?>[] get(String key) {
        String filter = this.filtersMap.get(key);
        ServiceReference<?>[] references = new ServiceReference[0];

        if (filter != null) {
            List<ServiceReference<?>> referencesList = this.referencesMap.get(this.filtersMap.get(key));

            references = referencesList == null ? references : referencesList.toArray(references);
        }
        return references;
    }
}
