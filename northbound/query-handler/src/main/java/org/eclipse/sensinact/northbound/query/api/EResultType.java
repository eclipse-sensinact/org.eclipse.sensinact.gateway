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
package org.eclipse.sensinact.northbound.query.api;

/**
 * List of possible result types
 */
public enum EResultType {

    ERROR,
    COMPLETE_LIST,
    PROVIDERS_LIST,
    SERVICES_LIST,
    RESOURCES_LIST,
    DESCRIBE_PROVIDER,
    DESCRIBE_SERVICE,
    DESCRIBE_RESOURCE,
    GET_RESPONSE,
    SET_RESPONSE,
    ACT_RESPONSE,
    SUBSCRIPTION_RESPONSE,
    SUBSCRIPTION_NOTIFICATION,
    UNSUBSCRIPTION_RESPONSE,
}
