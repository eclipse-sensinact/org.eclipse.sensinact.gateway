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
package org.eclipse.sensinact.gateway.core.method.trigger;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintConstantPair;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Default core's {@link AccessMethodTriggerFactory} implementation
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class DefaultAccessMethodTriggerFactory implements AccessMethodTriggerFactory {
    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTriggerFactory#
     * handle(java.lang.String)
     */
    @Override
    public boolean handle(String type) {
        try {
            return AccessMethodTrigger.Type.valueOf(type) != null;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTriggerFactory#
     * newInstance(org.eclipse.sensinact.gateway.common.bundle.Mediator, org.json.JSONObject)
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public <P> AccessMethodTrigger<P> newInstance(Mediator mediator, JSONObject jsonTrigger) throws InvalidValueException {
        if (JSONObject.NULL.equals(jsonTrigger)) {
            throw new InvalidValueException("Null JSON trigger definition");
        }
        AccessMethodTrigger<P> trigger = null;
        try {
            String jsonType = jsonTrigger.getString(AccessMethodTrigger.TRIGGER_TYPE_KEY);

            AccessMethodTrigger.Type type = AccessMethodTrigger.Type.valueOf(jsonType);

            boolean passOn = jsonTrigger.optBoolean(AccessMethodTrigger.TRIGGER_PASS_ON);
            int index = -1;

            switch (type) {
                case CONDITIONAL:
                    List<ConstraintConstantPair> constraints = new ArrayList<ConstraintConstantPair>();
                    index = jsonTrigger.optInt(AccessMethodTrigger.TRIGGER_INDEX_KEY);
                    JSONArray constants = jsonTrigger.optJSONArray(Constant.TRIGGER_CONSTANTS_KEY);
                    int constantsIndex = 0;
                    int length = constants == null ? 0 : constants.length();

                    for (; constantsIndex < length; constantsIndex++) {
                        JSONObject constantObject = constants.getJSONObject(constantsIndex);

                        constraints.add(new ConstraintConstantPair(ConstraintFactory.Loader.load(mediator.getClassLoader(), constantObject.opt(Constant.TRIGGER_CONSTRAINT_KEY)), constantObject.opt(Constant.TRIGGER_CONSTANT_KEY)));
                    }
                    trigger = (AccessMethodTrigger<P>) new ConditionalConstant(mediator, index, constraints, passOn);
                    break;
                case CONSTANT:
                    Object constant = jsonTrigger.opt(Constant.TRIGGER_CONSTANT_KEY);
                    trigger = (AccessMethodTrigger<P>) new Constant(constant, passOn);
                    break;
                case COPY:
                    index = jsonTrigger.optInt(AccessMethodTrigger.TRIGGER_INDEX_KEY);
                    trigger = (AccessMethodTrigger<P>) new Copy(index, passOn);
                    break;
                default:
                    throw new InvalidValueException(new StringBuilder().append("Unknown calculation identifier :").append(jsonType).toString());
            }
        } catch (Exception e) {
            throw new InvalidValueException(e);
        }
        return trigger;
    }
}
