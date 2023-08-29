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
package org.eclipse.sensinact.prototype.writable;

import org.eclipse.sensinact.core.annotation.propertytype.ProviderName;
import org.eclipse.sensinact.core.annotation.propertytype.WhiteboardResource;
import org.eclipse.sensinact.core.annotation.verb.SET;
import org.eclipse.sensinact.core.annotation.verb.UriParam;
import org.eclipse.sensinact.core.annotation.verb.UriParam.UriSegment;
import org.osgi.service.component.annotations.Component;

/**
 * This component provides different resource handlers for multiple resources.
 * Each handler can propagate a value assignment and return the real one.
 *
 * The handler is called when a SET verb is applied to the resource. It can
 * return a value that will be stored in sensiNact as the value to returned when
 * calling GET.
 */
@WhiteboardResource // Adds the property to be detected by sensiNact
@ProviderName({ "foo", "bar", "foobar" }) // Names of the providers those resources are provided by
@Component(service = _02_MultiWritableBasedResource.class) // The component must provide a service to be detected
public class _02_MultiWritableBasedResource {

    /**
     * A SET method for resource "fizz" of service "example". Note that the resource
     * type will be the return type of the method if not explicitly given. The
     * handler takes a single argument from the caller (the new value), but accepts
     * different URI parameters (see {@link UriParam}) from sensiNact.
     *
     * @param provider Provider name
     * @param value    New value to set
     */
    @SET(model = "model", service = "example", resource = "fizz")
    public String setFizz(@UriParam(UriSegment.PROVIDER) String provider, String value) {
        return "set:" + value;
    }

    /**
     * A SET method for resource "buzz" of service "example". Note that the resource
     * type will be the return type of the method if not explicitly given. The
     * handler takes a single argument from the caller (the new value), but accepts
     * different URI parameters (see {@link UriParam}) from sensiNact.
     *
     * @param provider Provider name
     * @param value    New value to set
     */
    @SET(model = "model", service = "example", resource = "buzz")
    public Long setBuzz(@UriParam(UriSegment.PROVIDER) String provider, Long value) {
        return value * 2;
    }

    /**
     * A SET method for resource "fizzbuzz" of service "example". Note that the
     * resource type will be the return type of the method if not explicitly given.
     * The handler takes a single argument from the caller (the new value), but
     * accepts different URI parameters (see {@link UriParam}) from sensiNact.
     *
     * @param provider Provider name
     * @param value    New value to set
     */
    @SET(model = "model", service = "example", resource = "fizzbuzz")
    public Double setFizzBuzz(@UriParam(UriSegment.PROVIDER) String provider, Double value) {
        return Math.max(value, 255d);
    }
}
