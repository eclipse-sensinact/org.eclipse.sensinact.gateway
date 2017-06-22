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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.AbstractSnaMessage;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl;

/**
 * @see AbstractSnaMessage
 *
 * @author Remi Druilhe
 */
public class AppSnaMessage extends SnaErrorMessageImpl
{
    /**
     * Constructor of the AppSnaMessage
     * @see SnaErrorMessageImpl#SnaErrorMessageImpl(Mediator, String, Error, int)
     * @param uri the URI of the service
     * @param type the type of the message
     * @param message the string message
     */
    public AppSnaMessage(Mediator mediator, String uri, Error type, String message)
    {
        super(mediator, uri, type);
        super.putValue("message", message);
    }
}
