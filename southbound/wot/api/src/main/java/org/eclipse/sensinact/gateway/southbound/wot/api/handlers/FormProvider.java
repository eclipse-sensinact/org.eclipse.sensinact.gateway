/*********************************************************************
* Copyright (c) 2024 Kentyou.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Thomas Calmant (Kentyou) - Initial contribution
**********************************************************************/

package org.eclipse.sensinact.gateway.southbound.wot.api.handlers;

import java.net.URI;
import java.util.List;

import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.gateway.southbound.wot.api.ActionAffordance;
import org.eclipse.sensinact.gateway.southbound.wot.api.EventAffordance;
import org.eclipse.sensinact.gateway.southbound.wot.api.Form;
import org.eclipse.sensinact.gateway.southbound.wot.api.PropertyAffordance;
import org.eclipse.sensinact.gateway.southbound.wot.api.Thing;

/**
 * Description of a form handler. Must be able to describe itself in a form
 */
public interface FormProvider {

    String PROP_PROTOCOL = "sensinact.wot.form.protocol";

    /**
     * Returns the list of forms available for the given property affordance.
     *
     * Do not update the forms directly in the Thing bean
     *
     * @param httpBaseUri Root of the HTTP server providing the VOFactory HTTP
     *                    northbound. Useful to get the host name used by the client
     *                    to connect the server.
     * @param resource    Described resource
     * @param thing       Described thing (being updated)
     * @param property    Property to provide forms for
     * @return List of forms to add to the thing
     */
    List<Form> getPropertyForms(URI httpBaseUri, SensinactResource resource, Thing thing, PropertyAffordance property);

    /**
     * Returns the list of forms available for the given action affordance.
     *
     * Do not update the forms directly in the Thing bean
     *
     * @param httpBaseUri Root of the HTTP server providing the VOFactory HTTP
     *                    northbound. Useful to get the host name used by the client
     *                    to connect the server.
     * @param resource    Described resource
     * @param thing       Described thing (being updated)
     * @param action      Action to provide forms for
     * @return List of forms to add to the thing
     */
    List<Form> getActionForms(URI httpBaseUri, SensinactResource resource, Thing thing, ActionAffordance action);

    /**
     * Returns the list of forms available for the given event affordance.
     *
     * Do not update the forms directly in the Thing bean
     *
     * @param httpBaseUri Root of the HTTP server providing the VOFactory HTTP
     *                    northbound. Useful to get the host name used by the client
     *                    to connect the server.
     * @param resource    Described resource
     * @param thing       Described thing (being updated)
     * @param event       Event to provide forms for
     * @return List of forms to add to the thing
     */
    List<Form> getEventForms(URI httpBaseUri, SensinactResource resource, Thing thing, EventAffordance event);
}
