/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.basic.time;

import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;

/**
 * This class abstracts the time functions
 *
 * @author Remi Druilhe
 * @see AbstractFunction
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
