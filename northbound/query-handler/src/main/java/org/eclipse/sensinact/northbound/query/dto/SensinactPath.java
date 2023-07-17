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
package org.eclipse.sensinact.northbound.query.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Represents a path in the sensiNact nomenclature
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(using = SensinactPathDeserializer.class)
public class SensinactPath {
    /**
     * Target provider
     */
    public String provider;

    /**
     * Target service
     */
    public String service;

    /**
     * Target resource
     */
    public String resource;

    /**
     * Target metadata
     */
    public String metadata;

    /**
     * @param provider Provider ID
     * @param service  Service name
     * @param resource Resource name
     * @param metadata Metadata name
     */
    public SensinactPath(final String provider, final String service, final String resource, final String metadata) {
        this.provider = provider;
        this.service = service;
        this.resource = resource;
        this.metadata = metadata;
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider, service, resource, metadata);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof SensinactPath)) {
            return false;
        }

        final SensinactPath other = (SensinactPath) obj;
        return Objects.equals(provider, other.provider) && Objects.equals(service, other.service)
                && Objects.equals(resource, other.resource) && Objects.equals(metadata, other.metadata);
    }

    /**
     * @param provider Provider ID
     * @param service  Service name
     * @param resource Resource name
     */
    public SensinactPath(final String provider, final String service, final String resource) {
        this(provider, service, resource, null);
    }

    /**
     * @param provider Provider ID
     * @param service  Service name
     */
    public SensinactPath(final String provider, final String service) {
        this(provider, service, null, null);
    }

    /**
     * @param provider Provider ID
     */
    public SensinactPath(final String provider) {
        this(provider, null, null, null);
    }

    /**
     * Empty constructor
     */
    public SensinactPath() {
        this(null, null, null, null);
    }

    /**
     * Checks if this path has any meaningful value
     */
    public boolean isEmpty() {
        return !hasProvider() && !hasService() && !hasResource() && !hasMetadata();
    }

    /**
     * Checks if this path has a valid provider
     */
    public boolean hasProvider() {
        return provider != null && !provider.isBlank();
    }

    /**
     * Checks if this path has a valid provider
     */
    public boolean hasService() {
        return service != null && !service.isBlank();
    }

    /**
     * Checks if this path has a valid provider
     */
    public boolean hasResource() {
        return resource != null && !resource.isBlank();
    }

    /**
     * Checks if this path has a valid provider
     */
    public boolean hasMetadata() {
        return metadata != null && !metadata.isBlank();
    }

    /**
     * Checks if this path targets a specific provider
     */
    public boolean targetsSpecificProvider() {
        return hasProvider() && service == null && resource == null && metadata == null;
    }

    /**
     * Checks if this path targets a specific service
     */
    public boolean targetsSpecificService() {
        return hasProvider() && hasService() && resource == null && metadata == null;
    }

    /**
     * Checks if this path targets a specific resource
     */
    public boolean targetsSpecificResource() {
        return hasProvider() && hasService() && hasResource() && metadata == null;
    }

    /**
     * Checks if this path targets a specific resource metadata
     */
    public boolean targetsSpecificMetadata() {
        return hasProvider() && hasService() && hasResource() && hasMetadata();
    }

    @Override
    public String toString() {
        return String.format("Path(provider=%s, service=%s, resource=%s, metadata=%s)", provider, service, resource,
                metadata);
    }

    /**
     * Returns the path as a URI
     */
    public String toUri() {

        final List<String> items = new ArrayList<>(4);
        for (final String item : Arrays.asList(provider, service, resource, metadata)) {
            if (item == null) {
                break;
            }
            items.add(item);
        }

        return "/" + String.join("/", items);
    }

    /**
     * Tries to get a value from an array checking array length first. Empty strings
     * are considered null.
     *
     * @param array Source array
     * @param index Targeted index
     * @return The string value or null
     */
    private static String safeArrayGet(final String[] array, final int index) {
        if (index < array.length) {
            final String value = array[index].trim();
            if (value.isEmpty()) {
                return null;
            }
            return value;
        }

        return null;
    }

    /**
     * Reads the path from a short URI
     *
     * @param uri Short URI
     * @return The parsed path
     * @throws IllegalArgumentException Invalid URI
     */
    public static SensinactPath fromUri(final String uri) throws IllegalArgumentException {
        final SensinactPath path = new SensinactPath();

        if (uri == null || uri.isBlank()) {
            // No data
            return path;
        }

        final String[] parts = uri.split("/");
        if (parts.length >= 1 && !parts[0].isEmpty()) {
            throw new IllegalArgumentException("URI must start with a slash /");
        }

        path.provider = safeArrayGet(parts, 1);
        path.service = safeArrayGet(parts, 2);
        path.resource = safeArrayGet(parts, 3);
        path.metadata = safeArrayGet(parts, 4);
        return path;
    }
}
