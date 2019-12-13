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
package org.eclipse.sensinact.gateway.core.remote;

public class SensinactCoreBaseIFaceManagerFactoryImpl implements SensinactCoreBaseIFaceManagerFactory  {

	private SensinactCoreBaseIFaceManager sensinactCoreBaseIFaceManager;
	
	@Override
	public SensinactCoreBaseIFaceManager instance() {
		if(this.sensinactCoreBaseIFaceManager == null) {
			this.sensinactCoreBaseIFaceManager = new SensinactCoreBaseIFaceManagerImpl();
		}
		return this.sensinactCoreBaseIFaceManager;
	}

}
