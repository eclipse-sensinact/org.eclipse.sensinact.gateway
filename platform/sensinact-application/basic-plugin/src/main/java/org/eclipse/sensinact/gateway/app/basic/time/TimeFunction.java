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

package org.eclipse.sensinact.gateway.app.basic.time;

import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;

/**
 * This class abstracts the time functions
 *
 * @see AbstractFunction
 *
 * @author Remi Druilhe
 */
public abstract class TimeFunction<T> extends AbstractFunction<T> {

    /**
     * The list of supported operators
     */
    public enum TimeOperator {
        SLEEP("sleep");

        private String type;

        TimeOperator(String type) {
            this.type = type;
        }

        public String getOperator() {
            return type;
        }
    }
}
