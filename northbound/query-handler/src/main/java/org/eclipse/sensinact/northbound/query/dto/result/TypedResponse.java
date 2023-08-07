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
import org.eclipse.sensinact.northbound.query.dto.result.jackson.TypedResponseDeserializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Holds a response of a given type
 */
@JsonDeserialize(using = TypedResponseDeserializer.class)
public class TypedResponse<T extends SubResult> extends AbstractResultDTO {

    public T response;

    public TypedResponse() {
        super(EResultType.ERROR);
    }

    public TypedResponse(final EResultType type) {
        super(type);
    }
}
