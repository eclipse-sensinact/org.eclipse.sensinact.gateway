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
package org.eclipse.sensinact.sensorthings.sensing.rest.access;

import java.util.List;

/**
 * memory repository cache for storing dto that are not assign to datastream
 *
 * @param <M>
 */
public interface IDtoMemoryCache<M> {

    /**
     * add dto in cache on id
     *
     * @param id
     * @param dto
     */
    public void addDto(String id, M dto);

    /**
     * remove dto by id if exists
     *
     * @param id
     */
    public void removeDto(String id);

    /**
     * get the dto by id
     *
     * @param id
     * @return
     */
    public M getDto(String id);

    /**
     * return list of dto cached
     *
     * @return
     */
    public List<M> values();

    /**
     * get type of DTO to store in cache
     */
    public Class<M> getType();
}
