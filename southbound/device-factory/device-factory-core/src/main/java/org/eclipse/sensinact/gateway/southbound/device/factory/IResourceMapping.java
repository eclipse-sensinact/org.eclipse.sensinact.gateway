/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.device.factory;

import java.util.Map;

/**
 *
 */
public interface IResourceMapping {

    /**
     * Checks if this mapping is a literal mapping
     */
    boolean isLiteral();

    /**
     * Returns the name of the service
     */
    String getService();

    /**
     * Returns the name of the resource
     */
    String getResource();

    /**
     * Returns the name of the metadata, if any
     */
    String getMetadata();

    /**
     * Checks if this mapping targets a metadata
     */
    boolean isMetadata();

    /**
     * Returns the resource path
     */
    String getResourcePath();

    /**
     * Returns a new instance of the mapping with resolved variables
     *
     * @param variables Resolved variables
     * @return The record path with replaced variables
     * @throws InvalidResourcePathException Resolved resource path is invalid
     * @throws VariableNotFoundException    A variable wasn't resolved
     */
    IResourceMapping fillInVariables(final Map<String, String> variables)
            throws InvalidResourcePathException, VariableNotFoundException;

    /**
     * Returns a copy of this mapping with a path that meets the sensiNact naming
     * requirements
     *
     * @return This object if the key was valid or a new one with a new path
     * @throws InvalidResourcePathException Invalid new resource path
     */
    IResourceMapping ensureValidPath() throws InvalidResourcePathException;
}
