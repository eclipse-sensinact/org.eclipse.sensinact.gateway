/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.json;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.AbstractSnaMessage;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl;

/**
 * @author Remi Druilhe
 * @see AbstractSnaMessage
 */
public class AppSnaMessage extends SnaErrorMessageImpl {
    /**
     * Constructor of the AppSnaMessage
     *
     * @param uri     the URI of the service
     * @param type    the type of the message
     * @param message the string message
     * @see SnaErrorMessageImpl#SnaErrorMessageImpl(Mediator, String, Error, int)
     */
    public AppSnaMessage(String uri, Error type, String message) {
        super(uri, type);
        super.putValue("message", message);
    }
}
