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

package org.eclipse.sensinact.gateway.southbound.wot.core;

public @interface Configuration {

    /**
     * If true, use the "@type" entry as the Thing/provider model name. Ignored when
     * a Thing has multiple types.
     */
    boolean model_useType() default true;

    /**
     * Set a prefix to the model
     */
    String model_prefix() default "wot_";

    /**
     * If true, use the "id" (optional) entry in the catalog as the provider name,
     * instead of "title" (mandatory)
     */
    boolean naming_useId() default false;

    /**
     * Prefix to use for provider names
     */
    String naming_prefix() default "wot_";
}
