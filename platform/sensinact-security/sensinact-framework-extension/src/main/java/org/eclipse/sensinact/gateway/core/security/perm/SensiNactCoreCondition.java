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
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.wiring.BundleRevision;
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
public class SensiNactCoreCondition implements Condition
{
	private static final String	CONDITION_TYPE	=
			"org.eclipse.sensinact.gateway.core.security.perm.SensiNactCoreCondition";

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
	public static Condition getCondition(final Bundle bundle,
			final ConditionInfo info) 
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
		String bundleLocation = AccessController.doPrivileged(
		new PrivilegedAction<String>() 
		{
			public String run() 
			{
				String location = null;
				if((bundle.adapt(BundleRevision.class
						).getTypes() & BundleRevision.TYPE_FRAGMENT) != 0)
				{
					String hostName = bundle.getHeaders().get(Constants.FRAGMENT_HOST);
					if("sensinact-core".equals(hostName) 
							&& bundle.getState()==Bundle.RESOLVED)
					{
						location = info.getArgs()[0];
						
					} else
					{
						location = bundle.getLocation();
					}
				} else
				{
					location = bundle.getLocation();
				}
				return location;
			}
		});
		
		Filter filter = null;
		try
		{
			filter = FrameworkUtil.createFilter("(location="
					+ escapeLocation(args[0]) + ")");
		}
		catch (InvalidSyntaxException e)
		{
			// this should never happen, but just in case
			throw new RuntimeException("Invalid filter: " + e.getFilter(), e);
		}
		if(bundleLocation == null)
		{
			return Condition.FALSE;
		}
		Dictionary<String, String> matchProps = new Hashtable<String, String>(2);
		matchProps.put("location", bundleLocation);
		
		boolean negate = (args.length == 2) ? "!".equals(args[1]) : false;
		
		return (negate ^ filter.match(matchProps)) ? Condition.TRUE
				: Condition.FALSE;
	}

	private Bundle bundle;
	private ConditionInfo info;

	private SensiNactCoreCondition()
	{}
	

	public SensiNactCoreCondition(Bundle bundle, ConditionInfo info)
	{
		this.bundle = bundle;
		this.info = info;
	}
	
	/**
	 * Escape the value string such that '(', ')' and '\' are escaped. The '\'
	 * char is only escaped if it is not followed by a '*'.
	 * 
	 * @param value unescaped value string.
	 * @return escaped value string.
	 */
	private static String escapeLocation(final String value) {
		boolean escaped = false;
		int inlen = value.length();
		int outlen = inlen << 1; /* inlen * 2 */

		char[] output = new char[outlen];
		value.getChars(0, inlen, output, inlen);

		int cursor = 0;
		for (int i = inlen; i < outlen; i++) {
			char c = output[i];
			switch (c) {
				case '\\' :
					if (i + 1 < outlen && output[i + 1] == '*')
						break;
				case '(' :
				case ')' :
					output[cursor] = '\\';
					cursor++;
					escaped = true;
					break;
			}

			output[cursor] = c;
			cursor++;
		}

		return escaped ? new String(output, 0, cursor) : value;
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
		return SensiNactCoreCondition.getCondition(
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
