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
package org.eclipse.sensinact.gateway.util;

/**
 * Uri string helpers
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UriUtils {
    public static final String WILDCARD = "*";

    public static final String PATH_SEPARATOR = "/";
    public static final String ROOT = PATH_SEPARATOR;

    /**
     * Returns the distinct elements of the uri
     * passed as parameter into an array of strings
     *
     * @param uri the string uri to decompose
     * @return the elements array of the specified
     * uri
     */
    public static String[] getUriElements(String uri) {
        String formatedUri = formatUri(uri);
        if (formatedUri == ROOT) {
            return new String[0];
        }
        String[] elements = formatedUri.split(PATH_SEPARATOR);
        String[] uriElements = new String[elements.length - 1];
        System.arraycopy(elements, 1, uriElements, 0, elements.length - 1);
        return uriElements;

    }

    public static String getUri(String[] uriElements) {
        int index = 0;
        int length = uriElements == null ? 0 : uriElements.length;

        StringBuilder uriBuilder = new StringBuilder();
        for (; index < length; index++) {
            if (uriElements[index].trim().length() == 0) {
                continue;
            }
            uriBuilder.append(uriElements[index].startsWith("/") ? "" : PATH_SEPARATOR);
            uriBuilder.append(uriElements[index]);
        }
        return uriBuilder.toString();
    }

    /**
     * Returns the parent string uri of the one
     * passed as parameter
     *
     * @param uri the uri to return the uri parent of
     * @return the specified uri's parent uri
     */
    public static String getRoot(String uri) {
        String formatedUri = formatUri(uri);
        if (formatedUri.intern() == ROOT.intern()) {
            return ROOT;
        }
        String[] uriElements = formatedUri.split(PATH_SEPARATOR);

        int index = 0;
        int length = 2;

        StringBuilder buffer = new StringBuilder();
        for (; index < length; index++) {
            if (uriElements[index] == null || uriElements[index].length() == 0) {
                continue;
            }
            buffer.append(PATH_SEPARATOR);
            buffer.append(uriElements[index]);
        }
        return formatUri(buffer.toString());
    }

    /**
     * Returns the parent string uri of the one
     * passed as parameter
     *
     * @param uri the uri to return the uri parent of
     * @return the specified uri's parent uri
     */
    public static String getParentUri(String uri) {
        String formatedUri = formatUri(uri);
        if (formatedUri.intern() == ROOT.intern()) {
            return ROOT;
        }
        String[] uriElements = formatedUri.split(PATH_SEPARATOR);

        int index = 0;
        int length = uriElements.length - 1;

        StringBuilder buffer = new StringBuilder();
        for (; index < length; index++) {
            if (uriElements[index] == null || uriElements[index].length() == 0) {
                continue;
            }
            buffer.append(PATH_SEPARATOR);
            buffer.append(uriElements[index]);
        }
        return formatUri(buffer.toString());
    }

    /**
     * Returns the parent string uri of the one
     * passed as parameter
     *
     * @param uri the uri to return the uri parent of
     * @return the specified uri's parent uri
     */
    public static String getChildUri(String uri) {
        StringBuilder buffer = new StringBuilder();
        String formatedUri = formatUri(uri);

        if (formatedUri.intern() == ROOT.intern()) {
            return ROOT;
        }
        String[] uriElements = formatedUri.split(PATH_SEPARATOR);

        int length = uriElements.length;
        int index = 2;

        for (; index < length; index++) {
            if (uriElements[index] == null || uriElements[index].length() == 0) {
                continue;
            }
            buffer.append(PATH_SEPARATOR);
            buffer.append(uriElements[index]);
        }
        if (buffer.length() == 0) {
            return ROOT;
        }
        return buffer.toString();
    }

    /**
     * Returns the last part of the uri string
     * passed as parameter.
     *
     * @param uri the uri from which to extract the leaf
     *            element
     * @return the last part of the specified uri
     */
    public static String getLeaf(String uri) {
        String formatedUri = formatUri(uri);
        if (formatedUri.intern() == ROOT.intern()) {
            return ROOT;
        }
        String[] uriElements = formatedUri.split(PATH_SEPARATOR);
        return uriElements[uriElements.length - 1];
    }

    /**
     * Formats and returns the uri string passed as
     * parameter. Formating meaning to add the path
     * separator constant at the beginning of the
     * uri if missing, and to remove it from the end
     * of the uri if existing
     *
     * @param uri the uri to format
     * @return the formated string uri
     */
    public static String formatUri(String uri) {
        String formatedUri = null;
        if (uri == null) {
            return ROOT;

        } else {
            formatedUri = new String(uri.trim());
        }
        if (!formatedUri.startsWith(PATH_SEPARATOR)) {
            formatedUri = new StringBuilder().append(PATH_SEPARATOR).append(formatedUri).toString();
        }
        if (formatedUri.length() > 1 && formatedUri.endsWith(PATH_SEPARATOR)) {
            formatedUri = formatedUri.substring(0, formatedUri.length() - 1);
        }
        if (formatedUri.length() == 1) {
            return ROOT;
        }
        return formatedUri;
    }
}
