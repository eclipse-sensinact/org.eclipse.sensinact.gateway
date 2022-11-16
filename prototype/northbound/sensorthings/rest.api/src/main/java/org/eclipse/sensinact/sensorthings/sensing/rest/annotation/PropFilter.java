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
package org.eclipse.sensinact.sensorthings.sensing.rest.annotation;

import jakarta.ws.rs.NameBinding;

/**
 * This annotation indicates that the resource method has a path parameter
 * named <code>prop</code> which should be used to restrict the serialized
 * output to contain only the named property
 */
@NameBinding
public @interface PropFilter {

}
