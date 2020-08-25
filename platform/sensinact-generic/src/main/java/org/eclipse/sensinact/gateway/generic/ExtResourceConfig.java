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
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.Name;
import org.eclipse.sensinact.gateway.core.AttributeBuilder;
import org.eclipse.sensinact.gateway.core.RequirementBuilder;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.generic.parser.AttributeDefinition;
import org.eclipse.sensinact.gateway.generic.parser.MethodDefinition;
import org.eclipse.sensinact.gateway.generic.parser.XmlResourceConfigHandler;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.eclipse.sensinact.gateway.util.xml.XMLUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Extended {@link ExtResourceConfig} generated from an xml file
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ExtResourceConfig extends ResourceConfig implements Iterable<MethodDefinition> {
    /**
     * Returns the array of BasisXmlResourceConfig described in the XML file whose
     * string URL is passed as parameter
     *
     * @param mediator the associated {@link Mediator}
     * @param xml      the string XML file's URL describing a set of
     *                 {@link ExtResourceConfig}s
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static XmlResourceConfigHandler loadFromXml(Mediator mediator, URL xml) throws ParserConfigurationException, SAXException, IOException {
        if (xml != null) {
            InputStream inputStream = xml.openStream();
            return ExtResourceConfig.loadFromXml(mediator, inputStream);
        }
        return null;
    }

    /**
     * @param mediator
     * @param fileInputStream
     * @return
     */
    public static XmlResourceConfigHandler loadFromXml(Mediator mediator, InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        URL schema = ExtResourceConfig.class.getClassLoader().getResource("sensinact-resource.xsd");

        XmlResourceConfigHandler handler = new XmlResourceConfigHandler(mediator);
        try {
            XMLUtil.parse(handler, schema, new InputSource(inputStream));
            return handler;

        } catch (Exception e) {
            mediator.error(e, e.getMessage());
        }
        return null;
    }

    protected byte[] identifier;
    protected byte subscriptionMode;
    protected List<AttributeDefinition> attributeDefinitions;
    protected List<MethodDefinition> methodDefinitions;

    /**
     * Constructor
     */
    public ExtResourceConfig() {
        this(new ArrayList<AttributeDefinition>(),new ArrayList<MethodDefinition>());
    }

    public ExtResourceConfig(List<AttributeDefinition> attributeDefinitions, List<MethodDefinition> methodDefinitions) {
    	this.attributeDefinitions = attributeDefinitions;
        this.methodDefinitions = methodDefinitions;
	}

	/**
     * Defines the command bytes array
     *
     * @param identifier the command bytes array
     */
    public void setIdentifier(byte[] identifier) {
        this.identifier = identifier;
    }

    /**
     * @inheritDoc
     * @see ExtResourceConfig#identifier()
     */
    public byte[] getIdentifier() {
        return this.identifier;
    }

    /**
     * Defines the subscription mode handled by the
     * {@link ResourceImpl} instances based on this
     * ResourceConfig
     *
     * @param subscriptionMode the handled subscription mode(s)
     */
    public void setSubscriptionMode(byte subscriptionMode) {
        this.subscriptionMode = subscriptionMode;
    }

    /**
     * Returns the subscription mode handled by the
     * {@link ResourceImpl} instances based on this
     * ResourceConfig
     *
     * @return the handled subscription mode(s)
     */
    public int getSubscriptionMode() {
        return this.subscriptionMode;
    }

    /**
     * Returns the set of {@link AttributeBuilder}s for
     * the configured {@link Resource} type
     *
     * @return the set of {@link AttributeBuilder}s for the
     * configured {@link Resource} type
     */
    @Override
    public List<AttributeBuilder> getAttributeBuilders(String service) {
        List<AttributeBuilder> builders = new ArrayList<AttributeBuilder>();
        builders.addAll(super.getAttributeBuilders(service));

        Iterator<AttributeDefinition> iterator = this.attributeDefinitions.iterator();

        while (iterator.hasNext()) {
            AttributeDefinition attributeDefinition = iterator.next();
            if (!attributeDefinition.isTargeted(service)) {
                continue;
            }
            int builderIndex = -1;
            if ((builderIndex = builders.indexOf(new Name<AttributeBuilder>(attributeDefinition.getName()))) > -1) {
                AttributeBuilder builder = builders.get(builderIndex);
                List<RequirementBuilder> requirementBuilders = attributeDefinition.getRequirementBuilders(service);
                int index = 0;
                int length = requirementBuilders.size();
                for (; index < length; index++) {
                    requirementBuilders.get(index).apply(service, builder);
                }
                builder.addConstraints(attributeDefinition.getConstraints(service));
                builder.addMetadataBuilders(attributeDefinition.getMetadataBuilders());
            } else {
                AttributeBuilder attributeBuilder = attributeDefinition.getAttributeBuilder(service);
                if (attributeBuilder != null) {
                    builders.add(attributeBuilder);
                }
            }
        }
        return builders;
    }
    
    @Override
    public List<String> getObserveds(String service) {
        List<String> observeds = new ArrayList<>();
        observeds.addAll(super.getObserveds(service));

        Iterator<AttributeDefinition> iterator = this.attributeDefinitions.iterator();

        while (iterator.hasNext()) {
            AttributeDefinition attributeDefinition = iterator.next();
            if (!attributeDefinition.isObserved(service)) {
                continue;
            }
            observeds.add(UriUtils.getUri(new String[] {service,this.getName(service),attributeDefinition.getName()}));
        }
        return observeds;
    }

    /**
     * @InheritedDoc
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<MethodDefinition> iterator() {
        return Collections.unmodifiableList(this.methodDefinitions).iterator();
    }
}
