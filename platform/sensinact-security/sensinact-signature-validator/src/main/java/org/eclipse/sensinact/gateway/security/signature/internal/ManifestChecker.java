/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.security.signature.internal;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.Attributes;

public class ManifestChecker {
    private ManifestChecker() {
    }

    private static byte[] getNextSection(byte[] rawManifestData, int offset) {
        int startOfNext = 0;
        int length = 0;
        boolean found = false;
        int line = 0;
        if (rawManifestData != null) {
            int j = offset;
            final int k = rawManifestData.length;
            int flag = 0;
            byte byte0;
            for (; j < k && !found; j++) {
                byte0 = rawManifestData[j];
                int c =((byte0==13)||(byte0==10))?1:0;
                boolean s = byte0==32;
                if(c == 1) {
                	flag+=c;
                	continue;
                }
                if((flag=s?0:flag)==0) {
                	continue;
                }
                line+=1;
                if(line == 2) {
                	startOfNext = j;
                	found = true;
                } else {
                	flag=0;
                }                
            }
        }        
        if(startOfNext == 0) {
        	startOfNext = rawManifestData.length;
        }
        length = startOfNext - offset;
        if (length < 0) {
            length = 0;
        }
        if (length > 0) {
        	byte[] newSectionData = new byte[length];
            System.arraycopy(rawManifestData, offset, newSectionData, 0, length);
            return newSectionData;
        }
        return null;
    }

    public static Map<String, String> extractEntryHashes(Mediator mediator, InputStream iStream, CryptographicUtils cryptoUtils, String hashAlgo) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        final Map<String, String> dataMap = new HashMap<String, String>();
        final byte[] fileData = IOUtils.read(iStream,true);
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
            if(entryName != null && entryName.length()>0) {
            	entryHash = cryptoUtils.getHashValue(currentEntryData, shaDigestType);
            	dataMap.put(entryName, entryHash);
            }
            offset += currentEntryData.length;
        }        
        return dataMap;
    }

    protected static String getEntryHash(final Attributes atts) {
        String entryHash = "";
        if (atts != null) {
            final Iterator iter = atts.entrySet().iterator();
            if (iter.hasNext()) {
                entryHash = (String) ((Map.Entry) iter.next()).getValue();
            }
        }
        return entryHash;
    }

    private static String getEntryName(byte[] currentEntryData) throws IOException {
    	String nameHeader = "Name: ", line;
        byte[] buffer = new byte[currentEntryData.length];
        int pos = 0;
        int i = 0;
        byte[] prefix = nameHeader.getBytes();
        for(i=0;i<prefix.length;i++) {
        	if(currentEntryData[i]!=prefix[i]) {
        		break;
        	}
        }
        if(i==prefix.length){
	        for(i=6;i<currentEntryData.length;i++) {
	        	if((currentEntryData[i]==10 && i < currentEntryData.length-2  && currentEntryData[i+1]==13 && currentEntryData[i+2]==32)
			        	||(currentEntryData[i]==13 && i < currentEntryData.length-2  && currentEntryData[i+1]==10 && currentEntryData[i+2]==32)) {
			        		i+=2;
			        		continue;
			    }
	        	if((currentEntryData[i]==10 && i < currentEntryData.length-1 && currentEntryData[i+1]==32)
		        	||(currentEntryData[i]==13 && i < currentEntryData.length-1 && currentEntryData[i+1]==32)) {
		        		i+=1;
		        		continue;
		        }
	        	if((currentEntryData[i]==10 && i < currentEntryData.length-2  && currentEntryData[i+1]==13 && currentEntryData[i+2]!=32)
		        	||(currentEntryData[i]==13 && i < currentEntryData.length-2  && currentEntryData[i+1]==10 && currentEntryData[i+2]!=32)) {
		        		break;
		        }
	        	buffer[pos++] = currentEntryData[i];
	        }
        }
        if(pos > 0) {
        	byte[] copy = new byte[pos];
        	System.arraycopy(buffer,0,copy,0,pos);
        	line = new String(copy);
        	return line;
        }     
        return null;
    }

}
