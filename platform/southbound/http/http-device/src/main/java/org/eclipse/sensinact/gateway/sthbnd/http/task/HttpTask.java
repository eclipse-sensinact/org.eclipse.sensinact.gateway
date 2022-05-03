/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.task;

import org.eclipse.sensinact.gateway.generic.uri.URITask;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpConnectionConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.MappingDescription;

/**
 * Extended {@link URITask} dedicated to HTTP communication
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface HttpTask<RESPONSE extends HttpResponse, REQUEST extends Request<RESPONSE>> extends 
HttpConnectionConfiguration<RESPONSE, REQUEST>, URITask {

    HttpTask<RESPONSE, REQUEST> setPacketType(Class<? extends HttpPacket> packetType);
    
    HttpTask<RESPONSE, REQUEST> setDirect(boolean direct);

    HttpTask<RESPONSE, REQUEST> setMapping(MappingDescription[] mapping);
    
    MappingDescription[] getMapping();
    		
    REQUEST build();    
    
    
}
