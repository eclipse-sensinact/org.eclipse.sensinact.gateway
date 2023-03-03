/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.query.dto.notification;

/**
 * Common fields of a resource notification
 */
public abstract class AbstractResourceNotificationDTO {

    /**
     * Resource provider
     */
    public String provider;

    /**
     * Resource service
     */
    public String service;

    /**
     * Resource name
     */
    public String resource;

    /**
     * Notification time stamp
     */
    public long timestamp;
}
