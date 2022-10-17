/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.action;

import org.eclipse.sensinact.prototype.annotation.propertytype.ProviderName;
import org.eclipse.sensinact.prototype.annotation.propertytype.WhiteboardResource;
import org.eclipse.sensinact.prototype.annotation.verb.ACT;
import org.eclipse.sensinact.prototype.annotation.verb.UriParam;
import org.eclipse.sensinact.prototype.annotation.verb.UriParam.UriSegment;
import org.osgi.service.component.annotations.Component;

/**
 * Multiple providers from a single service, a single method for all resources
 */
@WhiteboardResource
@ProviderName({ "foo", "bar", "foobar" })
@Component(service = _03_MultiActionResource.class)
public class _03_MultiActionResource {

    @ACT(service = "example", resource = "fizz")
    @ACT(service = "example", resource = "buzz")
    @ACT(service = "example2", resource = "fizzbuzz")
    public void setValue(@UriParam(UriSegment.PROVIDER) String provider, @UriParam(UriSegment.SERVICE) String service,
            @UriParam(UriSegment.RESOURCE) String resource) {
        // Get the actual value from the sensor
    }
}
