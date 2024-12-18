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

package org.eclipse.sensinact.gateway.southbound.wot.api;

public interface Operations {

    String READ_PROPERTY = "readproperty";
    String WRITE_PROPERTY = "writeproperty";
    String OBSERVE_PROPERTY = "observeproperty";
    String UNOBSERVE_PROPERTY = "unobserveproperty";

    String INVOKE_ACTION = "invokeaction";
    String QUERY_ACTION = "queryaction";
    String CANCEL_ACTION = "cancelaction";

    String SUBSCRIBE_EVENT = "subscribeevent";
    String UNSUBSCRIBE_EVENT = "unsubscribeevent";

    String READ_ALL_PROPERTIES = "readallproperties";
    String WRITE_ALL_PROPERTIES = "writeallproperties";

    String READ_MULTIPLE_PROPERTIES = "readmultipleproperties";
    String WRITE_MULTIPLE_PROPERTIES = "writemultipleproperties";

    String OBSERVE_ALL_PROPERTIES = "observeallproperties";
    String UNOBSERVE_ALL_PROPERTIES = "unobserveallproperties";

    String SUBSCRIBE_ALL_EVENTS = "subscribeallevents";
    String UNSUBSCRIBE_ALL_EVENTS = "unsubscribeallevents";

    String QUERY_ALL_ACTIONS = "queryallactions";
}
