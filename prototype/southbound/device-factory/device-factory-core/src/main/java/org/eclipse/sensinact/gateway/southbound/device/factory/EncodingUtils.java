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

import java.util.Arrays;

/**
 *
 */
public class EncodingUtils {

    /**
     * Removes the UTF-8 Byte Order Mark from the given input. Make sure the
     * expected encoding is UTF-8 before calling this method
     *
     * @param rawInput Bytes of an UTF-8 content
     * @return The content without the BOM prefix
     */
    public static byte[] removeBOM(final byte[] rawInput) {
        if (rawInput != null && rawInput.length > 3) {
            final int char0 = rawInput[0] & 0xFF;
            final int char1 = rawInput[1] & 0xFF;
            final int char2 = rawInput[2] & 0xFF;

            if (char0 == 0xEF && char1 == 0xBB && char2 == 0xBF) {
                // Got an UTF-8 with BOM, remove the BOM marker
                return Arrays.copyOfRange(rawInput, 3, rawInput.length);
            }
        }

        return rawInput;
    }
}
