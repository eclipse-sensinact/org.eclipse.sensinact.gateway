/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.onem2m.osgi;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.InvalidProtocolStackException;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTaskConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.KeyValuePair;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.SimpleHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.onem2m.internal.OneM2MHttpGetConfigurator;
import org.eclipse.sensinact.gateway.sthbnd.http.onem2m.task.OneM2MDiscoveryTask;
import org.eclipse.sensinact.gateway.sthbnd.http.onem2m.task.OneM2MGetTask;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpActivator;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.Constants;

@HttpTasks(tasks = {@SimpleHttpTask(commands = {Task.CommandType.GET}, configuration = @HttpTaskConfiguration(host = "$(http.onem2m.host)", port = "$(http.onem2m.port)", path = "/$(http.onem2m.cse.base)", acceptType = "application/json", contentType = "application/json", headers = {@KeyValuePair(key = "X-M2M-Origin", value = "SOrigin")}, content = OneM2MHttpGetConfigurator.class, direct = true))})
@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends HttpActivator {
    /**
     * @inheritDoc
     * @see AbstractActivator#doStart()
     */
    @Override
    public void doStart() throws Exception {
        super.mediator.setTaskProcessingContextHandler(this.getProcessingContextHandler());
        this.mediator.setTaskProcessingContextFactory(this.getTaskProcessingContextFactory());
        this.mediator.setChainedTaskProcessingContextFactory(this.getChainedTaskProcessingContextFactory());
        ExtModelConfiguration<? extends HttpPacket> configuration = ExtModelConfigurationBuilder.instance(mediator, getPacketType()
        ).withStartAtInitializationTime(isStartingAtInitializationTime()
        ).withServiceBuildPolicy(BuildPolicy.BUILD_NON_DESCRIBED.getPolicy()
        ).withResourceBuildPolicy(BuildPolicy.BUILD_NON_DESCRIBED.getPolicy()
        ).withDesynchronization(true
        ).build(getResourceDescriptionFile(), getDefaults());
        this.endpoint = this.configureProtocolStackEndpoint();
        this.connect(configuration);
    }

    /**
     * @inheritDoc
     * @see HttpActivator#connect(ExtModelConfiguration)
     */
    @Override
    public void connect(ExtModelConfiguration configuration) throws InvalidProtocolStackException {
        super.endpoint.registerDiscoveryTask(new OneM2MDiscoveryTask(mediator, endpoint));
        super.endpoint.setGetTaskType(OneM2MGetTask.class);
        super.connect(configuration);
    }
}
