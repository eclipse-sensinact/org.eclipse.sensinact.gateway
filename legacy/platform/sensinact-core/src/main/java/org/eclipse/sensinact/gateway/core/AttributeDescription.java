/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.common.primitive.PrimitiveDescription;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

/**
 * {@link Description} of an {@link Attribute}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AttributeDescription extends PrimitiveDescription {
	private static final String EMPTY = "";
	private static final String METADATA_KEY = "metadata";

	private final boolean hidden;
	private final MetadataDescription[] metadataDescriptions;

	/**
	 * Constructor
	 * 
	 * @param attribute
	 *            the {@link Attribute} to describe
	 * @param metadataDescriptions
	 *            the array of {@link Description}s of the {@link Metadata} which
	 *            belong to the {@link Attribute} to describe
	 */
	public AttributeDescription(Attribute attribute, MetadataDescription[] descriptions) {
		super(attribute);
		this.hidden = attribute.isHidden();
		this.metadataDescriptions = descriptions;
	}

	/**
	 * Returns true if the described {@link Attribute} JSON description is hidden;
	 * returns false otherwise
	 * 
	 * @return
	 *         <ul>
	 *         <li>true if the described {@link Attribute} JSON description is
	 *         hidden;</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	public boolean isHidden() {
		return this.hidden;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see PrimitiveDescription#getJSON()
	 */
	@Override
	public String getJSON() {
		// if the described attribute is defined as hidden
		if (this.isHidden()) {
			return EMPTY;
		}
		// clones the current JSON description
		JsonObjectBuilder description = super.getJsonObject();
		// appends the JSON formated current value
		description.add(VALUE_KEY, this.getJsonValue());
		// appends JSON description of Metadata
		// associated to the described Attribute
		// if defined as dynamic
		int index = 0;
		int length = metadataDescriptions.length;

		for (; index < length; index++) {
			MetadataDescription metadataDescription = this.metadataDescriptions[index];

			if (!Metadata.LOCKED.equals(metadataDescription.getName())
					&& !Metadata.HIDDEN.equals(metadataDescription.getName())
					&& !Attribute.NICKNAME.equals(metadataDescription.getName())
					&& !Modifiable.FIXED.equals(metadataDescription.getModifiable())) {
				description.add(metadataDescription.getName(), getJsonValue(metadataDescription));
			}
			if (Attribute.NICKNAME.equals(metadataDescription.getName())) {
				description.add(NAME_KEY, CastUtils.cast(String.class, metadataDescription.getValue()));
			}
		}
		return description.build().toString();
	}

	/**
	 * @inheritDoc
	 * 
	 * @see PrimitiveDescription#getJSONDescription()
	 */
	@Override
	public String getJSONDescription() {
		JsonObject description = this.getJSONObjectDescription();
		if (description == null) {
			return EMPTY;
		}
		return description.toString();
	}

	/**
	 * Returns the JSON object representation of the described {@link Attribute}
	 * 
	 * @return the JSON object representation of the described {@link Attribute}
	 */
	protected JsonObject getJSONObjectDescription() {
		// if the described attribute is defined as hidden
		if (this.isHidden()) {
			return null;
		}
		// clones the current JSON description
		JsonObjectBuilder description = super.getJsonObject();

		// appends the JSON formated current value
		// if it is not modifiable
		if (Modifiable.FIXED.equals(this.getModifiable())) {
			description.add(VALUE_KEY, CastUtils.cast(JsonValue.class, this.getValue()));
		}
		// appends JSON description of Metadata
		// associated to the described Attribute
		JsonArrayBuilder metadataJSON = JsonProviderFactory.getProvider().createArrayBuilder();

		int index = 0;
		int length = metadataDescriptions.length;

		for (; index < length; index++) {
			MetadataDescription metadataDescription = this.metadataDescriptions[index];

			if (Metadata.LOCKED.equals(metadataDescription.getName())
					|| Metadata.HIDDEN.equals(metadataDescription.getName())) {
				continue;
			}
			JsonObject metadataDescriptionJSON = metadataDescription.getJSONObjectDescription();

			if (metadataDescriptionJSON != null) {
				metadataJSON.add(metadataDescriptionJSON);
			}
		}
		description.add(METADATA_KEY, metadataJSON);
		return description.build();
	}
}
