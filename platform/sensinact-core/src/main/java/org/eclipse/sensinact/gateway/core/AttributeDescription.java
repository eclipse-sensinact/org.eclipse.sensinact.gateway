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
import org.json.JSONArray;
import org.json.JSONObject;

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
		JSONObject description = super.getJSONObject();
		// appends the JSON formated current value
		description.put(VALUE_KEY, toJson(this.getType(), this.getValue()));
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
				description.put(metadataDescription.getName(),
						toJson(metadataDescription.getType(), metadataDescription.getValue()));
			}
			if (Attribute.NICKNAME.equals(metadataDescription.getName())) {
				description.put(NAME_KEY, metadataDescription.getValue());
			}
		}
		return description.toString();
	}

	/**
	 * @inheritDoc
	 * 
	 * @see PrimitiveDescription#getJSONDescription()
	 */
	@Override
	public String getJSONDescription() {
		JSONObject description = this.getJSONObjectDescription();
		if (description == null) {
			return EMPTY;
		}
		return description.toString(INDENT_FACTOR);
	}

	/**
	 * Returns the JSON object representation of the described {@link Attribute}
	 * 
	 * @return the JSON object representation of the described {@link Attribute}
	 */
	protected JSONObject getJSONObjectDescription() {
		// if the described attribute is defined as hidden
		if (this.isHidden()) {
			return null;
		}
		// clones the current JSON description
		JSONObject description = super.getJSONObject();

		// appends the JSON formated current value
		// if it is not modifiable
		if (Modifiable.FIXED.equals(this.getModifiable())) {
			description.put(VALUE_KEY, toJson(this.getType(), this.getValue()));
		}
		// appends JSON description of Metadata
		// associated to the described Attribute
		JSONArray metadataJSON = new JSONArray();

		int index = 0;
		int length = metadataDescriptions.length;

		for (; index < length; index++) {
			MetadataDescription metadataDescription = this.metadataDescriptions[index];

			if (Metadata.LOCKED.equals(metadataDescription.getName())
					|| Metadata.HIDDEN.equals(metadataDescription.getName())) {
				continue;
			}
			JSONObject metadataDescriptionJSON = metadataDescription.getJSONObjectDescription();

			if (metadataDescriptionJSON != null) {
				metadataJSON.put(metadataDescriptionJSON);
			}
		}
		description.put(METADATA_KEY, metadataJSON);
		return description;
	}
}
