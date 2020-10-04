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
