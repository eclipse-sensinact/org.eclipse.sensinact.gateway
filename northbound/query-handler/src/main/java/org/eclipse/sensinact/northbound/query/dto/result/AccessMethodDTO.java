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
package org.eclipse.sensinact.northbound.query.dto.result;

import java.util.ArrayList;
import java.util.List;

/**
 * Description of an access method (GET, SET, ACT)
 */
public class AccessMethodDTO {

    /**
     * Method name
     */
    public String name;

    /**
     * Method parameters
     *
     * FIXME: should it be named-based with current implementation?
     */
    public List<AccessMethodParameterDTO> parameters = new ArrayList<>();
}
