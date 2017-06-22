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
package org.eclipse.sensinact.gateway.sthbnd.liveobjects.internal;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.liveobjects.LiveObjectsConstant;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author Rémi Druilhe
 */
public class LiveObjectsPacketReader extends SimplePacketReader<HttpPacket> {

    LiveObjectsPacketReader(Mediator mediator) {
        super(mediator);
    }

    /**
     * @inheritDoc
     *
     * @see SimplePacketReader#parse(Packet)
     */
    @Override
    public void parse(HttpPacket packet) throws InvalidPacketException {
        String content = new String(packet.getBytes());
        Object json = new JSONTokener(content).nextValue();

        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject)json;

            if(jsonObject.has("data")) {
                JSONArray devices = jsonObject.getJSONArray("data");

                for(int i = 0; i < devices.length(); i++) {
                    super.setServiceProviderId(devices.getJSONObject(i).getString("namespace") + ":" +
                            devices.getJSONObject(i).getString("id").replace(" ", ""));
                    super.setServiceId("admin");
                    super.setResourceId("connected");
                    super.setData(devices.getJSONObject(i).getBoolean("connected"));
                    super.configure();
                }
            }
        } else if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray)json;

            JSONObject jsonObject = jsonArray.getJSONObject(0);

            if(jsonArray.getJSONObject(0).has("streamId")) {
                String serviceProvider = jsonObject.getString("streamId").replace(LiveObjectsConstant.URN, "");

                super.setServiceProviderId(serviceProvider);
                super.setServiceId("test");
                super.setResourceId("temp");
                super.setData(jsonObject.getJSONObject("value").getDouble("temp"));
                super.setCommand(CommandType.GET);
                super.configure();

                String location = jsonObject.getJSONObject("location").getInt("lat") + ":" +
                        jsonObject.getJSONObject("location").getInt("lon");

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
