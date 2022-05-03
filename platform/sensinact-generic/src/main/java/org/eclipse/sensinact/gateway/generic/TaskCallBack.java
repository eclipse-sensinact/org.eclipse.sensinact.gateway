/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.common.execution.Executable;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class TaskCallBack {
    private final Executable<Task, Void> executor;

    /**
     * @throws InvalidTaskCallBackException
     */
    public TaskCallBack(final Method method, final Object object) throws InvalidTaskCallBackException {
        Class<?>[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length != 1 || !Task.class.isAssignableFrom(parameterTypes[0]) || !method.getDeclaringClass().isAssignableFrom(object.getClass())) {
            throw new InvalidTaskCallBackException();
        }
        this.executor = new Executable<Task, Void>() {
            @Override
            public Void execute(Task task) throws Exception {
                method.setAccessible(true);
                method.invoke(object, task);
                return null;
            }
        };
    }

    /**
     * Constructor
     *
     * @param executor
     */
    public TaskCallBack(Executable<Task, Void> executor) {
        this.executor = executor;
    }

    /**
     * @param task
     */
    public void callback(Task task) {
        try {
            this.executor.execute(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
