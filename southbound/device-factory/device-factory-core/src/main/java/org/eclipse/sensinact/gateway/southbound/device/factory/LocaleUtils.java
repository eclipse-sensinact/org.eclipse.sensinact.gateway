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
*   Thomas Calmant (Kentyou) - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.device.factory;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods to find a locale
 */
public class LocaleUtils {

    private static final Logger logger = LoggerFactory.getLogger(LocaleUtils.class);

    /**
     * Try to find the locale from the given string.
     *
     * The string can be a language code, a language code and a country code or a
     * language code, a country code and a variant.
     * Parts are separated by an underscore character, for example: <code>fr_FR</code>
     *
     * @param strLocale Locale string
     * @return
     */
    public static Locale fromString(final String strLocale) {
        if (strLocale == null || strLocale.isBlank()) {
            return null;
        }

        final String[] parts = strLocale.split("_");
        switch (parts.length) {
        case 1:
            return new Locale(parts[0]);

        case 2:
            return new Locale(parts[0], parts[1]);

        case 3:
            return new Locale(parts[0], parts[1], parts[2]);

        default:
            // Invalid locale string
            logger.warn("Unhandled number locale {}", strLocale);
            return null;
        }
    }
}
