/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.basic.math;

import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;

/**
 * This class abstracts the math functions
 *
 * @author Remi Druilhe
 * @see AbstractFunction
 */
public abstract class MathFunction<T> extends AbstractFunction<T> {

    /**
     * List of the supported operators
     */
    public enum MathOperator {
        ADDITION("add"), SUBTRACTION("sub"), MULTIPLICATION("times"), DIVISION("div"), MODULO("mod"), ASSIGNMENT("<-");
        private String type;

        MathOperator(String type) {
            this.type = type;
        }

        public String getOperator() {
            return type;
        }
    }

    MathFunction() {
    }
}
