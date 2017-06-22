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
import org.eclipse.sensinact.gateway.common.props.KeysCollection;
import org.eclipse.sensinact.gateway.common.props.TypedProperties;
import org.eclipse.sensinact.gateway.common.primitive.PathElement;

/**
 * Abstract implementation of an {@link AbstractSnaMessage}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractSnaMessage<S extends Enum<S> 
& KeysCollection & SnaMessageSubType> extends TypedProperties<S> 
implements SnaMessage<S>
{				
	/**
	 * Constructor
	 * 
	 * @param uri
	 * @param type
	 */
	protected AbstractSnaMessage(Mediator mediator, String uri, S type)
	{
		super(mediator, type);
		super.putValue(SnaConstants.URI_KEY, uri);
		
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see PathElement#getPath()
	 */
	public String getPath()
	{
		return super.<String>get(SnaConstants.URI_KEY);
	}

	/**
	 * Returns the {@link SnaMessage.Type} to which
	 * this extended {@link SnaMessage}'s type belongs to
	 */
	public SnaMessage.Type getSnaMessageType()
	{
		S type = super.getType();
		return type.getSnaMessageType();
	}
	
}
