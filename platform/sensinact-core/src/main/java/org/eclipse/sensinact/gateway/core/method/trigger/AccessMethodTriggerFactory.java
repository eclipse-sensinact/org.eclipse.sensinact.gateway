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
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * {@link AccessMethodTrigger} factory service
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface AccessMethodTriggerFactory {
    /**
     * ThreadLocal Loader constant
     */
    public static final ThreadLocal<Loader> LOADER = new ThreadLocal<Loader>() {
        /**
         * @inheritDoc
         *
         * @see java.lang.ThreadLocal#initialValue()
         */
        public Loader initialValue() {
            return new Loader();
        }
    };

    /**
     * Loader of AccessMethodTriggerFactory services
     * available in the system
     */
    public final class Loader {
        /**
         * Returns a CalculationFactory instance handling the
         * string operator passed as parameter if it can be
         * found ; otherwise returns null
         *
         * @param name the string operator for which to retrieve the
         *             appropriate CalculationFactory
         * @return a CalculationFactory instance handling the
         * string operator passed as parameter
         */
        public final AccessMethodTriggerFactory load(Mediator mediator, String name) {
            //use the default implementation of the AccessMethodTriggerFactory
            //interface. If the type of the trigger is not handled try to
            //find an appropriate factory in the system by the way of the
            //ServiceLoader (simple service-provider loading facility)
            DefaultAccessMethodTriggerFactory defaultFactory = new DefaultAccessMethodTriggerFactory();

            if (defaultFactory.handle(name)) {
                return defaultFactory;
            }
            //use the ServiceLoader
            ServiceLoader<AccessMethodTriggerFactory> loader = ServiceLoader.load(AccessMethodTriggerFactory.class, mediator.getClassLoader());

            Iterator<AccessMethodTriggerFactory> iterator = loader.iterator();

            while (iterator.hasNext()) {
                AccessMethodTriggerFactory factory = iterator.next();
                if (factory.handle(name)) {
                    //exit the loop if the appropriate factory is found
                    return factory;
                }
            }
            return null;
        }
    }

    /**
     * Returns true if this CalculationFactory can
     * create a {@link AccessMethodTrigger} instance whose
     * string type (identifier) is the same as the one passed
     * as parameter; returns false otherwise
     *
     * @param type the string type of the {@link AccessMethodTrigger}
     * @return <ul>
     * <li>true if this CalculationFactory can
     * create a {@link AccessMethodTrigger} instance
     * whose type is the specified one</li>
     * <li>false otherwise</li>
     * </ul>
     */
    boolean handle(String type);

    /**
     * Creates and returns a {@link AccessMethodTrigger} instance
     * using its JSON formated description
     *
     * @param trigger {@link JSONObject} describing the Calculation to
     *                instantiate
     * @return a new {@link AccessMethodTrigger} instance
     * @throws InvalidValueException if the {@link AccessMethodTrigger} cannot be instantiated
     */
    <P> AccessMethodTrigger<P> newInstance(Mediator mediator, JSONObject trigger) throws InvalidValueException;
}
 
