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
package org.eclipse.sensinact.gateway.generic.local;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.InvalidProtocolStackException;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.Task.RequestType;
import org.eclipse.sensinact.gateway.generic.annotation.AnnotationResolver;
import org.eclipse.sensinact.gateway.generic.annotation.TaskCommand;
import org.eclipse.sensinact.gateway.generic.annotation.TaskCommand.SynchronizationPolicy;
import org.eclipse.sensinact.gateway.generic.annotation.TaskExecution;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 * Basis {@link ProtocolStackEndpoint} implementation
 *
 * @param <P> extended {@link BasisPacket} type
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class LocalProtocolStackEndpoint<P extends Packet> extends ProtocolStackEndpoint<P> {
    public static final Task.RequestType REQUEST_TYPE = Task.RequestType.LOCAL;

    /**
     * {@link AnnotationExecutor}s list of registered
     * profiles
     */
    private List<AnnotationExecutor> executors;

    /**
     * Map of already resolved string path of SnaObject to
     * the associated BasisExecutor
     */
    private Map<PathCommandKey, AnnotationExecutor> cache;

    private AnnotationResolver resolver;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} that will be used by the LocalProtocolStackConnector 
     * to instantiate to interact with the OSGi host environment
     */
    public LocalProtocolStackEndpoint(Mediator mediator) {
        super(mediator);
        this.resolver = new AnnotationResolver(mediator);
        this.executors = new ArrayList<AnnotationExecutor>();
        this.cache = new HashMap<PathCommandKey, AnnotationExecutor>();

        this.buildExecutors();
    }

    @Override
    public void connect(ExtModelConfiguration<P> manager) throws InvalidProtocolStackException {
        this.resolver.addInjectableInstance(Mediator.class, super.mediator);
        this.resolver.addInjectableInstance(LocalProtocolStackEndpoint.class, this);
        this.resolver.addInjectableInstance(ExtModelConfiguration.class, manager);
        this.resolver.buildInjected();
        super.connect(manager);
    }

    @Override
    public RequestType getRequestType() {
        return REQUEST_TYPE;
    }

    @Override
    public void send(Task task) {    	   	 
        try {
            this.execute(task);
        } catch (Exception e) {
            task.abort(AccessMethod.EMPTY);
            mediator.error(e);
        }
    }

    /**
     * Adds the specified Object instance whose type is passed as parameter to this 
     * BasisProtocolStackConnector's {@link AnnotationResolver} to make it accessible to 
     * {@link TaskExecution} annotated types
     *
     * @param injectableType the Object instance type
     * @param injectable     the Object instance to make accessible to {@link
     * TaskExecution} annotated types
     */
    public <T> void addInjectableInstance(Class<T> injectableType, T injectable) {
        this.resolver.addInjectableInstance(injectableType, injectable);
    }

    /**
     * Adds the Object instance passed as parameter to this BasisProtocolStackConnector's 
     * {@link AnnotationResolver} to make it accessible to {@link TaskExecution} annotated types
     *
     * @param injectable the Object instance to make accessible to {@link
     * TaskExecution} annotated types
     */
    public <T> void addInjectableInstance(T injectable) {
        this.addInjectableInstance((Class<T>) injectable.getClass(), injectable);
    }

    /**
     * Creates the {@link BasisExcutor}s to associate to {@link TaskCommand} annotated methods in
     * the list of {@link TaskExecution} annotated types passed as parameter
     *
     * @param classes List of {@link TaskExecution} annotated types to explore to search 
     * {@link TaskCommand} annotated methods
     */
    private void buildExecutors() {
        Iterator<Object> iterator = this.resolver.iterator();

        while (iterator.hasNext()) {
            final Object instance = iterator.next();

            Map<Method, TaskCommand> instanceMap = ReflectUtils.getAnnotatedMethods(instance.getClass(), TaskCommand.class);

            if (instanceMap.isEmpty()) {
                continue;
            }
            Iterator<Map.Entry<Method, TaskCommand>> instanceIterator = instanceMap.entrySet().iterator();

            while (instanceIterator.hasNext()) {
                Map.Entry<Method, TaskCommand> methodEntry = instanceIterator.next();

                final Method method = methodEntry.getKey();
                final TaskCommand annotation = methodEntry.getValue();

                String target = annotation.target();
                CommandType command = annotation.method();
                SynchronizationPolicy sync = annotation.synchronization();

                TaskExecution taskInvoker = instance.getClass().getAnnotation(TaskExecution.class);

                String[] profiles = taskInvoker.profile();
                if (profiles == null || profiles.length == 0) {
                    profiles = new String[]{ResourceConfig.ALL_PROFILES};
                }
                int index = 0;
                int length = profiles.length;

                for (; index < length; index++) {
                    AnnotationExecutor executor = new AnnotationExecutor(target, command, sync, method.isVarArgs(), method.getParameterTypes(), profiles[index]) {
                        @Override
                        public Object execute(Task task) throws Exception {
                            method.setAccessible(true);
                            Object[] taskParameters = task.getParameters();

                            int length = taskParameters == null || (method.isVarArgs() && Array.getLength(taskParameters) == 0) ? 0 : taskParameters.length;

                            Object[] parameters = new Object[length + 1];
                            parameters[0] = task.getPath();
                            if (length > 0) {
                                System.arraycopy(taskParameters, 0, parameters, 1, length);
                            }
                            Object result = method.invoke(instance, method.isVarArgs() ? new Object[]{parameters} : parameters);

                            if (this.isSynchronous()) {
                                task.setResult(result);
                            }
                            return result;
                        }
                    };
                    this.executors.add(executor);
                }
            }
        }
        Collections.sort(this.executors, new Comparator<AnnotationExecutor>() {
            @Override
            public int compare(AnnotationExecutor basis1, AnnotationExecutor basis2) { 
               boolean basis1AllProfile = basis1.isProfile(ResourceConfig.ALL_PROFILES);
               boolean basis2AllProfile = basis2.isProfile(ResourceConfig.ALL_PROFILES);

               boolean basis1AllTargets = basis1.isAllTargets();
               boolean basis2AllTargets = basis2.isAllTargets();
               
               if (basis1AllProfile == basis2AllProfile) {
            	   
            	   if(basis1AllTargets && !basis2AllTargets)
            		   return 1;
            	   if(!basis1AllTargets && basis2AllTargets)
            		   return -1;
            	   
            	   int basis1WildcardIndex = basis1.getName().indexOf('*');
               	   int basis2WildcardIndex = basis2.getName().indexOf('*');
                   
                   if(basis1WildcardIndex>-1 && basis2WildcardIndex==-1)
                   	    return 1;
                   if(basis1WildcardIndex==-1 && basis2WildcardIndex>-1)
                   	    return -1;
               	   if(basis1WildcardIndex>-1 && basis2WildcardIndex>-1) {            	
	               		String[] thisPathElements = UriUtils.getUriElements(basis1.getName());
	               		String[] thatPathElements = UriUtils.getUriElements(basis2.getName());
	               		int i=0;
	               		for(;i<thisPathElements.length && !"*".equals(thisPathElements[i]);i++);
	               		int j=0;
	               		for(;j<thatPathElements.length && !"*".equals(thatPathElements[j]);j++);
	               		if(i==j) {
	               			int k=i+1;
	                   		for(;k<thisPathElements.length && !"*".equals(thisPathElements[k]);k++);
	                   		k=k==thisPathElements.length?0:k;
	                   		int l=j+1;
	                   		for(;l<thatPathElements.length && !"*".equals(thatPathElements[l]);l++);
	                   		l=l==thatPathElements.length?0:l;
	                   		i=k;
	                   		j=l;
	               		}
	               		return i<j?-1:1;            			
               		}
                    return basis1.length() < basis2.length()?1:-1;
                }
                return basis1AllProfile?1:-1;         	   
            }
        });
    }

    /**
     * Executes the {@link Task} passed as parameter by retrieving and invoking 
     * the associated {@link AnnotationExecutor} if it exists
     *
     * @param task the {@link Task} to execute
     * 
     * @return the object result of the {@link AnnotationExecutor} invocation
     * 
     * @throws Exception if a {@link AnnotationExecutor} cannot be retrieved
     * or the one thrown by its invocation
     */
    protected Object execute(Task task) throws Exception {
        String path = task.getPath();
        CommandType commandType = task.getCommand();
        String profile = task.getProfile();

        if (profile == null) 
            profile = ResourceConfig.ALL_PROFILES;

        PathCommandKey key = new PathCommandKey(path, profile, commandType);
        AnnotationExecutor executor = cache.get(key);

        if (executor == null) {
            Object[] parameters = task.getParameters();
            int index = 1;
            int length = parameters == null ? 0 : parameters.length;
            length += 1;

            Class<?>[] parameterTypes = new Class<?>[length];

            //the path is automatically provided as first parameter
            parameterTypes[0] = String.class;
            for (; index < length; index++) {
                parameterTypes[index] = parameters[index - 1]== null
                	?Object.class:parameters[index - 1].getClass();
            }
            Iterator<AnnotationExecutor> iterator = this.executors.listIterator();

            while (iterator.hasNext()) {
                executor = iterator.next();

                if (!executor.equals(commandType) || !executor.equals(parameterTypes) || !executor.isProfile(profile)) {
                    executor = null;
                    continue;
                }
                index = 0;
                String[] thisPathElements = UriUtils.getUriElements(path);
                String[] thatPathElements = UriUtils.getUriElements(executor.getName());
                int thisLength = thisPathElements.length;
                int thatLength = thatPathElements.length;

                if (thisLength < thatLength) {
                    executor = null;
                    continue;
                }
                for (; index < thatLength && (thatPathElements[index].equals(UriUtils.WILDCARD) || thatPathElements[index].equals(thisPathElements[index])); index++);
                
                if (index < thatLength) {
                    executor = null;
                    continue;
                }
                break;
            }
            if (executor == null)
                executor = new EmptyAnnotationExecutor(path, commandType, parameterTypes, profile);
            
            cache.put(key, executor);
        }
        return executor.execute(task);
    }

    @Override
    public Task createTask(Mediator mediator, CommandType command, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters) {
    	Task task =  super.wrap(Task.class, new GenericLocalTask(mediator, command, this, path, profileId, resourceConfig, parameters)); 
    	return task;
    }

    /**
     * Key of the cache map
     */
    private class PathCommandKey {
        public final String path;
        public final CommandType command;
        public final String profileId;
        private final int hash;

        /**
         * Constructor
         *
         * @param path
         * @param command
         */
        PathCommandKey(String path, String profileId, CommandType command) {
            this.path = path;
            this.command = command;
            this.profileId = profileId;
            this.hash = new StringBuilder().append(path).append("_").append(profileId).append("_").append(command).toString().hashCode();
        }

        /**
         * @inheritDoc
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object object) {
            if (object.getClass() == PathCommandKey.class) {
                PathCommandKey key = (PathCommandKey) object;
                return key.hashCode() == this.hash;
            }
            return false;
        }

        /**
         * @inheritDoc
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return this.hash;
        }
    }

    /**
     * Extended BasisExecutor provided an empty execution
     */
    private class EmptyAnnotationExecutor extends AnnotationExecutor {
        /**
         * @param target
         * @param commandType
         * @param parameterTypes
         * @param profile
         */
        EmptyAnnotationExecutor(String target, CommandType commandType, Class<?>[] parameterTypes, String profile) {
            super(target, commandType, TaskCommand.SynchronizationPolicy.SYNCHRONOUS, false, parameterTypes, profile);
        }

        /**
         * @inheritDoc
         * @see Executable#execute(java.lang.Object)
         */
        @Override
        public Object execute(Task task) throws Exception {
            task.abort(AccessMethod.EMPTY);
            return AccessMethod.EMPTY;
        }
    }
}
