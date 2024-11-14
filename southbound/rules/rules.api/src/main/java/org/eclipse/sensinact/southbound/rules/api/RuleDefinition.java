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
package org.eclipse.sensinact.southbound.rules.api;

import java.util.List;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;

public interface RuleDefinition {

    public static final String RULE_NAME_PROPERTY = "sensinact.rule.name";

    public ICriterion getInputFilter();

    public void evaluate(List<ProviderSnapshot> data, ResourceUpdater updater);

}
