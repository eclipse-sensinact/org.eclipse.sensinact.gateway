/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.generic.dto;

import java.util.List;

/**
 * A special update dto type where multiple values are updated in a single event
 */
public final class BulkGenericDto {

    public List<GenericDto> dtos;

}