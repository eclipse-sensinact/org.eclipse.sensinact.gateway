/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.security.signature.test;

import org.assertj.core.api.Assertions;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.eclipse.sensinact.gateway.security.signature.api.SignatureValidatorConstants;
import org.junit.jupiter.api.Disabled;
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
public class BundleValidationTest {
	
	@InjectService(timeout = 500, filter ="("+SignatureValidatorConstants.PREFIX_+"type=mock)")
	BundleValidation jval;

	@Test
	public void testCheck_Fan_OK(@InjectInstalledBundle(value = "fan.jar") Bundle fan)
			throws Exception {
		Assertions.assertThat(jval.check(fan)).isNotNull();
	}

	@Test
	public void testCheck_Button_OK(@InjectInstalledBundle(value = "button.jar") Bundle button)
			throws Exception {
		Assertions.assertThat(jval.check(button)).isNotNull();
	}
	
	@Test
	@Disabled
	//TODO: find out why fail stbischof
	public void testCheck_FailerFan_NotOK(@InjectInstalledBundle(value = "failer-fan.jar") Bundle failerFan)
			throws Exception {
		Assertions.assertThat(jval.check(failerFan)).isNull();
	}
	
	@Test
	public void testCheck_TestBundleNoSign_NotOK(@InjectInstalledBundle(value = "tb_nosign_fail.jar") Bundle nosign)
			throws Exception {
		Assertions.assertThat(jval.check(nosign)).isNull();
	}
}
