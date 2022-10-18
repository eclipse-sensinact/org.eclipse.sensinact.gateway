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

import org.eclipse.sensinact.prototype.annotation.propertytype.ProviderName;
import org.eclipse.sensinact.prototype.annotation.propertytype.WhiteboardResource;
import org.eclipse.sensinact.prototype.annotation.verb.ACT;
import org.eclipse.sensinact.prototype.annotation.verb.UriParam;
import org.eclipse.sensinact.prototype.annotation.verb.UriParam.UriSegment;
import org.osgi.service.component.annotations.Component;

/**
 * Multiple providers from a single service, different methods for each resource
 */
@WhiteboardResource
@ProviderName({ "foo", "bar", "foobar" })
@Component(service = _02_MultiActionResource.class)
public class _02_MultiActionResource {

    @ACT(service = "example", resource = "fizz")
    public void setFizz(@UriParam(UriSegment.PROVIDER) String provider) {
    }

    @ACT(service = "example", resource = "buzz")
    public void setBuzz(@UriParam(UriSegment.PROVIDER) String provider) {
    }

    @ACT(service = "example", resource = "fizzbuzz")
    public void setFizzBuzz(@UriParam(UriSegment.PROVIDER) String provider) {
    }
}
