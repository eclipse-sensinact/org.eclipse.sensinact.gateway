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
package org.eclipse.sensinact.prototype.model;

import java.util.Map;

import org.eclipse.sensinact.prototype.command.CommandScoped;

/**
 * A model for a Provider
 */
public interface Model extends Modelled, CommandScoped {

    ServiceBuilder<Service> createService(String service);

    Map<String, ? extends Service> getServices();

}
