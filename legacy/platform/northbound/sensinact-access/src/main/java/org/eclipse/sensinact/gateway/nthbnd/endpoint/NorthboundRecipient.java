/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.Recipient;

/**
 * Abstract {@link Recipient} type implementation to be extended to
 * parameterize calls to subscribe access methods
 */
public abstract class NorthboundRecipient implements Recipient {
    /**
     * the {@link Mediator} allowing to interact with the OSGi
     * host environment
     */
    protected Mediator mediator;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} that will allow the
     *                 NorthboundRecipient to be instantiated to interact with the
     *                 OSGi host environment
     */
    public NorthboundRecipient(Mediator mediator) {
        super();
        this.mediator = mediator;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.primitive.JSONable#getJSON()
     */
    @Override
    public String getJSON() {
        return null;
    }
}
