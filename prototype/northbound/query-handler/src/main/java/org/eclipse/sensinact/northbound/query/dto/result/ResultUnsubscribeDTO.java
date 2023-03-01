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
package org.eclipse.sensinact.northbound.query.dto.result;

import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.api.EResultType;

/**
 * Subscription succeeded
 */
public class ResultUnsubscribeDTO extends AbstractResultDTO {

    /**
     * ID of the subscription
     */
    public String subscriptionId;

    public ResultUnsubscribeDTO() {
        super(EResultType.UNSUBSCRIPTION_RESPONSE);
    }
}
