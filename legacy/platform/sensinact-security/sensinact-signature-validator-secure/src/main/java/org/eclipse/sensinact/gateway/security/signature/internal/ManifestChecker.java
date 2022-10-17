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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.Attributes;

public class ManifestChecker {
    private ManifestChecker() {
        // no action
    }

    public static byte[] getNextSection(final byte[] fileData, final int offset) {
        // final Logger logger = Logger.getLogger(LoggerConfig.JARVAL_LOGGER);
        final int nextSectionLength = ManifestChecker.getNextSectionLength(fileData, offset);

        // logger.log(Level.ALL, "section length: "+nextSectionLength);
        final byte[] newSectionData = new byte[nextSectionLength];
        if (nextSectionLength != 0) {
            System.arraycopy(fileData, offset, newSectionData, 0, nextSectionLength);
        }
        return newSectionData;
    }

    private static int getNextSectionLength(final byte[] rawManifestData, final int offset) {
        int startOfNext = 0;
        int length = 0;
        boolean found = false;
        if (rawManifestData != null) {
            int j = offset;
            final int k = rawManifestData.length;
            boolean flag = true;
            byte byte0;
            for (; j < k && !found; j++) {
                byte0 = rawManifestData[j];
                //final byte[] array = { byte0 };
                switch (byte0) {
                    case 13: // '\r'
                        if (j < k && rawManifestData[j + 1] == 10) {
                            j++;
                        }
                        if (flag || j == k - 1) {
                            startOfNext = j + 1;
                            found = true;
                            length = startOfNext - offset;
                        }
                        flag = true;
                        break;
                    case 10: // '\n'
                        if (flag || j == k - 1) {
                            startOfNext = j + 1;
                            found = true;
                            length = startOfNext - offset;
                        }
                        flag = true;
                        break;
                    default:
                        flag = false;
                        break;
                }
            }
        }
        if (!found) {
            length = startOfNext - offset;
        }
        // to check
        if (length < 0) {
            length = 0;
        }
        return length;
    }

    public static Map<String, String> extractEntryHashes(Mediator mediator, InputStream iStream, CryptographicUtils cryptoUtils, String hashAlgo) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        final Map<String, String> dataMap = new HashMap<String, String>();
        final byte[] fileData = IOUtils.read(iStream);
        String shaDigestType = null;
        String shaDigestMfMain = null;

        if ("SHA-1".equals(hashAlgo)) {
            shaDigestType = "SHA1-Digest";
            shaDigestMfMain = "SHA1-Digest-Manifest-Main-Attributes";

        } else if ("SHA-256".equals(hashAlgo)) {
            shaDigestType = "SHA-256-Digest";
            shaDigestMfMain = "SHA-256-Digest-Manifest-Main-Attributes";

        } else {
            //let's try
            shaDigestType = hashAlgo + "-Digest";
            shaDigestMfMain = hashAlgo + "-Digest-Manifest-Main-Attributes";
        }
        // get main attributes
        final byte[] mfMainAttributes = SignedBundleManifest.getManifestMainAttributesAsBytes(fileData);

        final String mfMainAttributesHash = cryptoUtils.getHashValue(mfMainAttributes, shaDigestType);
        dataMap.put(shaDigestMfMain, mfMainAttributesHash);
        // for all entries
        // get entry
        byte[] currentEntryData;
        String entryName, entryHash;
        int offset = mfMainAttributes.length;

        while ((currentEntryData = ManifestChecker.getNextSection(fileData, offset)) != null && currentEntryData.length > 0) {
            entryName = ManifestChecker.getEntryName(currentEntryData);
            entryHash = cryptoUtils.getHashValue(currentEntryData, shaDigestType);
            dataMap.put(entryName, entryHash);
            offset += currentEntryData.length;
        }
        return dataMap;
    }

    protected static String getEntryHash(final Attributes atts) {
        String entryHash = "";
        if (atts != null) {
            final Iterator<Map.Entry<Object, Object>> iter = atts.entrySet().iterator();
            if (iter.hasNext()) {
                entryHash = (String) ((Map.Entry<Object, Object>) iter.next()).getValue();
            }
        }
        return entryHash;
    }

    private static String getEntryName(byte[] currentEntryData) throws IOException {
        String entryName = "", nameHeader = "Name: ", line;
        BufferedReader bReader = null;
        try {
            bReader = new BufferedReader(new InputStreamReader(removeShortLines(new ByteArrayInputStream(currentEntryData))));

            boolean nameFound = false;
            while (!nameFound && (line = bReader.readLine()) != null) {
                if (line.startsWith(nameHeader)) {
                    entryName = line.substring(nameHeader.length());
                    nameFound = true;
                }
            }
        } finally {
            if (bReader != null) {
                try {
                    bReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return entryName;
    }

    public static InputStream removeShortLines(InputStream iStream) throws IOException {
        BufferedReader bReader = new BufferedReader(new InputStreamReader(iStream));
        String line;
        int shortlineLength = 70;
        StringBuffer longLineData = new StringBuffer();
        ;
        while ((line = bReader.readLine()) != null) {
            longLineData = addLine(bReader, longLineData, line, shortlineLength);
        }
        return new ByteArrayInputStream(longLineData.toString().getBytes());
    }

    public static StringBuffer addLine(final BufferedReader bReader, final StringBuffer longLineData, final String line, final int shortlineLength) throws IOException {
        String currentLine = line;
        if (currentLine != null) {
            if (currentLine.length() < shortlineLength) {
                longLineData.append(currentLine).append("\r\n");
            } else if (currentLine.length() == shortlineLength) {
                longLineData.append(currentLine);
                while ((currentLine = bReader.readLine()).startsWith(" ")) {
                    longLineData.append(currentLine.substring(1));
                }
                longLineData.append("\r\n");
                addLine(bReader, longLineData, currentLine, shortlineLength);
            }
        }
        return longLineData;
    }
}
