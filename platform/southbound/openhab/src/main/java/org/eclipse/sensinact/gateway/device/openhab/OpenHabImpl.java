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
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author Jander Nascimento<Jander.BotelhodoNascimento@cea.fr>
 */
@Component(name = "OpenHab")
@Provides(specifications = {OpenHab.class})
public class OpenHabImpl implements OpenHab {

    private static final Long DELAY_START=7000l;
    private static final Logger LOG = LoggerFactory.getLogger(OpenHabImpl.class);

    private Set<OpenHabItem> managed=new HashSet<OpenHabItem>();

    @Requires(filter = "(factory.name=OpenHabDevice)")
    Factory openhabDeviceFactory;

    OpenHabDeviceDiscovery openhabDiscovery;

    ScheduledExecutorService executor =  Executors.newScheduledThreadPool(1);

    /*Executors.newFixedThreadPool(1,
            new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    Thread t = Executors.defaultThreadFactory().newThread(r);
                    t.setDaemon(true);
                    return t;
                }
            });
*/
    @Property
    private String ip;

    @Property(value = "8080")
    private Integer port;

    private String openHabURL;

    @Validate
    public void initialize(){
        LOG.info("Instantiating openhab monitor {}:{}", ip, port);
        openHabURL=String.format("http://%s:%d/",getIp(),getPort());
        openhabDiscovery =new OpenHabDeviceDiscovery(this);
        executor.schedule(openhabDiscovery,DELAY_START,TimeUnit.MILLISECONDS);
    }

    @Invalidate
    public void uninitialize(){
        LOG.info("Stopping openhab monitor {}:{}", ip, port);
        openhabDiscovery.desactivate();
        executor.shutdown();
    }

    public String getIp() {
        return ip;
    }

    public Integer getPort() {
        return port;
    }

    public String getOpenHabURL() {
        return openHabURL;
    }

    @Override
    public void deviceHeartBeat(Set<OpenHabItem> items) {

        /**
         * Remove items that dissapeared
         */
        HashSet<OpenHabItem> itemsDisappeared = new HashSet<OpenHabItem>(managed);
        itemsDisappeared.removeAll(items);

        for(OpenHabItem item:itemsDisappeared){
            LOG.info("Item {} is not any longer available in OpenHab, removing it from sensinact",item.getName());
            item.getInstance().dispose();
            managed.remove(item);
        }

        /**
         * Iterate among the list of items available in OpenHab and add them
         */
        for(OpenHabItem item:items){

            if(!managed.contains(item)){

                LOG.info("Item {} is new, adding it into sensinact",item.getName());

                Hashtable properties=new Hashtable();
                properties.put("instance.name", String.format("%s%s",openHabURL,item.getName()));
                properties.put("name", item.getName());
                properties.put("type", item.getType());
                properties.put("value", item.getState());
                properties.put("url", item.getLink());

                try {
                    ComponentInstance ci=openhabDeviceFactory.createComponentInstance(properties);
                    item.setInstance(ci);
                    managed.add(item);
                } catch (UnacceptableConfiguration unacceptableConfiguration) {
                    LOG.error("Wrong component configuration",unacceptableConfiguration);
                } catch (MissingHandlerException e) {
                    LOG.error("Missing handlers",e);
                } catch (ConfigurationException e) {
                    LOG.error("Wrong configuration",e);
                }

            }

        }
    }

}
