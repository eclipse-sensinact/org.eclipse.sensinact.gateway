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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(configurationPid = "sensinact.virtual.temperature", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class VirtualTemperatureSensorComponent {

    public @interface Config {

        public long interval() default 30000L;

        public double min() default 0.0d;

        public double max() default 30.0d;

        public double latitude();

        public double longitude();

        public String name();

        public long sensor_count() default 1;
    }

    @Reference
    DataUpdate push;

    private Random random = new Random();

    private final List<VirtualTemperatureSensor> sensors = new ArrayList<>();

    @Activate
    void start(Config config) throws Exception {

        String namePrefix = config.name();
        for (int i = 0; i < config.sensor_count(); i++) {

            Point point = new Point();
            point.coordinates = new Coordinates();
            point.coordinates.latitude = config.latitude();
            point.coordinates.longitude = config.longitude();

            double deltaRange = (i * 0.001d);
            int latRange = (i % 11) - 5;
            int lngRange = ((i + 5) % 11) - 5;

            point.coordinates.latitude += (deltaRange * latRange);
            point.coordinates.longitude += (deltaRange * lngRange);

            String name = config.sensor_count() == 1 ? namePrefix : String.format("%s_%d", namePrefix, i);

            sensors.add(new VirtualTemperatureSensor(push, name, random, config.interval(), config.min(), config.max(),
                    point));
        }
    }

    @Deactivate
    void stop() {
        sensors.forEach(VirtualTemperatureSensor::stop);
    }
}
