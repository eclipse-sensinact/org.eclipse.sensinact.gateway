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
package org.eclipse.sensinact.gateway.commands.gogo.internal.shell;

import org.eclipse.sensinact.gateway.api.message.SnaMessage;
import org.eclipse.sensinact.gateway.commands.gogo.osgi.CommandServiceMediator;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRecipient;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.json.JSONObject;

/**
 * {@link NorthboundRecipient} dedicated to subscribe access method
 * using the shell
 */
public class ShellRecipient extends NorthboundRecipient {
    /**
     * Constructor
     *
     * @param mediator the {@link CommandServiceMediator} allowing
     *                 the ShellRecipient to be instantiated to interact with the
     *                 OSGi host environment
     */
    public ShellRecipient(CommandServiceMediator mediator) {
        super(mediator);
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.api.message.Recipient#
     * callback(java.lang.String, org.eclipse.sensinact.gateway.api.message.SnaMessage[])
     */
    public void callback(String callbackId, SnaMessage[] messages) {
        int index = 0;
        int length = messages == null ? 0 : messages.length;

        StringBuilder builder = new StringBuilder();
        builder.append(JSONUtils.OPEN_BRACE);
        builder.append("\"callbackId\" : \"");
        builder.append(callbackId);
        builder.append("\",\"messages\" :");
        builder.append(JSONUtils.OPEN_BRACKET);
        for (; index < length; index++) {
            builder.append(index == 0 ? "" : ",");
            builder.append(messages[index].getJSON());
        }
        builder.append(JSONUtils.CLOSE_BRACKET);
        builder.append(JSONUtils.CLOSE_BRACE);

        ((CommandServiceMediator) super.mediator).getOutput().outputUnderlined("Callback", 2);

        ((CommandServiceMediator) super.mediator).getOutput().output(new JSONObject(builder.toString()), 2);
    }
}
