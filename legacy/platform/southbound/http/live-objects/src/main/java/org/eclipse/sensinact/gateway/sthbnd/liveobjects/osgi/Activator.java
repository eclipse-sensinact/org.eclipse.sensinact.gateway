/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.liveobjects.osgi;

import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.InvalidProtocolStackException;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTaskConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.KeyValuePair;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.SimpleHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpActivator;
import org.eclipse.sensinact.gateway.sthbnd.liveobjects.task.LiveObjectsGetAssetsList;
import org.eclipse.sensinact.gateway.sthbnd.liveobjects.task.LiveObjectsUserAuthentication;
import org.eclipse.sensinact.gateway.sthbnd.liveobjects.task.LiveObjectsUserLogout;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.Constants;

/**
 * Activator for the Orange LiveObjects bridge
 *
 * @inheritDoc AbstractActivator
 */
@HttpTasks(tasks = {@SimpleHttpTask(commands = {CommandType.GET}, configuration = @HttpTaskConfiguration(acceptType = "application/json", contentType = "application/json", scheme = "https", host = "liveobjects.orange-business.com", path = "/api/v0/data/streams/urn:lo:nsid:@context[task.serviceProvider]", query = {@KeyValuePair(key = "limit", value = "1")}))})
@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends HttpActivator {
    /**
     * @inheritDoc
     * @see HttpActivator#
     * connect(ExtModelConfiguration)
     */
    @Override
    protected void connect(ExtModelConfiguration configuration) throws InvalidProtocolStackException {
        super.endpoint.registerDiscoveryTask(new LiveObjectsUserAuthentication(super.endpoint, (String) mediator.getProperty("sensinact.mail"), (String) mediator.getProperty("sensinact.user"), (String) mediator.getProperty("sensinact.password")));

        super.endpoint.registerDiscoveryTask(new LiveObjectsGetAssetsList(super.endpoint));

        super.endpoint.registerDisconnexionTask(new LiveObjectsUserLogout(super.endpoint));

        super.connect(configuration);
    }
}
