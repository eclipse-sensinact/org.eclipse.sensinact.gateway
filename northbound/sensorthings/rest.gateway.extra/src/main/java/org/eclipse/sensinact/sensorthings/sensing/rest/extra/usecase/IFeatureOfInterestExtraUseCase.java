/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;

public interface IFeatureOfInterestExtraUseCase {
    /**
     * get feature of interest base on id else null
     *
     * @param id
     * @return
     */
    public FeatureOfInterest getInMemoryFeatureOfInterest(String id);

    /**
     * remove feature of interest base on id if exists
     *
     * @param id
     * @return
     */
    public FeatureOfInterest removeInMemoryFeatureOfInterest(String id);

}
