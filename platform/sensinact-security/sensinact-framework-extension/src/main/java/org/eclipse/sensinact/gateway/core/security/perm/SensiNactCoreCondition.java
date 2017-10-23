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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
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
	private static final class ConditionPod
	{
		Condition c = null;
	}
	
	
	private static final String	CONDITION_TYPE	=
			"org.eclipse.sensinact.gateway.core.security.perm.SensiNactCoreCondition";

	private static final char ESCAPE = '\\';
	
	private static final char WILDCARD = '*';
	
	private static final Bundle findBundle(BundleContext context, String name, boolean location)
	{
		Bundle bundle = null;
		if ( name != null && context!=null)
		{
			if(location)
			{
				bundle = context.getBundle(name);
				if((bundle.adapt(BundleRevision.class).getTypes() 
						& BundleRevision.TYPE_FRAGMENT) != 0
						&& bundle.getState()==Bundle.RESOLVED)
				{
					bundle = findBundle(context, bundle.getHeaders().get(
						Constants.FRAGMENT_HOST), false);	
				}
				return bundle;
			}
			Bundle[] bundles  = context.getBundles();
			
			int index = 0;
			int length = bundles == null?0:bundles.length;
			for(;index < length; index++)
			{
				final Bundle tmp = bundles[index];
				if(name.equals(tmp.getSymbolicName()))
				{
					if((tmp.adapt(BundleRevision.class).getTypes() 
						& BundleRevision.TYPE_FRAGMENT) != 0
						&& tmp.getState()==Bundle.RESOLVED)
					{
						bundle = findBundle(context, tmp.getHeaders().get(
								Constants.FRAGMENT_HOST), false);	
					} else
					{
						bundle = tmp;
					}
					break;
				}
			}
		}
		return bundle;
	}
	
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
		final char[] expected = info.getArgs()[0].toCharArray();
		 
		if(expected.length == 1 && (expected[0]== WILDCARD))
		{
			//it means that ACCEPT {["*" "!"]...} will give the same result as DENY {["*"]...}
			return Condition.TRUE;
		}		
		final Condition complies = (info.getArgs().length == 2 
			&& "!".equals(info.getArgs()[1])) ?Condition.FALSE:Condition.TRUE;
		final Condition uncomplies = complies.equals(Condition.FALSE)
				?Condition.TRUE:Condition.FALSE;
		
		final ConditionPod pod = new ConditionPod();
		pod.c = uncomplies;
		
		AccessController.doPrivileged(new PrivilegedAction<Void>() 
		{
			//TODO: TO BE IMPROVED (A LOT)
			public Void run() 
			{	
				//Is it the right way to do it ? Or is it better to use
				//the bundle argument classloader ?
				
				//retieve the appropriate class loader
				Bundle b = null;
				ClassLoader classloader = null;
				try
				{
					b = findBundle(bundle.getBundleContext(), info.getArgs()[0], true);
					if(b == null)
					{ 
						if((bundle.adapt(BundleRevision.class).getTypes() 
						& BundleRevision.TYPE_FRAGMENT) != 0 
						&& bundle.getState() == Bundle.RESOLVED)
						{
							b = findBundle(bundle.getBundleContext(),
								bundle.getHeaders().get(Constants.FRAGMENT_HOST), 
								false);	
						} else
						{
							b = bundle;
						}
					}
					classloader = b.adapt(BundleWiring.class).getClassLoader();
					
				} catch(Exception e)
				{
					classloader = Thread.currentThread().getContextClassLoader();
				}
				//search into the current thread's calls stack the one 
				//coming from the allowed code base
				StackTraceElement[] stacktraceElements = Thread.currentThread().getStackTrace();
				
				//System.out.println(Thread.currentThread().getContextClassLoader());
				
				for(StackTraceElement e : stacktraceElements)
				{
					Class<?> c = null;
					try
					{
						c = classloader.loadClass(e.getClassName());
						if(c == null)
						{
							continue;
						}
						char[] ccs = c.getProtectionDomain(
							).getCodeSource().getLocation().toString(
									).toCharArray();
						
						int index = 0;
						//TODO: handle escape char
						for(;;)
						{
							if(index >= ccs.length 
							|| index >= expected.length)
							{
								break;
							}
							if(expected[index] == WILDCARD)
							{
								index++;
								break;
							}
							if(ccs[index] != expected[index])
							{
								index=0;
								break;
							}
							index++;
						}
						if(index > 0)
						{
							pod.c = complies;
							break;
						}
//						if(c!=null)
//						{
//							System.out.println(e.getClassName() + " / " + 
//							c.getProtectionDomain().getCodeSource().getLocation().toString());
//						}		
					}
					catch (Exception ex)
					{
						continue;
					}			
				}
				return null;
			}
		});	
		return pod.c;
	}

	private Bundle bundle;
	private ConditionInfo info;

	private SensiNactCoreCondition(){}
	

	public SensiNactCoreCondition(Bundle bundle, ConditionInfo info)
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
