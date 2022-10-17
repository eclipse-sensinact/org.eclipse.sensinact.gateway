/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.commands.gogo.internal.shell;

import java.io.StringReader;

import org.eclipse.sensinact.gateway.commands.gogo.internal.CommandServiceMediator;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRecipient;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.spi.JsonProvider;

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
     * @see org.eclipse.sensinact.gateway.core.message.Recipient#
     * callback(java.lang.String, org.eclipse.sensinact.gateway.core.message.SnaMessage[])
     */
    public void callback(String callbackId, SnaMessage<?>[] messages) {
        JsonProvider provider = JsonProviderFactory.getProvider();
        
        JsonArrayBuilder jab = provider.createArrayBuilder();
        if(messages != null) {
        	for (SnaMessage<?> m : messages) {
        		jab.add(provider.createReader(new StringReader(m.getJSON())).readObject());
        	}
        }
        
        JsonObject jo = provider.createObjectBuilder()
        		.add("callbackId", callbackId)
        		.add("messages", jab)
        		.build();

        ((CommandServiceMediator) super.mediator).getOutput().outputUnderlined("Callback", 2);

        ((CommandServiceMediator) super.mediator).getOutput().output(jo, 2);
    }
}
