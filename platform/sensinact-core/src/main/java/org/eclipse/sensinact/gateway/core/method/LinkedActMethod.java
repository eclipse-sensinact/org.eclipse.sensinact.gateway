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
package org.eclipse.sensinact.gateway.core.method;


import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * Extended {@link AccessMethod} dedicated to an Actuation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class LinkedActMethod extends ActMethod
{	    
    private final ActMethod linked;

    /**
	 * Constructor
	 * 
     * @param mediator
     * @param uri
     * @param linked
     */
    public LinkedActMethod(Mediator mediator, String uri,
    		ActMethod linked, boolean copyActMethod)
    {
    	super(mediator, uri, linked!=null?linked.preProcessingExecutor:null);
    	this.linked = linked;
    	
    	if(copyActMethod && linked != null)
    	{
    		linkedSignatures();
    	}
    }

    /**
     * Links all registered executors of the {@link ActMethod}
     * passed as parameter to this LinkedActMethod
     * 
     * @param linked
     * 		the {@link ActMethod} to link with
     */
    private final void linkedSignatures()
    {
    	if(this.linked == null)
    	{
    		return;
    	}
    	Iterator<Map.Entry<Signature, Deque<AccessMethodExecutor>>> 
    	signaturesIterator = linked.map.entrySet().iterator();
    	
    	while(signaturesIterator.hasNext())
    	{
    		Map.Entry<Signature, Deque<AccessMethodExecutor>> entry =
    				signaturesIterator.next(); 
    		
    		super.map.put((Signature)entry.getKey().clone(), 
    				new LinkedList<AccessMethodExecutor>(entry.getValue()));
    	}
    	Iterator<Map.Entry<Shortcut,Signature>> shortcutsIterator = 
    			linked.shortcuts.entrySet().iterator();
    	
    	while( shortcutsIterator.hasNext())
    	{
    		Map.Entry<Shortcut,Signature> entry = 
    				shortcutsIterator.next();  
    		
    		super.shortcuts.put((Shortcut)entry.getKey().clone(), 
    				(Shortcut)entry.getValue().clone());
    	}
    }
    
    /**
     * Links the registered executors for the specified 
     * {@link Signature} of the {@link ActMethod} passed 
     * as parameters, to this LinkedActMethod
     * 
     * @param linked
     * 		the {@link ActMethod} to link with
     * @param Signature
     * 		the {@Signature} of the {@link ActMethod} 
     * 		to link with
     */
    public final Signature linkedSignature(Signature signature)
    {
    	Signature thatSignature = null;
    	
    	if(linked==null || signature==null || signature.getName().intern()
    		!= super.getName().intern() || 
    		(thatSignature = linked.getSignature(
    		signature.getParameterTypes()))==null)
		{
			return null;
		}		
		Deque<AccessMethodExecutor> executors =
			linked.map.get(thatSignature);

		LinkedList<Signature> chain = new LinkedList<Signature>();
		Signature shortcut = thatSignature;
		Signature reference = null;	
			 
		if(executors == null)
		{				
			while((reference = linked.shortcuts.get(shortcut))!= null)
			{
				chain.add(shortcut);
				shortcut = reference;
			}
			if((executors = linked.map.get(shortcut)) == null)
			{						
				return null;		
			}
		}
		
		shortcut = (Signature) shortcut.clone();		
		super.map.put(shortcut, new LinkedList<AccessMethodExecutor>(executors));

		while(!chain.isEmpty())
		{
			reference = shortcut;
			shortcut = (Signature) chain.removeLast().clone();
			super.shortcuts.put((Shortcut) shortcut, reference);
		}
		return shortcut;
    }
    
    /**
     * Links the registered executors for the specified 
     * {@link Signature} of the {@link ActMethod} passed 
     * as parameters, to this LinkedActMethod
     * 
     * @param linked
     * 		the {@link ActMethod} to link with
     * @param Signature
     * 		the {@Signature} of the {@link ActMethod} 
     * 		to link with
     */
    public final void createShortcut(
    		Signature signature, Shortcut shortcut)
    {
    	Signature thatSignature = null;
    	
    	if(linked==null || signature==null || signature.getName().intern()
    		!= super.getName().intern() || 
    		(thatSignature = linked.getSignature(
    		signature.getParameterTypes()))==null)
		{
			return;
		}		
		Deque<AccessMethodExecutor> executors =
			linked.map.get(thatSignature);
		
		Map<Integer, Parameter> fixedParameters =
			new HashMap<Integer, Parameter>();
			
		fixedParameters.putAll(shortcut.getFixedParameters());
		
		Signature thatShortcut = thatSignature;
		Signature reference = null;	
			 
		if(executors == null)
		{				
			while((reference = linked.shortcuts.get(thatShortcut))!= null)
			{
				fixedParameters.putAll(((Shortcut)thatShortcut
						).getFixedParameters());				
				thatShortcut = reference;
			}
			if((executors = linked.map.get(thatShortcut)) == null)
			{						
				return;		
			}
		}		
		List<Parameter> parameters = new ArrayList<Parameter>();
		Iterator<Parameter> iterator = shortcut.iterator();
		while(iterator.hasNext())
		{
			parameters.add(iterator.next());
		}
		super.map.put(new Shortcut(super.mediator, 
				signature.getName(), signature.getResponseType(), 
				parameters.toArray(new Parameter[parameters.size()]), 
				fixedParameters), new LinkedList<AccessMethodExecutor>(executors));
    }
}
