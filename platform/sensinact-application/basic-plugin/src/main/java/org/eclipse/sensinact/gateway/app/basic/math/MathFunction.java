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
package org.eclipse.sensinact.gateway.app.basic.math;

import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * This class abstracts the math functions
 *
 * @author Remi Druilhe
 * @see AbstractFunction
 */
public abstract class MathFunction<T> extends AbstractFunction<T> {
    protected Mediator mediator;

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

    MathFunction(Mediator mediator) {
        this.mediator = mediator;
    }
}
