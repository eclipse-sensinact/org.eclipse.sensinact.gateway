/*********************************************************************
* Copyright (c) 2024 Kentyou.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Thomas Calmant (Kentyou) - Initial contribution
**********************************************************************/

package org.eclipse.sensinact.gateway.southbound.wot.api.constants;

public interface WoTConstants {

    /**
     * Default model package URI for Web of Thing providers
     */
    String MODEL_PACKAGE_URI = "https://eclipse.org/sensinact/wot/";

    /**
     * Name of the service holding action and value resources
     */
    String WOT_SERVICE = "wot";

    /**
     * Name of the service handling events
     */
    String EVENT_SERVICE = "wot_event";

    /**
     * Argument name to use in sensiNact actions when none is defined in the Thing
     * Description
     */
    String DEFAULT_ARG_NAME = "value";
}
