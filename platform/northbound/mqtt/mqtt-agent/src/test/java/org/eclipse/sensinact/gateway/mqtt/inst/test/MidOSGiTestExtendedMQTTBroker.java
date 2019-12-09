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
package org.eclipse.sensinact.gateway.mqtt.inst.test;

import java.util.Map;

import org.junit.Before;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class MidOSGiTestExtendedMQTTBroker extends MidOSGiTestExtended{
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    /**
     * @throws Exception
     */
    public MidOSGiTestExtendedMQTTBroker(int count) throws Exception {
        super(count);
    }

    /**
     * @throws Exception
     */
    @Before
    @Override
    public void init() throws Exception {
    	super.init();
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.test.MidOSGiTest#doInit(java.util.Map)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void doInit(Map configuration) {
    	configuration.put("felix.auto.start.1",  
        "file:target/felix/bundle/org.osgi.service.component.jar "+  
        "file:target/felix/bundle/org.osgi.service.cm.jar "+  
        "file:target/felix/bundle/org.osgi.service.metatype.jar "+  
        "file:target/felix/bundle/org.osgi.namespace.extender.jar "+  
        "file:target/felix/bundle/org.osgi.util.promise.jar "+  
        "file:target/felix/bundle/org.osgi.util.function.jar "+  
        "file:target/felix/bundle/org.osgi.service.log.jar "  +
        "file:target/felix/bundle/org.apache.felix.log.jar " + 
        "file:target/felix/bundle/org.apache.felix.scr.jar " +
		"file:target/felix/bundle/org.apache.felix.fileinstall.jar " +
		"file:target/felix/bundle/org.apache.felix.configadmin.jar " + 
		"file:target/felix/bundle/org.apache.felix.framework.security.jar ");
        configuration.put("felix.auto.install.2", 
        "file:target/felix/bundle/slf4j-api.jar "
        + "file:target/felix/bundle/slf4j-impl.jar  "
        + "file:target/felix/bundle/sensinact-utils.jar " 
        + "file:target/felix/bundle/sensinact-common.jar "   
        + "file:target/felix/bundle/sensinact-framework-extension.jar "  
        + "file:target/felix/bundle/sensinact-test-configuration.jar ");
        configuration.put("felix.auto.start.2", "file:target/felix/bundle/mqtt-server.jar ");

        configuration.put("org.osgi.framework.system.packages.extra","sun.misc");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.filename", "target/felix/bundle/keystore.jks");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.password", "sensiNact_team");
    }

}
