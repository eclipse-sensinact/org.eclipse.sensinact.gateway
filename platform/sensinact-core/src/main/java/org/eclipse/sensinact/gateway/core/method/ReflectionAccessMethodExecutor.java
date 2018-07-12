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
package org.eclipse.sensinact.gateway.core.method;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.json.JSONObject;

import java.lang.reflect.Method;

/**
 * Extended  {@link AbstractAccessMethodExecutor} implementation
 * referring to a java {@link Method}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ReflectionAccessMethodExecutor extends AbstractAccessMethodExecutor {
    private final Method method;
    private Object target;

    /**
     * Constructor
     *
     * @throws InvalidReflectiveExecutorException
     */
    public ReflectionAccessMethodExecutor(Object target, Method method) throws InvalidReflectiveExecutorException {
        super();
        if (!method.getDeclaringClass().isAssignableFrom(target.getClass())) {
            throw new InvalidReflectiveExecutorException("Incompatible method and target types");
        }
        if (!JSONObject.class.isAssignableFrom((Class<?>) method.getGenericReturnType()) && !Void.class.isAssignableFrom((Class<?>) method.getGenericReturnType())) {
            throw new InvalidReflectiveExecutorException("Incompatible returned type : JSONObject or Void expected");
        }
        this.method = method;
        this.target = target;
    }

    /**
     * Executes this extended {@link Executable} if the
     * parameters wrapped by the {@link AccessMethodResponseBuilder}
     * argument comply the registered constraints
     *
     * @param parameter the {@link AccessMethodResponseBuilder} parameterizing the
     *                  execution, in which to stack the JSON formated
     *                  result object of the execution
     * @throws Exception
     */
    protected void doExecute(AccessMethodResponseBuilder responseBuilder) throws Exception {
        Object result = method.invoke(target, responseBuilder.getParameters());

        if (result != null && responseBuilder.getComponentType().isAssignableFrom(result.getClass())) {
            responseBuilder.push(result);
        }
    }
}
