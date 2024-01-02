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
package org.eclipse.sensinact.gateway.southbound.device.factory.impl;

import org.eclipse.sensinact.gateway.southbound.device.factory.IResourceMapping;
import org.eclipse.sensinact.gateway.southbound.device.factory.InvalidResourcePathException;

/**
 *
 */
public abstract class AbstractResourceMapping implements IResourceMapping {

    /**
     * Resource service
     */
    private final String service;

    /**
     * Resource name
     */
    private final String resource;

    /**
     * Resource metadata name
     */
    private final String metadata;

    /**
     * Resource path
     */
    private final String path;

    /**
     * Parses the resource path
     *
     * @param rcPath Mapping resource path
     * @throws InvalidResourcePathException Error parsing the resource path
     */
    public AbstractResourceMapping(final String rcPath) throws InvalidResourcePathException {
        if (rcPath.startsWith("$") || rcPath.startsWith("@")) {
            // Variable path
            service = null;
            resource = null;
            metadata = null;
            path = rcPath;
        } else {
            // Fixed path
            final String[] parts = rcPath.split("/");
            if (parts.length < 2) {
                throw new InvalidResourcePathException(String.format("Not enought parts in path '%s'", rcPath));
            } else if (parts.length > 3) {
                throw new InvalidResourcePathException(String.format("Too many parts in path '%s'", rcPath));
            }

            service = parts[0].strip();
            if (service.isEmpty()) {
                throw new InvalidResourcePathException(String.format("Empty service name in path '%s'", rcPath));
            }
            resource = parts[1].strip();
            if (resource.isEmpty()) {
                throw new InvalidResourcePathException(String.format("Empty resource name in path '%s'", rcPath));
            }

            if (parts.length == 3) {
                metadata = parts[2].strip();
                if (metadata.isEmpty()) {
                    throw new InvalidResourcePathException(String.format("Empty metadata name in path '%s'", rcPath));
                }
            } else {
                metadata = null;
            }

            if (metadata != null) {
                path = String.join("/", service, resource, metadata);
            } else {
                path = String.join("/", service, resource);
            }
        }
    }

    /**
     * Returns a new instance of the same type, based on the given path
     *
     * @param path (Sanitized) resource path
     * @return A new instance based on the given path
     * @throws InvalidResourcePathException Invalid new resource path
     */
    protected abstract IResourceMapping newInstance(final String path) throws InvalidResourcePathException;

    /**
     * Returns the name of the service
     */
    @Override
    public String getService() {
        return service;
    }

    /**
     * Returns the name of the resource
     */
    @Override
    public String getResource() {
        return resource;
    }

    /**
     * Returns the name of the metadata, if any
     */
    @Override
    public String getMetadata() {
        return metadata;
    }

    /**
     * Checks if this mapping targets a metadata
     */
    @Override
    public boolean isMetadata() {
        return metadata != null;
    }

    /**
     * Returns the resource path
     */
    @Override
    public String getResourcePath() {
        return path;
    }

    @Override
    public IResourceMapping ensureValidPath(final boolean asciiOnly) throws InvalidResourcePathException {
        if (path == null) {
            // Not a path
            return this;
        }

        final String cleaned;
        if (asciiOnly) {
            cleaned = NamingUtils.asciiSanitizeName(path, true);
        } else {
            cleaned = NamingUtils.sanitizeName(path, true);
        }
        if (cleaned.equals(path)) {
            // Nothing to do
            return this;
        } else {
            return newInstance(cleaned);
        }
    }
}
