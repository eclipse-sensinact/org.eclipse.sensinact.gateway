/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.checker;

import org.eclipse.sensinact.gateway.app.api.exception.InvalidApplicationException;
import org.eclipse.sensinact.gateway.app.manager.component.ComponentConstant;
import org.eclipse.sensinact.gateway.app.manager.json.AppComponent;
import org.eclipse.sensinact.gateway.app.manager.json.AppParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * This class aims at checking different properties of the architecture of an application when it is installed
 *
 * @author Remi Druilhe
 */
public class ArchitectureChecker {
    public static void checkApplication(String applicationName, List<AppComponent> components) throws InvalidApplicationException {
        List<String> identifiers = ArchitectureChecker.checkUniqueOutput(applicationName, components);
        ArchitectureChecker.checkVariableExist(components, identifiers);
        //ArchitectureChecker.checkBoundComponents(components);
        //ArchitectureChecker.checkCycledComponents(components);
    }

    private static List<String> checkUniqueOutput(String applicationName, List<AppComponent> components) throws InvalidApplicationException {
        List<String> identifiers = new ArrayList<String>();
        for (AppComponent component : components) {
            String uri = "/" + applicationName + "/" + component.getIdentifier() + "/" + ComponentConstant.RESULT_DATA;
            if (!identifiers.contains(uri)) {
                identifiers.add(uri);
            } else {
                throw new InvalidApplicationException("The variable " + uri + " already exists.");
            }
        }
        return identifiers;
    }

    private static void checkVariableExist(List<AppComponent> components, List<String> identifiers) throws InvalidApplicationException {
        for (AppComponent component : components) {
            for (AppParameter parameter : component.getFunction().getRunParameters()) {
                if (parameter.getType().equals("variable")) {
                    if (!identifiers.contains(parameter.getValue())) {
                        throw new InvalidApplicationException("The variable " + parameter.getValue() + " does not exist.");
                    }
                }
            }
        }
    }
    /*private static void checkBoundComponents(List<AppComponent> components)
            throws InvalidApplicationException {
        for(AppComponent component : components) {
            List<AbstractDataListener> events = component.getTransitionBlock().getTriggers();
            for(AbstractDataListener event : events) {
                if(event.getVariable().getType().equals("variable")) {
                    if(!variableMap.containsKey(event.getVariable().getValue())) {
                        throw new InvalidApplicationException("Some components are not bound to existing variables.");
                    }
                }
            }
        }
    }*/
    /*private static void checkCycledComponents(List<AppComponent> components)
            throws InvalidApplicationException {
        //TODO: find the user of the output.
        //TODO: checks that the parents are not bound to child output
    }*/
}
