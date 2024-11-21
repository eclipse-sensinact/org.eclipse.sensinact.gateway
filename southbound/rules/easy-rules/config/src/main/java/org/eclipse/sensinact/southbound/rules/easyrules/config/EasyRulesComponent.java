/*********************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Kentyou - initial implementation
 **********************************************************************/
package org.eclipse.sensinact.southbound.rules.easyrules.config;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.jexl3.introspection.JexlPermissions;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelectorFilterFactory;
import org.eclipse.sensinact.southbound.rules.api.ResourceUpdater;
import org.eclipse.sensinact.southbound.rules.api.RuleDefinition;
import org.eclipse.sensinact.southbound.rules.easyrules.EasyRulesRuleDefinition;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.core.RuleBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component(configurationPid = "sensinact.rules.easyrules")
public class EasyRulesComponent implements RuleDefinition {

    public @interface Config {

        String sensinact_rule_name() default "";

        String[] resource_selectors();

        String[] rule_definitions();
    }

    public static class RuleDTO {
        public String name;
        public String description;
        public String condition;
        @JsonFormat(with = Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        public List<String> action;
        public int priority;
    }

    @Reference
    ResourceSelectorFilterFactory filterFactory;

    private final ObjectMapper mapper = new ObjectMapper();

    private final JexlEngine jexl = new JexlBuilder().permissions(JexlPermissions.UNRESTRICTED).safe(false).create();

    private EasyRulesRuleDefinition ruleDefinition;

    private Config config;

    @Activate
    void start(Config config) {
        this.config = config;
        String[] resource_selectors = config.resource_selectors();
        if(resource_selectors == null || resource_selectors.length == 0) {
            throw new IllegalArgumentException("At least one resource selector must be defined");
        }
        ICriterion resourceSelector = filterFactory.parseResourceSelector(Arrays.stream(resource_selectors).map(this::readResourceSelector));

        String[] rules = config.rule_definitions();
        if(rules == null || rules.length == 0) {
            throw new IllegalArgumentException("At least one rule must be defined");
        }
        Rules r = new Rules(Arrays.stream(rules)
                .map(this::readRuleDTO)
                .map(this::toRule)
                .collect(toSet()));

        ruleDefinition = new EasyRulesRuleDefinition(resourceSelector, r);
    }

    private ResourceSelector readResourceSelector(String s) {
        try {
            return mapper.readValue(s, ResourceSelector.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("The resource selector is not valid: " + s, e);
        }
    }

    private RuleDTO readRuleDTO(String s) {
        try {
            return mapper.readValue(s, RuleDTO.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("The rule definition is not valid: " + s, e);
        }
    }

    private Rule toRule(RuleDTO dto) {
        Objects.requireNonNull(dto.name, () -> "There is an unnamed rule in config "
                + config.sensinact_rule_name());
        Objects.requireNonNull(dto.condition, () -> "The rule " + dto.name + " in config "
                + config.sensinact_rule_name() + " must specify a condition");
        JexlExpression condition = jexl.createExpression(dto.condition);
        List<JexlScript> actions = dto.action == null ? List.of() :
            dto.action.stream().map(jexl::createScript).collect(toList());

        RuleBuilder rb = new RuleBuilder()
                .name(dto.name)
                .description(dto.description)
                .priority(dto.priority)
                .when(f -> (Boolean) condition.evaluate(new MapContext(f.asMap())));
        for(JexlScript je : actions) {
            rb = rb.then(f -> je.execute(new MapContext(f.asMap())));
        }
        return rb.build();
    }

    @Override
    public ICriterion getInputFilter() {
        return ruleDefinition.getInputFilter();
    }

    @Override
    public void evaluate(List<ProviderSnapshot> data, ResourceUpdater updater) {
        ruleDefinition.evaluate(data, updater);
    }

}
