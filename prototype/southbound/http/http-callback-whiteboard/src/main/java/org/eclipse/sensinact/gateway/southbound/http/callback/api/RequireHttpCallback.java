/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.southbound.http.callback.api;

import static org.osgi.namespace.implementation.ImplementationNamespace.IMPLEMENTATION_NAMESPACE;

import org.osgi.annotation.bundle.Requirement;

@Requirement(namespace = IMPLEMENTATION_NAMESPACE, name = "sensinact.http.callback.whiteboard", version = "0.0.1")
public @interface RequireHttpCallback {

}
