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

package org.eclipse.sensinact.gateway.southbound.wot.core;

public interface GeoNamespaces {

    String W3_GEO_WGS84_SCHEMA = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    String W3_GEO_WGS84_LATITUDE = W3_GEO_WGS84_SCHEMA + "lat";
    String W3_GEO_WGS84_LONGITUDE = W3_GEO_WGS84_SCHEMA + "long";
    String W3_GEO_WGS84_ALTITUDE = W3_GEO_WGS84_SCHEMA + "alt";

    String SCHEMA_ORG_SCHEMA = "https://schema.org#";
    String SCHEMA_ORG_GEO_COORDINATES = SCHEMA_ORG_SCHEMA + "GeoCoordinates";
}
