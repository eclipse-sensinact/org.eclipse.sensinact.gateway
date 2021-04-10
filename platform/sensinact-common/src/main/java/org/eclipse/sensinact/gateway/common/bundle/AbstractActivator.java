/*
 * Copyright (c) 2020 - 2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.common.bundle;

import org.eclipse.sensinact.gateway.common.interpolator.Interpolator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import java.util.Map;

/**
 * Abstract implementation of the {@link BundleActivator} interface
 */
public abstract class AbstractActivator<M extends Mediator> implements BundleActivator {
    /**
     * Completes the starting process
     */
    public abstract void doStart() throws Exception;

    /**
     * Completes the stopping process
     */
    public abstract void doStop() throws Exception;

    /**
     * Creates and returns the specific {@link Mediator}
     * extended implementation instance
     *
     * @param context the current {@link BundleContext}
     * @return the specific {@link Mediator} extended
     * implementation instance
     */
    public abstract M doInstantiate(BundleContext context);

    /**
     * {@link Mediator} extended implementation instance
     */
    protected M mediator;

    protected void injectPropertyFields() throws Exception {
        this.mediator.debug("Starting introspection in bundle %s", mediator.getContext().getBundle().getSymbolicName());
        //This line creates an interpolator and inject the properties into the activator
        Interpolator interpolator = new Interpolator(this.mediator);
        interpolator.getInstance(this);
        for(Map.Entry<String,String> entry:interpolator.getPropertiesInjected().entrySet()){
            //This will allow to define default properties directly in the abstract madiator
            if(!mediator.properties.containsKey(entry.getKey())){
                mediator.properties.put(entry.getKey(),entry.getValue());
            }
        }
    }

    @Override
    public void start(final BundleContext context) throws Exception {        
    	this.mediator = AbstractActivator.this.initMediator(context);
        try {
             injectPropertyFields();
        } catch (Exception e) {
            mediator.error(e);
        }
        AbstractActivator.this.doStart();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        try{
            doStop();
        }catch (Exception e){
            e.printStackTrace();
        }

        if (this.mediator != null) {
            this.mediator.deactivate();
        }
        this.mediator = null;
    }

    /**
     * Initializes and returns the {@link Mediator} of the
     * current {@link Bundle}
     *
     * @param context The current {@link BundleContext}
     * @return the newly created {@link Mediator}
     */
    protected M initMediator(BundleContext context) {
        M mediator = doInstantiate(context);
        return mediator;
    }
}
