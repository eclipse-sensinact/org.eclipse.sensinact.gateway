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

import org.eclipse.sensinact.gateway.generic.TaskManager;

/**
 * Basis implementation of a {@link PayloadResourceFragment}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class PayloadResourceFragmentImpl implements PayloadResourceFragment {
    private String taskIdentifier;
    protected final String attributeId;
    protected final String metadataId;
    protected final Object data;
    protected long timestamp;

    /**
     * Constructor
     *
     * @param data the object data
     */
    public PayloadResourceFragmentImpl(Object data) {
        this(null, null, data);
    }

    /**
     * Constructor
     *
     * @param attributeId the string identifier of the targeted {@link Attribute}
     * @param data        the object data
     */
    public PayloadResourceFragmentImpl(String attributeId, String metadataId, Object data) {
        this.attributeId = attributeId;
        this.metadataId = metadataId;
        this.data = data;
        this.timestamp = -1;
    }

    /**
     * @inheritDoc
     * @see PayloadResourceFragment#getAttributeId()
     */
    @Override
    public String getAttributeId() {
        return this.attributeId;
    }

    /**
     * @inheritDoc
     * @see PayloadResourceFragment#getMetadataId()
     */
    @Override
    public String getMetadataId() {
        return this.metadataId;
    }

    /**
     * @inheritDoc
     * @see PayloadResourceFragment#getData()
     */
    @Override
    public Object getData() {
        return this.data;
    }

    /**
     * @inheritDoc
     * @see PayloadResourceFragment#
     * AsTaskIdValuePair()
     */
    @Override
    public TaskIdValuePair AsTaskIdValuePair(String taskIdentifier) {
        if (taskIdentifier == null) {
            return null;
        }
        this.taskIdentifier = taskIdentifier;

        if (this.attributeId != null) {
            this.taskIdentifier = new StringBuilder().append(taskIdentifier).append(TaskManager.IDENTIFIER_SEP_CHAR).append(this.attributeId).toString();

            if (this.metadataId != null) {
                this.taskIdentifier = new StringBuilder().append(this.taskIdentifier).append(TaskManager.IDENTIFIER_SEP_CHAR).append(this.metadataId).toString();
            }
        }
        return new TaskIdValuePair(this.taskIdentifier, data, this.getTimestamp());
    }

    /**
     * Defines the timestamp of the update of the
     * targeted Attribute
     *
     * @param the update timestamp
     */
    public long setTimestamp(long timestamp) {
        return this.timestamp = timestamp;
    }

    /**
     * @inheritDoc
     * @see PayloadResourceFragment#getTimestamp()
     */
    @Override
    public long getTimestamp() {
        if (timestamp == -1) {
            return System.currentTimeMillis();
        }
        return this.timestamp;
    }

    /**
     * @InheritedDoc
     * @see PayloadResourceFragment#
     * getTaskIdentifier()
     */
    @Override
    public String getTaskIdentifier() {
        return this.taskIdentifier;
    }

    /**
     * @inheritDoc
     * @see Nameable#getName()
     */
    @Override
    public String getName() {
        return this.taskIdentifier;
    }
}
