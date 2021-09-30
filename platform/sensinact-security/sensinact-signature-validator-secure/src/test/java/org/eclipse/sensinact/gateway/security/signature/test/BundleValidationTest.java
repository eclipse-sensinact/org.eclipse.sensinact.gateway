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
package org.eclipse.sensinact.gateway.security.signature.test;

import org.assertj.core.api.Assertions;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.eclipse.sensinact.gateway.security.signature.api.SignatureValidatorConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.test.common.annotation.InjectInstalledBundle;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.context.InstalledBundleExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/*
 * signature validation with embedded archive: embedded archives are to be signed by the same signer as the main archive
 * testCheckNOKWithEmbeddedArchive not performed
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(InstalledBundleExtension.class)
@ExtendWith(ServiceExtension.class)
public class BundleValidationTest{
	
	@InjectService(timeout = 500, filter =  "("+SignatureValidatorConstants.PREFIX_+"type=secure)")
	BundleValidation jval;

	@Test
	public void testCheck_Fan_OK(@InjectInstalledBundle(value = "fan.jar") Bundle fan)
			throws Exception {
		Assertions.assertThat(jval.check(fan)).isNotNull();
	}
	
	@Test
	public void testCheck_FailerFan_NotOK(@InjectInstalledBundle(value = "failer-fan.jar") Bundle failerFan)
			throws Exception {
		Assertions.assertThat(jval.check(failerFan)).isNull();
	}
	
	@Test
	public void testCheck_Button_OK(@InjectInstalledBundle(value = "button.jar") Bundle button)
			throws Exception {
		Assertions.assertThat(jval.check(button)).isNotNull();
	}
	
//    @Test
//    public void testCheckFanOK() throws BundleException, NoSuchAlgorithmException, KeyStoreManagerException {
//    	BundleValidation jval = validator();
//        Bundle fan = super.context.installBundle("file:./target/extra/fan.jar");
//        ////logger.log(Level.INFO, "testCheckOK");
//        String result = null;
//        try {
//            result = jval.check(fan);
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        } finally {
//            fan.uninstall();
//        }
//        Assert.assertTrue(result != null);
//    }
//
//    @Test
//    public void testCheckFanKO() throws BundleException, NoSuchAlgorithmException, KeyStoreManagerException {
//    	BundleValidation jval = validator();
//        Bundle failer = super.context.installBundle("file:./src/test/resources/failer-fan.jar");
//
//        ////logger.log(Level.INFO, "testCheckOK");
//        String result = null;
//        try {
//            result = jval.check(failer);
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        } finally {
//           failer.uninstall();
//        }
//        Assert.assertTrue(result == null);
//    }
//
//    @Test
//    public void testCheckButtonOK() throws BundleException, NoSuchAlgorithmException, KeyStoreManagerException {
//    	BundleValidation jval = validator();
//        Bundle button = super.context.installBundle("file:./target/extra/button.jar");
//
//        ////logger.log(Level.INFO, "testCheckOK");
//        String result = null;
//        try {
//            result = jval.check(button);
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        } finally {
//           button.uninstall();
//        }
//        Assert.assertTrue(result != null);
//    }

}
