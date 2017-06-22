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

package org.eclipse.sensinact.gateway.app.basic.osgi;

import org.eclipse.sensinact.gateway.app.api.plugin.PluginInstaller;
import org.eclipse.sensinact.gateway.app.basic.installer.BasicInstaller;
import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.osgi.framework.ServiceRegistration;

import java.io.FileOutputStream;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @see BundleActivator
 *
 * @author Remi Druilhe
 */
public class Activator extends AbstractActivator<Mediator> {

    private static final String PLUGIN_NAME = "BasicPlugin";

    private ServiceRegistration basicInstallerRegistration;

    /**
     * @see BundleActivator#start(BundleContext)
     */
    public void doStart() throws Exception {
        Dictionary<String, String> installProperties = new Hashtable<String, String>();
        installProperties.put("plugin.name", PLUGIN_NAME);

        basicInstallerRegistration = super.mediator.getContext().registerService(
                PluginInstaller.class.getCanonicalName(),
                new BasicInstaller(super.mediator),
                installProperties);
    }

    /**
     * @see BundleActivator#stop(BundleContext)
     */
    public void doStop() throws Exception {
        this.basicInstallerRegistration.unregister();
    }

    /**
     * @inheritDoc
     *
     * @see AbstractActivator#doInstantiate(BundleContext, int, FileOutputStream)
     */
    @Override
    public Mediator doInstantiate(BundleContext context) 
    {
        return new Mediator(context);
    }
}
