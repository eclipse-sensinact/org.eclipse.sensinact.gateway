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
package org.eclipse.sensinact.gateway.core;

import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.common.primitive.Elements;

/**
 * Abstract {@link ModelElementDescriptionOld} implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class ModelElementDescription<D extends Description> 
extends Elements<D> implements Description
{

	protected Mediator mediator;

	protected ModelElementDescription (
			Mediator mediator, String uri, 
			List<D> descriptions)
	{
		super(uri);
		this.mediator = mediator;
		super.elements.addAll(descriptions);
	}
}
