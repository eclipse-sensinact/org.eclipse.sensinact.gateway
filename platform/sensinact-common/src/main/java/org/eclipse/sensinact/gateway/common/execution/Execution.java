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
package org.eclipse.sensinact.gateway.common.execution;

/**
 * An Execution gathers a variable set of {@link ExecutionContext}s whose
 * it handles their {@link Executable}'s configuration of
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Execution {
    /**
     * Policy of parameterization applying on each {@link ExecutionContext}
     * encapsulated {@link Executable}
     *
     * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
     */
    enum ExecutionParameterizingPolicy {
        //each SnaExecutor is parameterized with the same
        //set of parameters
        UNIQUE_SET_OF_PARAMETERS, //SnaExecutors are parameterized using a dedicated
        //set of parameters
        DISTINCT_PARAMETERS, //The result returned by an SnaExecutor becomes the
        //parameter passed to the next one
        RESULT_TO_PARAMETERS;
    }

    /**
     * Policy applying for global result building of then parameterization
     * applying on each {@link ExecutionContext} encapsulated {@link
     * Executable}
     *
     * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
     */
    enum ExecutionResultPolicy {
        //the array of each result is returned
        ALL, //all results are gathered in one
        ALL_TO_ONE, //the first result is returned
        FIRST, //the last result is returned
        LAST;
    }

    /**
     * Returns the ExecutionParameterizingPolicy of this Execution
     *
     * @return this Execution's ExecutionParameterizingPolicy
     */
    ExecutionParameterizingPolicy getExecutionParameterizingPolicy();

    /**
     * Returns the ExecutionResultPolicy of this Execution
     *
     * @return this Execution's ExecutionResultPolicy
     */
    ExecutionResultPolicy getExecutionResultPolicy();

    /**
     * Prepares this Execution
     *
     * @param parameters the initial variable set of object parameters
     */
    void prepare(Object... parameters);

    /**
     * Processes this Execution
     *
     * @param parameters the variable set of object parameters
     */
    void process(Object... parameters);

    /**
     * Concludes this Execution
     */
    void conclude();

    /**
     * Returns the object resulting of this Execution
     * processing. This result depends on the defined
     * {@link ExecutionResultPolicy}
     *
     * @return this Execution processing result
     */
    Object getResult();
}
