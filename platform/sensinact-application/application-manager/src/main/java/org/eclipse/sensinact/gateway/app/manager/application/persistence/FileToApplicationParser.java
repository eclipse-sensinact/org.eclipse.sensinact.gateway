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
package org.eclipse.sensinact.gateway.app.manager.application.persistence;

import org.eclipse.sensinact.gateway.app.api.persistence.dao.Application;
import org.eclipse.sensinact.gateway.app.manager.application.persistence.exception.ApplicationParseException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class FileToApplicationParser {
    public static Application parse(String filepath) throws ApplicationParseException {
        try {
            File file = new File(filepath);
            List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
            StringBuffer sb = new StringBuffer();
            for (String line : lines) sb.append(line);
            final String content = sb.toString();
            JSONObject jsonFileContent = new JSONObject(content);
            JSONArray array = jsonFileContent.getJSONArray("parameters");
            final String applicationName = array.getJSONObject(0).getString("value");
            final JSONObject jsonApplicationContent = array.getJSONObject(1).getJSONObject("value");
            final String filename = file.getName();
            final Integer indexOfLastDot = filename.lastIndexOf('.');
            String filenameNoExtention = null;
            //@TODO: Verify if the validation works
            if (indexOfLastDot != -1) {
                filenameNoExtention = filename.substring(0, indexOfLastDot);
            } else {
                filenameNoExtention = filename;
            }
            if (!applicationName.equals(filenameNoExtention)) {
                throw new ApplicationParseException("The file name and the application name should be the same");
            }
            final String diggest = new String(MessageDigest.getInstance("MD5").digest(content.getBytes()));
            Application application = new Application(applicationName, diggest, jsonFileContent);//jsonApplicationContent
            return application;
        } catch (IOException e) {
            throw new ApplicationParseException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new ApplicationParseException(e);
        }
    }
}
