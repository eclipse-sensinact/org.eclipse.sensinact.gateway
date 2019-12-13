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
package org.eclipse.sensinact.gateway.security.signature.test;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.eclipse.sensinact.gateway.security.signature.internal.BundleValidationImpl;
import org.eclipse.sensinact.gateway.security.signature.internal.KeyStoreManagerException;
import org.eclipse.sensinact.gateway.test.MidOSGiTest;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/*
 * signature validation with embedded archive: embedded archives are to be signed by the same signer as the main archive
 * testCheckNOKWithEmbeddedArchive not performed
 */
public class BundleValidationTest extends MidOSGiTest{
	
	private static final String DEFAULT_KEYSTORE_FILE_PATH = "../cert/keystore.jks";
    private static final String DEFAULT_KEYSTORE_PASSWORD = "sensiNact_team";
    
	public BundleValidationTest() throws Exception {
		super();
	}

    private BundleValidation validator( ) throws NoSuchAlgorithmException, KeyStoreManagerException {

		BundleValidation jval = new BundleValidationImpl(new Mediator(super.context)) {
            @Override
            protected String getKeyStoreFileName() {
                return BundleValidationTest.DEFAULT_KEYSTORE_FILE_PATH;
            }

            @Override
            protected String getKeyStorePassword() {
                return BundleValidationTest.DEFAULT_KEYSTORE_PASSWORD;
            }

            @Override
            protected String getSignerPassword() {
                return BundleValidationTest.DEFAULT_KEYSTORE_PASSWORD;
            }
        };
        return jval;
    }

    @Test
    public void testCheckFanOK() throws BundleException, NoSuchAlgorithmException, KeyStoreManagerException {
    	BundleValidation jval = validator();
        Bundle fan = super.context.installBundle("file:./target/extra/fan.jar");
        ////logger.log(Level.INFO, "testCheckOK");
        String result = null;
        try {
            result = jval.check(fan);
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            fan.uninstall();
        }
        Assert.assertTrue(result != null);
    }

    @Test
    public void testCheckFanKO() throws BundleException, NoSuchAlgorithmException, KeyStoreManagerException {
    	BundleValidation jval = validator();
        Bundle failer = super.context.installBundle("file:./src/test/resources/failer-fan.jar");

        ////logger.log(Level.INFO, "testCheckOK");
        String result = null;
        try {
            result = jval.check(failer);
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
           failer.uninstall();
        }
        Assert.assertTrue(result == null);
    }

    @Test
    public void testCheckButtonOK() throws BundleException, NoSuchAlgorithmException, KeyStoreManagerException {
    	BundleValidation jval = validator();
        Bundle button = super.context.installBundle("file:./target/extra/button.jar");

        ////logger.log(Level.INFO, "testCheckOK");
        String result = null;
        try {
            result = jval.check(button);
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
           button.uninstall();
        }
        Assert.assertTrue(result != null);
    }

	@Override
	protected void doInit(Map configuration) {		
		configuration.put("org.osgi.framework.system.packages.extra", 
		"org.eclipse.sensinact.gateway.generic;version= \"2.1.0\","+
		"org.eclipse.sensinact.gateway.generic.*;version= \"2.1.0\"," +
		"org.eclipse.sensinact.gateway.common;version= \"2.1.0\"," +  
		"org.eclipse.sensinact.gateway.common.*;version= \"2.1.0\"," + 
		"org.eclipse.sensinact.gateway.util;version= \"2.1.0\"," + 
		"org.eclipse.sensinact.gateway.util.*;version= \"2.1.0\"," + 
		"json-20140107.jar;version= \"2.1.0\"," + 
		"org.json;version;version= \"2.1.0\"," + 
		"org.json.zip;version=\"2.1.0\"");
	}

	@Override
	protected boolean isExcluded(String name) {
		return false;
	}
}
