/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
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
