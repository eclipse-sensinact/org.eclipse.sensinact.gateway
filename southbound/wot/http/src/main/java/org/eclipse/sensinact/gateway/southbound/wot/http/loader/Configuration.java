/*
 * Copyright 2024 Kentyou
 * Proprietary and confidential
 *
 * All Rights Reserved.
 * Unauthorized copying of this file is strictly prohibited
 */
package org.eclipse.sensinact.gateway.southbound.wot.http.loader;

public @interface Configuration {

    /**
     * URL to download a Thing Description from
     */
    String url();
}
