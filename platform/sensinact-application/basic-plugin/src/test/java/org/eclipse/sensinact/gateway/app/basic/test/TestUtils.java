/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.basic.test;

import org.eclipse.sensinact.gateway.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class TestUtils {
    public static String readFile(InputStream stream, Charset encoding) throws IOException {
        String output = new String(IOUtils.read(stream, stream.available(), true), encoding);
        stream.close();
        return output;
    }
}