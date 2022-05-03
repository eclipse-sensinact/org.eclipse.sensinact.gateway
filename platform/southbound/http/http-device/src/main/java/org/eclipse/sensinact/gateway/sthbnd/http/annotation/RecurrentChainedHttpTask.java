/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.annotation;

import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.task.JSONHttpChainedTasks;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface RecurrentChainedHttpTask {
    Class<? extends HttpChainedTasks> chaining() default JSONHttpChainedTasks.class;

    CommandType command() default CommandType.GET;

    long period() default 60 * 1000;

    long delay() default 1000;

    long timeout() default -1;

    HttpTaskConfiguration configuration();

    HttpChildTaskConfiguration[] chain();
}
