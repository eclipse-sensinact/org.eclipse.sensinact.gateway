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
package org.eclipse.sensinact.gateway.core.method.trigger;

import org.eclipse.sensinact.gateway.api.core.AttributeDescription;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;
import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 * AccessMethodTrigger's argument builder
 */
public abstract class TriggerArgumentBuilder<P, I> {
	
	public static final String EMPTY = "EMPTY";
	public static final String PARAMETER = "PARAMETER";
	public static final String INTERMEDIATE = "INTERMEDIATE";
	public static final String RESPONSE = "RESPONSE";
	public static final String MODEL = "MODEL";
		
	public static final class Empty extends TriggerArgumentBuilder<Void, Void> {

		public Empty() {
			super(null);			
		}

		@Override
		public Object build(Void v) {
			return null;
		}
	}
	
	public static final class Parameter extends  TriggerArgumentBuilder<Integer, AccessMethodResponseBuilder<?,?>> {

		public Parameter(int index) {
			super(index);			
		}

		@Override
		public Object build(AccessMethodResponseBuilder<?,?> accessMethodResponseBuilder) {
			return accessMethodResponseBuilder.getParameter(super.arg.intValue());
		}
	}

	public static final class Intermediate extends  TriggerArgumentBuilder<Void,AccessMethodResponseBuilder<?,?>> {

		public Intermediate() {
			super(null);			
		}

		@Override
		public AccessMethodResponseBuilder<?,?> build(AccessMethodResponseBuilder<?,?> accessMethodResponseBuilder) {
			return accessMethodResponseBuilder;
		}
	}

	public static final class Response extends  TriggerArgumentBuilder<Void,AccessMethodResponseBuilder<?,?>> {

		public Response() {
			super(null);			
		}

		@Override
		public AccessMethodResponse<?> build(AccessMethodResponseBuilder<?,?> accessMethodResponseBuilder) {
			return accessMethodResponseBuilder.createAccessMethodResponse();
		}
	}

	public static final class Model extends  TriggerArgumentBuilder<String,ServiceImpl> {

		public Model(String path) {
			super(path);			
		}

		@Override
		public AttributeDescription build(ServiceImpl service) {
			String[] elements=  UriUtils.getUriElements(super.arg);
			int length = elements.length;
			if(length < 1 ||length > 2) {
				return null;
			}
			if(service == null) {
				return null;
			}
			ResourceImpl r = service.getResource(elements[0]);
			if(r == null) {
				return null;
			}
			AttributeDescription d = null;			
			String attributeName = length==1?r.getDefault():elements[1];
			if(attributeName != null) {
				d = r.getDescription(attributeName);
			}
			return d;
		}
	}
	
	public abstract Object build(I i);
	
	private final P arg;
	
	protected TriggerArgumentBuilder(P arg) {
		this.arg = arg;
	}	
}