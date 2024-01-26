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
package org.eclipse.sensinact.core.command.impl;

import java.util.Map;

import org.osgi.util.promise.Promise;

public interface ActionHandler {

    public Promise<Object> act(String modelPackageUri, String model, String provider, String service, String resource,
            Map<String, Object> parameters);
}
