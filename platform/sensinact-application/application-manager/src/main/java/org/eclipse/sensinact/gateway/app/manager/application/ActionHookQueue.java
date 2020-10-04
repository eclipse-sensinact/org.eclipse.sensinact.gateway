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
package org.eclipse.sensinact.gateway.app.manager.application;

import org.eclipse.sensinact.gateway.app.api.exception.LifeCycleException;
import org.eclipse.sensinact.gateway.app.api.plugin.PluginHook;
import org.eclipse.sensinact.gateway.app.manager.component.DataListenerItf;
import org.eclipse.sensinact.gateway.app.manager.component.DataProviderItf;
import org.eclipse.sensinact.gateway.app.manager.component.Event;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.osgi.framework.ServiceReference;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class aims at creating a queue where actions are stored and
 * fired when the application reaches the end of the tree.
 */
class ActionHookQueue implements DataListenerItf {
    private final AppServiceMediator mediator;
    private final Queue<PluginHook> actionQueue;

    ActionHookQueue(AppServiceMediator mediator) {
        this.mediator = mediator;
        this.actionQueue = new ConcurrentLinkedQueue<PluginHook>();//new LinkedList<PluginHook>();
    }

    public void instantiate() throws LifeCycleException {
        try {
            String filter = "(&(objectClass=" + DataProviderItf.class.getName() + ")" + "(type=" + PluginHook.class.getName() + "))";
            ServiceReference[] serviceReferences = mediator.getServiceReferences(filter);
//            Mediator.ServiceCaller caller = mediator.CALLERS.get();
//
//            caller.attach();
//
//            caller.callServices(DataProviderItf.class, new Executor<DataProviderItf, Void>() {
//                @Override
//                public Void execute(DataProviderItf service) throws Exception {
//                    service.addListener(ActionHookQueue.this, null);
//                    return null;
//                }
//            });
//
//            if (caller.release() == 0) {
//                mediator.CALLERS.remove();
//            }
            if(serviceReferences!=null) {
	            for (ServiceReference serviceReference : serviceReferences) {
	                DataProviderItf dataProviderItf = ((DataProviderItf) mediator.getService(serviceReference));
	                dataProviderItf.addListener(this, null);
	            }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new LifeCycleException("Unable to instantiate the ActionHookQueue > " + e.getMessage());
        }
    }

    public void uninstantiate() throws LifeCycleException {
        try {
            String filter = "(&(objectClass=" + DataProviderItf.class.getName() + ")" + "(type=" + PluginHook.class.getName() + "))";
            ServiceReference[] serviceReferences = mediator.getContext().getServiceReferences((String) null, filter);
            for (ServiceReference serviceReference : serviceReferences) {
                DataProviderItf dataProviderItf = (DataProviderItf) mediator.getService(serviceReference);
                dataProviderItf.removeListener(this);
            }
        } catch (Exception e) {
            throw new LifeCycleException("Unable to uninstantiate the ActionHookQueue > " + e.getMessage());
        }
    }

    /**
     * Add a new {@link Event} on a notification
     *
     * @param event the event to store
     */
    public void eventNotification(Event event) {
        actionQueue.add((PluginHook) event.getData().getValue());
    }

    /**
     * Fire the previously stored hooks
     */
    void fireHooks() {
        //System.out.println(actionQueue.size() + " actions in the queue");
        try {
            for (PluginHook hook : actionQueue) {
                hook.fireHook();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.actionQueue.clear();
        }
    }
}
