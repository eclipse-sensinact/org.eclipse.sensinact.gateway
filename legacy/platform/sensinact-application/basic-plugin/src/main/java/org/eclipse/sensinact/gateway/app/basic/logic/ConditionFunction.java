/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.basic.logic;

import org.eclipse.sensinact.gateway.app.api.exception.NotAReadableResourceException;
import org.eclipse.sensinact.gateway.app.api.exception.ResourceNotFoundException;
import org.eclipse.sensinact.gateway.app.api.exception.ServiceNotFoundException;
import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;
import org.eclipse.sensinact.gateway.app.api.function.DataItf;

import java.util.List;

/**
 * This abstract class holds the testCondition method that are implemented by both single and double condition
 *
 * @author Remi Druilhe
 * @see AbstractFunction
 */
abstract class ConditionFunction extends AbstractFunction<Boolean> {
    /**
     * @see AbstractFunction#process(List)
     */
    public void process(List<DataItf> datas) {
        try {
            super.update(testCondition(datas));
            return;
        } catch (NotAReadableResourceException e) {
            e.printStackTrace();
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        } catch (ServiceNotFoundException e) {
            e.printStackTrace();
        }
        super.update(null);
    }

    /**
     * Fire the test condition
     *
     * @param datas the variables to tests
     * @throws NotAReadableResourceException
     * @throws ResourceNotFoundException
     * @throws ServiceNotFoundException
     */
    public abstract Boolean testCondition(List<DataItf> datas) throws NotAReadableResourceException, ResourceNotFoundException, ServiceNotFoundException;
}
