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
{
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
	 * Returns true if the {@link Filtering} service 
	 * wrapped by this FilteringAccessor is able
	 * to handle the String type of filter passed as 
	 * parameter; returns false otherwise
	 * 
	 * @param type the String type of filter
	 * 
	 * @return 
	 * <ul>
	 * 		<li>true if the specified type of filter is 
	 * 			handled by the wrapped {@link Filtering}
	 * 			service</li>
	 * 		<li> false otherwise</li>
	 * </ul> 
	 */
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
	 * Returns the String formated LDAP component part of the 
	 * {@link Filtering} service wrapped by this FilteringAccessor.
	 * The returned filter is used to discriminate the elements on 
	 * which the wrapped {@link Filtering} service will be applied on 
	 * 
	 * @return the String formated LDAP component part 
	 * of the wrapped Filtering service
	 */
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
		String ldap  = filtering.getLDAPComponent(super.filter);
		this.mediator.getContext().ungetService(
				this.reference);
		return ldap;
	}

	/**
	 * Applies the {@link Filtering} service wrapped by this 
	 * FilteringAccessor on the specified object argument and 
	 * returns the String result
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
