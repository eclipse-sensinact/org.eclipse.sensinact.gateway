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
package org.eclipse.sensinact.gateway.sthbnd.http.task;

import org.eclipse.sensinact.gateway.generic.uri.URITask;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpConnectionConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;

/**
 * Extended {@link URITask} dedicated to HTTP communication
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface HttpTask<RESPONSE extends HttpResponse, REQUEST extends Request<RESPONSE>> extends 
HttpConnectionConfiguration<RESPONSE, REQUEST>, URITask {

    HttpTask<RESPONSE, REQUEST> setPacketType(Class<? extends HttpPacket> packetType);

    
    HttpTask<RESPONSE, REQUEST> setDirect(boolean direct);

    REQUEST build();    
}
