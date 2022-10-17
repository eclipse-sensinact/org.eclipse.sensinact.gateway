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
