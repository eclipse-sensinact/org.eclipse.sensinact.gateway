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
package org.eclipse.sensinact.gateway.device.openhab;

import org.eclipse.sensinact.gateway.device.openhab.sensinact.OpenHabPacket;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;

import org.apache.felix.ipojo.annotations.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Component
@Provides
public class OpenHabDevice {

    private static final Logger LOG = LoggerFactory.getLogger(OpenHabDevice.class);

    //private ExecutorService executor=Executors.newSingleThreadExecutor();
    ExecutorService executor = Executors.newFixedThreadPool(1,
        new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
        });

    @Requires(filter = "(&(objectClass=org.eclipse.sensinact.gateway.generic.core.ProtocolStackConnector)(name=openhab))")
    ProtocolStackEndpoint openHabConnector;

    @Property
    private String name;
    @Property
    private String type;
    @Property
    private String url;
    @Property(mandatory = false,name = "ON")
    private String value;

    private Callback callback=new Callback() {
        @Override
        public void finished(JSONObject state) {
            LOG.info("Received OpenHab notification update {}",state.toString());
            try {
                value=state.getString("state");
                OpenHabPacket packet=new OpenHabPacket(name,false);
                packet.setCurrentState(OpenHabDevice.this);
                OpenHabDevice.this.openHabConnector.process(packet);
            } catch (InvalidPacketException e) {
                LOG.error("Invalid package, it cannot be processed",e);
            }
            executor.submit(new UpdateNotification(url,callback ));
        }

        @Override
        public void failed(Integer code,Exception error) {
            if(!(error instanceof ConnectException)) {
                LOG.error("OpenHab Long polling failed");
                executor.submit(new UpdateNotification(url, callback));
            }else {
                OpenHabPacket packet=new OpenHabPacket(name,false);
                packet.isGoodbye(true);
                try {
                    OpenHabDevice.this.openHabConnector.process(packet);
                } catch (InvalidPacketException e1) {
                    LOG.error("Invalid package, it cannot be processed", e1);
                }
                LOG.error("OpenHab Long polling failed, probably the openhab is offline");
            }
        }
    };

    @Validate
    public void subscribeUpdate(){
        executor.submit(new UpdateNotification(url,callback ));
    }

    @Invalidate
    public void unsubscribeUpdate(){
        executor.shutdown();
    }

    public void act(String command, Object... parameters) {
        try {
            if(command.equals("TURN_ON")){
                invokeRest("ON");
            } else if(command.equals("TURN_OFF")){
                invokeRest("OFF");
            }
        }catch(ArrayIndexOutOfBoundsException paramException){
            LOG.error("Impossible to execute command {}, probably you're forgot to pass all required parameters",command,parameters,paramException);
        }
    }

    public void invokeRest(String command){
        try {

            LOG.debug("Invoking OpenHab bind on URL {} with value {}", new Object[]{url, command});

            URL url = new URL(this.url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","text/plain");
            conn.getOutputStream().write(command.getBytes());
            if (conn.getResponseCode()<200||conn.getResponseCode()>=300) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }else {
                LOG.debug("Command {} on URL {} executed successfully ", command, this.url);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            StringBuffer sb=new StringBuffer();

            while ((output = br.readLine()) != null) {
                sb.append(output);
            }

            LOG.debug("Response from server: {}", sb.toString());

            conn.disconnect();

        } catch (MalformedURLException e) {
            LOG.error("Bad URL",e);
        } catch (IOException e) {
            LOG.error("Connection Lost", e);
        } catch (RuntimeException e){
            LOG.error("Execution failed", e);
        }
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    interface Callback{
        void finished(JSONObject state);
        void failed(Integer code,Exception e);
    }

    class UpdateNotification implements Runnable {

        private String urlString;
        private Callback callback;

        public UpdateNotification(String urlString,Callback callback){
            this.urlString=urlString;
            this.callback=callback;
        }

        public void run(){

            Integer lastErrorCode=-1;

            try {
                URL url = new URL(urlString);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.addRequestProperty("Accept", "application/json");
                conn.addRequestProperty("X-Atmosphere-Transport", "long-polling");

                LOG.info("Subscribing long-pooling on device {}", urlString);

                if (conn.getResponseCode() < 200 || conn.getResponseCode() >= 300) {
                    lastErrorCode=conn.getResponseCode();
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));

                String output;

                StringBuffer sb = new StringBuffer();

                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }

                LOG.debug("OpenHab server response {}",sb.toString());

                if(sb.toString().trim().length()!=0){
                    JSONObject state=new JSONObject(sb.toString());
                    callback.finished(state);
                }else {
                    callback.failed(lastErrorCode,new NullPointerException("No content received"));
                }

            }catch(Exception e){
                callback.failed(lastErrorCode,e);
            }
        }
    }
}
