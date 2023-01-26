/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.rest.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.List;

import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;

import com.fasterxml.jackson.annotation.JsonInclude;

public class CompleteProviderDescriptionDTO {

    /**
     * Provider ID
     */
    public String name;

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
     * List of services
     */
    public List<CompleteServiceDescriptionDTO> services;
}
