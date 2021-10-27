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

package org.eclipse.sensinact.gateway.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An AttributeBuilder allows to create an {@link Attribute} and to defines
 * requirements to satisfy to create it properly
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public final class AttributeBuilder implements Nameable, Cloneable {
	
	private static final Logger LOG = LoggerFactory.getLogger(AttributeBuilder.class);
	/**
	 * the property name words separator
	 */
	public static final String SEP = "_";

	/**
	 * the name prefix of a default property value
	 */
	public static final String PROPERTY_DEFAULT_PREFIX = "DEFAULT_";

	/**
	 * the name of the constant specifying the array of AttributeBuilders
	 */
	public static final String ATTRIBUTES_PROPERTY = "ATTRIBUTES";

	/**
	 * Attribute building requirement
	 */
	public static enum Requirement {
		HIDDEN, MODIFIABLE, VALUE, TYPE;
	}

	/**
	 * Returns the merged array of predefined {@link AttributeBuilder}s for all
	 * extended {@link Resource} interfaces that the {@link Class} instance passed
	 * as parameter implements
	 * 
	 * @param resourceType
	 *            the class for which retrieve all predefined
	 *            {@link AttributeBuilder}s
	 * @return the merged array of predefined {@link AttributeBuilder}s
	 */
	public static final AttributeBuilder[] getAttributeBuilders(LinkedList<Class<Resource>> list) {
		AttributeBuilder[] attributeBuilders = null;
		Iterator<Class<Resource>> iterator = list.descendingIterator();

		while (iterator.hasNext()) {
			Class<? extends Resource> resourceInterface = iterator.next();
			try {
				attributeBuilders = mergeAttributeBuilders((AttributeBuilder[]) resourceInterface
						.getDeclaredField(AttributeBuilder.ATTRIBUTES_PROPERTY).get(null), attributeBuilders);

			} catch (Exception e) {
				continue;
			}
		}
		return attributeBuilders;
	}

	/**
	 * Merges the current and inherited {@link AttributeBuilder}s arrays passed as
	 * parameters. {@link AttributeBuilder} coming from the inherited array will
	 * have the lower indexes
	 * 
	 * @param current
	 *            the first of the two arrays to merge
	 * @param inherited
	 *            the second of two arrays to merge
	 * @return the @link AttributeBuilder}s array resulting of the merge of the
	 *         current and inherited arrays
	 */
	private static final AttributeBuilder[] mergeAttributeBuilders(AttributeBuilder[] current,
			AttributeBuilder[] inherited) {
		AttributeBuilder[] merged = null;
		if (current == null || current.length == 0) {
			if (inherited != null && inherited.length > 0) {
				merged = clone(inherited, inherited.length);
			}
		} else if (inherited == null || inherited.length == 0) {
			if (current != null && current.length > 0) {
				merged = clone(current, current.length);
			}
		} else {
			int delta = inherited.length;
			merged = clone(inherited, current.length + delta);
			int index = 0;
			for (; index < current.length; index++) {
				merged[index + delta] = (AttributeBuilder) current[index].clone();
			}
		}
		return merged;
	}

	/**
	 * Returns a copy of the AttributeBuilders array passed as parameters.
	 * 
	 * @param builders
	 *            the AttributeBuilders array to copy
	 * @param length
	 *            the length of the returned copy. If the length is inferior than
	 *            the one of the array to clone, this last one is truncated. If the
	 *            length is greater than the one of the array to clone, the returned
	 *            array is bigger and fill with null
	 * @return a copy of the AttributeBuilders array passed as parameters
	 */
	protected static final AttributeBuilder[] clone(AttributeBuilder[] builders, int length) {
		if (builders == null || length < 0) {
			return null;
		}
		int index = 0;
		AttributeBuilder[] clone = new AttributeBuilder[length];
		for (; index < builders.length && index < length; index++) {
			clone[index] = (AttributeBuilder) builders[index].clone();
		}
		return clone;
	}

	/**
	 * the type of the {@link Attribute} to build
	 */
	protected Class<?> type;
	/**
	 * the initial value of the {@link Attribute} to build
	 */
	protected Object value;
	/**
	 * the modifiable policy of the {@link Attribute} to build
	 */
	protected Modifiable modifiable;
	/**
	 * the name of the {@link Attribute} to build
	 */
	protected boolean hidden;
	/**
	 * the requirements as a list
	 */
	protected final Requirement[] requirementsArray;

	/**
	 * the requirements as a list
	 */
	protected Deque<Requirement> requirementsList;

	/**
	 * the constraints as a list
	 */
	private Deque<Constraint> constraintsList;

	/**
	 * the metadata builders as a list
	 */
	private List<MetadataBuilder> metadataBuilders;

	/**
	 * the name of the {@link Attribute} to build
	 */
	public final String name;

	/**
	 * Constructor
	 * 
	 * @param requirementsArray
	 *            the array of required field which apply on this AttributeBuilder
	 *            to create the appropriate {@link Attribute}
	 * 
	 * @throws InvalidAttributeException
	 */
	public AttributeBuilder(String name, Requirement[] requirementsArray) throws InvalidAttributeException {
		if (name == null) {
			throw new InvalidAttributeException("attribute name required");
		}
		this.requirementsArray = new Requirement[requirementsArray == null ? 0 : requirementsArray.length];

		if (this.requirementsArray.length > 0) {
			System.arraycopy(requirementsArray, 0, this.requirementsArray, 0, this.requirementsArray.length);
		}
		this.requirementsList = new LinkedList<Requirement>(Arrays.asList(this.requirementsArray));

		this.constraintsList = new LinkedList<Constraint>();
		this.metadataBuilders = new ArrayList<MetadataBuilder>();
		this.name = name;
	}

	/**
	 * Constructor
	 * 
	 * @param requirements
	 *            the array of required field which apply on this AttributeBuilder
	 *            to create the appropriate {@link Attribute}
	 * 
	 * @throws InvalidAttributeException
	 */
	private AttributeBuilder(String name, Requirement[] requirementsArray, Deque<Requirement> requirementsList,
		boolean hidden, Modifiable modifiable, Class<?> type, Object value) throws InvalidAttributeException {
		
		if (name == null) 
			throw new InvalidAttributeException("attribute name required");
		this.requirementsArray = new Requirement[requirementsArray == null ? 0 : requirementsArray.length];

		if (this.requirementsArray.length > 0) 
			System.arraycopy(requirementsArray, 0, this.requirementsArray, 0, this.requirementsArray.length);
		
		this.requirementsList = new LinkedList<Requirement>(requirementsList);
		this.constraintsList = new LinkedList<Constraint>();
		this.metadataBuilders = new ArrayList<MetadataBuilder>();
		this.name = name;
		this.hidden = hidden;
		this.modifiable = modifiable;
		this.type = type;
		this.value = value;
	}

	public void addMetadataBuilder(MetadataBuilder metadataBuilder) {
		this.metadataBuilders.add(metadataBuilder);
	}

	public void addMetadataBuilders(List<MetadataBuilder> metadataBuilder) {
		this.metadataBuilders.addAll(metadataBuilder);
	}

	public Object clone() {
		AttributeBuilder clone = new AttributeBuilder(this.name, this.requirementsArray, this.requirementsList,
				this.hidden, this.modifiable, this.type, this.value);

		Iterator<Constraint> iterator = this.constraintsList.iterator();

		while (iterator.hasNext()) {
			clone.addConstraint(iterator.next());
		}
		return clone;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see Nameable#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Defines the type of the {@link Attribute} to build
	 * 
	 * @param typeName
	 *            the type of the {@link Attribute} to build
	 * @return this AttributeBuilder
	 */
	public AttributeBuilder type(Class attributeType) {
		if (attributeType != null && this.updateRequirements(Requirement.TYPE)) {
			this.type = attributeType;
		}
		return this;
	}

	/**
	 * Defines the initial value of the {@link Attribute} to build
	 * 
	 * @param value
	 *            the initial value of the {@link Attribute} to build
	 * @return this AttributeBuilder
	 */
	public AttributeBuilder value(Object value) {
		this.updateRequirements(Requirement.VALUE);
		this.value = value;
		return this;
	}

	/**
	 * Defines whether the value of the JSON description of the {@link Attribute} to
	 * build is hidden or not
	 * 
	 * @param hidden
	 *            <ul>
	 *            <li>true if the JSON description of the {@link Attribute} to build
	 *            is hidden</li>
	 *            <li>false otherwise</li>
	 *            </ul>
	 * @return this AttributeBuilder
	 */
	public AttributeBuilder hidden(boolean hidden) {
		if (this.updateRequirements(Requirement.HIDDEN)) {
			this.hidden = hidden;
		}
		return this;
	}

	/**
	 * Defines whether the value of the {@link Attribute} to create can be modified
	 * or not
	 * 
	 * @param modifiable
	 *            the {@link Modifiable} policy applying on the value of the
	 *            {@link Attribute}
	 * @return this AttributeBuilder
	 */
	public AttributeBuilder modifiable(Modifiable modifiable) {
		if (this.updateRequirements(Requirement.MODIFIABLE)) {
			this.modifiable = modifiable;
		}
		return this;
	}

	/**
	 * Creates and returns an {@link Attribute} for the {@link ResourceImpl} passed
	 * as parameter, and associated to the specified {@link Mediator} to interact
	 * with the host OSGi environment
	 * 
	 * @param mediator
	 *            the associated {@link Mediator}
	 * @param resource
	 *            the {@link ResourceImpl} to which the Attribute to create will be
	 *            attached
	 * @param typeConfig
	 *            the TypeConfig gathering configuration informations
	 * @return the new created {@link Attribute}
	 * 
	 * @throws InvalidAttributeException
	 *             if an error occurred while creating the Attribute
	 */
	public Attribute getAttribute(Mediator mediator, ResourceImpl resource, TypeConfig resourceTypeConfig)
			throws InvalidAttributeException {
		if (resource == null) 
			return null;
		
		String constantPrefix = new StringBuilder().append(this.name.toUpperCase()).append(SEP).toString();

		// retrieve fixed values
		int index = 0;
		int length = this.requirementsArray.length;

		for (; index < length; index++) {
			Requirement requirement = this.requirementsArray[index];
			String constantName = new StringBuilder().append(constantPrefix).append(requirement.name()).toString();
			this.setRequirementValue(mediator, requirement, resourceTypeConfig, constantName, true);
		}
		while (this.requirementsList.size() > 0) {
			Requirement requirement = this.requirementsList.removeFirst();
			String constantName = new StringBuilder().append(PROPERTY_DEFAULT_PREFIX).append(constantPrefix
				).append(requirement.name()).toString();
			this.setRequirementValue(mediator, requirement, resourceTypeConfig, constantName, false);
		}
		if (this.isComplete()) {
			try {
				Attribute attribute = new Attribute(mediator, resource, this.name, this.type, this.value,
						this.modifiable, this.hidden);

				if (!this.constraintsList.isEmpty()) 
					attribute.addMetadata(new Metadata(mediator, Metadata.CONSTRAINTS, Constraint[].class,
							this.constraintsList.toArray(new Constraint[0]), Modifiable.FIXED));
				
				if (!this.metadataBuilders.isEmpty()) {
					Iterator<MetadataBuilder> iterator = this.metadataBuilders.iterator();
					while (iterator.hasNext()) 
						attribute.addMetadata(iterator.next().getMetadata());
				}
				return attribute;

			} catch (InvalidValueException e) {
				throw new InvalidAttributeException(e.getMessage(), e);
			}
		}
		throw new InvalidAttributeException(new StringBuilder().append(resource.getPath())
				.append(" Creating attribute '").append(this.name).append("' : missing requirements ")
				.append(Arrays.toString(this.requirementsList.toArray())).toString());
	}

	/**
	 * Retrieves the value of the constant whose name is passed as parameter and set
	 * it as value of the specified {@link Requirement}
	 * 
	 * @param requirement
	 * @param resourceTypeConfig
	 * @param constantName
	 * @param fromTop
	 * 
	 * @return
	 */
	private void setRequirementValue(Mediator mediator, Requirement requirement, TypeConfig resourceTypeConfig,
			String constantName, boolean fromTop) {
		Object constant = resourceTypeConfig.getConstantValue(constantName, fromTop);

		if (constant != null) {
			try {
				AttributeBuilder.class.getDeclaredField(requirement.name().toLowerCase()).set(this, constant);
				this.updateRequirements(requirement);

			} catch (Exception e) {
				if (LOG.isErrorEnabled()) {
					LOG.error(e.getMessage(),e);
				}
			}
		}
	}

	/**
	 * Returns true if all required fields have been set; returns false otherwise
	 * 
	 * @return
	 *         <ul>
	 *         <li>true if all required fields have been set</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	public boolean isComplete() {
		return (this.requirementsList.isEmpty() && this.name != null && this.type != null);
	}

	/**
	 * Update the requirements list by removing the one satisfied, passed as
	 * parameter
	 * 
	 * @param requirement
	 *            the Requirement that has been satisfied
	 */
	private boolean updateRequirements(Requirement requirement) {
		return this.requirementsList.remove(requirement);
	}

	/**
	 * Adds a {@link Constraint} that will apply on the {@link Attribute}s built
	 * using this AttributeBuilder
	 * 
	 * @param constraint
	 *            the {@link Constraint} to add
	 */
	public void addConstraint(Constraint constraint) {
		this.constraintsList.add(constraint);
	}

	/**
	 * Adds the {@link Constraint}s that will apply on the {@link Attribute}s built
	 * using this AttributeBuilder
	 * 
	 * @param constraints
	 *            the list of{@link Constraint}s to add
	 */
	public void addConstraints(List<Constraint> constraints) {
		this.constraintsList.addAll(constraints);
	}

}
