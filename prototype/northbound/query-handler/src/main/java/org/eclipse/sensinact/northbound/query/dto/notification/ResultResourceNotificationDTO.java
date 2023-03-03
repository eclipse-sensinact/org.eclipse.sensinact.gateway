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

import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.api.EResultType;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Notification result DTO send on each event
 */
public class ResultResourceNotificationDTO extends AbstractResultDTO {

    /**
     * Subscription ID
     */
    public String subscriptionId;

    /**
     * Notification content
     */
    @JsonDeserialize(using = ResourceNotificationDeserializer.class)
    public AbstractResourceNotificationDTO notification;

    public ResultResourceNotificationDTO() {
        super(EResultType.SUBSCRIPTION_NOTIFICATION);
    }
}
