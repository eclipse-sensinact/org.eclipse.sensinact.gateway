/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.api.function;


/**
 * This interface is used by the third party developers to access to a data and its metadatas.
 *
 * @author Rémi Druilhe
 */
public interface DataItf {
    /**
     * Get the source URI of this data
     *
     * @return the source URI
     */
    String getSourceUri();

    /**
     * Get the value of the {@link Data}
     *
     * @return the value
     */
    Object getValue();

    /**
     * Get the Java type of the {@link Data}
     *
     * @return the Java type
     */
    Class<?> getType();

    /**
     * Get the timestamp of the data
     *
     * @return the timestamp
     */
    long getTimestamp();
}
