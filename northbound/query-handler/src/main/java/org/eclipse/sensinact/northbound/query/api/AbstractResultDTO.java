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
package org.eclipse.sensinact.northbound.query.api;

import org.eclipse.sensinact.northbound.query.dto.notification.ResultResourceNotificationDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ErrorResultDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultActDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultDescribeProvidersDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListProvidersDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListResourcesDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListServicesDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultSubscribeDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultUnsubscribeDTO;
import org.eclipse.sensinact.northbound.query.dto.result.TypedResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeId;

/**
 * Represents a Northbound query response
 */
@JsonSubTypes({ @Type(value = ErrorResultDTO.class, name = "ERROR"),
        @Type(value = ResultDescribeProvidersDTO.class, name = "COMPLETE_LIST"),
        @Type(value = ResultListProvidersDTO.class, name = "PROVIDERS_LIST"),
        @Type(value = ResultListServicesDTO.class, name = "SERVICES_LIST"),
        @Type(value = ResultListResourcesDTO.class, name = "RESOURCES_LIST"),
        @Type(value = TypedResponse.class, names = { "DESCRIBE_PROVIDER", "DESCRIBE_SERVICE", "DESCRIBE_RESOURCE",
                "GET_RESPONSE", "SET_RESPONSE" }),
        @Type(value = ResultActDTO.class, name = "ACT_RESPONSE"),
        @Type(value = ResultSubscribeDTO.class, name = "SUBSCRIPTION_RESPONSE"),
        @Type(value = ResultResourceNotificationDTO.class, name = "SUBSCRIPTION_NOTIFICATION"),
        @Type(value = ResultUnsubscribeDTO.class, name = "UNSUBSCRIPTION_RESPONSE") })
public abstract class AbstractResultDTO {

    /**
     * Result content type
     */
    @JsonTypeId
    public final EResultType type;

    /**
     * Targeted URI as used
     */
    public String uri;

    /**
     * Placeholder for the user-given request ID
     */
    @JsonInclude(Include.NON_NULL)
    public String requestId;

    /**
     * Result code
     */
    public int statusCode;

    /**
     * Error message
     */
    @JsonInclude(Include.NON_NULL)
    public String error;

    /**
     * Sets up the result type
     *
     * @param type Result type
     */
    public AbstractResultDTO(final EResultType type) {
        this.type = type;
    }
}
