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

package org.eclipse.sensinact.gateway.common.automata;

import java.net.URL;

/**
 * Signature of a service which provides an xml model of a 
 * {@link FrameModel}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface FrameModelProvider 
{
	/**
	 * Returns the {@link URL} of the xml file describing 
	 * the model
	 * 
	 * @return
	 * 		the {@link URL} of the the xml file describing 
	 * 		the model
	 */
	URL getModelURL();
}
