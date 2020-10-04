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
package org.eclipse.sensinact.gateway.protocol.http.test;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.sensinact.gateway.util.ReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JettyTestServer implements Runnable, CallbackCollection {
    private Server server;
    Map<Class<? extends Annotation>, List<Callback>> callbacks;

    public JettyTestServer(int port) throws Exception {
        this.callbacks = new HashMap<Class<? extends Annotation>, List<Callback>>();
        this.server = new Server(port);
        ServletHandler handler = new ServletHandler();
        ServletHolder holder = new ServletHolder(new JettyTestCallbackServlet(this));
        holder.setName("callbackServlet");

        handler.addServletWithMapping(holder, "/");
        this.server.setHandler(handler);
    }

    public void registerCallback(Object callback) {
        Map<Method, doGet> getMethods = ReflectUtils.getAnnotatedMethods(callback.getClass(), doGet.class);

        if (getMethods != null && getMethods.size() > 0) {
            List<Callback> callbackList = this.callbacks.get(doGet.class);

            if (callbackList == null) {
                callbackList = new ArrayList<Callback>();
                this.callbacks.put(doGet.class, callbackList);
            }
            Iterator<Method> iterator = getMethods.keySet().iterator();
            while (iterator.hasNext()) {
                callbackList.add(new Callback(callback, iterator.next()));
            }
        }
        Map<Method, doPost> postMethods = ReflectUtils.getAnnotatedMethods(callback.getClass(), doPost.class);
        if (postMethods != null && postMethods.size() > 0) {
            List<Callback> callbackList = this.callbacks.get(doPost.class);

            if (callbackList == null) {
                callbackList = new ArrayList<Callback>();
                this.callbacks.put(doPost.class, callbackList);
            }
            Iterator<Method> iterator = postMethods.keySet().iterator();
            while (iterator.hasNext()) {
                callbackList.add(new Callback(callback, iterator.next()));
            }
        }
    }

    public boolean isStarted() {
        return this.running;
    }

    public void start() throws Exception {
        this.server.start();
    }

    public void stop() throws Exception {
        this.server.stop();
    }

    public void join() throws Exception {
        this.server.join();
        Thread.sleep(2000);
    }

    private boolean running = false;

    /**
     * @inheritDoc
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        running = true;
        try {
            this.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        running = false;
    }

    /**
     * @param class1
     * @return
     */
    public List<Callback> getdoGetCallbacks() {
        return this.callbacks.get(doGet.class);
    }

    /**
     * @param class1
     * @return
     */
    public List<Callback> getdoPostCallbacks() {
        return this.callbacks.get(doPost.class);
    }
}
