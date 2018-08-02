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
