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

import java.util.Collection;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Intermediate helper to use a {@link Filtering} service registered in
 * the OSGi host environment
 *  
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class FilteringAccessor extends FilteringDefinition 
implements Filtering {

	private ServiceReference<Filtering> reference;
	private Mediator mediator;
	
	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing
	 * the FilteringAccessor to be instantiated to
	 * interact with the OSGi host environment
	 * @param filterDefinition the {@link FilteringDefinition} 
	 * parameterizing the instantiation of the
	 * FilteringAccessor to be created
	 */
	public FilteringAccessor(Mediator mediator, 
			FilteringDefinition filteringDefinition) 
	{
		super(filteringDefinition.type, 
		filteringDefinition.filter);
		this.mediator = mediator;
		try 
		{
			Collection<ServiceReference<Filtering>> references = 
				mediator.getContext().getServiceReferences(
				Filtering.class,String.format("(type=%s)", 
						super.type));
		
			if(references == null ||references.size()!=1)
			{
				new RuntimeException(
				"Unable to retrieve the appropriate Filtering service reference");
			}
			this.reference = references.iterator().next();
				
		} catch (InvalidSyntaxException e) {
			
			throw new RuntimeException(e);
		}
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Filtering#
	 * handle(java.lang.String)
	 */
	@Override
	public boolean handle(String type) 
	{
		Filtering filtering = this.mediator.getContext(
			).getService(reference);
		
		if(filtering == null)
		{
			mediator.error(
			"Unable to retrieve the appropriate Filtering service");
			return false;
		}	
		boolean handle = filtering.handle(type);
		this.mediator.getContext().ungetService(
				this.reference);
		return handle;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Filtering#
	 * getLDAPComponent()
	 */
	@Override
	public String getLDAPComponent() 
	{
		Filtering filtering = this.mediator.getContext(
			).getService(reference);
		if(filtering == null)
		{
			mediator.error(
			"Unable to retrieve the appropriate Filtering service");
			return null;
		}	
		String ldap  = filtering.getLDAPComponent();
		this.mediator.getContext().ungetService(
				this.reference);
		return ldap;
	}
	
	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Filtering#
	 * apply(java.lang.String, java.lang.Object)
	 */
	@Override
	public String apply(String filter, Object obj) 
	{
		this.mediator.warn(
		"You should use the single argument implementation");
		return null;
	}

	/**
	 * Applies this FilteringAccessor's filter on the specified
	 * object argument and returns the String result
	 * 
	 * @param obj the Object value to be filtered
	 * 
	 * @return the String result of the filtering process
	 */
	public String apply(Object obj) 
	{
		Filtering filtering = this.mediator.getContext(
			).getService(reference);
		if(filtering == null)
		{
			mediator.error(
			"Unable to retrieve the appropriate Filtering service");
			return null;
		}		
		String result  = filtering.apply(super.filter, obj);
		this.mediator.getContext().ungetService(
				this.reference);
		return result;
	}
}
