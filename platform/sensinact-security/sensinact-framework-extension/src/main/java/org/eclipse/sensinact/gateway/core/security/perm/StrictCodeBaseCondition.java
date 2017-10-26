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

package org.eclipse.sensinact.gateway.core.security.perm;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Dictionary;

import org.osgi.framework.Bundle;
import org.osgi.service.condpermadmin.Condition;
import org.osgi.service.condpermadmin.ConditionInfo;

/**
 * Condition to test if the location of a bundle matches or does not match a
 * pattern. Since the bundle's location cannot be changed, this condition is
 * immutable.
 * 
 * <p>
 * Pattern matching is done according to the filter string matching rules.
 * 
 */
public class StrictCodeBaseCondition implements Condition
{
	private static final String	CONDITION_TYPE	=
		"org.eclipse.sensinact.gateway.core.security.perm.StrictCodeBaseCondition";
	
	private static final String STRICT_CODEBASE_SOURCES_TYPE =
		"org.eclipse.sensinact.gateway.core.security.perm.StrictCodeBaseCondition$1";
	
	private static CodeBaseCondition.CodeBaseSources _sources = null;
	
	/**
	 * Constructs a condition that tries to match the passed Bundle's location
	 * to the location pattern.
	 * 
	 * @param bundle The Bundle being evaluated.
	 * @param info The ConditionInfo from which to construct the condition. The
	 *        ConditionInfo must specify one or two arguments. The first
	 *        argument of the ConditionInfo specifies the location pattern
	 *        against which to match the bundle location. Matching is done
	 *        according to the filter string matching rules. Any '*' characters
	 *        in the first argument are used as wildcards when matching bundle
	 *        locations unless they are escaped with a '\' character. The
	 *        Condition is satisfied if the bundle location matches the pattern.
	 *        The second argument of the ConditionInfo is optional. If a second
	 *        argument is present and equal to "!", then the satisfaction of the
	 *        Condition is negated. That is, the Condition is satisfied if the
	 *        bundle location does NOT match the pattern. If the second argument
	 *        is present but does not equal "!", then the second argument is
	 *        ignored.
	 * @return Condition object for the requested condition.
	 */
	public static Condition getCondition(final Bundle bundle, final ConditionInfo info) 
	{
		if (!CONDITION_TYPE.equals(info.getType()))
		{
			throw new IllegalArgumentException(
				"ConditionInfo must be of type \"" + CONDITION_TYPE + "\"");
		}
		String[] args = info.getArgs();
		if (args.length != 1 && args.length != 2)
		{
			throw new IllegalArgumentException("Illegal number of args: " + args.length);
		}
		final Condition complies = (info.getArgs().length == 2 
			&& "!".equals(info.getArgs()[1])) ?Condition.FALSE:Condition.TRUE;
		
		final Condition uncomplies = complies.equals(Condition.FALSE)
				?Condition.TRUE:Condition.FALSE;
		
		final CodeBaseCondition.ConditionWrapper pod = 
				new CodeBaseCondition.ConditionWrapper();
		
		pod.c = uncomplies;
		
		if(_sources == null)
		{
			String[] s = info.getArgs()[0].split(",");	
			_sources = new CodeBaseCondition.CodeBaseSources(
					Arrays.<String>asList(s), CONDITION_TYPE, 
					STRICT_CODEBASE_SOURCES_TYPE){};
		}
		_sources.check(STRICT_CODEBASE_SOURCES_TYPE);
		
		AccessController.doPrivileged(new PrivilegedAction<Void>() 
		{
			public Void run() 
			{									
				//search into the current thread's calls stack the one 
				//coming from the allowed code base
				StackTraceElement[] stacktraceElements =
						Thread.currentThread().getStackTrace();
				
				int index = 0;
				int length =  stacktraceElements==null?0:stacktraceElements.length;
				
				for(;index < length; index++)
				{
					StackTraceElement e = stacktraceElements[index];						
					if(_sources.getCache().contains(e.getClassName()))
					{
						pod.c = complies;
						break;
					}			
				}
				return null;
			}
		});	
		return pod.c;
	}
	
	private Bundle bundle;
	private ConditionInfo info;

	private StrictCodeBaseCondition(){}
	
	public StrictCodeBaseCondition(Bundle bundle, ConditionInfo info)
	{
		this.bundle = bundle;
		this.info = info;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.osgi.service.condpermadmin.Condition#isPostponed()
	 */
	@Override
	public boolean isPostponed()
	{
		return false;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.osgi.service.condpermadmin.Condition#isSatisfied()
	 */
	@Override
	public boolean isSatisfied()
	{
		return StrictCodeBaseCondition.getCondition(
				bundle, info).isSatisfied();
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.osgi.service.condpermadmin.Condition#isMutable()
	 */
	@Override
	public boolean isMutable()
	{
		return true;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.osgi.service.condpermadmin.Condition#
	 * isSatisfied(org.osgi.service.condpermadmin.Condition[], java.util.Dictionary)
	 */
	@Override
	public boolean isSatisfied(Condition[] conditions,
	        Dictionary<Object, Object> context)
	{
		if(conditions != null  && conditions.length > 0)
		{
			for(Condition condition : conditions)
			{
				if(!condition.isSatisfied())
				{
					return false;
				}
			}
		}
		return true;
	}
}
