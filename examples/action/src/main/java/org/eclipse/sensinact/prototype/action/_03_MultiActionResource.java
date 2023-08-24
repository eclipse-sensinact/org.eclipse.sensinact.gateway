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
package org.eclipse.sensinact.prototype.action;

import org.eclipse.sensinact.core.annotation.propertytype.ProviderName;
import org.eclipse.sensinact.core.annotation.propertytype.WhiteboardResource;
import org.eclipse.sensinact.core.annotation.verb.ACT;
import org.eclipse.sensinact.core.annotation.verb.UriParam;
import org.eclipse.sensinact.core.annotation.verb.UriParam.UriSegment;
import org.osgi.service.component.annotations.Component;

/**
 * This sample component handles the ACT verb for multiple resources of multiple
 * providers, sharing a single method for all resources
 */
@WhiteboardResource // Adds the property to be detected by sensiNact
@ProviderName({ "foo", "bar", "foobar" }) // Names of the providers those resources are provided by
@Component(service = _03_MultiActionResource.class) // The component must provide a service to be detected
public class _03_MultiActionResource {

    /**
     * An ACT handler for multiple resources, from different services. The action
     * handler takes no argument from the caller, but accepts different URI
     * parameters (see {@link UriParam}) from sensiNact.
     *
     * @param provider Provider name
     * @param service  Service name
     * @param resource Resource name
     */
    @ACT(model = "testModel", service = "example", resource = "fizz")
    @ACT(model = "testModel", service = "example", resource = "buzz")
    @ACT(model = "testModel", service = "example2", resource = "fizzbuzz")
    public void actResource(@UriParam(UriSegment.PROVIDER) String provider,
            @UriParam(UriSegment.SERVICE) String service, @UriParam(UriSegment.RESOURCE) String resource) {
        // Do work based on resource information
    }
}
