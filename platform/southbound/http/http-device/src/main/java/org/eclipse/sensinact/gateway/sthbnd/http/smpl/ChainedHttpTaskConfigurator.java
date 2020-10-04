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
package org.eclipse.sensinact.gateway.sthbnd.http.smpl;

import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpChildTaskConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.HttpTaskConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
class ChainedHttpTaskConfigurator extends SimpleTaskConfigurator {

    /**
     * Data structure gathering an string identifier and
     * an HttpTaskConfigurator
     */
    //use to keep ordered HttpTaskConfigurator
    final class Link implements Nameable {
        public final String identifier;
        public final SimpleTaskConfigurator configurator;

        Link(String identifier, SimpleTaskConfigurator configurator) {
            this.identifier = identifier;
            this.configurator = configurator;
        }

        @Override
        public String getName() {
            return this.identifier;
        }

    }

    private Deque<Link> chain;

    /**
     * Constructor
     *
     * @param mediator   the {@link Mediator} allowing to interact
     *                   with the OSGi host environment
     * @param profile    the String profile identifier to apply to
     *                   {@link Task}s to be configured by the ChainedTaskconfigurationExecutor
     *                   to be instantiated
     * @param command    the {@link CommandType} of the {@link Task}s that the
     *                   ChainedTaskconfigurationExecutor to be instantiated can configure
     * @param annotation the parent {@link HttpTaskConfiguration}
     * @param chain      the chain of {@link HttpChildTaskConfiguration} applying on
     *                   {@link HttpChainedTask}s to be configured
     */
    public ChainedHttpTaskConfigurator(SimpleHttpProtocolStackEndpoint endpoint, String profile, CommandType command, HttpTaskUrlConfigurator urlBuilder, HttpTaskConfiguration annotation, HttpChildTaskConfiguration[] chain) {
        super(endpoint, null, command, urlBuilder, annotation);
        this.chain = new LinkedList<Link>();
        int index = 0;
        int length = chain == null ? 0 : chain.length;
        for (; index < length; index++) {
            SimpleTaskConfigurator executable = new SimpleTaskConfigurator(endpoint, profile, command, urlBuilder, annotation, chain[index]);
            this.chain.addLast(new Link(chain[index].identifier(), executable));
        }
    }

    /**
     * @inheritDoc
     * @see SimpleTaskConfigurator#
     * configure(HttpTask, java.lang.Object[])
     */
    @Override
    public <T extends HttpTask<?, ?>> void configure(T task) throws Exception {
        if (!HttpChainedTasks.class.isAssignableFrom(task.getClass())) {
            super.configure(task);
            return;
        }
        try {
            HttpChainedTasks chained = (HttpChainedTasks) task;

            Iterator<Link> iterator = this.chain.iterator();

            while (iterator.hasNext()) {
                Link link = iterator.next();
                @SuppressWarnings("rawtypes")
				HttpChainedTask subTask = chained.addChainedTask(link.identifier);

                HttpTaskProcessingContext context = super.endpoint.createChainedContext(link.configurator, chained, subTask);

                if (context != null) {
                    super.endpoint.getMediator().registerProcessingContext(subTask, context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}