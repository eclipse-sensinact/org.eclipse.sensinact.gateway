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
package org.eclipse.sensinact.prototype;

public final class EventTopicNames {

    /**
     * The topic name for pushed events using annotated dtos
     */
    public static final String DTO_UPDATE_EVENTS = "sensiNact/push/event";

    /**
     * The topic name for pushed events using the generic dtos
     */
    public static final String GENERIC_UPDATE_EVENTS = "sensiNact/push/generic";

}
