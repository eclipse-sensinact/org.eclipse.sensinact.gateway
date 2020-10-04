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
package org.eclipse.sensinact.gateway.sthbnd.http.openweather.internal;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.LocationResource;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.eclipse.sensinact.gateway.util.crypto.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 */
public class OpenWeatherPacketReader extends SimplePacketReader<HttpPacket> {
    /**
     * @param mediator
     */
    public OpenWeatherPacketReader(Mediator mediator) {
        super(mediator);
    }

    /**
     * @InheritedDoc
     * @see PacketReader#
     * parse(Packet)
     */
    @Override
    public void parse(HttpPacket packet) throws InvalidPacketException {
        try {
            String content = new String(packet.getBytes());
            String serviceProvider = null;
            JSONArray jsonArray = new JSONArray(content);
            JSONObject jsonObject = jsonArray.optJSONObject(0);

            JSONObject weatherObject = jsonObject.optJSONObject("weather");

            if (!JSONObject.NULL.equals(weatherObject) && weatherObject.length() != 0) {
                serviceProvider = weatherObject.getString("name");
                this.parseWeather(serviceProvider, weatherObject);
            }
            jsonObject = jsonArray.optJSONObject(1);

            String iconObject = jsonObject.optString("icon");

            if (iconObject != null && iconObject.length() != 0) {
                super.setServiceProviderId(serviceProvider);
                super.setServiceId("weather");
                super.setResourceId("image");
                super.setData(Base64.encodeBytes(iconObject.getBytes()));
                super.configure();
            }
        } catch (Exception e) {
            mediator.error(e);
            throw new InvalidPacketException(e);
        }
    }

    /**
     * @param station
     * @param object
     */
    private void parseWeather(String station, JSONObject object) {
        long timestamp = object.optLong("dt") * 1000L;
        JSONObject coord = object.optJSONObject("coord");
        if (coord != null) {
            super.setServiceProviderId(station);
            super.setServiceId(ServiceProvider.ADMINISTRATION_SERVICE_NAME);
            super.setResourceId(LocationResource.LOCATION);
            super.setData(new StringBuilder().append(coord.optDouble("lat")).append(JSONUtils.COLON).append(coord.optDouble("lon")).toString());
            super.setCommand(CommandType.GET);
            super.configure();
        }
        JSONArray weather = object.optJSONArray("weather");
        JSONObject content = null;
        if (weather != null && (content = weather.optJSONObject(0)) != null) {
            super.setServiceProviderId(station);
            super.setServiceId("weather");
            super.setResourceId("state");
            super.setData(content.opt("main"));
            super.setTimestamp(timestamp);
            super.setCommand(CommandType.GET);
            super.configure();

            super.setServiceProviderId(station);
            super.setServiceId("weather");
            super.setResourceId("description");
            super.setData(content.opt("description"));
            super.setTimestamp(timestamp);
            super.setCommand(CommandType.GET);
            super.configure();
        }
        JSONObject wind = object.optJSONObject("wind");
        if (wind != null) {
            super.setServiceProviderId(station);
            super.setServiceId("weather");
            super.setResourceId("wind");
            super.setData(wind.opt("speed"));
            super.setTimestamp(timestamp);
            super.setCommand(CommandType.GET);
            super.configure();

            super.setServiceProviderId(station);
            super.setServiceId("weather");
            super.setResourceId("orientation");
            super.setData(wind.opt("deg"));
            super.setTimestamp(timestamp);
            super.setCommand(CommandType.GET);
            super.configure();
        }
        JSONObject main = object.optJSONObject("main");
        if (main != null) {
            super.setServiceProviderId(station);
            super.setServiceId("weather");
            super.setResourceId("temperature");
            super.setData(main.opt("temp"));
            super.setTimestamp(timestamp);
            super.setCommand(CommandType.GET);
            super.configure();
            super.setServiceProviderId(station);
            super.setServiceId("weather");
            super.setResourceId("humidity");
            super.setData(main.opt("humidity"));
            super.setTimestamp(timestamp);
            super.setCommand(CommandType.GET);
            super.configure();

            super.setServiceProviderId(station);
            super.setServiceId("weather");
            super.setResourceId("pressure");
            super.setData(main.opt("pressure"));
            super.setTimestamp(timestamp);
            super.setCommand(CommandType.GET);
            super.configure();
        }
    }
}
