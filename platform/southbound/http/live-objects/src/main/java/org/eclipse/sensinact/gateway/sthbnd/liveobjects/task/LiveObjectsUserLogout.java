/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.liveobjects.task;

import org.eclipse.sensinact.gateway.generic.TaskTranslator;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpConnectionConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpRequest;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTaskImpl;
import org.eclipse.sensinact.gateway.sthbnd.liveobjects.LiveObjectsConstant;
import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 * @author Rémi Druilhe
 */
public class LiveObjectsUserLogout extends HttpTaskImpl<SimpleHttpResponse, SimpleHttpRequest> {
    
	public LiveObjectsUserLogout(TaskTranslator transmitter) {
        super(null, transmitter, SimpleHttpRequest.class, UriUtils.ROOT, 
        		null, null, null);
    }

    @Override
    public String getUri() {
        return LiveObjectsConstant.ROOT_URL + "/logout?cookie=false";
    }

    @Override
    public String getHttpMethod() {
        return HttpConnectionConfiguration.POST;
    }
}
