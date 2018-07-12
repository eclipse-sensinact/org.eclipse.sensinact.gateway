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
package org.eclipse.sensinact.gateway.sthbnd.http;

import org.eclipse.sensinact.gateway.common.primitive.PathElement;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfiguration;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;

/**
 * @param <RESPONSE>
 * @param <REQUEST>
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface HttpConnectionConfiguration<RESPONSE extends HttpResponse, REQUEST extends Request<RESPONSE>> extends ConnectionConfiguration<RESPONSE, REQUEST>, PathElement {
    /**
     * If defined as direct, the content of an {@link HttpResponse} responding
     * to an {@link HttpRequest} based on this HttpRequestConfiguration
     * is put as its result, otherwise the response is parsed using the
     * dedicated {@link PacketReader}. An HttRequestConfiguration is defined
     * as indirect by default
     *
     * @return <ul>
     * <li>true id the configuration is direct</li>
     * <li>false otherwise</li>
     * </ul>
     */
    boolean isDirect();

    /**
     * The extended {@link HttpPacket} type associated to this
     * connection configuration
     *
     * @return this connection configuration's {@link HttpPacket}
     * type
     */
    Class<? extends HttpPacket> getPacketType();

    /**
     * The access method's {@link CommandType} at the origin
     * of this connection configuration
     *
     * @return the initial access method's {@link CommandType}
     */
    CommandType getCommand();

}
