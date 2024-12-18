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

package org.eclipse.sensinact.gateway.southbound.wot.api.dataschema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NumberSchema extends DataSchema {

    public Double minimum;
    public Double exclusiveMinimum;
    public Double maximum;
    public Double exclusiveMaximum;
    public Double multipleOf;

    public NumberSchema() {
        super("number");
    }
}
