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

import org.eclipse.sensinact.gateway.core.ServiceProviderProcessableContainer;

import java.util.Iterator;

/**
 * A PacketReader provides access to the semantic units of a {@link
 * Packet} reified as {@link PayloadFragment}s. Its also iterable over all
 * {@link TaskIdValuePair}s that can be build by parsing the treated
 * packet
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface PacketReader<P extends Packet> extends ServiceProviderProcessableContainer<PayloadFragment> {
    /**
     * Parses the {@link Packet} passed as parameter
     *
     * @param packet the {@link Packet} to parse
     * @throws InvalidPacketException
     */
    void parse(P packet) throws InvalidPacketException;

    /**
     * Returns an {@link Iterator} over identified {@link
     * TaskIdValuePair}s in the entire parsed {@link Packet}
     *
     * @return an {@link Iterator} over {@link TaskIdValuePair}s
     * of the entire parsed {@link Packet}
     */
    Iterator<TaskIdValuePair> getTaskIdValuePairs();

    /**
     * This {@link PacketReader} is informed that the {@link Task}
     * whose identifier is passed as parameter has been treated. The
     * associated {@link PayloadResourceFragment} is removed from its
     * {@link PayloadServiceFragment} holder, which belongs to a {@link PayloadFragment}
     * of the parsed {@link Packet}, to avoid a redundant treatment
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
    void treated(String taskIdentifier);

    /**
     * Clears all previously defined field
     */
    void reset();

}
