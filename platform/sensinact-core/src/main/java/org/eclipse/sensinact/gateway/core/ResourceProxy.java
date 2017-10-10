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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.Describable;
import org.eclipse.sensinact.gateway.common.primitive.Name;
import org.eclipse.sensinact.gateway.common.primitive.Typable;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodDescription;
import org.eclipse.sensinact.gateway.core.method.UnaccessibleAccessMethod;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;

/**
 * A {@link Resource} proxy
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ResourceProxy extends ModelElementProxy<AttributeDescription>
implements ElementsProxy<AttributeDescription> ,Typable<Resource.Type>
{
	/**
	 * {@link AccessMethod}s of this ResourceProxy
	 */
	protected final Map<String, AccessMethod> methods;	
	
	/**
	 * the {@link Resource.Type} of the {@link ResourceImpl}
	 * this ResourceProxy is the proxy of
	 */
	private final Resource.Type type;
	
	/**
	 * Constructor
	 * 
	 * @param resource
	 * 		the proxied resource
	 */
	ResourceProxy(Mediator mediator, ResourceImpl resource, 
			List<AttributeDescription> descriptions, 
			List<MethodAccessibility> methodAccessibilities)
	{
		super(mediator, Resource.class, resource.getPath());

		super.elements.addAll(descriptions);
		this.type = resource.getType();
		
		Map<String, AccessMethod> methods = new HashMap<String, AccessMethod>();		
		AccessMethod.Type[] existingTypes=AccessMethod.Type.values();
		
		int index = 0;
		int length = existingTypes==null?0:existingTypes.length;
				
		for(;index < length; index++)
		{ 
			AccessMethod method = null;
			if((method=resource.getAccessMethod(existingTypes[index]))==null)
			{
				continue;
			}
			int accessIndex = -1;
			
			if((accessIndex = methodAccessibilities.indexOf(
				new Name<MethodAccessibility>(existingTypes[index].name())))==-1 || 
					!methodAccessibilities.get(accessIndex).isAccessible()) 
			{
				methods.put(existingTypes[index].name(), 
					new UnaccessibleAccessMethod(mediator,super.uri,
							existingTypes[index]));
			} else
			{
				methods.put(existingTypes[index].name(),method);
			}
		}
		this.methods = Collections.<String,AccessMethod>unmodifiableMap(
				methods);
	}

	/**
	 * @inheritDoc
	 *
	 * @see Typable#getType()
	 */
    @Override
    public Resource.Type getType()
    {
	    return this.type;
    }

    /**
     * @inheritDoc
     *
     * @see Describable#getDescription()
     */
    @SuppressWarnings("unchecked")
	public ResourceDescription getDescription()
    {
    	int index= 0;
    	
    	Iterator<Map.Entry<String, AccessMethod>> 
    	iterator = this.methods.entrySet().iterator();
    	
    	AccessMethodDescription[] descriptions =
    			new AccessMethodDescription[this.methods.size()];
    	
    	while(iterator.hasNext())
    	{
    		Map.Entry<String, AccessMethod> 
    		entry = iterator.next(); 
    		descriptions[index++] = entry.getValue().getDescription();
    	}
	    return new ResourceDescription(super.mediator,
	    		this.type, super.uri, super.elements, 
	    		descriptions);
    }

	/**
	 * @inheritDoc
	 *
     * @see SensiNactResourceModelElementProxy#
     * getAccessMethod(String)
     */
    @Override
	public AccessMethod getAccessMethod(String type)
    {
	    return this.methods.get(type);
    }
}
