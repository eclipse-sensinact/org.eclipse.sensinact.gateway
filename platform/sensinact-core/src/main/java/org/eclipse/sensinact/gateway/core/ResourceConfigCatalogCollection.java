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

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A collection of {@link ResourceConfigCatalog}s
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ResourceConfigCatalogCollection
{
	Deque<ResourceConfigCatalog> catalogs;
	
	/**
	 * Constructor
	 */
	public ResourceConfigCatalogCollection()
	{
		this.catalogs = new LinkedList<ResourceConfigCatalog>();
	}
	
	/**
	 * Adds the {@link ResourceConfigCatalog} passed 
	 * as parameter 
	 * 
	 * @param catalog the {@link ResourceConfigCatalog} to add
	 */
	public void add(ResourceConfigCatalog catalog)
	{
		if(catalog == null)
		{
			return;
		}
		synchronized(this.catalogs)
		{
			this.catalogs.add(catalog);
		}
	}
	
	/**
	 * Deletes the {@link ResourceConfigCatalog} passed 
	 * as parameter 
	 * 
	 * @param catalog the {@link ResourceConfigCatalog} to delete
	 */
	public void delete(ResourceConfigCatalog catalog)
	{
		if(catalog == null)
		{
			return;
		}
		synchronized(this.catalogs)
		{
			this.catalogs.remove(catalog);
		}
	}

	/**
	 * Returns an {@link Iterator} over the list of
	 * {@link ResourceConfigCatalog}s handled by this
	 * ResourceConfigCatalogCollection
	 * 
	 * @return an {@link Iterator} over this 
	 *  ResourceConfigCatalogCollection's  
	 *  {@link ResourceConfigCatalog}s list
	 */
	public Iterator<ResourceConfigCatalog> iterator()
	{	
		final ResourceConfigCatalog[] catalogsArray;
		
		synchronized(this.catalogs)
		{
			catalogsArray = new 
			ResourceConfigCatalog[this.catalogs.size()];			
			this.catalogs.toArray(catalogsArray);
		}
		return new Iterator<ResourceConfigCatalog>()
		{
			int position = 0;
			
			/**
			 * @inheritDoc
			 *
			 * @see java.util.Iterator#hasNext()
			 */
			@Override
			public boolean hasNext() 
			{
				return position < catalogsArray.length;
			}

			/**
			 * @inheritDoc
			 *
			 * @see java.util.Iterator#next()
			 */
			@Override
			public ResourceConfigCatalog next()
			{
				if(hasNext())
				{
					return catalogsArray[position++];
				}
				return null;
			}

			/**
			 * @inheritDoc
			 *
			 * @see java.util.Iterator#remove()
			 */
			@Override
			public void remove() {}
		};
	}
	
	/**
	 * Retrieves and returns the previously registered {@link ResourceConfig} 
	 * described by the {@link ResourceDescriptor} passed as parameter 
	 * if it exists in this ResourceConfigCatalogCollection
	 *  
	 * @param descriptor the {@link ResourceDescriptor} describing the 
	 * {@link ResourceConfig} to be returned
	 * 
	 * @return the previously registered {@link ResourceConfig} described by 
	 * the specified {@link ResourceDescriptor} if it exists ; returns 
	 * null otherwise
	 */
	public ResourceConfig getResourceConfig(ResourceDescriptor descriptor) 
	{
		Iterator<ResourceConfigCatalog> iterator = 
				this.iterator();
		
		ResourceConfig resourceConfig = null;
		
		while(iterator.hasNext())
		{
			try
			{
				ResourceConfigCatalog catalog = iterator.next();			
				if((resourceConfig = catalog.getResourceConfig(
						descriptor))!=null)
				{
					break;
				}
			}
			catch(ClassCastException e)
			{
				continue;
			}
		}
		return resourceConfig;
	}
}
