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
package org.eclipse.sensinact.gateway.nthbnd.rest;

import org.eclipse.sensinact.gateway.test.MidOSGiTest;
import org.eclipse.sensinact.gateway.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class TestRestAccess extends MidOSGiTest {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//

    protected static final String HTTP_ROOTURL = "http://127.0.0.1:8899/sensinact";
    protected static final String WS_ROOTURL = "/sensinact";

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    /**
     * @throws MalformedURLException
     * @throws IOException
     */
    public TestRestAccess() throws Exception {
        super();
    }

    /**
     * @inheritDoc
     * @see MidOSGiTest#isExcluded(java.lang.String)
     */
    public boolean isExcluded(String fileName) {
        if ("org.apache.felix.framework.security.jar".equals(fileName)) {
            return true;
        }
        return false;
    }

    /**
     * @inheritDoc
     * @see MidOSGiTest#doInit(java.util.Map)
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void doInit(Map configuration) {
    	configuration.put("felix.auto.start.1",  
                "file:target/felix/bundle/org.osgi.service.component.jar "+  
                "file:target/felix/bundle/org.osgi.service.cm.jar "+  
                "file:target/felix/bundle/org.osgi.service.metatype.jar "+  
                "file:target/felix/bundle/org.osgi.namespace.extender.jar "+  
                "file:target/felix/bundle/org.osgi.util.promise.jar "+  
                "file:target/felix/bundle/org.osgi.util.function.jar "+  
                "file:target/felix/bundle/org.osgi.util.pushstream.jar "+
                "file:target/felix/bundle/org.osgi.service.log.jar "  +
                "file:target/felix/bundle/org.apache.felix.log.jar " + 
                "file:target/felix/bundle/org.apache.felix.scr.jar " +
        		"file:target/felix/bundle/org.apache.felix.fileinstall.jar " +
        		"file:target/felix/bundle/org.apache.felix.configadmin.jar " + 
        		"file:target/felix/bundle/org.apache.felix.framework.security.jar ");
        configuration.put("felix.auto.install.2",  
        	    "file:target/felix/bundle/org.eclipse.paho.client.mqttv3.jar " + 
                "file:target/felix/bundle/mqtt-utils.jar " + 
        	    "file:target/felix/bundle/sensinact-utils.jar " + 
                "file:target/felix/bundle/sensinact-common.jar " + 
        	    "file:target/felix/bundle/sensinact-datastore-api.jar " + 
                "file:target/felix/bundle/sensinact-security-none.jar " + 
                "file:target/felix/bundle/sensinact-generic.jar " + 
        	//  "file:target/felix/bundle/sensinact-remote-osgi.jar "+
                "file:target/felix/bundle/slf4j-api.jar " + 
                "file:target/felix/bundle/slf4j-simple.jar");
        configuration.put("felix.auto.start.2", 
        		"file:target/felix/bundle/sensinact-signature-validator.jar " + 
        		"file:target/felix/bundle/sensinact-core.jar ");
        configuration.put("felix.auto.start.3", 
        		"file:target/felix/bundle/org.apache.felix.http.servlet-api.jar " + 
                "file:target/felix/bundle/org.apache.felix.http.jetty.jar " + 
        		"file:target/felix/bundle/http.jar " +
        		"file:target/felix/bundle/sensinact-northbound-access.jar " +
                "file:target/felix/bundle/dynamicBundle.jar ");
        configuration.put("felix.auto.start.4",  
                "file:target/felix/bundle/slider.jar " + 
        		"file:target/felix/bundle/light.jar ");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.filename", "target/felix/bundle/keystore.jks");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.password", "sensiNact_team");

        configuration.put("org.eclipse.sensinact.gateway.location.latitude", "45.2d");
        configuration.put("org.eclipse.sensinact.gateway.location.longitude", "5.7d");

        configuration.put("org.osgi.service.http.port", "8899");
        configuration.put("org.apache.felix.http.jettyEnabled", true);
        configuration.put("org.apache.felix.http.whiteboardEnabled", true);

        try {
        	String fileName = "sensinact.config";
            File testFile = new File(new File("src/test/resources"), fileName);
            URL testFileURL = testFile.toURI().toURL();
            FileOutputStream output = new FileOutputStream(new File(loadDir,fileName));
            byte[] testCng = IOUtils.read(testFileURL.openStream(), true);
            IOUtils.write(testCng, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
