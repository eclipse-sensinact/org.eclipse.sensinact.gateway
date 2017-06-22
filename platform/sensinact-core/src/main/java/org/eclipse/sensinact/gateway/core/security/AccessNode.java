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
package org.eclipse.sensinact.gateway.core.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.Name;
import org.eclipse.sensinact.gateway.core.ModelElement;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethod.Type;
import org.eclipse.sensinact.gateway.util.tree.PathNode;


/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AccessNode extends PathNode
{
	//********************************************************************//
	//						NESTED DECLARATIONS	     					  //
	//********************************************************************//
    
	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//
    
	//********************************************************************//
	//						STATIC DECLARATIONS  						  //
	//********************************************************************//	    
    
	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//

	protected Mediator mediator;	
	protected Map<AccessLevelOption, List<MethodAccessibility>> accesses;
	private AccessProfile profile;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 */
    public AccessNode(Mediator mediator)
	{
    	super();
    	this.mediator = mediator;
    	this.accesses = new EnumMap<AccessLevelOption, 
    		List<MethodAccessibility>>(AccessLevelOption.class);
	}

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param name
	 */
    public AccessNode(Mediator mediator, String name)
	{
    	this(mediator, name, false);
	}
    
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param name
	 * @param isPattern
	 */
    public AccessNode(Mediator mediator, String name, boolean isPattern)
	{
    	super(name, isPattern);
    	this.mediator = mediator;
		this.accesses = new EnumMap<AccessLevelOption, 
			List<MethodAccessibility>>(AccessLevelOption.class);
	}

	/**
     * Defines the Map of available {@link AccessMethod.Type} for all 
     * pre-defined {@link AccessLevelOption}s according to the one
     * passed as parameter
     *  
     * @param option
     * 		the {@link AccessProfileOption} holding the {@link AccessProfile}
     * 		for which to build the Map of available {@link AccessMethod.Type} 
     * 		for	pre-defined {@link AccessLevelOption}s
     */
	public AccessNode withAccessProfile(AccessProfileOption option)
	{
		return  withAccessProfile(option.getAccessProfile());
	}
	
	/**
     * Defines the Map of available {@link AccessMethod.Type} for all 
     * pre-defined {@link AccessLevelOption}s according to the {@link 
     * AccessProfile} passed as parameter
     *  
     * @param profile
     * 		the {@link AccessProfile} for which to build the
     * 		Map of available {@link AccessMethod.Type} for
     * 		pre-defined {@link AccessLevelOption}s
     */
	public AccessNode withAccessProfile(AccessProfile profile)
    {
    	if(this.profile != null || profile == null)
    	{
    		return this;
    	}
    	this.profile = profile;
		try
        {	   
			Set<MethodAccess> methodAccesses = profile.getMethodAccesses();
		    if(methodAccesses.isEmpty())
	        {
	        	return this;
	        }
    		int[] accessLevels = null;

	        Iterator<AccessLevelOption> iterator = 
	        	Arrays.asList(AccessLevelOption.values()).iterator();
	        
    		AccessMethod.Type[] types = AccessMethod.Type.values();
    		
        	accessLevels = new int[types.length];	
        	Iterator<MethodAccess> accessesIterator = methodAccesses.iterator();
        	while(accessesIterator.hasNext())
	        {
        		MethodAccess methodAccess = accessesIterator.next();    	
	        	int level = methodAccess.getAccessLevel().getLevel();
	        	int ind = methodAccess.getMethod().ordinal();
	        	accessLevels [ind] = level;	        	
	        }	  
	        int index = -1;
	        int length = types==null?0:types.length;
	        
	        while(iterator.hasNext())
	        {
	        	index = 0;
	        	AccessLevelOption optionLevel = iterator.next();
	        	
	        	for(;index < length;index++)
	        	{	    			
	        		this.setAccessibleMethod(types[index], optionLevel, 
	        		(accessLevels[index]<= optionLevel.getAccessLevel(
	        				).getLevel()));
	        	}
	        }
        }
        catch (Exception e)
        {
	       mediator.error(e);
        }
		return this;
    }
	
	/**
	 * Returns the list of {@link MethodAccessibility} available for 
	 * the {@link ModelElement} mapped to this AccessNode and the
	 * {@link AccessLevelOption} passed as parameter
	 * 
	 * @param userAccessEntity the {@link AccessLevelOption} for which 
	 * to retrieve the set of {@link MethodAccessibility}
	 * 
	 * @return the set of {@link MethodAccessibility} for the mapped 
	 * {@link ModelElement} and the specified {@link AccessLevelOption}
	 */
	public List<MethodAccessibility> getAccessibleMethods(
			AccessLevelOption accessLevelOption)
	{		
		List<MethodAccessibility> accesses = new ArrayList<MethodAccessibility>();
		AccessMethod.Type[] types = AccessMethod.Type.values();
		
		int index = 0;
		int length = types.length;
		
		for(;index<length;index++)
		{
			accesses.add(new MethodAccessibilityImpl(
			types[index],accessLevelOption,this.isAccessibleMethod(
					types[index],accessLevelOption)));
		}
		return accesses;
	}
	
	/**
	 * Defines the availability of the {@link AccessMethod.Type} passed as 
	 * parameter for the {@link AccessLevel} also passed as parameter
	 * 
	 * @param accessMethod
	 * 		the {@link AccessMethod.Type} to define the availability of for 
	 * 		the specified {@link AccessLevel}
	 * @param optionLevel
	 * 		the {@link AccessLevel} for which to define the specified 
	 * 		{@link AccessMethod.Type} availability
	 * @param accessibility
	 * 		<ul>
	 * 			<li>true if the specified {@link AccessMethod.Type} is accessible
	 * 				for the specified {@link AccessLevel}</li>
	 * 			<li>false otherwise</li>
	 * 		</ul>
	 */
	protected void setAccessibleMethod(AccessMethod.Type accessMethod,
			AccessLevelOption optionLevel, boolean accessibility)
	{
		List<MethodAccessibility> methodAccesses = this.accesses.get(optionLevel);
		if(methodAccesses == null)
		{			
			methodAccesses = new ArrayList<MethodAccessibility>();
			this.accesses.put(optionLevel, methodAccesses);
		}
		methodAccesses.add(new MethodAccessibilityImpl(accessMethod,
				optionLevel, accessibility));
	}	
	
	/** 
	 * Returns true if the {@link AccessMethod.Type} passed as parameter is 
	 * available for the {@link AccessLevel} passed as parameter; otherwise
	 * returns false
	 * 
	 * @param accessMethod
	 * 		the {@link AccessMethod.Type} to define the availability of for 
	 * 		the specified {@link AccessLevel}
	 * @param accessLevel
	 * 		the {@link AccessLevel} for which to define the specified 
	 * 		{@link AccessMethod.Type} availability
	 * @return 
	 * 		<ul>
	 * 			<li>true if the specified {@link AccessMethod.Type} is accessible
	 * 				for the specified {@link AccessLevel}</li>
	 * 			<li>false otherwise</li>
	 * 		</ul>
	 */
	protected boolean isAccessibleMethod(AccessMethod.Type accessMethod,
			AccessLevelOption optionLevel)
	{
		int index = -1;
		boolean accessible = false;
		
		List<MethodAccessibility> methodAccesses;
		
		if(this.accesses == null || (methodAccesses = this.accesses.get(
			optionLevel))==null || (index = methodAccesses.indexOf(
				new Name<MethodAccessibility>(accessMethod.name()))) ==-1)
		{			
			accessible =  this.parent==null?false:
				((AccessNode)this.parent).isAccessibleMethod(
						accessMethod, optionLevel);
		} else
		{
			MethodAccessibility access =  methodAccesses.get(index);
			accessible = access==null?false:access.isAccessible();
		}
		return 	accessible;
	}

	/**
	 * Returns the 'minimum' required {@link AccessLevelOption} allowing 
	 * to invoke the {@link AccessMethod.Type} passed as parameter
	 * on a {@link ModelElement} mapped to this {@link AccessNode}
	 *  
	 * @param method the targeted {@link AccessMethod.Type}
	 * 
	 * @return  the minimum required {@link AccessLevel} to invoke
	 * the specified the {@link AccessMethod.Type}
	 */
	public AccessLevelOption getAccessLevelOption(Type method)
	{
		AccessLevelOption[] accessLevelOptions = AccessLevelOption.values();
		int index = 0;
		int length = accessLevelOptions==null?0:accessLevelOptions.length;
		for(;index < length; index++)
		{					
			if(this.isAccessibleMethod(method, accessLevelOptions[index]))
			{
				return accessLevelOptions[index];
			}
		}
		return AccessLevelOption.OWNER;		
	}
}
