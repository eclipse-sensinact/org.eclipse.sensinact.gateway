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
package org.eclipse.sensinact.gateway.core.message;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * Extended notification message dedicated to update events
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SnaErrorMessageImpl extends AbstractSnaErrorfulMessage<SnaErrorMessage.Error> implements SnaErrorMessage {
	/**
	 * Constructor
	 */
	protected SnaErrorMessageImpl(Mediator mediator, String uri, SnaErrorMessage.Error type) {
		super(mediator, uri, type);
	}
}
