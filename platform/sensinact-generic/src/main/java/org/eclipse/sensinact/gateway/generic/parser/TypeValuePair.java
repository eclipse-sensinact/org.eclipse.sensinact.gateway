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
package org.eclipse.sensinact.gateway.generic.parser;

final class TypeValuePair {
    public final Class<?> type;
    public final Object value;

    TypeValuePair(Class<?> type, Object value) {
        this.type = type;
        this.value = value;
    }
}