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
