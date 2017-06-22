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
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.ServiceProvider;


/**
 * a Task gathers data relative to a command invocation
 * on a specific resource using a set of parameters
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Task extends JSONable
{     
    public static final long DEFAULT_TIMEOUT = 5000;
    
	/**
     * A Task can be at the origin of a 'STREAM' or an 'URI'
     * typed request
     */
    static enum RequestType
    {
    	LOCAL,
        STREAM,
        URI;
    }
    
    /**
     * Enumeration of the possible life cycle
     * states of a Task
     */
    static enum LifecycleStatus
    {
        INITIALIZED,
        LAUNCHED,
        ACKNOWLEDGED,
        EXECUTED,
        ABORDED;
    }
    
    /**
     *  Enumeration of exchanged command types
	 */
	public static enum CommandType
	{
	    GET,
	    SET,
	    ACT,
	    SUBSCRIBE,
	    UNSUBSCRIBE,
	    SERVICES_ENUMERATION
//	    , 
//	    HELLO, 
//	    GOODBYE
	    ;
	}
    
//    /**
//     * Task dedicated to SERVICE_ENUMERATION typed command execution
//     */
//    interface ServicesEnumeration extends Task
//    {
//        public static final Task.CommandType COMMAND = Task.CommandType.SERVICES_ENUMERATION;
//    }
//
//    /**
//     * Task dedicated to HELLO typed command execution 
//     */
//    interface Hello extends Task
//    {
//        public static final Task.CommandType COMMAND = Task.CommandType.HELLO;
//    }
//
//    /**
//     * Task dedicated to GOODBYE typed command execution 
//     */
//    interface Goodbye extends Task
//    {
//        public static final Task.CommandType COMMAND = Task.CommandType.GOODBYE;
//    }
//    
//    /**
//     * Task dedicated to GET typed command execution (to rely to
//     * the GET typed access {@link Method} 
//     */
//    interface Get extends Task
//    {
//        public static final Task.CommandType COMMAND = Task.CommandType.GET;
//    }
//    
//
//    /**
//     * Task dedicated to SET typed command execution (to rely to
//     * the SET typed access {@link Method} 
//     */
//    interface Set extends Task
//    {
//        public static final Task.CommandType COMMAND = Task.CommandType.SET;
//    }
//    
//
//    /**
//     * Task dedicated to ACT typed command execution (to rely to
//     * the ACT typed access {@link Method} 
//     */
//    interface Act extends Task
//    {
//        public static final Task.CommandType COMMAND = Task.CommandType.ACT;
//    }
//
//    /**
//     * Task dedicated to SUBSCRIBE typed command execution (to rely to
//     * the SUBSCRIBE typed access {@link Method} 
//     */
//    interface Subscribe extends Task
//    {
//        public static final Task.CommandType COMMAND = Task.CommandType.SUBSCRIBE;
//    }
//
//    /**
//     * Task dedicated to UNSUBSCRIBE typed command execution (to rely to
//     * the UNSUBSCRIBE typed access {@link Method} 
//     */
//    interface Unsubscribe extends Task
//    {
//        public static final Task.CommandType COMMAND = Task.CommandType.UNSUBSCRIBE;
//    }
    
    /**
     * Returns the {@link RequestType} of this task
     * 
     * @return 
     *      the {@link RequestType} of this task
     */
    RequestType getRequestType();

    /**
     * Returns the {@link CommandType} this task
     * executes
     * 
     * @return 
     *      the {@link CommandType} this task
     *      executes
     */
    CommandType getCommand();
    
    /**
     * Returns the {@link LifecycleStatus} of this task
     * 
     * @return 
     *      the {@link LifecycleStatus} of this task
     */
    LifecycleStatus getLifecycleStatus();

    /**
     * Returns this {@link Task} string identifier
     * 
     * @return
     *      this {@link Task} string identifier
     */
    String getTaskIdentifier();

    /**
     * Defines this {@link Task} string identifier
     * 
     * @param taskIdentifier
     *      this {@link Task} string identifier
     */
    void setTaskIdentifier(String taskIdentifier);
    
    /**
     * Returns the {@link ExtResourceConfig} mapped to the
     * {@link Resource} on which the invoked command
     * applies
     * 
     * @return 
     *      the {@link ResourceConfig} mapped to the
     *      {@link Resource} on which the invoked command
     *      applies
     */
    ResourceConfig getResourceConfig();

    /**
     * Returns the objects array used to parameterize 
     * the command invocation
     * 
     * @return 
     *      the objects array used to parameterize 
     *      the command invocation
     */
    Object[] getParameters();
    
    /**
     * Returns the string path of the ModelElement
     * which has initialized this task
     * 
     * @return
     * 		the string path of the initializer 
     * 		SnaObject
     */
    String getPath();

    /**
     * Returns the string profile identifier of the 
     * {@link ServiceProvider} targeted  by the task
     * 
     * @return the string profile identifier of the 
     * targeted {@link ServiceProvider}
     */
    String getProfile();

    /**
     * Returns true if the result object of 
     * this Task execution is available, or
     * returns false otherwise
     * 
     * @return
     *      true if the result object is 
     *      available; <br/>false otherwise
     */
    boolean isResultAvailable();
    
    /**
     * Sets the result object of this Task 
     * execution
     * 
     * @param result
     *      the result object of this Task 
     *      execution
     */
    void setResult(Object result);

    /**
     * Sets the result object of this Task 
     * execution
     * 
     * @param result
     *      the result object of this Task 
     *      execution
     * @param timestamp
     * 		the timestamp of the result object
     * 		update
     */
    void setResult(Object result, long timestamp);
    
    /**
     * Returns the timestamp of the update of this 
     * task's result object
     * 
	 * @return
	 * 		the timestamp of the update of this 
     * 		task's result object
	 */
    long getTimestamp();

    /**
     * Returns the result object of this Task 
     * execution
     * 
     * @return
     *      the result object of this Task 
     *      execution
     */
    Object getResult();
    
    /**
     * Executes this {@link Task}
     */
    public void execute();

    /**
     * Returns the maximum of milliseconds to wait
     * for a response
     * 
     * @return
     *      the maximum of milliseconds to wait
     *      for a response
     */
    long getTimeout();

    /**
     * Sets the maximum of milliseconds to wait
     * for a response
     * 
     * @return
     *      the maximum of milliseconds to wait
     *      for a response
     */
    void setTimeout(long timeout);

    /**
     * Defines the Life cycle status of this 
     * task as ABORTED and defines the object
     * passed as parameter as the execution result 
     * of this task  
     * 
     * @param result
     *      the object to set as the execution
     *      result of this task
     */
    void abort(Object result);
    
    /**
     * Registers a  {@link TaskCallBack} to trigger when 
     * the result of this task is set
     * 
     * @param callback
     *      the {@link TaskCallBack} to trigger
     *      when the result of this task is set
     */
    void registerCallBack(TaskCallBack callback);

 }
