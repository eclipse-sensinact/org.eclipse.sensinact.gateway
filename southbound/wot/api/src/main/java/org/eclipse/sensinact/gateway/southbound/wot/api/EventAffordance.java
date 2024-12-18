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

import org.eclipse.sensinact.gateway.southbound.wot.api.dataschema.DataSchema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventAffordance extends InteractionAffordance {

    public DataSchema subscription;
    public DataSchema data;
    public DataSchema dataResponse;
    public DataSchema cancellation;
}
