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
*   Thomas Calmant (Kentyou) - initial implementation
**********************************************************************/
package org.eclipse.sensinact.core.whiteboard;

public interface WhiteboardConstants {

    /**
     * Property held by whiteboard handler service to indicate the list of providers
     * it supports to be called for.
     *
     * Property can be null, a string or a collection or array of strings.
     *
     * If the property is null or an empty collection, the handler accepts all
     * providers.
     *
     * If multiple handlers match the provider, the one with the smallest non-empty
     * list of providers is preferred
     */
    String PROP_PROVIDERS = "sensiNact.provider.name";

    /**
     * Property held by whiteboard handler service to indicate the model package URI
     * it supports to be called for.
     *
     * Property is a string that can be null, in which case it is computed based on
     * default package and the given model name
     */
    String PROP_MODEL_PACKAGE_URI = "sensiNact.whiteboard.modelPackageUri";

    /**
     * Property held by whiteboard handler service to indicate the name of the model
     * it supports to be called for.
     *
     * Property is a string that can't be null
     */
    String PROP_MODEL = "sensiNact.whiteboard.model";

    /**
     * Property held by whiteboard handler service to indicate the name of the
     * service it supports to be called for.
     *
     * Property can be null to indicate any service
     */
    String PROP_SERVICE = "sensiNact.whiteboard.service";

    /**
     * Property held by whiteboard handler service to indicate the name of the
     * resource it supports to be called for.
     *
     * Property is a string that can be null to indicate any resource
     */
    String PROP_RESOURCE = "sensiNact.whiteboard.resource";

    /**
     * Property held by whiteboard handler service to indicate that it requires the
     * whiteboard to create the resource. Value handlers (GET/SET) must implement
     * {@link WhiteboardResourceDescription} and action handlers must implement
     * {@link WhiteboardActDescription} to be able to describe their resource.
     *
     * Property is a boolean that can be null.
     */
    String PROP_AUTO_CREATE = "sensiNact.whiteboard.create";
}
