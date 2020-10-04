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
package org.eclipse.sensinact.gateway.test.configuration.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionUpdate;

import java.util.ConcurrentModificationException;
import java.util.List;


/**
 * Bundle Activator
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Activator implements BundleActivator {
    /**
     * @throws BundleException
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#
     * doStart()
     */
    public void start(BundleContext context) throws Exception {
        String codeBase = context.getProperty("org.eclipse.sensinact.gateway.test.codeBase");

        if (codeBase == null || codeBase.length() == 0) {
            return;
        }
        String[] codeBases = codeBase.split(",");

        ServiceReference<ConditionalPermissionAdmin> sRef = context.getServiceReference(ConditionalPermissionAdmin.class);

        ConditionalPermissionAdmin cpa = null;

        if (sRef == null) {
            throw new BundleException("ConditionalPermissionAdmin services needed");
        }
        cpa = context.getService(sRef);

        ConditionalPermissionUpdate cpu = cpa.newConditionalPermissionUpdate();
        List<ConditionalPermissionInfo> piList = cpu.getConditionalPermissionInfos();

        int index = 0;
        int length = codeBases.length;
        for (; index < length; index++) {
            piList.add(cpa.newConditionalPermissionInfo(String.format("ALLOW {[org.eclipse.sensinact.gateway.core.security.perm.CodeBaseCondition \"%s\"]" + "(java.security.AllPermission \"\" \"\")" + "} null", codeBases[index])));
        }
        if (!cpu.commit()) {
            throw new ConcurrentModificationException("Permissions changed during update");
        }
    }

    /**
     * @inheritDoc
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        //nothing to do here
    }

}
