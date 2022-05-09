/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.basic.string;

import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;

/**
 * This class abstracts the string functions
 *
 * @author Remi Druilhe
 * @see AbstractFunction
 */
public abstract class StringFunction<T> extends AbstractFunction<T> {
    /**
     * The list of supported operators
     */
    public enum StringOperator {
        CONCATENATE("concat"), SUBSTRING("substr");
        private String type;

        StringOperator(String type) {
            this.type = type;
        }

        public String getOperator() {
            return type;
        }
    }
}
