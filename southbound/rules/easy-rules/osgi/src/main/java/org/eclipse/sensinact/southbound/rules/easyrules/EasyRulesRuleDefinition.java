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
package org.eclipse.sensinact.southbound.rules.easyrules;

import java.util.List;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.southbound.rules.api.ResourceUpdater;
import org.eclipse.sensinact.southbound.rules.api.RuleDefinition;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;

public class EasyRulesRuleDefinition implements RuleDefinition {

    private final ICriterion filter;
    private final RulesEngine engine;
    private final Rules rules;

    public EasyRulesRuleDefinition(ICriterion filter, Rules rules) {
        this(filter, rules, new DefaultRulesEngine());
    }

    public EasyRulesRuleDefinition(ICriterion filter, Rules rules, RulesEngine engine) {
        this.filter = filter;
        this.engine = engine;
        this.rules = rules;
    }

    @Override
    public ICriterion getInputFilter() {
        return filter;
    }

    @Override
    public void evaluate(List<ProviderSnapshot> data, ResourceUpdater updater) {
        engine.fire(rules, EasyRulesHelper.toFacts(data, updater));
    }

}
