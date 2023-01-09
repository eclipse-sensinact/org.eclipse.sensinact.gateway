/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.virtual.temperature;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Promise;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component(configurationPid = "sensinact.virtual.temperature", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class VirtualTemperatureSensor {

    public @interface Config {

        public long interval() default 30000L;

        public double min() default 0.0d;

        public double max() default 30.0d;

        public double latitude();

        public double longitude();

        public String name();
    }

    @Reference
    PrototypePush push;

    private Config config;

    private Random random = new Random();

    private final AtomicBoolean active = new AtomicBoolean(true);

    @Activate
    void start(Config config) throws Exception {

        this.config = config;

        GenericDto dto = new GenericDto();
        dto.model = VirtualTemperatureDto.VIRTUAL_TEMPERATURE_MODEL;
        dto.provider = config.name();
        dto.service = "admin";
        dto.resource = "location";

        Point point = new Point();
        point.coordinates = new Coordinates();
        point.coordinates.latitude = config.latitude();
        point.coordinates.longitude = config.longitude();

        dto.value = new ObjectMapper().writeValueAsString(point);

        push.pushUpdate(dto).getValue();

        // Update then run update again
        repeatedUpdate();
    }

    @Deactivate
    void stop() {
        active.set(false);
    }

    private Promise<Void> repeatedUpdate() {
        return update().delay(config.interval()).then(p -> active.get() ? repeatedUpdate() : p);
    }

    Promise<Void> update() {

        VirtualTemperatureDto dto = new VirtualTemperatureDto();
        dto.provider = config.name();
        dto.temperature = config.min() + ((config.max() - config.min()) * random.nextDouble());

        return push.pushUpdate(dto).map(x -> null);
    }
}
