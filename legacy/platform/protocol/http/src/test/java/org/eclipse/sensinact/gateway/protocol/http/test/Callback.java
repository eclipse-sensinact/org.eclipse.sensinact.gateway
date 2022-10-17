/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.protocol.http.test;

import java.lang.reflect.Method;

public class Callback {
    public final Object target;
    public final Method method;

    Callback(Object target, Method method) {
        this.target = target;
        this.method = method;
    }

    public Object invoke(Object[] parameters) throws Exception {
        this.method.setAccessible(true);
        return this.method.invoke(this.target, parameters);
    }
}