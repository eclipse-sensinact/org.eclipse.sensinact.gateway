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
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ResourceConfig;

/**
 * Handles {@link Task}s creation and translation into an appropriate
 * command according to the protocol in use
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface TaskTranslator {
    /**
     * Returns a new created {@link Get} task
     *
     * @param mediator       the associated {@link Mediator}
     * @param command        the {@link CommandType} of the specific {@link Task}
     *                       to be created
     * @param path           the string path of the SnaObject from which the task is made
     * @param profileId      the string profile identifier of the {@link ModelInstance}
     *                       from which the task is made
     * @param profileId
     * @param resourceConfig the {@link ExtResourceConfig} mapped to the resource
     *                       on which applies the task
     * @param parameters     the objects array parameterizing the task execution
     * @return a new created {@link Get} task
     */
    public Task createTask(Mediator mediator, Task.CommandType command, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters);

//	/**
//     * Returns a new created {@link Get} task
//     * 
//     * @param mediator
//     *      the associated {@link Mediator} 
//     * @param path
//     *      the string path of the SnaObject from which the task is made
//     * @param profileId
//     *      the string profile identifier of the {@link ModelInstance}
//     *      from which the task is made
//	 * @param profileId 
//     * @param resourceConfig
//     *      the {@link ExtResourceConfig} mapped to the resource on which 
//     *      applies the task
//     * @param parameters
//     *      the objects array parameterizing the task execution            
//     * @return 
//     *      a new created {@link Get} task
//     */ 
//    public Task createGetTask(Mediator mediator, 
//    	String path, String profileId, ResourceConfig resourceConfig,
//    	Object[] parameters);
//    /**
//     * Returns a new created {@link Set} task
//     * 
//     * @param mediator
//     *      the associated {@link Mediator} 
//     * @param path
//     *      the string path of the SnaObject from which the task is made
//     * @param profileId
//     *      the string profile identifier of the {@link ModelInstance}
//     *      from which the task is made
//     * @param resourceConfig
//     *      the {@link ExtResourceConfig} mapped to the resource on which 
//     *      applies the task
//     * @param parameters
//     *      the objects array parameterizing the task execution            
//     * @return 
//     *      a new created {@link Set} task
//     */
//    public Task.Set createSetTask(Mediator mediator, 
//        	String path, String profileId, ResourceConfig resourceConfig,
//        	Object[] parameters);
//    /**
//     * Returns a new created {@link Act} task
//     * 
//     * @param mediator
//     *      the associated {@link Mediator} 
//     * @param path
//     *      the string path of the SnaObject from which the task is made
//     * @param profileId
//     *      the string profile identifier of the {@link ModelInstance}
//     *      from which the task is made
//     * @param resourceConfig
//     *      the {@link ExtResourceConfig} mapped to the resource on which 
//     *      applies the task
//     * @param parameters
//     *      the objects array parameterizing the task execution            
//     * @return 
//     *      new created {@link Act} task
//     */
//    public Task.Act createActTask(Mediator mediator, 
//        	String path, String profileId, ResourceConfig resourceConfig,
//        	Object[] parameters);
//    
//    /**
//     * Returns a new created {@link Subscribe} task
//     * 
//     * @param mediator
//     *      the associated {@link Mediator} 
//     * @param path
//     *      the string path of the SnaObject from which the task is made
//     * @param profileId
//     *      the string profile identifier of the {@link ModelInstance}
//     *      from which the task is made
//     * @param resourceConfig
//     *      the {@link ExtResourceConfig} mapped to the resource on which 
//     *      applies the task
//     * @param parameters
//     *      the objects array parameterizing the task execution            
//     * @return 
//     *      a new created {@link Subscribe} task
//     */
//    public Task.Subscribe createSubscribeTask(Mediator mediator, 
//        	String path, String profileId, ResourceConfig resourceConfig,
//        	Object[] parameters);
//    
//    /**
//     * Returns a new created {@link Unsubscribe} task
//     * 
//     * @param mediator
//     *      the associated {@link Mediator}
//     * @param path
//     *      the string path of the SnaObject from which the task is made
//     * @param profileId
//     *      the string profile identifier of the {@link ModelInstance}
//     *      from which the task is made
//     * @param resourceConfig
//     *      the {@link ExtResourceConfig} mapped to the resource on which 
//     *      applies the task
//     * @param parameters
//     *      the objects array parameterizing the task execution            
//     * @return 
//     *      a new created {@link Unsubscribe} task
//     */
//    public Task.Unsubscribe createUnsubscribeTask(Mediator mediator, 
//        	String path, String profileId, ResourceConfig resourceConfig,
//        	Object[] parameters);
//
//    /**
//     * Returns a new {@link ServicesEnumeration} task
//     * 
//     * @param mediator
//     *      the associated {@link Mediator} 
//     * @param path
//     *      the string path of the SnaObject from which the task is made 
//     * @param profileId
//     *      the string profile identifier of the {@link ModelInstance}
//     *      from which the task is made  
//     * @return 
//     *      a new created {@link ServicesEnumeration} task
//     */
//    public Task.ServicesEnumeration createServicesEnumerationTask(
//            Mediator mediator, String path, String profileId); 

    /**
     * Returns the {@link Request.Type} of the {@link Task}s
     * this transmitter is able to send
     *
     * @return the {@link Request.Type} of the {@link Task}s
     * this transmitter is able to send
     */
    Task.RequestType getRequestType();

    /**
     * Sends the {@link Task} passed as parameter
     *
     * @param task the task to be transmitted
     */
    void send(Task task);
}
