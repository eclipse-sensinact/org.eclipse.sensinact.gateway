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
package org.eclipse.sensinact.gateway.southbound.device.factory;

/**
 * Definition of the placeholders to inject resources in the admin service
 */
public interface IPlaceHolderKeys {

    /**
     * Record value is the model package uri
     */
    String KEY_MODEL_PACKAGE_URI = "@modelPackageUri";

    /**
     * Record value is the model name
     */
    String KEY_MODEL = "@model";

    /**
     * Record value is the provider ID
     */
    String KEY_PROVIDER = "@provider";

    /**
     * Record value is the provider friendly name
     */
    String KEY_NAME = "@name";

    /**
     * Record value is a GeoJSON location object or an old-format SensiNact location
     * (<code>lat:lon[:alt]</code>)
     */
    String KEY_LOCATION = "@location";

    /**
     * Record value is a latitude (to use with {@link #KEY_LONGITUDE})
     */
    String KEY_LATITUDE = "@latitude";

    /**
     * Record value is a longitude (to use with {@link #KEY_LATITUDE})
     */
    String KEY_LONGITUDE = "@longitude";

    /**
     * Record value is an altitude (to use with {@link #KEY_LATITUDE} and
     * {@link #KEY_LONGITUDE})
     */
    String KEY_ALTITUDE = "@altitude";

    /**
     * Record value is a timestamp (auto-detection of ms vs s precision)
     */
    String KEY_TIMESTAMP = "@timestamp";

    /**
     * Record value is date-time string
     */
    String KEY_DATETIME = "@datetime";

    /**
     * Record value is "local" date string
     */
    String KEY_DATE = "@date";

    /**
     * Record value is local or an offset time string
     */
    String KEY_TIME = "@time";
}
