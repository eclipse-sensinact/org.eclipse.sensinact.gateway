/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.pull;

import org.eclipse.sensinact.prototype.annotation.dto.Data;
import org.eclipse.sensinact.prototype.annotation.dto.Provider;
import org.eclipse.sensinact.prototype.annotation.dto.Service;
import org.eclipse.sensinact.prototype.annotation.propertytype.WhiteboardResource;
import org.eclipse.sensinact.prototype.annotation.verb.GET;
import org.eclipse.sensinact.prototype.annotation.verb.GET.ReturnType;
import org.osgi.service.component.annotations.Component;

/**
 * A DTO defines the resource(s) returned by this GET method
 */
@WhiteboardResource
@Component(service = _02_DTOPullBasedResource.class)
public class _02_DTOPullBasedResource {

    @GET(ReturnType.DTO)
    public SimpleDTO getValue() {
        // Get the value from the sensor
        return null;
    }

    @Provider("pull_example")
    @Service("dto")
    public static class SimpleDTO {

        @Data
        public int count;

    }
}
