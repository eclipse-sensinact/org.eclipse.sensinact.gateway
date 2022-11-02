/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation 
**********************************************************************/
package org.eclipse.sensinact.prototype.dto.impl;

import java.time.Instant;

public abstract class AbstractUpdateDto {

    /**
     * The model to use, if null then a unique model may be created
     */
    public String model;

    /**
     * The provider name for this update. Must be set
     */
    public String provider;

    /**
     * The service name for this update. Must be set
     */
    public String service;

    /**
     * The resource name for this update. Must be set
     */
    public String resource;

    /**
     * The timestamp for this update. If not set then the current time is used.
     */
    public Instant timestamp;
}
