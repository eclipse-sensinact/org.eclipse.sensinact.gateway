/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.json;

import org.eclipse.sensinact.gateway.common.constraint.Absolute;
import org.eclipse.sensinact.gateway.common.constraint.Delta;
import org.eclipse.sensinact.gateway.common.constraint.Different;
import org.eclipse.sensinact.gateway.common.constraint.Fixed;
import org.eclipse.sensinact.gateway.common.constraint.MaxExclusive;
import org.eclipse.sensinact.gateway.common.constraint.MaxInclusive;
import org.eclipse.sensinact.gateway.common.constraint.MinExclusive;
import org.eclipse.sensinact.gateway.common.constraint.MinInclusive;

/**
 * Resumes the operators available in the sensinact-core
 *
 * @author Rémi Druilhe
 */
public class AppOperator {
    public static String getOperator(String operator) {
        if (operator.equals("equal")) {
            return Fixed.OPERATOR;
        } else if (operator.equals("lesserThan")) {
            return MaxExclusive.OPERATOR;
        } else if (operator.equals("lesserEqual")) {
            return MaxInclusive.OPERATOR;
        } else if (operator.equals("greaterThan")) {
            return MinExclusive.OPERATOR;
        } else if (operator.equals("greaterEqual")) {
            return MinInclusive.OPERATOR;
        } else if (operator.equals("abs")) {
            return Absolute.OPERATOR;
        } else if (operator.equals("diff")) {
            return Different.OPERATOR;
        } else if (operator.equals("delta")) {
            return Delta.OPERATOR;
        } else {
            return operator;
        }
    }
}
