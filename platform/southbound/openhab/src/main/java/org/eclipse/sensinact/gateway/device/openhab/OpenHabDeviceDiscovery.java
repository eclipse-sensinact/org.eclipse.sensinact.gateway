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

import org.eclipse.sensinact.gateway.device.openhab.internal.OpenHabItem;
import org.json.JSONArray;
import org.json.JSONException;
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
import java.util.HashSet;
import java.util.Set;

/**
 * @Author Jander Nascimento<Jander.BotelhodoNascimento@cea.fr>
 */
public class OpenHabDeviceDiscovery extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(OpenHabDeviceDiscovery.class);
    private OpenHab open;
    private final Integer DELAY_RETRY =5000;
    private Boolean activated=true;
    private String openHabURL="";

    OpenHabDeviceDiscovery(OpenHab open){
        this.open=open;
        super.setDaemon(true);
        openHabURL=open.getOpenHabURL()+"rest/items/";
    }

    public void desactivate(){
        LOG.info("Desactivating devices fetching on URL {}", openHabURL);
        activated=false;
    }

    public void run(){

        while(activated){

            HttpURLConnection conn=null;

            try {

                String openURL=openHabURL+"?type=json";

                LOG.debug("Fetching new devices from {}", openURL);

                URL url = new URL(openURL);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type","application/json");

                if (conn.getResponseCode()>=200&&conn.getResponseCode()<300) {

                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            (conn.getInputStream())));

                    String output;

                    StringBuffer sb=new StringBuffer();

                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }

                    LOG.debug("Response from server: {}", sb.toString());

                    JSONObject jo=new JSONObject(sb.toString());

                    Set<OpenHabItem> items=new HashSet<OpenHabItem>();

                    JSONArray itemsArray=new JSONArray();

                    try {
                        itemsArray=jo.getJSONArray("item");
                    }catch(JSONException e){
                        JSONObject jsonObject=jo.getJSONObject("item");
                        itemsArray.put(jsonObject);
                    }finally {

                        for(int x=0;x<itemsArray.length();x++){
                            JSONObject ob=itemsArray.getJSONObject(x);

                            OpenHabItem item=new OpenHabItemWrapper(ob).getItem();

                            items.add(item);

                            if(!item.getType().equalsIgnoreCase("SwitchItem")){
                                LOG.warn("OpenHab device {} is of the type {}, which is not yet supported by Sensinact bridge",item.getName(),item.getType());
                                continue;
                            }
                        }
                    }
                    open.deviceHeartBeat(items);
                }

            } catch (MalformedURLException e) {
                LOG.error("Bad URL",e);
                desactivate();
            } catch (ConnectException e){
                LOG.error("Execution failed, retry in {} ms", DELAY_RETRY, e);
            } catch (IOException e) {
                LOG.error("I/O exception",e);
                desactivate();
            } catch (JSONException e) {
                LOG.error("JSONException, rest service probably is still loading, retry in {} ms",DELAY_RETRY,e);
            } finally {
                if(conn!=null){
                    conn.disconnect();
                }
                try {
                    Thread.sleep(DELAY_RETRY);
                } catch (InterruptedException e) {
                    LOG.error("Failed to delay execution.", e);
                }
            }

        }
        LOG.info("Thread for devices fetching on URL {} is finished", openHabURL);

    }

    class OpenHabItemWrapper {

        JSONObject jo;

        public OpenHabItemWrapper(JSONObject object){
            this.jo=object;
        }

        public OpenHabItem getItem(){

            String type=jo.getString("type");
            String name=jo.getString("name");
            String link=jo.getString("link");
            String state=jo.getString("state");

            OpenHabItem item=new OpenHabItem(type,name,link,state);

            return item;
        }

    }

}

