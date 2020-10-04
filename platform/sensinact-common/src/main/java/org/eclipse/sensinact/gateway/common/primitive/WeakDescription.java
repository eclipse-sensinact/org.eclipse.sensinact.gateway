/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.common.primitive;

import java.lang.ref.WeakReference;

/**
 * An extended {@link WeakReference} referencing
 * a {@link PrimitiveDescription}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class WeakDescription<P extends PrimitiveDescription> extends WeakReference<P> {
    /**
     * Constructor
     *
     * @param referent the {@link PrimitiveDescription} object the WeakReference
     *                 to instantiate refers to
     */
    public WeakDescription(P referent) {
        super(referent);
    }

    /**
     * This method is called whenever the described
     * {@link Primitive} value is changed.
     *
     * @param updated the updated value of the
     *                described {@link Primitive}
     */
    public void update(Object updated) {
        P description = null;
        if ((description = super.get()) == null) {
            return;
        }
        description.update(updated);
    }
}
