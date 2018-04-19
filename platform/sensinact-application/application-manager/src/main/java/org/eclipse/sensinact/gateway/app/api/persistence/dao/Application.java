package org.eclipse.sensinact.gateway.app.api.persistence.dao;

import org.json.JSONObject;

public class Application {

    private String name;
    private String diggest;
    private JSONObject content;

    public Application(String name, String diggest, JSONObject content) {
        this.name = name;
        this.diggest = diggest;
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
