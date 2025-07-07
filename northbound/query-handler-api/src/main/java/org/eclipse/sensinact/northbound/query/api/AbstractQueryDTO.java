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

import org.eclipse.sensinact.northbound.query.dto.SensinactPath;
import org.eclipse.sensinact.northbound.query.dto.query.QueryActDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryDescribeDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryGetDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryListDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QuerySetDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QuerySnapshotDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QuerySubscribeDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryUnsubscribeDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Represents a WebSocket query root body
 */
@JsonSubTypes({ @Type(value = QueryListDTO.class, name = "LIST"),
        @Type(value = QueryDescribeDTO.class, name = "DESCRIBE"),
        @Type(value = QuerySnapshotDTO.class, name = "GET_SNAPSHOT"), @Type(value = QueryGetDTO.class, name = "GET"),
        @Type(value = QuerySetDTO.class, name = "SET"), @Type(value = QueryActDTO.class, name = "ACT"),
        @Type(value = QuerySubscribeDTO.class, name = "SUBSCRIBE"),
        @Type(value = QueryUnsubscribeDTO.class, name = "UNSUBSCRIBE") })
@JsonTypeInfo(use = Id.NAME, include = As.EXISTING_PROPERTY, property = "operation")
public abstract class AbstractQueryDTO {

    /**
     * Target URI
     */
    public SensinactPath uri;

    /**
     * Kind of operation
     */
    public final EQueryType operation;

    /**
     * Placeholder for a user-given request ID
     */
    @JsonInclude(Include.NON_NULL)
    public String requestId;

    /**
     * Sets up the operation type
     *
     * @param operation Operation type
     */
    public AbstractQueryDTO(final EQueryType operation) {
        this.operation = operation;
    }
}
