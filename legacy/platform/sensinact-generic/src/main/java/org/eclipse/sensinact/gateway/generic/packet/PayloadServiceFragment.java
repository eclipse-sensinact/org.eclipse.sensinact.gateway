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

import org.eclipse.sensinact.gateway.core.ServiceProcessableData;
import org.eclipse.sensinact.gateway.generic.Task;

import java.util.List;

/**
 * A semantic unit of a {@link Packet}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface PayloadServiceFragment extends ServiceProcessableData<PayloadResourceFragment> {
    /**
     * Returns the {@link Task.CommandType} of the Task this
     * PayloadFragment responds to
     *
     * @return the {@link Task.CommandType} of the Task this
     * PayloadFragment responds to
     */
    Task.CommandType getCommand();

    /**
     * Returns this {@link PayloadServiceFragment} as a List of
     * {@link TaskIdValuePair}s
     *
     * @return this {@link PayloadServiceFragment} as a List of
     * {@link TaskIdValuePair}s
     * @link serviceProviderIdentifier
     * the string identifier of the {@link ServiceProvider}
     * targeted by the parent SubPacket of this PayloadFragment
     */
    List<TaskIdValuePair> AsTaskIdValuePair(String serviceProviderIdentifier);

    /**
     * Returns the number of {@link PayloadResourceFragment} of
     * this PayloadFragment
     *
     * @return the number of {@link PayloadResourceFragment} of
     * this PayloadFragment
     */
    int size();

    /**
     * This {@link PayloadServiceFragment} is informed that the {@link Task}
     * whose identifier is passed as parameter has been treated. The
     * associated {@link PayloadResourceFragment} is removed to avoid
     * a redundant treatment
     *
     * @param taskIdentifier the String identifier of the treated {@link Task}
     * @return <ul>
     * <li>
     * true if the associated {@link PayloadResourceFragment}
     * has been deleted
     * </li>
     * <li>
     * false if no associated {@link PayloadResourceFragment}
     * can be found
     * </li>
     * </ul>
     */
    boolean treated(String taskIdentifier);
}
