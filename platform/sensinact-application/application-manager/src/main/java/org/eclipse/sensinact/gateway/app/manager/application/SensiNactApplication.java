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
package org.eclipse.sensinact.gateway.app.manager.application;

import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessage;

/**
 * sensiNact Application service
 */
public interface SensiNactApplication extends Nameable, Recipient {
    /**
     * Starts this SensiNactApplication
     *
     * @return
     */
    SnaErrorMessage start();

    /**
     * Stops this SensiNactApplication
     *
     * @return
     */
    SnaErrorMessage stop();

}
