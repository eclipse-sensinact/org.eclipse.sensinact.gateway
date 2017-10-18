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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.eclipse.sensinact.gateway.test.MidOSGiTest;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class TestRestAccess extends MidOSGiTest
{
	//********************************************************************//
	//						NESTED DECLARATIONS			  			      //
	//********************************************************************//

	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//

	//********************************************************************//
	//						STATIC DECLARATIONS							  //
	//********************************************************************//
	
	protected static final String HTTP_ROOTURL = "http://localhost:8091/sensinact";
	protected static final String WS_ROOTURL = "/sensinact";
	

	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//

	/**
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public TestRestAccess() throws Exception
	{
		super();
	}

	/**
	 * @inheritDoc
	 *
	 * @see MidOSGiTest#isExcluded(java.lang.String)
	 */
	public boolean isExcluded(String fileName)
	{
		if("org.apache.felix.framework.security.jar".equals(fileName))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see MidOSGiTest#doInit(java.util.Map)
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void doInit(Map configuration)
	{
		configuration.put("felix.auto.start.1",
		    "file:target/felix/bundle/org.osgi.compendium.jar "
		  + "file:target/felix/bundle/org.apache.felix.configadmin.jar "
		  + "file:target/felix/bundle/org.apache.felix.framework.security.jar ");

		configuration.put("felix.auto.install.2",
		    "file:target/felix/bundle/sensinact-utils.jar "
		  + "file:target/felix/bundle/sensinact-common.jar "
		  + "file:target/felix/bundle/sensinact-datastore-api.jar "
		  + "file:target/felix/bundle/sensinact-framework-extension.jar "
		  + "file:target/felix/bundle/sensinact-security-none.jar "
		  + "file:target/felix/bundle/sensinact-generic.jar");
		
		configuration.put("felix.auto.start.2",
		  	"file:target/felix/bundle/sensinact-test-configuration.jar "
		  + "file:target/felix/bundle/sensinact-signature-validator.jar "
		  +	"file:target/felix/bundle/sensinact-core.jar ");

		configuration.put("felix.auto.start.3", 
		"file:target/felix/bundle/javax.servlet-api.jar "
		+ "file:target/felix/bundle/org.apache.felix.http.api.jar "
		+ "file:target/felix/bundle/org.apache.felix.http.jetty.jar " 
		+ "file:target/felix/bundle/http.jar "
		+ "file:target/felix/bundle/sensinact-northbound-access.jar "
		+ "file:target/felix/bundle/dynamicBundle.jar");

		configuration.put("felix.auto.start.4", 
		"file:target/felix/bundle/slider.jar "
		+ "file:target/felix/bundle/light.jar ");

		configuration.put("org.eclipse.sensinact.gateway.security.jks.filename", "target/felix/bundle/keystore.jks");
		configuration.put("org.eclipse.sensinact.gateway.security.jks.password","sensiNact_team");

		configuration.put("org.osgi.service.http.port", "8091");
		configuration.put("org.apache.felix.http.jettyEnabled", "true");
		configuration.put("org.apache.felix.http.whiteboardEnabled", "true");
	}
}
