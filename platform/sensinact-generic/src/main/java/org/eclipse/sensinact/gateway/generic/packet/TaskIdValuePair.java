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
package org.eclipse.sensinact.gateway.generic.packet;

import org.eclipse.sensinact.gateway.common.primitive.Nameable;

/**
 * Data structure gathering the identifier of a {@link Task}
 * and its result value Object
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class TaskIdValuePair implements Nameable {
    public final String taskIdentifier;
    public final Object value;
    public final long timestamp;

    /**
     * Constructor
     *
     * @param taskIdentifier the identifier of a {@link Task}
     * @param value          result value Object of the identified {@link Task}
     */
    TaskIdValuePair(String taskIdentifier, Object value, long timestamp) {
        this.taskIdentifier = taskIdentifier;
        this.timestamp = timestamp;
        this.value = value;
    }

    /**
     * @inheritDoc
     * @see Nameable#getName()
     */
    @Override
    public String getName() {
        return this.taskIdentifier;
    }

    /**
     * @return
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * @return
     */
    public long getTimestamp() {
        return this.timestamp;
    }
}
