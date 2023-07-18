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
package org.eclipse.sensinact.northbound.filters.sensorthings;

/**
 * Context of use of a filter (endpoint in use)
 */
public enum EFilterContext {

    THINGS, LOCATIONS, HISTORICAL_LOCATIONS, DATASTREAMS, SENSORS, OBSERVATIONS, OBSERVED_PROPERTIES,
    FEATURES_OF_INTEREST,
}
