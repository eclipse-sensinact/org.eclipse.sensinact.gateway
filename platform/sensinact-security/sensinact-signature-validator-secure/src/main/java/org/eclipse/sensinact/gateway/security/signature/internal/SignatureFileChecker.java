/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.security.signature.internal;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;

public class SignatureFileChecker {
    // private static final Logger logger=
    // Logger.getLogger(LoggerConfig.JARVAL_LOGGER);
    private SignatureFileChecker() {
        // no action
    }

    private static boolean checkAbsenceOfModification(final SignedBundle signedJar, final SignatureFile pretendedSigFile) throws IOException {
        final Set<?> sigEntries = pretendedSigFile.getEntries().keySet();
        final Set<?> mfEntries = signedJar.getManifest().getEntries().keySet();
        // logger.log(Level.DEBUG, "sigEntries size: "+sigEntries.size());
        // logger.log(Level.DEBUG, "mfEntries size: "+mfEntries.size());
        boolean noAddition = false;
        boolean noRemoval = false;
        if (sigEntries.containsAll(mfEntries)) {
            noRemoval = true;
        }
        if (mfEntries.containsAll(sigEntries)) {
            noAddition = true;
        }
        // logger.log(Level.DEBUG, "no addition: "+noAddition);
        // logger.log(Level.DEBUG, "no removal: "+noRemoval);
        return noAddition && noRemoval;
    }

    private static boolean checkHashValuesValid(Mediator mediator, SignedBundle signedBundle, SignatureFile pretendedSigFile, CryptographicUtils cryptoUtils) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        boolean manifestEntriesValid = true;
        final Attributes pretendedMainAttributes = pretendedSigFile.getMainAttributes();

        final Map<?, ?> pretendedEntries = pretendedSigFile.getEntries();

        final Map<String, String> dataMap = ManifestChecker.extractEntryHashes(mediator, signedBundle.getEntry("/META-INF/MANIFEST.MF").openStream(), cryptoUtils, pretendedSigFile.getHashAlgo());
        // logger.log(Level.DEBUG, "number of entries: "+dataMap.size());
        String entryName, currentHash, pretendedHash;
        Map.Entry<String, String> entry;
        boolean currentEntryValid;
        for (final Iterator<Entry<String, String>> iter = dataMap.entrySet().iterator(); iter.hasNext() && manifestEntriesValid; ) {
            entry = iter.next();
            entryName = (String) entry.getKey();
            // logger.log(Level.ALL, "entry name: "+entryName);
            currentHash = (String) entry.getValue();
            // logger.log(Level.ALL, entryName+", "+currentHash);
            if ("SHA1-Digest-Manifest-Main-Attributes".equals(entryName)) {
                pretendedHash = (String) pretendedMainAttributes.getValue(entryName);
            } else if ("SHA-256-Digest-Manifest-Main-Attributes".equals(entryName)) {
                pretendedHash = (String) pretendedMainAttributes.getValue(entryName);
            } else {
                pretendedHash = ManifestChecker.getEntryHash((Attributes) pretendedEntries.get(entryName));
            }
            if (pretendedHash == null) {
                // logger.log(Level.DEBUG,
                // "No pretended Hash in Reference Signature File");
                manifestEntriesValid = false;
            } else {
                currentEntryValid = pretendedHash.equals(currentHash);
                // if(!currentEntryValid){
                // logger.log(Level.DEBUG, "entry "+entryName+" has an invalid
                // hash");
                // logger.log(Level.DEBUG,
                // "pretended hash: "+pretendedHash
                // +"; real hash: "+currentHash);
                // }
                manifestEntriesValid &= currentEntryValid;
            }
        }
        return manifestEntriesValid;
    }

    protected static boolean checkEntriesValidity(Mediator mediator, SignedBundle signedJar, SignatureFile pretendedSigFile, CryptographicUtils cryptoUtils) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        // logger.log(Level.DEBUG, "SignatureFileChecker.checkEntriesValidity");
        final boolean absenceOfModifications = SignatureFileChecker.checkAbsenceOfModification(signedJar, pretendedSigFile);
        // logger.log(Level.DEBUG,
        // "SignatureFileChecker: absence of modifications, "
        // +absenceOfModifications);
        // check that the hash values are valid
        final boolean hashesValid = SignatureFileChecker.checkHashValuesValid(mediator, signedJar, pretendedSigFile, cryptoUtils);
        // logger.log(Level.DEBUG,
        // "SignatureFileChecker: hashes valid, "+hashesValid);
        return absenceOfModifications && hashesValid;
    }
}
