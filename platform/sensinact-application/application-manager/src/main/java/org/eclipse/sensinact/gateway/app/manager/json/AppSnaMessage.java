/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.app.manager.json;

import org.eclipse.sensinact.gateway.api.message.ErrorMessageImpl;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * @author Remi Druilhe
 * @see AbstractSnaMessage
 */
public class AppSnaMessage extends ErrorMessageImpl {
    /**
     * Constructor of the AppSnaMessage
     *
     * @param uri     the URI of the service
     * @param type    the type of the message
     * @param message the string message
     * @see ErrorMessageImpl#SnaErrorMessageImpl(Mediator, String, Error, int)
     */
    public AppSnaMessage(Mediator mediator, String uri, Error type, String message) {
        super(mediator, uri, type);
        super.putValue("message", message);
    }
}
