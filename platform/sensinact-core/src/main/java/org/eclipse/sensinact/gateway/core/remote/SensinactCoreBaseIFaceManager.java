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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

public interface SensinactCoreBaseIFaceManager {

	public static final String EMPTY_NAMESPACE = "#LOCAL#";
	public static final String FILTER_MAIN = "org.eclipse.sensinact.gateway.core.api.SensinactCoreBaseIface";
	public static final String REMOTE_NAMESPACE_PROPERTY = "org.eclipse.sensinact.remote.namespace";
	
	String namespace();
    
    void start(Mediator mediator);
    
    void stop();
}
