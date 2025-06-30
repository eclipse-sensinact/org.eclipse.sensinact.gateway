/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.filters.resource.selector.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.filters.api.FilterParserException;
import org.eclipse.sensinact.filters.api.IFilterParser;
import org.eclipse.sensinact.filters.propertytypes.FiltersSupported;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelectorFilterFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Provides the ResourceSelector filter creation service
 */
@Component(configurationPid = "sensinact.resource.selector")
@FiltersSupported(ResourceSelectorFilterFactory.RESOURCE_SELECTOR_FILTER)
public class ResourceSelectorComponent implements ResourceSelectorFilterFactory, IFilterParser {

    @interface Config {
        // This is temporary until we can use a released Typed Event
        // implementation with wildcard support
        boolean single_level_wildcard_enabled() default false;
    }

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Activate
    Config config;

    @Override
    public ICriterion parseResourceSelector(ResourceSelector selector) {
        return new ResourceSelectorCriterion(selector, config.single_level_wildcard_enabled());
    }

    @Override
    public ICriterion parseResourceSelector(Stream<ResourceSelector> selectors) {
        Optional<ICriterion> or = selectors.map(this::parseResourceSelector).reduce(ICriterion::or);
        return or.orElseThrow(() -> new IllegalArgumentException("No selectors defined"));
    }

    @Override
    public ICriterion parseFilter(String query, String queryLanguage, Map<String, Object> parameters)
            throws FilterParserException {
        try (JsonParser parser = mapper.createParser(query)) {
            if (parser.nextToken() == JsonToken.START_ARRAY) {
                List<ResourceSelector> list = new ArrayList<>();
                while (parser.nextToken() == JsonToken.START_OBJECT) {
                    list.add(parser.readValueAs(ResourceSelector.class));
                }
                if (parser.currentToken() != JsonToken.END_ARRAY) {
                    throw new JsonParseException(parser, "Expected a complete array of Resource Selector objects");
                }
                if (parser.nextToken() != null) {
                    throw new JsonParseException(parser, "Unexpected additional content after the array");
                }
                return parseResourceSelector(list.stream());
            } else {
                return parseResourceSelector(parser.readValueAs(ResourceSelector.class));
            }
        } catch (Exception e) {
            throw new FilterParserException("Failed to parse the resource selector", e);
        }
    }

}
