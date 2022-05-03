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

import org.json.JSONArray;
import org.json.JSONObject;

public enum KodiApi {
    GETADDONS("Addons.GetAddons") {
        /**
         * @inheritDoc
         *
         * @see KodiApi#getData(org.json.JSONObject)
         */
        @Override
        public Object getData(Object result) throws Exception {
            return ((JSONObject) result).getJSONArray("addons");
        }

        /**
         * @inheritDoc
         *
         * @see KodiApi#getContent(java.lang.Object[])
         */
        @Override
        public JSONObject getContent(Object[] parameters) throws Exception {
            return null;
        }
    }, GETSOURCES("Files.GetSources") {
        /**
         * @inheritDoc
         *
         * @see KodiApi#getData(org.json.JSONObject)
         */
        @Override
        public Object getData(Object result) throws Exception {
            JSONArray files = ((JSONObject) result).getJSONArray("files");
            JSONArray movies = new JSONArray();
            for (int i = 0; i < files.length(); i++) {
                movies.put(files.getJSONObject(i).getString("file"));
            }
            return movies;
        }

        /**
         * @inheritDoc
         *
         * @see KodiApi#getContent(java.lang.Object[])
         */
        @Override
        public JSONObject getContent(Object[] parameters) throws Exception {
            return new JSONObject().put("media", "video");
        }
    }, GETDIRECTORY("Files.GetDirectory") {
        /**
         * @inheritDoc
         *
         * @see KodiApi#getData(org.json.JSONObject)
         */
        @Override
        public Object getData(Object result) throws Exception {
            JSONArray sources = ((JSONObject) result).getJSONArray("sources");
            return sources.getJSONObject(0).getString("file");
        }

        /**
         * @throws Exception
         * @inheritDoc
         *
         * @see KodiApi#getContent(java.lang.Object[])
         */
        @Override
        public JSONObject getContent(Object[] parameters) throws Exception {
            return new JSONObject().put("media", "video").put("directory", parameters[0]);
        }
    }, GETVOLUME("Application.GetProperties") {
        /**
         * @inheritDoc
         *
         * @see KodiApi#getData(org.json.JSONObject)
         */
        @Override
        public Object getData(Object result) throws Exception {
            return ((JSONObject) result).getInt("volume");
        }

        /**
         * @inheritDoc
         *
         * @see KodiApi#getContent(java.lang.Object[])
         */
        @Override
        public JSONObject getContent(Object[] parameters) throws Exception {
            return new JSONObject().put("properties", new JSONArray().put("volume"));
        }
    }, PLAYEROPEN("Player.Open") {
        /**
         * @inheritDoc
         *
         * @see KodiApi#getContent(java.lang.Object[])
         */
        @Override
        public JSONObject getContent(Object[] parameters) throws Exception {
            return new JSONObject().put("item", new JSONObject().put("file", parameters[0]));
        }
    }, OTHER(null);

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
     * JSONObject} passed as parameter
     *
     * @param result the {@link JSONObject} containing the
     *               data object to be extracted
     * @return the extracted data object from the {@link JSONObject}
     * argument
     * @throws Exception
     */
    public Object getData(Object result) throws Exception {
        return null;
    }

    /**
     * Returns the {@link JSONObject} to be posted to access
     * this Kodi API
     *
     * @return the {@link JSONObject} to be posted
     * @throws Exception
     */
    public JSONObject getContent(Object[] parameters) throws Exception {
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
