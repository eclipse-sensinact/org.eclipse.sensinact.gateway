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
package org.eclipse.sensinact.gateway.sthbnd.liveobjects.task;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpConnectionConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpRequest;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpAuthenticationTask;
import org.eclipse.sensinact.gateway.sthbnd.liveobjects.LiveObjectsConstant;
import org.json.JSONObject;

/**
 * @author Rémi Druilhe
 */
public class LiveObjectsUserAuthentication extends HttpAuthenticationTask<SimpleHttpResponse, SimpleHttpRequest> {
    private final String email;
    private final String login;
    private final String password;

    public LiveObjectsUserAuthentication(Mediator mediator, HttpProtocolStackEndpoint transmitter, String email, String login, String password) {
        super(mediator, transmitter, SimpleHttpRequest.class);
        this.email = email;
        this.login = login;
        this.password = password;
        super.setAuthenticationHeaderKey(LiveObjectsConstant.X_API_KEY);
        Executable<Object, String> tokenExtractor = new Executable<Object, String>() {
            @Override
            public String execute(Object parameter) throws Exception {

                JSONObject content = new JSONObject(new String((byte[]) parameter));
                String token = content.getJSONObject(LiveObjectsConstant.JSON_FIELD_APIKEY).getString(LiveObjectsConstant.JSON_FIELD_APIKEY_VALUE);
                return token;
            }
        };
        super.registerTokenExtractor(tokenExtractor);
    }

    @Override
    public String getUri() {
        return LiveObjectsConstant.ROOT_URL + LiveObjectsConstant.ROOT_PATH + "auth?cookie=false";
    }

    @Override
    public Object getContent() {
        JSONObject json = new JSONObject();
        json.put("email", email);
        json.put("login", login);
        json.put("password", password);
        return json.toString();
    }

    @Override
    public String getHttpMethod() {
        return HttpConnectionConfiguration.POST;
    }

    @Override
    public String getAccept() {
        return "application/json";
    }

    @Override
    public String getContentType() {
        return "application/json";
    }
}
