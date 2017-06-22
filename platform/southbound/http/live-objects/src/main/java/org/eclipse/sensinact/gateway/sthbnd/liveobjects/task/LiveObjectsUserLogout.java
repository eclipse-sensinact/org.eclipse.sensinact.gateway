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
package org.eclipse.sensinact.gateway.sthbnd.liveobjects.task;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.TaskTranslator;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpConnectionConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpRequest;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.eclipse.sensinact.gateway.sthbnd.liveobjects.LiveObjectsConstant;
import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 * @author Rémi Druilhe
 */
public class LiveObjectsUserLogout extends HttpTask<SimpleHttpResponse,SimpleHttpRequest>
{
    public LiveObjectsUserLogout(Mediator mediator,  TaskTranslator transmitter)
    {
        super(mediator, null, transmitter, SimpleHttpRequest.class, 
        		UriUtils.ROOT, null, null, null);
    }

    @Override
    public String getUri()
    {
        return LiveObjectsConstant.ROOT_URL + "/logout?cookie=false";
    }

    @Override
    public String getHttpMethod()
    {
        return HttpConnectionConfiguration.POST;
    }
}
