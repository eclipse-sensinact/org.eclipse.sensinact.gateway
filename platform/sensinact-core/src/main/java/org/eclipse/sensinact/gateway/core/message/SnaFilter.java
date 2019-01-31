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
package org.eclipse.sensinact.gateway.core.message;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Changed;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.constraint.Expression;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.common.props.TypedProperties;
import org.eclipse.sensinact.gateway.core.message.SnaMessage.Type;

/**
 * Allows to check whether an {@link SnaMessage} has to be transmitted or
 * blocked, according to its type, the uri of the object sending it, or a set of
 * registered conditions
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SnaFilter implements JSONable {
	/**
	 * List of handled {@link SnaMessage.Type}s
	 */
	protected List<SnaMessage.Type> handledTypes;

	/**
	 * List of {@link Constraint}s applying on received events
	 */
	protected final List<Constraint> conditions;

	/**
	 * the valid String target
	 */
	protected final String sender;

	/**
	 * the {@link Pattern} of a valid target
	 */
	protected final Pattern pattern;

	/**
	 * Is the 'target' field a regular expression ?
	 */
	protected final boolean isPattern;

	/**
	 * Is this filter's 'matches' method returns the the logical complement of the
	 * target and conditions evaluation result ?
	 */
	protected final boolean isComplement;

	/**
	 * the changing contraint applying on the evaluated value(s)
	 */
	protected Changed changed;

	/**
	 * Mediator used to interact with the OSGi host environment
	 */
	protected final Mediator mediator;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param sender
	 */
	public SnaFilter(Mediator mediator, String sender) {
		this(mediator, sender, false, false);
	}

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param sender
	 * @param isPattern
	 * @param isComplement
	 */
	public SnaFilter(Mediator mediator, String sender, boolean isPattern, boolean isComplement) {
		this.mediator = mediator;
		this.sender = sender;
		this.conditions = new ArrayList<Constraint>();
		this.handledTypes = new ArrayList<SnaMessage.Type>();

		this.isPattern = isPattern;
		this.isComplement = isComplement;

		Pattern pattern = null;
		if (this.isPattern) {
			try {
				pattern = Pattern.compile(sender);

			} catch (PatternSyntaxException e) {
				pattern = null;
			}
		}
		this.pattern = pattern;
	}

	/**
	 * @param mediator
	 * @param sender
	 * @param constraints
	 */
	public SnaFilter(Mediator mediator, String sender, JSONArray constraints) {
		this(mediator, sender, false, false, constraints);
	}

	/**
	 * @param mediator
	 * @param sender
	 * @param isPattern
	 * @param isComplement
	 * @param constraints
	 */
	public SnaFilter(Mediator mediator, String sender, boolean isPattern, boolean isComplement, JSONArray constraints) {
		this(mediator, sender, isPattern, isComplement);
		int index = 0;
		int length = constraints == null ? 0 : constraints.length();
		for (; index < length; index++) {
			this.addCondition(constraints.getJSONObject(index));
		}
	}

	/**
	 * Returns the String sender this SnaFilter is targeting
	 * 
	 * @return this SnaFilter's targeted sender
	 */
	public String getSender() {
		return this.sender;
	}

	/**
	 * Adds a new {@link Constraint} to this filter, formated as a JSON object
	 * 
	 * @param jsonCondition
	 *            the JSON formated {@link Constraint} to add to this filter
	 */
	public void addCondition(JSONObject jsonCondition) {
		try {
			this.addCondition(ConstraintFactory.Loader.load(mediator.getClassLoader(), jsonCondition));
		} catch (ClassCastException e) {
			mediator.debug(e.getMessage());
		} catch (InvalidConstraintDefinitionException e) {
			mediator.debug(e.getMessage());
		}
	}

	/**
	 * Adds the {@link Constraint} passed as parameter to this filter
	 * 
	 * @param jsonCondition
	 *            the {@link Constraint} to add
	 */
	public void addCondition(Constraint condition) {
		if (condition == null) {
			return;
		}
		if (Changed.class.isAssignableFrom(condition.getClass())) {
			this.changed = (Changed) condition;

		} else if (Expression.class.isAssignableFrom(condition.getClass())) {
			ListIterator<Constraint> iterator = ((Expression) condition).listIterator();
			while (iterator.hasNext()) {
				Constraint constraint = iterator.next();
				if (Changed.class.isAssignableFrom(condition.getClass())) {
					this.changed = (Changed) condition;
					iterator.remove();
				}
			}
			if (!this.conditions.isEmpty()) {
				this.conditions.add(condition);
			}
		} else {
			this.conditions.add(condition);
		}
	}

	/**
	 * Adds the {@link SnaMessage.Type} passed as parameter to this filter
	 * 
	 * @param type
	 *            the {@link SnaMessage.Type} to add
	 */
	public void addHandledType(SnaMessage.Type type) {
		if (type == null) {
			return;
		}
		this.handledTypes.add(type);
	}

	/**
	 * Adds the {@link SnaMessage.Type}s from the array passed as parameter to this
	 * filter
	 * 
	 * @param types
	 *            the array of {@link SnaMessage.Type}s to add
	 */
	public void addHandledType(Type[] types) {
		int index = 0;
		int length = types == null ? 0 : types.length;

		for (; index < length; index++) {
			this.addHandledType(types[index]);
		}
	}

	/**
	 * Returns true if the {@link SnaMessage} matches this filter, meaning :
	 * 
	 * <ul>
	 * <li>its type is handled by this SnaFilter;</li>
	 * <li>its uri property (the sender) is equals to (or matches) this SnaFilter's
	 * one;</li>
	 * <li>and it mets the set of {@link Constraint}s held by this SnaFilter</li>
	 * </ul>
	 * If the complement field of this SnaFilter is set to true when the handled
	 * type is found, the logical complement is returned for the sender and
	 * conditions evaluations
	 * 
	 * @param message
	 *            the {@link SnaMessage} to check whether it matches this
	 *            SnaFilter's or not
	 * @return
	 *         <ul>
	 *         <li>true if the {@link SnaMessage} matches this SnaFilter</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	public boolean matches(SnaMessage<?> message) {
		boolean matchType = this.matchesType(message);
		if (!matchType) {
			return false;
		}
		boolean matchSender = this.matchesSender(message);
		if (!matchSender) {
			return matchSender ^ this.isComplement;
		}
		boolean matchCondition = this.matchesConditions(message);
		if (!matchCondition) {
			return matchCondition ^ this.isComplement;
		}
		return true ^ this.isComplement;
	}

	/**
	 * Returns true if the {@link SnaMessage}'s uri field value matches this
	 * SnaFilter's target
	 * 
	 * @param SnaMessage
	 *            the {@link SnaMessage} to check whether its specified uri value
	 *            matches this SnaFilter's target
	 * @return
	 *         <ul>
	 *         <li>true if the {@link SnaMessage}'s uri field value matches this
	 *         SnaFilter's target</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	boolean matchesSender(SnaMessage message) {
		boolean matchSender = false;
		String uri = message.getPath();
		if (!this.isPattern) {
			matchSender = this.sender.equals(uri);
		} else {
			matchSender = pattern.matcher(uri).matches();
		}
		if(!matchSender) {
			String namespace = (String) ((AbstractSnaMessage<?>)message
			).get("namespace");
			if(namespace!=null) {
				String[] uriElements = UriUtils.getUriElements(uri);
				uriElements[0] = new StringBuilder().append(namespace).append(
						":").append(uriElements[0]).toString();
				String resolvedUri=UriUtils.getUri(uriElements);
				if (!this.isPattern) {
					matchSender = this.sender.equals(resolvedUri);
				} else {
					matchSender = pattern.matcher(resolvedUri).matches();
				}
			}
		}
		return matchSender;
	}

	/**
	 * Returns true if the {@link SnaMessage}'s "value" mets the set of
	 * {@link Constraint}s held by this SnaFilter
	 * 
	 * @param SnaMessage
	 *            the {@link SnaMessage} to check whether its "value" complies this
	 *            SnaFilter's condition(s)
	 * @return
	 *         <ul>
	 *         <li>true if the {@link SnaMessage}'s "value" complies this
	 *         SnaFilter's condition(s)</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	boolean matchesType(SnaMessage message) {
		return handledTypes.contains(((SnaMessageSubType) message.getType()).getSnaMessageType());
	}

	/**
	 * Returns true if the {@link SnaMessage}'s type is handled by this SnaFilter
	 * 
	 * @param SnaMessage
	 *            the {@link SnaMessage} to check whether its type is handled by
	 *            this SnaFilter
	 * @return
	 *         <ul>
	 *         <li>true if the {@link SnaMessage}'s type is handled by this
	 *         SnaFilter</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	boolean matchesConditions(SnaMessage message) {
		Object toValidate = null;

		switch (((SnaMessageSubType) message.getType()).getSnaMessageType()) {
		case ERROR:
			break;
		case LIFECYCLE:
			switch ((SnaLifecycleMessage.Lifecycle) message.getType()) {
			case RESOURCE_APPEARING:
				JSONObject initial = new JSONObject(message.getJSON()).optJSONObject("initial");
				if (!JSONObject.NULL.equals(initial)) {
					toValidate = initial.opt("value");
				}
				break;
			case PROVIDER_APPEARING:
			case PROVIDER_DISAPPEARING:
			case RESOURCE_DISAPPEARING:
			case SERVICE_APPEARING:
			case SERVICE_DISAPPEARING:
			default:
				break;
			}
			break;
		case RESPONSE:
			JSONObject response = new JSONObject(message.getJSON()).optJSONObject("response");
			if (!JSONObject.NULL.equals(response)) {
				toValidate = response.opt("value");
			}
			break;
		case REMOTE:
			JSONObject remote = new JSONObject(message.getJSON()).optJSONObject("notification");
			if (!JSONObject.NULL.equals(remote)) {
				toValidate = remote.opt("namespace");
			}
			break;
		case UPDATE:
			switch ((SnaUpdateMessage.Update) message.getType()) {
				case ACTUATED:
					return false;
				case ATTRIBUTE_VALUE_UPDATED:
				case METADATA_VALUE_UPDATED:
					if (this.handleChangedStatus(((TypedProperties<?>) message).<Boolean>get("hasChanged"))) {
						JSONObject notification = new JSONObject(message.getJSON()).optJSONObject("notification");
						if (!JSONObject.NULL.equals(notification)) {
							toValidate = notification.opt("value");
						}
					} else {
						return false;
					}
					break;
				default:
					break;
			}
			break;
		default:
			break;
		}
		if (toValidate == null) {
			return true;
		}
		int index = 0;
		for (; index < this.conditions.size(); index++) {
			if (!this.conditions.get(index).complies(toValidate)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @return
	 *         <ul>
	 *         <li>true if this filter accepts unchanged value notifications</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	private boolean handleChangedStatus(Boolean status) {
		if (this.changed == null) {
			this.changed = new Changed(mediator.getClassLoader());
		}
		boolean handle = this.changed.complies(status);
		return handle;
	}

	/**
	 * @inheritDoc
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {
		if (!SnaFilter.class.isAssignableFrom(object.getClass())) {
			return false;
		}
		SnaFilter snaFilter = (SnaFilter) object;
		if (!snaFilter.sender.equals(this.sender)) {
			return false;
		}
		if (snaFilter.handledTypes.size() != this.handledTypes.size()
				|| snaFilter.conditions.size() != this.conditions.size()) {
			return false;
		}
		Iterator<SnaMessage.Type> typeIterator = this.handledTypes.iterator();
		while (typeIterator.hasNext()) {
			if (!snaFilter.handledTypes.contains(typeIterator.next())) {
				return false;
			}
		}
		Iterator<Constraint> constraintIterator = this.conditions.iterator();
		while (constraintIterator.hasNext()) {
			if (!snaFilter.conditions.contains(constraintIterator.next())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return
	 */
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		JSONArray array = new JSONArray();

		Iterator<Type> iterator = this.handledTypes.iterator();
		while (iterator.hasNext()) {
			array.put(iterator.next().name());
		}
		object.put("types", array);
		object.put("sender", this.sender);
		object.put("pattern", this.isPattern);
		object.put("complement", this.isComplement);

		array = new JSONArray();
		Iterator<Constraint> constraints = this.conditions.iterator();
		while (constraints.hasNext()) {
			array.put(new JSONObject(constraints.next().getJSON()));
		}
		object.put("conditions", array);
		return object;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.primitive.JSONable#getJSON()
	 */
	@Override
	public String getJSON() {
		return toJSONObject().toString();
	}
}
