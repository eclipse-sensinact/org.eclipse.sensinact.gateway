/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.liveobjects.internal;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.liveobjects.LiveObjectsConstant;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author Rémi Druilhe
 */
public class LiveObjectsPacketReader extends SimplePacketReader<HttpPacket> {
    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing to interact
     *                 with the OSGi host environment
     */
    public LiveObjectsPacketReader(Mediator mediator) {
        super(mediator);
    }

    /**
     * @inheritDoc
     * @see SimplePacketReader#parse(Packet)
     */
    @Override
    public void parse(HttpPacket packet) throws InvalidPacketException {
        String content = new String(packet.getBytes());
        Object json = new JSONTokener(content).nextValue();
        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            if (jsonObject.has("data")) {
                JSONArray devices = jsonObject.optJSONArray("data");
                int length = devices == null ? 0 : devices.length();

                for (int i = 0; i < length; i++) {
                    try {
                        JSONObject jo = devices.optJSONObject(i);
                        if (JSONObject.NULL.equals(jo)) {
                            continue;
                        }
                        String serviceProviderId = jo.getString("namespace") + ":" + jo.getString("id").replace(" ", "");
                        super.setServiceProviderId(serviceProviderId);
                        super.setServiceId("admin");
                        super.setResourceId("connected");
                        super.setData(jo.getBoolean("connected"));
                        super.configure();

                    } catch (JSONException e) {
                        throw new InvalidPacketException(e);
                    }
                }
            }
        } else if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) json;
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            if (jsonArray.getJSONObject(0).has("streamId")) {
                String serviceProvider = jsonObject.getString("streamId").replace(LiveObjectsConstant.URN, "");
                super.setServiceProviderId(serviceProvider);
                super.setServiceId("test");
                super.setResourceId("temp");
                super.setData(jsonObject.getJSONObject("value").getDouble("temp"));
                super.setCommand(CommandType.GET);
                super.configure();
                String location = jsonObject.getJSONObject("location").getInt("lat") + ":" + jsonObject.getJSONObject("location").getInt("lon");
                super.setServiceProviderId(serviceProvider);
                super.setServiceId("admin");
                super.setResourceId("location");
                super.setData(location);
                super.setCommand(CommandType.GET);
                super.configure();
            }
        }
    }
}
