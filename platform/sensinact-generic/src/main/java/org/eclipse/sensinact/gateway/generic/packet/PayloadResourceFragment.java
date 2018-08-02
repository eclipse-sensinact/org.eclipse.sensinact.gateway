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

import org.eclipse.sensinact.gateway.core.ResourceProcessableData;

/**
 * A semantic unit of a {@link Packet}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface PayloadResourceFragment extends ResourceProcessableData {
    /**
     * Returns this {@link PayloadResourceFragment} as a
     * {@link TaskIdValuePair} for the string taskIdentifier
     * passed as parameter. If the {@link Attribute}'s identifier
     * of this PayloadAttributeFragment is not null the returned
     * {@link TaskIdValuePair}  is
     *
     * @param taskIdentifier the string {@link Task}'s identifier for which to
     *                       return the {@link TaskIdValuePair} data structure
     * @return this {@link PayloadResourceFragment} as a
     * {@link TaskIdValuePair}
     */
    TaskIdValuePair AsTaskIdValuePair(String taskIdentifier);

    /**
     * Returns the task string identifier of the {@link
     * PayloadServiceFragment} holding this PayloadAttributeFragment,
     * completed by attribute and metadata identifiers if they
     * exist
     *
     * @return the completed task string identifier
     */
    String getTaskIdentifier();
}
