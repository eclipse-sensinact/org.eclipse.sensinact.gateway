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
package org.eclipse.sensinact.sensorthings.sensing.rest.filters;

import static java.util.stream.Collectors.toList;

import java.io.IOException;

import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.RefFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

/**
 * This filter converts the output into a self link
 */
@RefFilter
public class RefFilterImpl implements WriterInterceptor {

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        Object entity = context.getEntity();

        if (entity instanceof ResultList) {
            ResultList<? extends Self> resultList = (ResultList<?>) entity;
            ResultList<Self> newEntity = new ResultList<>(resultList.count(), resultList.nextLink(),
                    resultList.value().stream().map(r -> new SelfOnly(r.selfLink())).collect(toList()));
            context.setEntity(newEntity);
        } else if (entity instanceof Self) {
            Self self = (Self) entity;
            context.setEntity(new SelfOnly(self.selfLink()));
        } else if (entity instanceof ObjectNode) {
            ObjectNode node = (ObjectNode) entity;
            if (!node.isArray()) {
                node.retain("@iot.selfLink");
            } else {
                ArrayNode array = (ArrayNode) entity;
                for (int i = 0; i < array.size(); i++) {
                    final JsonNode jsonNode = array.get(i);
                    array.set(i, ((ObjectNode) jsonNode).get("@iot.selfLink"));
                }
            }
        } else if (entity != null) {
            throw new InternalServerErrorException("The entity " + entity + " does not have a reference");
        }

        context.proceed();
    }

    private record SelfOnly(String selfLink) implements Self {
    }
}
