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

import org.eclipse.sensinact.northbound.query.dto.result.ErrorResultDTO;

/**
 * Notification result DTO send on each event
 */
public class ErrorResultNotificationDTO extends ErrorResultDTO {

    /**
     * Subscription ID
     */
    public String subscriptionId;

    public ErrorResultNotificationDTO(final String subscriptionId) {
        super(500, "Error sending notification");
        this.subscriptionId = subscriptionId;
    }
}
