/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.liveobjects.task;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpConnectionConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpRequest;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpAuthenticationTask;
import org.eclipse.sensinact.gateway.sthbnd.liveobjects.LiveObjectsConstant;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;

import jakarta.json.JsonObject;

/**
 * @author Rémi Druilhe
 */
public class LiveObjectsUserAuthentication extends HttpAuthenticationTask<SimpleHttpResponse, SimpleHttpRequest> {
    private final String email;
    private final String login;
    private final String password;
    
    private final ObjectMapper mapper = JsonMapper.builder()
    		.addModule(new JSONPModule(JsonProviderFactory.getProvider()))
    		.build();

    public LiveObjectsUserAuthentication(HttpProtocolStackEndpoint transmitter, String email, String login, String password) {
        super(transmitter, SimpleHttpRequest.class);
        this.email = email;
        this.login = login;
        this.password = password;
        super.setAuthenticationHeaderKey(LiveObjectsConstant.X_API_KEY);
        Executable<Object, String> tokenExtractor = new Executable<Object, String>() {
            @Override
            public String execute(Object parameter) throws Exception {

                JsonObject content = mapper.readValue((byte[]) parameter, JsonObject.class);
                String token = content.getJsonObject(LiveObjectsConstant.JSON_FIELD_APIKEY).getString(LiveObjectsConstant.JSON_FIELD_APIKEY_VALUE);
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
    	Map<String, Object> map = new HashMap<>();
    	map.put("email", email);
    	map.put("login", login);
    	map.put("password", password);
    	try {
    		return mapper.writeValueAsString(map);
    	} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
