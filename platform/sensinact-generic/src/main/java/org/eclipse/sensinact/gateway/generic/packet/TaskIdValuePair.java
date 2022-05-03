/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
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
