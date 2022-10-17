/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.security.signature.internal;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

public class SignedBundleManifest extends Manifest {

    public SignedBundleManifest(Mediator mediator, InputStream iStream, CryptographicUtils cryptoUtils) throws IOException {
        super(iStream);
    }

    /**
     * A method for retrieving the main attributes of a manifest file as bytes.
     *
     * @param jar
     * @return byte[], the matching bytes
     */
    public static byte[] getManifestMainAttributesAsBytes(final Manifest manifest) throws IOException {
        final ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        manifest.write(oStream);
        oStream.flush();
        final byte[] manifestData = oStream.toByteArray();
        return getManifestMainAttributesAsBytes(manifestData);
    }

    public static byte[] getManifestMainAttributesAsBytes(final byte[] rawData) throws IOException {
        byte[] manifestMainAtts = null;
        if (rawData != null) {
            int mainAttsLength = getMainAttributesLength(rawData);
            manifestMainAtts = new byte[mainAttsLength];
            System.arraycopy(rawData, 0, manifestMainAtts, 0, mainAttsLength);
        }
        if (manifestMainAtts == null) {
            manifestMainAtts = new byte[0];
        }
        return manifestMainAtts;
    }

    public static byte[] getManifestMainAttributesAsBytes(Mediator mediator, InputStream manifestInputStream) throws IOException {
        // get manifest data
        byte[] rawData = IOUtils.read(manifestInputStream);
        // get main attributes of manifest
        return SignedBundleManifest.getManifestMainAttributesAsBytes(rawData);
    }

    public static int getMainAttributesLength(final byte[] rawManifestData) {
        int startOfNext = 0;
        if (rawManifestData != null) {
            int j = 0;
            int k = rawManifestData.length;
            boolean flag = true;
            for (; j < k; j++) {
                byte byte0 = rawManifestData[j];
                switch (byte0) {
                    case 13: // '\r'
                        if (j < k && rawManifestData[j + 1] == 10) j++;
                        // fall through
                    case 10: // '\n'
                        if (flag || j == k - 1) {
                            startOfNext = j + 1;
                            return startOfNext;
                        }
                        flag = true;
                        break;
                    default:
                        flag = false;
                        break;
                }
            }
        }
        return startOfNext;
    }
}
