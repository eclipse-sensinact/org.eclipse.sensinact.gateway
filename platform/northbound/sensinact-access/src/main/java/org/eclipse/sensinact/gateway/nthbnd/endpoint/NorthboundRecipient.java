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
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import org.eclipse.sensinact.gateway.api.message.Recipient;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;

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
