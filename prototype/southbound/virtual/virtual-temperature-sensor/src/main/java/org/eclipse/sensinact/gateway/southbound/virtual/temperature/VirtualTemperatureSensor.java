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

import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.promise.Promise;

public class VirtualTemperatureSensor {

    private final PrototypePush push;

    private final String name;

    private final Random random;

    private final long interval;

    private final double min;

    private final double max;

    private final AtomicBoolean active = new AtomicBoolean(true);

    VirtualTemperatureSensor(PrototypePush push, String name, Random random, long interval, double min, double max,
            GeoJsonObject location) throws Exception {

        this.push = push;
        this.name = name;
        this.random = random;
        this.interval = interval;
        this.min = min;
        this.max = max;

        GenericDto dto = new GenericDto();
        dto.model = VirtualTemperatureDto.VIRTUAL_TEMPERATURE_MODEL;
        dto.provider = name;
        dto.service = "admin";
        dto.resource = "location";
        dto.value = location;

        push.pushUpdate(dto).getValue();

        // Update then run update again
        repeatedUpdate();
    }

    @Deactivate
    void stop() {
        active.set(false);
    }

    private Promise<Void> repeatedUpdate() {
        return update().delay(interval).then(p -> active.get() ? repeatedUpdate() : p);
    }

    Promise<Void> update() {

        VirtualTemperatureDto dto = new VirtualTemperatureDto();
        dto.provider = name;
        dto.temperature = min + ((max - min) * random.nextDouble());

        return push.pushUpdate(dto).map(x -> null);
    }
}
