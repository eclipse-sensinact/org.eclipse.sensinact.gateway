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
package org.eclipse.sensinact.gateway.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe d'aide à l'interfacage avec le service paybox.
 * <p>
 * Toutes les informations parameterables sont sous la forme paybox.*
 */
public final class SignatureUtil {
    private static final Logger LOGGER = Logger.getLogger(SignatureUtil.class.getCanonicalName());
    private static final String BEGIN = "-----BEGIN ";
    private static final String END = "-----END ";
    /**
     * The Constant CHARSET.
     */
    private static final String CHARSET = "utf-8";
    /**
     * The Constant ENCRYPTION_ALGORITHM.
     */
    private static final String ENCRYPTION_ALGORITHM = "RSA";
    /**
     * The Constant HASH_ENCRIPTION_ALGORITHM.
     */
    private static final String HASH_ENCRYPTION_ALGORITHM = "SHA1withRSA";

    /**
     * constructeur privé pour classe statique.
     */
    private SignatureUtil() {
    }

    /**
     * Controle si une signature est bien celle du message à l'aide de la clé
     * publique de l'emmeteur?.
     *
     * @param message le message
     * @param sign    la signature
     * @param keyPath le chemin vers la clé publique.
     * @return true si la signature est bien celle du message avec la clé privé
     * attendue.
     */
    public static boolean checkSign(String message, String sign, String keyPath) {
        boolean ret = false;
        try {
            ret = SignatureUtil.verify(message, sign, SignatureUtil.getKey(keyPath));
        } catch (Exception e) {
            LOGGER.log(Level.CONFIG, e.getMessage(), e);
        }
        return ret;
    }

    /**
     * Récupère la clé publique à partir du chemin passé en paramètre.
     *
     * @param keyPath le chemin vers la clé.
     * @return la clé publique
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws IOException              Signals that an I/O exception has occurred.
     * @throws InvalidKeySpecException  the invalid key spec exception
     */
    private static PublicKey getKey(String keyPath) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        final KeyFactory keyFactory = KeyFactory.getInstance(SignatureUtil.ENCRYPTION_ALGORITHM);

        BufferedReader reader = new BufferedReader(new FileReader(keyPath));
        try {
            String line = reader.readLine();

            int index = -1;
            String type = null;
            byte[] content = null;

            Map<String, String> headers = new HashMap<String, String>();

            if (line != null && line.startsWith(BEGIN)) {
                line = line.substring(BEGIN.length());
                index = line.indexOf('-');
                type = line.substring(0, index);

                if (index > 0) {
                    String endMarker = END + type;
                    StringBuffer buf = new StringBuffer();

                    while ((line = reader.readLine()) != null) {
                        if (line.indexOf(":") >= 0) {
                            index = line.indexOf(':');
                            String hdr = line.substring(0, index);
                            String value = line.substring(index + 1).trim();
                            headers.put(hdr, value);
                            continue;
                        }
                        if (line.indexOf(endMarker) != -1) {
                            break;
                        }
                        buf.append(line.trim());
                    }
                    if (line == null) {
                        throw new IOException(endMarker + " not found");
                    }
                    content = Base64.getDecoder().decode(buf.toString());
                }
                return keyFactory.generatePublic(new X509EncodedKeySpec(content));
            }
        } finally {
            reader.close();
        }
        return null;
    }

    /**
     * effectue la vérification du message en fonction de la
     * signature et de la clé.
     *
     * @param message   le message
     * @param sign      la signature
     * @param publicKey la clé publique.
     * @return true, if successful
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     * @throws IOException
     */
    private static boolean verify(String message, String sign, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        final Signature sig = Signature.getInstance(SignatureUtil.HASH_ENCRYPTION_ALGORITHM);

        sig.initVerify(publicKey);
        sig.update(message.getBytes(SignatureUtil.CHARSET));
        byte[] bytes = java.util.Base64.getDecoder().decode(URLDecoder.decode(sign, SignatureUtil.CHARSET));
        return sig.verify(bytes);
    }
}