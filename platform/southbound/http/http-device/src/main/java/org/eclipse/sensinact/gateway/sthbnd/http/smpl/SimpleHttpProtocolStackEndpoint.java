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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.InvalidProtocolStackException;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.TaskTranslator;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpRequest;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.ChainedHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.RecurrentChainedHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.RecurrentHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.annotation.SimpleHttpTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SimpleHttpProtocolStackEndpoint extends HttpProtocolStackEndpoint {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    public static final Class<? extends HttpTask> GET_TASK = HttpTask.class;
    public static final Class<? extends HttpTask> SET_TASK = HttpTask.class;
    public static final Class<? extends HttpTask> ACT_TASK = HttpTask.class;
    public static final Class<? extends HttpTask> SUBSCRIBE_TASK = HttpTask.class;
    public static final Class<? extends HttpTask> UNSUBSCRIBE_TASK = HttpTask.class;
    public static final Class<? extends HttpTask> SERVICES_ENUMERATION_TASK = HttpTask.class;

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    private String endpointId;
    private Class<? extends HttpTask> getTaskClass = null;
    private Class<? extends HttpTask> setTaskClass = null;
    private Class<? extends HttpTask> actTaskClass = null;
    private Class<? extends HttpTask> subscribeTaskClass = null;
    private Class<? extends HttpTask> unsubscribeTaskClass = null;
    private Class<? extends HttpTask> servicesEnumerationTaskClass = null;
    protected Timer timer;
    protected Deque<RecurrentHttpTaskConfigurator> recurrences;
    protected Map<CommandType, HttpTaskBuilder> adapters;
    protected Map<CommandType, HttpTaskUrlConfigurator> builders;

    /**
     * @param mediator
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public SimpleHttpProtocolStackEndpoint(HttpMediator mediator) throws ParserConfigurationException, SAXException, IOException {
        super(mediator);
        this.recurrences = new LinkedList<RecurrentHttpTaskConfigurator>();
        this.adapters = new HashMap<CommandType, HttpTaskBuilder>();
        this.builders = new HashMap<CommandType, HttpTaskUrlConfigurator>();

        //Mediator classloader because we don't need to retrieve
        //all declared factories in the OSGi environment, but only
        //the one specified in the bundle instantiating this
        //SimpleHttpProtocolStackEndpoint
        ServiceLoader<HttpTaskUrlConfigurator> loader = ServiceLoader.load(HttpTaskUrlConfigurator.class, mediator.getClassLoader());

        Iterator<HttpTaskUrlConfigurator> iterator = loader.iterator();
        while (iterator.hasNext()) {
            HttpTaskUrlConfigurator builder = iterator.next();
            CommandType[] types = builder.handled();

            int index = 0;
            int length = types == null ? 0 : types.length;

            for (; index < length; index++) {
                this.builders.put(types[index], builder);
            }
        }
    }

    /**
     * @param chainedHttpTask
     */
    public void registerAdapter(ChainedHttpTask chainedHttpTask) {
        CommandType[] commands = chainedHttpTask.commands();
        int length = commands == null ? 0 : commands.length;
        int index = 0;

        for (; index < length; index++) {
            ChainedHttpTaskConfigurator executor = new ChainedHttpTaskConfigurator(this, chainedHttpTask.profile(), commands[index], this.builders.get(commands[index]), chainedHttpTask.configuration(), chainedHttpTask.chain());

            switch (commands[index]) {
                case ACT:
                    this.setActTaskType(chainedHttpTask.chaining());
                    break;
                case GET:
                    this.setGetTaskType(chainedHttpTask.chaining());
                    break;
                case SERVICES_ENUMERATION:
                    this.setServicesEnumerationTaskType(chainedHttpTask.chaining());
                    break;
                case SET:
                    this.setSetTaskType(chainedHttpTask.chaining());
                    break;
                case SUBSCRIBE:
                    this.setSubscribeTaskType(chainedHttpTask.chaining());
                    break;
                case UNSUBSCRIBE:
                    this.setUnsubscribeTaskType(chainedHttpTask.chaining());
                    break;
                default:
                    break;
            }
            this.adapters.put(commands[index], executor);
        }
    }

    /**
     * @param chainedHttpTask
     */
    public void registerAdapter(RecurrentChainedHttpTask chainedHttpTask) {
        RecurrentHttpTaskConfigurator executor = new RecurrentChainedTaskConfigurator(this, chainedHttpTask.command(), this.builders.get(chainedHttpTask.command()), chainedHttpTask.chaining(), chainedHttpTask.period(), chainedHttpTask.delay(), chainedHttpTask.timeout(), chainedHttpTask.configuration(), chainedHttpTask.chain());
        this.recurrences.add(executor);
    }

    /**
     * @param command
     * @param executor
     */
    public void registerAdapter(SimpleHttpTask httpTaskAnnotation) {
        CommandType[] commands = httpTaskAnnotation.commands();
        int length = commands == null ? 0 : commands.length;
        int index = 0;

        for (; index < length; index++) {
            SimpleTaskConfigurator executor = new SimpleTaskConfigurator(this, httpTaskAnnotation.profile(), commands[index], this.builders.get(commands[index]), httpTaskAnnotation.configuration());
            this.adapters.put(commands[index], executor);
        }
    }

    /**
     * @param httpTaskAnnotation
     */
    public void registerAdapter(RecurrentHttpTask httpTaskAnnotation) {
        RecurrentTaskConfigurator executor = new RecurrentTaskConfigurator(this, httpTaskAnnotation.command(), this.builders.get(httpTaskAnnotation.command()), this.getTaskType(httpTaskAnnotation.command()), httpTaskAnnotation.period(), httpTaskAnnotation.delay(), httpTaskAnnotation.timeout(), httpTaskAnnotation.recurrence());
        this.recurrences.add(executor);
    }

    /**
     * @inheritDoc
     * @see ProtocolStackEndpoint#
     * connect(ExtModelConfiguration)
     */
    public void connect(ExtModelConfiguration manager) throws InvalidProtocolStackException {
        super.connect(manager);

        Iterator<RecurrentHttpTaskConfigurator> iterator = this.recurrences.iterator();

        if (iterator.hasNext()) {
            this.timer = new Timer();
        }
        while (iterator.hasNext()) {
            final RecurrentHttpTaskConfigurator executable = iterator.next();
            TimerTask timerTask = new TimerTask() {
                private long timeout = 0;

                @Override
                public void run() {
                    if (timeout == 0) {
                        timeout = executable.getTimeout() == -1 ? -1 : (System.currentTimeMillis() + executable.getTimeout());
                    }
                    if (timeout > -1 && System.currentTimeMillis() > timeout) {
                        this.cancel();
                        return;
                    }
                    HttpTask<?, ?> task = ReflectUtils.getInstance(executable.getTaskType(), new Object[]{mediator, executable.handled(), SimpleHttpProtocolStackEndpoint.this, SimpleHttpRequest.class, UriUtils.ROOT, null, null, null});
                    try {
                        if (ChainedHttpTaskConfigurator.class.isAssignableFrom(executable.getClass())) {
                            executable.configure(task);

                        } else {
                            HttpTaskProcessingContext context = SimpleHttpProtocolStackEndpoint.this.createContext(executable, task);

                            if (context != null) {
                                ((HttpMediator) mediator).registerProcessingContext(task, context);
                            }
                        }
                        task.execute();
                    } catch (Exception e) {
                        mediator.error(e);
                    }
                }
            };
            this.timer.schedule(timerTask, executable.getDelay(), executable.getPeriod());
        }
    }

    /**
     * @return
     */
    public HttpMediator getMediator() {
        return (HttpMediator) super.mediator;
    }

    /**
     * Defines the string identifier of this SimpleHttpProtocolStackEndpoint
     *
     * @param endpointId
     */
    public void setEndpointIdentifier(String endpointId) {
        this.endpointId = endpointId;
    }

    /**
     * @inheritDoc
     * @see TaskTranslator#
     * send(Task)
     */
    public void send(Task task) {
        try {
            ((HttpMediator) mediator).configure((HttpTask<?, ?>) task);
            super.send(task);
        } catch (Exception e) {
            mediator.error(e);

        } finally {
            ((HttpMediator) mediator).unregisterProcessingContext((HttpTask<?, ?>) task);
        }
    }

    /**
     * @inheritDoc
     * @see TaskTranslator#
     * createTask(Mediator, Task.CommandType,
     * java.lang.String, java.lang.String, ResourceConfig, java.lang.Object[])
     */
    @Override
    public Task createTask(Mediator mediator, CommandType command, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters) {
        HttpTaskConfigurator configuration = this.adapters.get(command);
        if (configuration == null) {
            return null;
        }
        HttpTask<?, ?> task = ReflectUtils.getInstance(this.getTaskType(command), new Object[]{mediator, command, this, SimpleHttpRequest.class, path, profileId, resourceConfig, parameters});
        try {
            if (task.getPacketType() == null) {
                task.setPacketType(packetType);
            }
            if (ChainedHttpTaskConfigurator.class.isAssignableFrom(configuration.getClass())) {
                configuration.configure(task);

            } else {
                HttpTaskProcessingContext context = SimpleHttpProtocolStackEndpoint.this.createContext(configuration, task);

                if (context != null) {
                    ((HttpMediator) mediator).registerProcessingContext(task, context);
                }
            }
            return task;
        } catch (Exception e) {
            mediator.error(e);
            ((HttpMediator) mediator).unregisterProcessingContext(task);
        }
        return null;
    }

    /**
     * Build the task processing context, to be used to resolve configuration variables
     *
     * @param task the task for which to build the
     *             processing context
     */
    protected HttpTaskProcessingContext createContext(HttpTaskConfigurator httpTaskConfigurator, HttpTask<?, ?> task) {
        HttpTaskProcessingContextFactory factory = null;
        if ((factory = ((HttpMediator) mediator).getTaskProcessingContextFactory()) != null) {
            return factory.newInstance(httpTaskConfigurator, this.endpointId, task);
        }
        return null;
    }

    /**
     * Build the task processing context, to be used to resolve configuration variables
     *
     * @param task the task for which to build the
     *             processing context
     */
    protected <CHAINED extends HttpChainedTask<?>> HttpTaskProcessingContext createChainedContext(HttpTaskConfigurator httpTaskConfigurator, HttpChainedTasks<?, CHAINED> tasks, CHAINED task) {
        HttpChainedTaskProcessingContextFactory factory = null;

        if ((factory = ((HttpMediator) this.mediator).getChainedTaskProcessingContextFactory()) != null) {
            return factory.newInstance(httpTaskConfigurator, this.endpointId, tasks, task);
        }
        return null;
    }

    /**
     * Defines the extended {@link Task.Get} type to be used when
     * instantiating a new GET task
     *
     * @param getTaskClass the extended {@link Task.Get} type to be used
     */
    public void setGetTaskType(Class<? extends HttpTask> getTaskClass) {
        this.getTaskClass = getTaskClass;
    }

    /**
     * Returns the extended {@link Task.Get} type to be used when
     * instantiating a new GET task
     *
     * @return the extended {@link Task.Get} type to be used
     */
    public Class<? extends HttpTask> getGetTaskType() {
        if (this.getTaskClass == null) {
            return GET_TASK;
        }
        return this.getTaskClass;
    }

    /**
     * Defines the extended {@link Task.Set} type to be used when
     * instantiating a new SET task
     *
     * @param setTaskClass the extended {@link Task.Set} type to be used
     */
    public void setSetTaskType(Class<? extends HttpTask> setTaskClass) {
        this.setTaskClass = setTaskClass;
    }

    /**
     * Returns the extended {@link Task.Get} type to be used when
     * instantiating a new GET task
     *
     * @return the extended {@link Task.Get} type to be used
     */
    public Class<? extends HttpTask> getSetTaskType() {
        if (this.setTaskClass == null) {
            return SET_TASK;
        }
        return this.setTaskClass;
    }

    /**
     * Defines the extended {@link Task.Act} type to be used when
     * instantiating a new ACT task
     *
     * @param actTaskClass the extended {@link Task.Act} type to be used
     */
    public void setActTaskType(Class<? extends HttpTask> actTaskClass) {
        this.actTaskClass = actTaskClass;
    }

    /**
     * Returns the extended {@link Task.Act} type to be used when
     * instantiating a new ACT task
     *
     * @return the extended {@link Task.Act} type to be used
     */
    public Class<? extends HttpTask> getActTaskType() {
        if (this.actTaskClass == null) {
            return ACT_TASK;
        }
        return this.actTaskClass;
    }

    /**
     * Defines the extended {@link Task.Subscribe} type to be used when
     * instantiating a new SUBSCRIBE task
     *
     * @param subscribeTaskClass the extended {@link Task.Subscribe} type to be used
     */
    public void setSubscribeTaskType(Class<? extends HttpTask> subscribeTaskClass) {
        this.subscribeTaskClass = subscribeTaskClass;
    }

    /**
     * Returns the extended {@link Task.Subscribe} type to be used when
     * instantiating a new SUBSCRIBE task
     *
     * @return the extended {@link Task.Subscribe} type to be used
     */
    public Class<? extends HttpTask> getSubscribeTaskType() {
        if (this.subscribeTaskClass == null) {
            return SUBSCRIBE_TASK;
        }
        return this.subscribeTaskClass;
    }

    /**
     * Defines the extended {@link Task.Unsubscribe} type to be used when
     * instantiating a new UNSUBSCRIBE task
     *
     * @param unsubscribeTaskClass the extended {@link Task.Unsubscribe} type to be used
     */
    public void setUnsubscribeTaskType(Class<? extends HttpTask> unsubscribeTaskClass) {
        this.unsubscribeTaskClass = unsubscribeTaskClass;
    }

    /**
     * Returns the extended {@link Task.Unsubscribe} type to be used when
     * instantiating a new UNSUBSCRIBE task
     *
     * @return the extended {@link Task.Unsubscribe} type to be used
     */
    public Class<? extends HttpTask> getUnsubscribeTaskType() {
        if (this.unsubscribeTaskClass == null) {
            return UNSUBSCRIBE_TASK;
        }
        return this.unsubscribeTaskClass;
    }

    /**
     * Defines the extended {@link Task.ServicesEnumeration} type to be used when
     * instantiating a new SERVICES_ENUMERATION task
     *
     * @param servicesEnumerationTaskClass the extended {@link
     *                                     Task.ServicesEnumeration} type to be used
     */
    public void setServicesEnumerationTaskType(Class<? extends HttpTask> servicesEnumerationTaskClass) {
        this.servicesEnumerationTaskClass = servicesEnumerationTaskClass;
    }

    /**
     * Returns the extended {@link Task.ServicesEnumeration} type to be used when
     * instantiating a new SERVICES_ENUMERATION task
     *
     * @return the extended {@link Task.ServicesEnumeration} type to be used
     */
    public Class<? extends HttpTask> getServicesEnumerationTaskType() {
        if (this.servicesEnumerationTaskClass == null) {
            return SERVICES_ENUMERATION_TASK;
        }
        return this.servicesEnumerationTaskClass;
    }

    /**
     * @param command
     * @return
     */
    protected Class<? extends HttpTask> getTaskType(CommandType command) {
        switch (command) {
            case ACT:
                return this.getActTaskType();
            case GET:
                return this.getGetTaskType();
            case SERVICES_ENUMERATION:
                return this.getServicesEnumerationTaskType();
            case SET:
                return this.getSetTaskType();
            case SUBSCRIBE:
                return this.getSubscribeTaskType();
            case UNSUBSCRIBE:
                return this.getUnsubscribeTaskType();
            default:
                break;
        }
        return HttpTask.class;
    }

    /**
     * @inheritedDoc
     * @see HttpProtocolStackEndpoint#stop()
     */
    public void stop() {
        if (this.timer != null) {
            this.timer.cancel();
        }
        super.stop();
    }
}
