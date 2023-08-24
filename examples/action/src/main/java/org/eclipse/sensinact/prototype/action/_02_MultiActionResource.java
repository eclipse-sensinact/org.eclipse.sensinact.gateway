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
 * providers, with one method per resource
 */
@WhiteboardResource // Adds the property to be detected by sensiNact
@ProviderName({ "foo", "bar", "foobar" }) // Names of the providers those resources are provided by
@Component(service = _02_MultiActionResource.class) // The component must provide a service to be detected
public class _02_MultiActionResource {

    /**
     * An ACT handler for service "example" and resource "fizz". This action takes
     * takes no argument from the caller, but accepts the name of the provider of
     * the called resource as URI parameter (see {@link UriParam}) from sensiNact.
     *
     * @param provider Provider name
     */
    @ACT(model = "testModel", service = "example", resource = "fizz")
    public void actFizz(@UriParam(UriSegment.PROVIDER) String provider) {
    }

    /**
     * An ACT handler for service "example" and resource "buzz". This action takes
     * takes no argument from the caller, but accepts the name of the provider of
     * the called resource as URI parameter (see {@link UriParam}) from sensiNact.
     *
     * @param provider Provider name
     */
    @ACT(model = "testModel", service = "example", resource = "buzz")
    public void actBuzz(@UriParam(UriSegment.PROVIDER) String provider) {
    }

    /**
     * An ACT handler for service "example" and resource "fizzbuzz". This action
     * takes no argument from the caller, but accepts the name of the provider of
     * the called resource as URI parameter (see {@link UriParam}) from sensiNact.
     *
     * @param provider Provider name
     */
    @ACT(model = "testModel", service = "example", resource = "fizzbuzz")
    public void actFizzBuzz(@UriParam(UriSegment.PROVIDER) String provider) {
    }
}
