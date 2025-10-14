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
package org.eclipse.sensinact.northbound.query.dto.result;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;

import com.fasterxml.jackson.annotation.JsonInclude;

public class SnapshotLinkedProviderDTO {

    /**
     * Provider ID
     */
    public String name;

    /**
     * Model Name
     */
    @JsonInclude(NON_NULL)
    public String modelName;

    /**
     * Provider location, if available
     */
    @JsonInclude(NON_NULL)
    public GeoJsonObject location;

    /**
     * Provider icon, if available
     */
    @JsonInclude(NON_NULL)
    public String icon;

    /**
     * Provider friendly name, if available
     */
    @JsonInclude(NON_NULL)
    public String friendlyName;

    /**
     * Provider description, if available
     */
    @JsonInclude(NON_NULL)
    public String description;
}
