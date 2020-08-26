/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eclipse.sensinact.gateway.agent.storage.influxdb.internal;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.agent.storage.generic.StorageConnection;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Pong;
import org.json.JSONObject;

/**
 *
 * @author kleisar
 */
public class InfluxStorageConnection extends StorageConnection {

    private InfluxDB influxDB;
    private BatchPoints batchPoints;
    
    protected String databaseURL;

    public InfluxStorageConnection(Mediator mediator, String databaseURL, String userName, String password) throws IOException {
        super(mediator, userName, password);
        this.databaseURL = databaseURL;
        this.connect();
    }

    private void insert(String provider, String service, String resource, Object value) {
        System.out.println("insert " + provider + ", " + service + ", " + resource + " = " + value);
        Point point = null;
        Builder builder = Point.measurement("sensiNact")
                .tag("provider", provider)
                .tag("service", service)
                .tag("resource", resource);
        if (value instanceof String) {
            point = builder.addField("value", (String) value).build();
        } else if (value instanceof Double) {
            point = builder.addField("value", (Double) value).build();
        } else if (value instanceof Float) {
            point = builder.addField("value", (Float) value).build();
        } else if (value instanceof Integer) {
            point = builder.addField("value", (Integer) value).build();
        }

        influxDB.write(point);
        //batchPoints.point(point);
    }

    @Override
    public void sendRequest(JSONObject object) {
        try {
            String device = object.getString("device");
            String service = object.getString("service");
            String resource = object.getString("resource");
            Object value = object.get("value");
            this.insert(device, service, resource, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Creates a connection and returns true if it has been done properly.
     * Returns false otherwise  
     *
     * @return <ul>
     * 			<li>true if the connection has been properly done</li>
     * 			<li>false otherwise</li>
     * 		   </ul>
     */
    protected boolean connect() {
        influxDB = InfluxDBFactory.connect(this.databaseURL);
        influxDB.setDatabase("sensiNact");
        influxDB.setRetentionPolicy("autogen");
        Pong response = this.influxDB.ping();
        if (response.getVersion().equalsIgnoreCase("unknown")) {
            System.out.println("Error pinging server.");
            return false;
        } else {
//            this.influxDB.query(new Query("CREATE DATABASE sensiNact", ""));
//            batchPoints = BatchPoints
//                    .database("sensiNact")
//                    .retentionPolicy("defaultPolicy")
//                    .build();
        }
        influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
        influxDB.enableBatch(100, 5000, TimeUnit.MILLISECONDS); //wait 5s or 100Points, whichever occurs first before sending the points to influxdb
        return true;
    }

}
