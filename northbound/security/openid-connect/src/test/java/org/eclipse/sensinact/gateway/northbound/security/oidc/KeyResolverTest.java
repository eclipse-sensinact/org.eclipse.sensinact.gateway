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
package org.eclipse.sensinact.gateway.northbound.security.oidc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.nio.file.Paths;
import java.security.Key;

import org.eclipse.sensinact.gateway.northbound.security.oidc.Certificates.KeyInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.json.JsonMapper;

import io.jsonwebtoken.JwsHeader;

@ExtendWith(MockitoExtension.class)
public class KeyResolverTest {

    @Mock
    JwsHeader header;

    @ParameterizedTest
    @ValueSource(strings = { "example-key.json", "example-key2.json" })
    void testKeyDecode(String file) throws Exception {
        File json = Paths.get("src/test/resources/public-key-examples", file).toFile();
        Certificates certificates = JsonMapper.builder().build().readValue(json, Certificates.class);

        KeyResolver keyResolver = new KeyResolver(certificates.getKeys());

        for (KeyInfo keyInfo : certificates.getKeys()) {

            if (!"sig".equals(keyInfo.use))
                continue;

            Mockito.when(header.getKeyId()).thenReturn(keyInfo.keyId);
            Mockito.when(header.getAlgorithm()).thenReturn(keyInfo.algorithm);

            Key key = keyResolver.locate(header);

            assertNotNull(key);
            assertEquals(keyInfo.type, key.getAlgorithm());
        }
    }

}
