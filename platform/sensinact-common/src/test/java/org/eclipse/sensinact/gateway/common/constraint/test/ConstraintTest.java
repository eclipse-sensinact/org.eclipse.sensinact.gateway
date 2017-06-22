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

package org.eclipse.sensinact.gateway.common.constraint.test;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.skyscreamer.jsonassert.JSONAssert;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;

/**
 * test Constraint
 */
public class ConstraintTest
{		
	public static final String EXPRESSION = "[\"AND\","
		+ "{\"operator\":\"-\",\"operand\":14, \"complement\":false},"
		+ "{\"operator\":\"regex\",\"operand\":\"(0033)[0-9]{9}\",\"complement\":false}]";
	
	public static final String COLLECTION = 
		"{\"operator\":\"in\",\"operand\":[\"a\",\"b\",18,3], \"type\":\"string\", \"complement\":false}";

	public static final String RETURNED_COLLECTION = 
		"{\"operator\":\"in\",\"operand\":[\"a\",\"b\",\"18\",\"3\"], \"type\":\"string\", \"complement\":false}";

	public static final String ABSOLUTE = 
		"{\"operator\":\"abs\",\"operand\":[5,18], \"complement\":false}";
	
	public static final String RETURNED_ABSOLUTE = 
		"{\"operator\":\"abs\",\"operand\":[5.0,18.0], \"complement\":false}";
	
	private static final String LOG_FILTER = "("+Constants.OBJECTCLASS+"="+
		LogService.class.getCanonicalName()+")";	
	
	private static final String MOCK_BUNDLE_NAME = "MockedBundle";
	private static final long MOCK_BUNDLE_ID = 1;
	
	private final BundleContext context = Mockito.mock(BundleContext.class);
	private final Bundle bundle = Mockito.mock(Bundle.class);

	private Mediator mediator;
	
	@Before
	public void init() throws InvalidSyntaxException 
	{
		Filter filter = Mockito.mock(Filter.class);
		Mockito.when(filter.toString()).thenReturn(LOG_FILTER);		
		
		Mockito.when(context.createFilter(LOG_FILTER)).thenReturn(
				filter);
		Mockito.when(context.getServiceReferences((String)Mockito.eq(null), 
				Mockito.eq(LOG_FILTER))).thenReturn(null);		
		Mockito.when(context.getServiceReference(LOG_FILTER)
				).thenReturn(null);	

    	Mockito.when(context.getServiceReferences(
    			Mockito.anyString(),
    			Mockito.anyString())).then(
    					new Answer<ServiceReference[]>()
    		{
    			@Override
                public ServiceReference[] answer(InvocationOnMock invocation)
                        throws Throwable
                {
    				Object[] arguments = invocation.getArguments();
    				if(arguments==null || arguments.length !=2)
    				{
    					return null;
    				}
    				return null;	
                }
    		});
		Mockito.when(context.getService(Mockito.any(ServiceReference.class))).then(
					new Answer<Object>(){
	
				@Override
	            public Object answer(InvocationOnMock invocation)
	                    throws Throwable
	            {
					Object[] arguments = invocation.getArguments();
					if(arguments==null || arguments.length!=1)
					{
						return null;					
					}
					return null;	
	            }				
			}); 		
		Mockito.when(context.getBundle()).thenReturn(bundle);
		Mockito.when(bundle.getSymbolicName()).thenReturn(MOCK_BUNDLE_NAME);
		Mockito.when(bundle.getBundleId()).thenReturn(MOCK_BUNDLE_ID);
		
		mediator = new Mediator(context);
     }
	
	@Test
	public void testFactory() throws Exception
	{
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			
	        Constraint constraint = ConstraintFactory.Loader.load(
	        		classloader, new JSONArray(ConstraintTest.EXPRESSION));
	        Assert.assertTrue(constraint.complies("0033921976095"));
	        Assert.assertFalse(constraint.complies("0033aa9976095"));
	        Assert.assertFalse(constraint.complies("003368997609544"));

	        JSONAssert.assertEquals(ConstraintTest.EXPRESSION, constraint.getJSON(), false);

	        constraint = ConstraintFactory.Loader.load(
	        		classloader, new JSONObject(ConstraintTest.COLLECTION));
	        Assert.assertTrue(constraint.complies("3"));
	        Assert.assertTrue(constraint.complies("a"));
	        Assert.assertFalse(constraint.complies("d"));
	        
	        JSONAssert.assertEquals(ConstraintTest.RETURNED_COLLECTION, constraint.getJSON(), false);
	        
	        constraint = constraint.getComplement();
	        Assert.assertFalse(constraint.complies("3"));
	        Assert.assertFalse(constraint.complies("a"));
	        Assert.assertTrue(constraint.complies("d"));	
	        
	        constraint = ConstraintFactory.Loader.load(
	        		classloader,  new JSONObject(ConstraintTest.ABSOLUTE));
	        Assert.assertTrue(constraint.complies(23));
	        Assert.assertTrue(constraint.complies(25));
	        Assert.assertTrue(constraint.complies(13));
	        Assert.assertTrue(constraint.complies(10));
	        Assert.assertFalse(constraint.complies(17));
	        Assert.assertFalse(constraint.complies(18));
	        Assert.assertFalse(constraint.complies(16));
	        
	        JSONAssert.assertEquals(ConstraintTest.RETURNED_ABSOLUTE, constraint.getJSON(), false);
	}
}
