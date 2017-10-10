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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.gateway.core.method.AccessMethod;

/**
 * Pre-defined set of {@link AccessProfile} policies
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@SuppressWarnings("serial")
public enum AccessProfileOption
{	
	DEFAULT(new HashMap<AccessMethod.Type,Integer>()
	{{
		put(AccessMethod.Type.valueOf(
				AccessMethod.GET), new Integer(1));
		put(AccessMethod.Type.valueOf(
				AccessMethod.SET), new Integer(2));
		put(AccessMethod.Type.valueOf(
				AccessMethod.ACT), new Integer(2));
		put(AccessMethod.Type.valueOf(
				AccessMethod.SUBSCRIBE), new Integer(1));
		put(AccessMethod.Type.valueOf(
				AccessMethod.UNSUBSCRIBE), new Integer(1));
		put(AccessMethod.Type.valueOf(
				AccessMethod.DESCRIBE), new Integer(1));
	}}),
	ALL_ANONYMOUS(new HashMap<AccessMethod.Type,Integer>()
	{{
		put(AccessMethod.Type.valueOf(
				AccessMethod.GET), new Integer(1));
		put(AccessMethod.Type.valueOf(
				AccessMethod.SET), new Integer(1));
		put(AccessMethod.Type.valueOf(
				AccessMethod.ACT), new Integer(1));
		put(AccessMethod.Type.valueOf(
				AccessMethod.SUBSCRIBE), new Integer(1));
		put(AccessMethod.Type.valueOf(
				AccessMethod.UNSUBSCRIBE), new Integer(1));
		put(AccessMethod.Type.valueOf(
				AccessMethod.DESCRIBE), new Integer(1));
	}}),
	NO_ANONYMOUS(new HashMap<AccessMethod.Type,Integer>()
	{{
		put(AccessMethod.Type.valueOf(
				AccessMethod.GET), new Integer(2));
		put(AccessMethod.Type.valueOf(
				AccessMethod.SET), new Integer(2));
		put(AccessMethod.Type.valueOf(
				AccessMethod.ACT), new Integer(2));
		put(AccessMethod.Type.valueOf(
				AccessMethod.SUBSCRIBE), new Integer(2));
		put(AccessMethod.Type.valueOf(
				AccessMethod.UNSUBSCRIBE), new Integer(2));
		put(AccessMethod.Type.valueOf(
				AccessMethod.DESCRIBE), new Integer(2));
	}}),
	ADMIN(new HashMap<AccessMethod.Type,Integer>()
	{{
		put(AccessMethod.Type.valueOf(
				AccessMethod.GET), new Integer(3));
		put(AccessMethod.Type.valueOf(
				AccessMethod.SET), new Integer(3));
		put(AccessMethod.Type.valueOf(
				AccessMethod.ACT), new Integer(3));
		put(AccessMethod.Type.valueOf(
				AccessMethod.SUBSCRIBE), new Integer(3));
		put(AccessMethod.Type.valueOf(
				AccessMethod.UNSUBSCRIBE), new Integer(3));
		put(AccessMethod.Type.valueOf(
				AccessMethod.DESCRIBE), new Integer(3));
	}}),	
	OWNER(new HashMap<AccessMethod.Type,Integer>()
	{{
		put(AccessMethod.Type.valueOf(
				AccessMethod.GET), new Integer(3));
		put(AccessMethod.Type.valueOf(
				AccessMethod.SET), new Integer(4));
		put(AccessMethod.Type.valueOf(
				AccessMethod.ACT), new Integer(4));
		put(AccessMethod.Type.valueOf(
				AccessMethod.SUBSCRIBE), new Integer(3));
		put(AccessMethod.Type.valueOf(
				AccessMethod.UNSUBSCRIBE), new Integer(3));
		put(AccessMethod.Type.valueOf(
				AccessMethod.DESCRIBE), new Integer(3));
	}});
	
	
	/**
	 * @param map
	 * 
	 * @return 
	 */
	private static final Set<MethodAccess> buildMethodAccesses(
			HashMap<AccessMethod.Type, Integer> map)
	{
		Set<MethodAccess> methodAccesses = new HashSet<MethodAccess>();
		
		Iterator<Map.Entry<AccessMethod.Type, Integer>> iterator =
				map.entrySet().iterator();
		
		while(iterator.hasNext())
		{
			Map.Entry<AccessMethod.Type, Integer> entry = iterator.next();	
			AccessMethod.Type type = entry.getKey();
			AccessLevel accessLevel = new AccessLevelImpl(entry.getValue());
			methodAccesses.add(new MethodAccessImpl(accessLevel,type));
		}
		return methodAccesses;
	}
	
	/**
	 * Returns the AccessProfileOption holding the same {@link 
	 * AccessProfile} as the one passed as parameter; meaning
	 * that the same {@link AccessMethod.Type} has the same {@link 
	 * AccessLevel}
	 * 
	 * @param profile
	 * 		
	 * @return
	 */
	public static AccessProfileOption valueOf(AccessProfile profile)
	{		
		if(profile == null)
		{
			return AccessProfileOption.ALL_ANONYMOUS;
		}
		Set<MethodAccess> methodAccesses = profile.getMethodAccesses();
		Map<AccessMethod.Type, AccessLevel> profileMap = 
				new HashMap<AccessMethod.Type, AccessLevel>();
		
		Iterator<MethodAccess> iterator = methodAccesses.iterator();
		while(iterator.hasNext())
		{
			MethodAccess methodAccess = iterator.next();
			profileMap.put(methodAccess.getMethod(), 
					methodAccess.getAccessLevel());
		}

		AccessProfileOption[] values = AccessProfileOption.values();
		int index = 0;
		int length = values==null?0:values.length;
		
		AccessProfileOption value = null;
		
		for(;index < length ; index++)
		{
			AccessProfile optionProfile = values[index].getAccessProfile();
			Set<MethodAccess> optionAccesses = optionProfile.getMethodAccesses();

			Iterator<MethodAccess> optionIterator = optionAccesses.iterator();
			boolean found = true;
			
			while(optionIterator.hasNext())
			{
				MethodAccess methodAccess = optionIterator.next();
				if(profileMap.get(methodAccess.getMethod()).getLevel()
						!= methodAccess.getAccessLevel().getLevel())
				{
					found = false;
					break;
				}
			}
			if(found)
			{
				value = values[index];
				break;
			}
		}		
		return value;
	}
	
	private final AccessProfileImpl accessProfile;

	/**
	 * Returns the {@link AccessProfile} associated to this
	 * access policy
	 * 
	 * @return this access policy's {@link AccessProfile}
	 */
	public AccessProfile getAccessProfile()
	{
		return this.accessProfile;
	}
	
	/**
	 * Constructor
	 * 
	 * @param map 
	 */
	AccessProfileOption(HashMap<AccessMethod.Type,Integer> map)
	{
		Set<MethodAccess> methodAccesses  = buildMethodAccesses(map);
		this.accessProfile = new AccessProfileImpl(methodAccesses);
	}
	
}