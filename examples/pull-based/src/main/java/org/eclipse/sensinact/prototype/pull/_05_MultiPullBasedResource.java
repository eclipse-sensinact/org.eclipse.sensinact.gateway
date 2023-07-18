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
package org.eclipse.sensinact.prototype.pull;

import org.eclipse.sensinact.core.annotation.propertytype.ProviderName;
import org.eclipse.sensinact.core.annotation.propertytype.WhiteboardResource;
import org.eclipse.sensinact.core.annotation.verb.GET;
import org.eclipse.sensinact.core.annotation.verb.UriParam;
import org.eclipse.sensinact.core.annotation.verb.UriParam.UriSegment;
import org.osgi.service.component.annotations.Component;

/**
 * Multiple providers from a single service, a single method for all resources
 */
@WhiteboardResource
@ProviderName({ "foo", "bar", "foobar" })
@Component(service = _05_MultiPullBasedResource.class)
public class _05_MultiPullBasedResource {

    @GET(service = "example", resource = "fizz")
    @GET(service = "example", resource = "buzz")
    @GET(service = "example2", resource = "fizzbuzz")
    public String getValue(@UriParam(UriSegment.PROVIDER) String provider, @UriParam(UriSegment.SERVICE) String service,
            @UriParam(UriSegment.RESOURCE) String resource) {
        // Get the actual value from the sensor
        return null;
    }
}
