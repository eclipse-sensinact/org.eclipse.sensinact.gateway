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
package org.eclipse.sensinact.gateway.common.automata;

/**
 * Extended {@link PacketFrame} defining a generic communication packet
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class PacketFrame extends AbstractFrame {
    /**
     * Constructor
     */
    public PacketFrame() {
        super();
    }

    /**
     * @inheritDoc
     * @see AbstractFrame#isComplete()
     */
    @Override
    public boolean isComplete() {
        return true;
    }
}
