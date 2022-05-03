/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.api.persistence.dao;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Application {
    private final Logger LOG = LoggerFactory.getLogger(Application.class);
    private String name;
    private String diggest;
    private JSONObject content;

    public Application(String name, String diggest, JSONObject content) {
        this.name = name;
        this.diggest = diggest;
        this.content = content;
    }

    public Application(String name, JSONObject content) {
        this.name = name;
        try {
            this.diggest = new String(MessageDigest.getInstance("MD5").digest(content.toString().getBytes()));
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Impossible to calculate diggest of the application '{}'", name, e);
        }
        ;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject getContent() {
        return content;
    }

    public void setContent(JSONObject content) {
        this.content = content;
    }

    public String getDiggest() {
        return diggest;
    }
}
