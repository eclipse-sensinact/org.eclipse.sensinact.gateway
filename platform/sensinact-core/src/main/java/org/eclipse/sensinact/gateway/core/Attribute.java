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
package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.primitive.DescribablePrimitive;
import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.common.primitive.Name;
import org.eclipse.sensinact.gateway.common.primitive.Primitive;
import org.eclipse.sensinact.gateway.common.primitive.PrimitiveDescription;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Extended {@link Primitive} defining an Attribute of a {@link Resource}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Attribute extends DescribablePrimitive {
    public static final String NICKNAME = "nickname";

    /**
     * This Attribute's metadata.
     */
    protected final List<Metadata> metadata;
    private final ResourceImpl resource;
    private int recipient;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the
     *                 Attribute to be instantiated to interact with the OSGi host
     *                 environment
     * @param resource the ResourceImpl holding the Attribute
     *                 to be instantiated
     * @param name     the name of the Attribute to instantiate
     * @param type     the type of the Attribute to instantiate
     * @throws InvalidValueException
     */
    public Attribute(Mediator mediator, ResourceImpl resource, String name, Class<?> type) throws InvalidValueException {
        this(mediator, resource, name, type, null, Modifiable.UPDATABLE, false);
        this.recipient = 0;
    }

    /**
     * Constructor
     *
     * @param mediator   the {@link Mediator} allowing the
     *                   Attribute to be instantiated to interact with the OSGi host
     *                   environment
     * @param resource   the ResourceImpl holding the Attribute
     *                   to be instantiated
     * @param name       the name of the Attribute to instantiate
     * @param type       the type of the Attribute to instantiate
     * @param value      the initial value of the Attribute to instantiate
     * @param modifiable is the value of Attribute to instantiate
     *                   modifiable or not ?
     * @param hidden     does the Attribute to instantiate provide its own
     *                   JSON formated description or not
     * @throws InvalidValueException
     */
    public Attribute(Mediator mediator, ResourceImpl resource, String name, Class<?> type, Object value, Modifiable modifiable, boolean hidden) throws InvalidValueException {
        super(mediator, name, type);
        this.resource = resource;
        this.metadata = new ArrayList<Metadata>();

        if (value != null) {
            super.setValue(value);
        }
        Metadata modifiableMeta = new Metadata(super.mediator, Metadata.MODIFIABLE, Modifiable.class, modifiable, Modifiable.FIXED);
        this.addMetadata(modifiableMeta);
        Metadata hiddenMeta = new Metadata(super.mediator, Metadata.HIDDEN, boolean.class, hidden, Modifiable.FIXED);
        this.addMetadata(hiddenMeta);
        Metadata timestampMeta = new Metadata(super.mediator, Metadata.TIMESTAMP, long.class, System.currentTimeMillis(), Modifiable.UPDATABLE);
        this.addMetadata(timestampMeta);
        Metadata lockedMeta = new Metadata(super.mediator, Metadata.LOCKED, boolean.class, false, Modifiable.UPDATABLE);
        this.addMetadata(lockedMeta);
    }

    /**
     * Constructs an attribute from the given {@link JSONObject}
     * for the ResourceImpl passed as parameter
     *
     * @param mediator  the {@link Mediator} allowing the
     *                  Attribute to be instantiated to interact with the OSGi host
     *                  environment
     * @param attribute the {@link JSONObject} describing the
     *                  attribute to instantiate
     */
    protected Attribute(Mediator mediator, ResourceImpl resource, JSONObject attribute) throws InvalidValueException {
        super(mediator, attribute == null ? null : attribute.optString(PrimitiveDescription.NAME_KEY), attribute == null ? null : attribute.optString(PrimitiveDescription.TYPE_KEY));

        this.resource = resource;
        this.metadata = new ArrayList<Metadata>();

        // set the value if defined in the JSONObject
        Object ovalue = attribute.opt(PrimitiveDescription.VALUE_KEY);

        if (ovalue != null) {
            this.setValue(CastUtils.getObjectFromJSON(super.mediator.getClassLoader(), this.getType(), ovalue));
        }
        JSONArray metadataArray = attribute.optJSONArray("metadata");

        // adds Metadata specified in the JSON object if not null
        if (metadataArray != null) {
            int index = 0;
            for (; index < metadataArray.length(); index++) {
                Metadata metadata = new Metadata(super.mediator, metadataArray.getJSONObject(index));
                this.addMetadata(metadata);
            }
        }
        // modifiable Metadata added only if it does not already exist
        // it is not overridden if defined as modifiable
        if (!this.metadata.contains(new Name<Metadata>(Metadata.MODIFIABLE))) {
            Metadata modifiableMeta = new Metadata(super.mediator, Metadata.MODIFIABLE, Modifiable.class, Modifiable.MODIFIABLE, Modifiable.FIXED);
            this.addMetadata(modifiableMeta);
        }
        // hidden Metadata added only if it does not already exist
        // it is not overridden if defined as modifiable
        if (!this.metadata.contains(new Name<Metadata>(Metadata.HIDDEN))) {
            Metadata hiddenMeta = new Metadata(super.mediator, Metadata.HIDDEN, boolean.class, false, Modifiable.FIXED);
            this.addMetadata(hiddenMeta);
        }
        if (!this.metadata.contains(new Name<Metadata>(Metadata.TIMESTAMP))) {
            Metadata timestampMeta = new Metadata(super.mediator, Metadata.TIMESTAMP, long.class, System.currentTimeMillis(), Modifiable.UPDATABLE);
            this.addMetadata(timestampMeta);
        }
        if (!this.metadata.contains(new Name<Metadata>(Metadata.LOCKED))) {
            Metadata lockedMeta = new Metadata(super.mediator, Metadata.LOCKED, boolean.class, false, Modifiable.UPDATABLE);
            this.addMetadata(lockedMeta);
        }
    }

    /**
     * Defines the value of the {@link Metadata} whose whose
     * name is passed as parameter
     *
     * @param name  the name of the {@link Metadata} to set the value of
     * @param value the value to set
     * @return the extended {@link Description} describing the
     * modified Metadata {@link Metadata}
     * @throws InvalidValueException
     */
    public MetadataDescription setMetadataValue(String name, Object value) throws InvalidValueException {
        Metadata metadata = null;
        if (name == null || (metadata = this.get(name)) == null) {
            return null;
        }
        metadata.setValue(value);
        return (MetadataDescription) metadata.getDescription();
    }

    /**
     * Adds the {@link Metadata} passed as parameter to the list of
     * ones owned by this Attribute if it does not already exist or
     * replaces the existing one with the same name if it is defined
     * as modifiable
     *
     * @param metadata the {@link Metadata} to add or update
     */
    public void addMetadata(Metadata metadata) {
        Metadata meta = this.get(metadata.getName());
        synchronized (this.metadata) {
            if (meta == null) {
                this.metadata.add(metadata);
                super.weakDescription = null;

            } else {
                try {
                    this.setMetadataValue(metadata.getName(), metadata.getValue());

                } catch (InvalidValueException e) {
                    if (super.mediator.isErrorLoggable()) {
                        super.mediator.error(e, e.getMessage());
                    }
                }
                return;
            }
        }
    }

    /**
     * Removes the {@link Metadata} whose name is passed as
     * parameter from the list of ones owned by this Attribute if
     * it exists
     *
     * @param metadata the name of the {@link Metadata} to remove
     * @return <ul>
     * <li>true if the {@link Metadata} has been
     * removed properly</li>
     * <li>false if an error occurred or if the
     * Metadata does not exist</li>
     * </ul>
     */
    public boolean removeMetadata(String metadata) {
        if (metadata == null || Metadata.HIDDEN.intern() == metadata.intern() || Metadata.MODIFIABLE.intern() == metadata.intern() || Metadata.TIMESTAMP.intern() == metadata.intern() || Metadata.LOCKED.intern() == metadata.intern()) {
            return false;
        }
        Metadata existingMeta = this.remove(metadata);
        return (existingMeta != null);
    }

    /**
     * Sets this Primitive's value and defines the
     * timestamp of the value change
     *
     * @param value     the value to set
     * @param timestamp the timestamp of the value change
     * @throws InvalidValueException if the value cannot be set
     */
    @Override
    public Object setValue(Object value) throws InvalidValueException {
        return this.setValue(value, System.currentTimeMillis());
    }

    /**
     * Sets this Primitive's value and defines the
     * timestamp of the value change
     *
     * @param value     the value to set
     * @param timestamp the timestamp of the value change
     * @throws InvalidValueException if the value cannot be set
     */
    public Object setValue(Object value, long timestamp) throws InvalidValueException {
        if (Modifiable.FIXED.equals(this.getModifiable())) {
            throw new InvalidValueException("the value cannot be modified");
        }
        Object valueObject = cast(value);
        Object copy = CastUtils.copy(super.type, valueObject);

        synchronized (lock) {
            boolean hasChanged = ((valueObject == null && super.value != null) || (valueObject != null && !valueObject.equals(super.value)));

            this.beforeChange(copy);
            this.setMetadataValue(Metadata.TIMESTAMP, timestamp);
            super.value = valueObject;
            this.afterChange(copy, hasChanged);
        }
        return copy;
    }

    /**
     * @inheritDoc
     * @see Primitive#beforeChange(java.lang.Object)
     */
    @Override
    protected void beforeChange(Object value) throws InvalidValueException {
        Metadata metadata = this.get(Metadata.CONSTRAINTS);
        if (metadata == null) {
            return;
        }
        Constraint[] constraints = null;
        int length = 0;
        try {
            constraints = (Constraint[]) metadata.getValue();

        } finally {
            if (constraints == null || (length = constraints.length) == 0) {
                return;
            }
        }
        int index = 0;
        for (; index < length; index++) {
            if (!constraints[index].complies(value)) {
                StringBuilder buffer = new StringBuilder();
                buffer.append("value does not comply the constraint :");
                buffer.append(constraints[index].getJSON());
                String message = buffer.toString();
                throw new InvalidValueException(message);
            }
        }
    }

    /**
     * @throws InvalidValueException
     * @inheritDoc
     * @see DescribablePrimitive#
     * afterChange(java.lang.Object)
     */
    private void afterChange(Object value, boolean hasChanged) throws InvalidValueException {
        super.afterChange(value);
        this.resource.updated(this, value, hasChanged);
    }

    /**
     * Defines the {@link ResourceImpl} to notify
     * when the value of this Attribute has changed
     *
     * @param recipient the change notifications recipient
     */
    protected void addRecipient() {
        this.recipient++;
    }

    /**
     * Defines the change notifications recipient
     * as null
     */
    protected void deleteRecipient() {
        this.recipient--;
    }

    /**
     * @inheritDoc
     * @see Primitive #
     * createDescription(java.lang.String,java.lang.Class)
     */
    @Override
    protected AttributeDescription createDescription() {
        AttributeDescription description = new AttributeDescription(this, this.getAllDescriptions());
        return description;
    }

    /**
     * @inheritDoc
     * @see Primitive #isModifiable()
     */
    @Override
    public Modifiable getModifiable() {
        Modifiable modifiable = Modifiable.UPDATABLE;
        Metadata metadata = null;

        if ((metadata = this.get(Metadata.MODIFIABLE)) == null) {
            return modifiable;
        }
        try {
            modifiable = (Modifiable) metadata.getValue();
        } catch (ClassCastException e) {
            if (this.mediator.isErrorLoggable()) {
                this.mediator.error(e, e.getMessage());
            }
        }
        return modifiable;
    }

    /**
     * Returns true if this Attribute's value is locked;
     * returns false otherwise
     *
     * @return <ul>
     * <li>true is this Attribute's value is locked</li>
     * <li>false otherwise</li>
     * </ul>
     */
    public boolean getLocked() {
        boolean locked = false;
        try {
            locked = (Boolean) this.get(Metadata.LOCKED).getValue();

        } catch (Exception e) {
            this.mediator.error(e);
        }
        return locked;
    }

    /**
     * Defines this Attribute's value as locked
     */
    protected void lock() {
        try {
            this.get(Metadata.LOCKED).setValue(true);
        } catch (Exception e) {
            this.mediator.error(e);
        }
    }

    /**
     * Defines this Attribute value as unlocked
     */
    protected void unlock() {
        try {
            this.get(Metadata.LOCKED).setValue(false);
        } catch (Exception e) {
            this.mediator.error(e);
        }
    }

    /**
     * Returns true if this Attribute JSON description is hidden;
     * returns false otherwise
     *
     * @return <ul>
     * <li>true if this Attribute JSON description is
     * hidden;</li>
     * <li>false otherwise</li>
     * </ul>
     */
    public boolean isHidden() {
        Metadata metadata = this.get(Metadata.HIDDEN);
        boolean hidden = false;
        if (metadata != null) {
            try {
                hidden = ((Boolean) metadata.getValue()).booleanValue();
            } catch (ClassCastException e) {
                this.mediator.error(e, e.getMessage());
            }
        }
        return hidden;
    }

    /**
     * Returns the {@link Metadata} of this
     * Attribute, whose name is the same as
     * the specified one
     *
     * @param name the name of the searched {@link Metadata}
     * @return the  {@link Metadata} with the specified
     * name
     */
    protected Metadata get(String name) {
        int index = -1;
        Metadata metadata = null;
        synchronized (this.metadata) {
            if ((index = this.metadata.indexOf(new Name<Attribute>(name))) != -1) {
                metadata = this.metadata.get(index);
            }
        }
        return metadata;
    }

    /**
     * Removes the {@link Metadata} whose name is passed
     * as parameter
     *
     * @param primitive the name of the {@link Metadata} to remove
     * @return the removed {@link Metadata}
     */
    private Metadata remove(String name) {
        Metadata metadata = this.get(name);
        if (metadata == null) {
            synchronized (this.metadata) {
                this.metadata.remove(metadata);
                super.weakDescription = null;
            }
        }
        return metadata;
    }

    /**
     * Returns the array of {@link MetadataDescription}s
     * of all {@link Metadata} of this Attribute
     *
     * @return the {@link MetadataDescription}s array of
     * this Attribute
     */
    public MetadataDescription[] getAllDescriptions() {
        int index = 0;
        MetadataDescription[] descriptions = new MetadataDescription[this.metadata.size()];
        synchronized (this.metadata) {
            Iterator<Metadata> iterator = this.metadata.iterator();
            while (iterator.hasNext()) {
                descriptions[index++] = iterator.next().getDescription();
            }
        }
        return descriptions;
    }
}
