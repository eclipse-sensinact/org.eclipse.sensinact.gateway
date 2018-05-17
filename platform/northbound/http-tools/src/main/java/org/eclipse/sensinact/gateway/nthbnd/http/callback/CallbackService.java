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
package  org.eclipse.sensinact.gateway.nthbnd.http.callback;

import java.util.Dictionary;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.internal.CallbackServlet;

/**
 * A CallbackService provides the information allowing to create a {@link CallbackServlet}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface CallbackService {

	/**
	 * Returns the String pattern of the {@link CallbackServlet} 
	 * based on this CallbackService
	 * 
	 * @return the String pattern of this CallbackService
	 */
	String getPattern();
	
	/**
	 * Returns the initial set of properties of the {@link CallbackServlet} 
	 * based on this CallbackService
	 * 
	 * @return the set of properties of this CallbackService
	 */
	Dictionary getProperties();

	/**
	 * Returns the {@link Executable} in charge of processing the 
	 * callback, parameterized by a {@link CallbackContext}
	 * 
	 * @return the {@link Executable} processing the callback
	 */
	Executable<CallbackContext,Void> getCallbackProcessor();
}
