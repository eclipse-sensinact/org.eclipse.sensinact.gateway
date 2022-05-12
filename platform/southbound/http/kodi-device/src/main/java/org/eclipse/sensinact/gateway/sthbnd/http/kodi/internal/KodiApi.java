/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.kodi.internal;

import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public enum KodiApi {
	
    GETADDONS("Addons.GetAddons") {
        /**
         * @inheritDoc
         *
         * @see KodiApi#getData(jakarta.json.JsonObject)
         */
        @Override
        public JsonValue getData(JsonObject result) throws Exception {
            return result.getJsonArray("addons");
        }

        /**
         * @inheritDoc
         *
         * @see KodiApi#getContent(java.lang.Object[])
         */
        @Override
        public JsonObject getContent(Object[] parameters) throws Exception {
            return null;
        }
    }, GETSOURCES("Files.GetSources") {
        /**
         * @inheritDoc
         *
         * @see KodiApi#getData(jakarta.json.JsonObject)
         */
        @Override
        public JsonValue getData(JsonObject result) throws Exception {
            JsonArray files = result.getJsonArray("files");
            
            ArrayNode movies = mapper.createArrayNode();
            
            for (int i = 0; i < files.size(); i++) {
                movies.add(files.getJsonObject(i).getString("file"));
            }
            return mapper.convertValue(movies, JsonArray.class);
        }

        /**
         * @inheritDoc
         *
         * @see KodiApi#getContent(java.lang.Object[])
         */
        @Override
        public JsonObject getContent(Object[] parameters) throws Exception {
        	ObjectNode node = mapper.createObjectNode();
        	node.put("media", "video");
            return mapper.convertValue(node, JsonObject.class);
        }
    }, GETDIRECTORY("Files.GetDirectory") {
        /**
         * @inheritDoc
         *
         * @see KodiApi#getData(jakarta.json.JsonObject)
         */
        @Override
        public JsonValue getData(JsonObject result) throws Exception {
            JsonArray sources = result.getJsonArray("sources");
            return sources.getJsonObject(0).getValue("file");
        }

        /**
         * @throws Exception
         * @inheritDoc
         *
         * @see KodiApi#getContent(java.lang.Object[])
         */
        @Override
        public JsonObject getContent(Object[] parameters) throws Exception {
        	ObjectNode node = mapper.createObjectNode();
        	node.put("media", "video");
        	node.put("directory", parameters[0].toString());
            return mapper.convertValue(node, JsonObject.class);
        }
    }, GETVOLUME("Application.GetProperties") {
        /**
         * @inheritDoc
         *
         * @see KodiApi#getData(jakarta.json.JsonObject)
         */
        @Override
        public JsonValue getData(JsonObject result) throws Exception {
            return result.getJsonNumber("volume");
        }

        /**
         * @inheritDoc
         *
         * @see KodiApi#getContent(java.lang.Object[])
         */
        @Override
        public JsonObject getContent(Object[] parameters) throws Exception {
        	ObjectNode o = mapper.createObjectNode();
        	ArrayNode a = mapper.createArrayNode();
        	o.set("properties", a);
        	a.add("volume");
            return mapper.convertValue(o, JsonObject.class);
        }
    }, PLAYEROPEN("Player.Open") {
        /**
         * @inheritDoc
         *
         * @see KodiApi#getContent(java.lang.Object[])
         */
        @Override
        public JsonObject getContent(Object[] parameters) throws Exception {
        	ObjectNode o = mapper.createObjectNode();
        	ObjectNode o2 = mapper.createObjectNode();
        	o.set("item", o2);
        	o2.put("file", parameters[0].toString());
            return mapper.convertValue(o, JsonObject.class);
        }
    }, OTHER(null);

	protected final ObjectMapper mapper = JsonMapper.builder()
    		.addModule(new JSONPModule(JsonProviderFactory.getProvider()))
    		.build();
	
    /**
     * this KodiApi's name
     */
    private final String name;

    /**
     * Constructor
     *
     * @param name the string name
     *             of the KodiApi to be instantiated
     */
    KodiApi(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this KodiApi
     *
     * @return this KodiApi's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Extracts and returns the data object from the {@link
     * JsonObject} passed as parameter
     *
     * @param result the {@link JsonObject} containing the
     *               data object to be extracted
     * @return the extracted data object from the {@link JsonObject}
     * argument
     * @throws Exception
     */
    public JsonValue getData(JsonObject result) throws Exception {
        return null;
    }

    /**
     * Returns the {@link JsonObject} to be posted to access
     * this Kodi API
     *
     * @return the {@link JsonObject} to be posted
     * @throws Exception
     */
    public JsonObject getContent(Object[] parameters) throws Exception {
        return null;
    }

    /**
     * Retrieves and returns the KodiApi accordingly to
     * the string name passed as parameter
     *
     * @param name the name of the KodiApi to be
     *             returned
     * @return the KodiApi whose name is passed  as
     * parameter or null if it does not exist
     */
    public static final KodiApi fromName(String name) {
        if (name == null) {
            return null;
        }
        KodiApi[] apis = KodiApi.values();
        int index = 0;
        int length = apis == null ? 0 : apis.length;

        for (; index < length; index++) {
            if (name.equalsIgnoreCase(apis[index].getName())) {
                return apis[index];
            }
        }
        return null;
    }
}
