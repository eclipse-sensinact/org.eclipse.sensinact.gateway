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

import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.api.EResultType;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 *
 */
public class TypedResponse<T extends SubResult> extends AbstractResultDTO {

    @JsonSubTypes({ @Type(value = ResponseDescribeProviderDTO.class, name = "DESCRIBE_PROVIDER"),
            @Type(value = ResponseDescribeServiceDTO.class, name = "DESCRIBE_SERVICE"),
            @Type(value = ResponseDescribeResourceDTO.class, name = "DESCRIBE_RESOURCE"),
            @Type(value = ResponseGetDTO.class, names = { "GET_RESPONSE", "SET_RESPONSE" }) })
    @JsonTypeInfo(use = Id.NAME, include = As.EXTERNAL_PROPERTY, property = "type")
    public T response;

    public TypedResponse() {
        super(EResultType.ERROR);
    }

    public TypedResponse(final EResultType type) {
        super(type);
    }
}
