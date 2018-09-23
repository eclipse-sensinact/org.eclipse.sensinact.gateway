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
package org.eclipse.sensinact.gateway.sthbnd.http.smpl;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.InvalidProtocolStackException;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.ChainedHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.ChainedHttpTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.RecurrentChainedHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.RecurrentHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.SimpleHttpTask;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.osgi.framework.BundleContext;

import java.util.Collections;
import java.util.Map;

/**
 * Extended {@link AbstractActivator} dedicated to the automatic
 * configuration of a {@link SimpleHttpProtocolStackEndpoint}.
 * <p>
 * To allow this activator doing the {@link SimpleHttpProtocolStackEndpoint}
 * configuration, use the dedicated annotations to define how to handle HTTP
 * request to be built
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class HttpActivator extends AbstractActivator<HttpMediator> {
    /**
     *
     */
    protected SimpleHttpProtocolStackEndpoint endpoint;

    /**
     * @inheritDoc
     * @see AbstractActivator#doStart()
     */
    @Override
    public void doStart() throws Exception {
        super.mediator.setTaskProcessingContextHandler(this.getProcessingContextHandler());
        this.mediator.setTaskProcessingContextFactory(this.getTaskProcessingContextFactory());
        this.mediator.setChainedTaskProcessingContextFactory(this.getChainedTaskProcessingContextFactory());

        ExtModelConfiguration<? extends HttpPacket> configuration = 
        	ExtModelConfigurationBuilder.instance(
        		mediator, getPacketType()
        		).withStartAtInitializationTime(isStartingAtInitializationTime()
        		).withServiceBuildPolicy(getServiceBuildPolicy()
        		).withResourceBuildPolicy(getResourceBuildPolicy()
        		).build(getResourceDescriptionFile(), getDefaults());

        this.endpoint = this.configureProtocolStackEndpoint();
        this.connect(configuration);
    }

    /**
     * @param configuration
     * @throws InvalidProtocolStackException
     */
    protected void connect(ExtModelConfiguration configuration) throws InvalidProtocolStackException {
        this.endpoint.connect(configuration);
    }

    /**
     * @inheritDoc
     * @see AbstractActivator#doStop()
     */
    @Override
    public void doStop() throws Exception {
        this.endpoint.stop();
    }

    /**
     * @return
     */
    protected Map<String, String> getDefaults() {
        return Collections.<String, String>emptyMap();
    }

    /**
     * @return
     */
    protected boolean isStartingAtInitializationTime() {
        return true;
    }

    /**
     * @return
     */
    protected byte getResourceBuildPolicy() {
        return BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy();
    }

    /**
     * @return
     */
    protected byte getServiceBuildPolicy() {
        return (byte) (BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy() | BuildPolicy.BUILD_APPEARING_ON_DESCRIPTION.getPolicy());
    }

    /**
     * Returns extended type of {@link HttpPacket} handled by the {@link
     * SimpleHttpProtocolStackEndpoint} to be instantiated by this Activator
     *
     * @return the type of handled {@link HttpPacket}
     */
    protected Class<? extends HttpPacket> getPacketType() {
        return HttpPacket.class;
    }

    /**
     * Returns the type of {@link SimpleHttpProtocolStackEndpoint} to
     * be instantiated by this Activator
     *
     * @return the type of {@link SimpleHttpProtocolStackEndpoint} to
     * be instantiated
     */
    protected Class<? extends SimpleHttpProtocolStackEndpoint> getEndpointType() {
        return SimpleHttpProtocolStackEndpoint.class;
    }

    /**
     * Returns the {@link HttpTaskProcessingContextHandler} to be used by the {@link
     * HttpMediator} created by this Activator
     *
     * @return the {@link HttpTaskProcessingContextHandler} to be used
     */
    protected HttpTaskProcessingContextHandler getProcessingContextHandler() {
        return new DefaultHttpTaskProcessingContextHandler();
    }

    /**
     * Returns the {@link HttpTaskProcessingContextFactory} to be used to
     * create {@link HttpTaskProcessingContext}
     *
     * @return the {@link HttpTaskProcessingContextFactory} to be used
     */
    public HttpTaskProcessingContextFactory getTaskProcessingContextFactory() {
        return new DefaultHttpTaskProcessingContextFactory(mediator);
    }

    /**
     * Returns the {@link HttpChainedTaskProcessingContextFactory} to be used to
     * create {@link HttpTaskProcessingContext} dedicated to chained tasks
     *
     * @return the {@link HttpChainedTaskProcessingContextFactory} to be used
     */
    public HttpChainedTaskProcessingContextFactory getChainedTaskProcessingContextFactory() {
        return new DefaultHttpChainedTaskProcessingContextFactory(mediator);
    }

    /**
     * Return the relative path of the xml file describing the resources
     * provided by this bridge
     *
     * @return the relative path of the xml description file
     */
    protected String getResourceDescriptionFile() {
        return "resources.xml";
    }

    /**
     * @inheritDoc
     * @see AbstractActivator#doInstantiate(org.osgi.framework.BundleContext)
     */
    @Override
    public HttpMediator doInstantiate(BundleContext context) {
        return new HttpMediator(context);
    }

    /**
     * Instantiates and returns a new {@link SimpleHttpProtocolStackEndpoint}
     *
     * @returns a newly created {@link SimpleHttpProtocolStackEndpoint}
     */
    public SimpleHttpProtocolStackEndpoint configureProtocolStackEndpoint() throws Exception {
        SimpleHttpProtocolStackEndpoint endpoint = ReflectUtils.getInstance(getEndpointType(), new Object[]{mediator});

        HttpTasks taskArray = this.getClass().getAnnotation(HttpTasks.class);
        SimpleHttpTask[] tasks = taskArray == null ? null : taskArray.tasks();
        int index = 0;
        int length = tasks == null ? 0 : tasks.length;
        for (; index < length; index++) {
            endpoint.registerAdapter(tasks[index]);
        }
        RecurrentHttpTask[] recurrences = taskArray == null ? null : taskArray.recurrences();
        index = 0;
        length = recurrences == null ? 0 : recurrences.length;
        for (; index < length; index++) {
            endpoint.registerAdapter(recurrences[index]);
        }
        ChainedHttpTasks chainedTaskArray = this.getClass().getAnnotation(ChainedHttpTasks.class);
        ChainedHttpTask[] chainedTasks = chainedTaskArray == null ? null : chainedTaskArray.tasks();
        index = 0;
        length = chainedTasks == null ? 0 : chainedTasks.length;
        for (; index < length; index++) {
            endpoint.registerAdapter(chainedTasks[index]);
        }
        RecurrentChainedHttpTask[] recurrentChainedTasks = chainedTaskArray == null ? null : chainedTaskArray.recurrences();
        index = 0;
        length = recurrentChainedTasks == null ? 0 : recurrentChainedTasks.length;
        for (; index < length; index++) {
            endpoint.registerAdapter(recurrentChainedTasks[index]);
        }
        return endpoint;
    }
}
