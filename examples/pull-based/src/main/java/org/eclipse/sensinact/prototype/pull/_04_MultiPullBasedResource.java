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
import org.eclipse.sensinact.core.command.GetLevel;
import org.osgi.service.component.annotations.Component;

/**
 * This component defines multiple resources for a single service and different
 * providers. Each resource is handled by a specific method.
 *
 * Note that a resource handler is called if:
 * <ul>
 * <li>... the resource doesn't have a value yet</li>
 * <li>... if the value request is a strong GET (see
 * {@link GetLevel#STRONG})</li>
 * <li>... if the value request is a normal (default) GET (see
 * {@link GetLevel#NORMAL}) and if the data cache period has expired</li>
 * </ul>
 *
 * The handler is never called if the value request is a weak GET (see
 * {@link GetLevel#WEAK}).
 */
@WhiteboardResource // Adds the property to be detected by sensiNact
@ProviderName({ "foo", "bar", "foobar" }) // Names of the providers those resources are provided by
@Component(service = _04_MultiPullBasedResource.class) // The component must provide a service to be detected
public class _04_MultiPullBasedResource {

    /**
     * A GET handler for service "example" and resource "fizz". This handler accepts
     * the name of the provider of the called resource as URI parameter (see
     * {@link UriParam}) from sensiNact.
     *
     * @param provider Provider name
     */
    @GET(service = "example", resource = "fizz")
    public String getFizz(@UriParam(UriSegment.PROVIDER) String provider) {
        return "fizz";
    }

    /**
     * A GET handler for service "example" and resource "buzz". This handler accepts
     * the name of the provider of the called resource as URI parameter (see
     * {@link UriParam}) from sensiNact.
     *
     * @param provider Provider name
     */
    @GET(service = "example", resource = "buzz")
    public String getBuzz(@UriParam(UriSegment.PROVIDER) String provider) {
        return "buzz";
    }

    /**
     * A GET handler for service "example" and resource "fizzbuzz". This handler
     * accepts the name of the provider of the called resource as URI parameter (see
     * {@link UriParam}) from sensiNact.
     *
     * @param provider Provider name
     */
    @GET(service = "example", resource = "fizzbuzz")
    public String getFizzBuzz(@UriParam(UriSegment.PROVIDER) String provider) {
        return "fizzbuzz";
    }
}
