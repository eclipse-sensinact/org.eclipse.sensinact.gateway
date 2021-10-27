/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.message;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.props.TypedProperties;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SubscriptionFilter extends SnaFilter {
	/**
	 * Constructor
	 * 
	 * @param filter
	 * @param set
	 */
	public SubscriptionFilter(Mediator mediator, String filter, Set<Constraint> conditions) {
		super(mediator, filter);
		super.addHandledType(SnaMessage.Type.UPDATE);
		if (conditions != null) {
			Iterator<Constraint> iterator = conditions.iterator();
			while (iterator.hasNext()) {
				Constraint condition = iterator.next();
				super.addCondition(condition);
			}
		}
	}

	/**
	 * Constructor
	 * 
	 * @param filter
	 * @param set
	 */
	public SubscriptionFilter(Mediator mediator, String filter, JSONArray conditions) {
		super(mediator, filter, conditions);
	}

	/**
	 * @inheritDoc
	 *
	 * @see SnaFilter# matches(SnaMessage)
	 */
	@Override
	public	boolean matches(SnaMessage message) {
		if (!super.matches(message)) {
			return false;
		}
		if (this.conditions.isEmpty()) {
			return true;
		}
		JSONObject jsonObject = (JSONObject) ((TypedProperties<SnaUpdateMessage.Update>) message)
				.get(SnaConstants.NOTIFICATION_KEY);

		String type = jsonObject.getString(Resource.TYPE);
		Class<?> clazz = null;
		try {
			clazz = CastUtils.loadClass(mediator.getClassLoader(), type);
		} catch (ClassNotFoundException e) {
			return false;
		}
		Object object = CastUtils.cast(clazz, jsonObject.opt(DataResource.VALUE));

		Iterator<Constraint> iterator = this.conditions.iterator();

		while (iterator.hasNext()) {
			if (!iterator.next().complies(object)) {
				return false;
			}
		}
		return true;
	}
}
