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