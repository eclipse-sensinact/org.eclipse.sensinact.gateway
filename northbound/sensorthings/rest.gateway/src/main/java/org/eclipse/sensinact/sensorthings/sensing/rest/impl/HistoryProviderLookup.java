/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Data In Motion - initial implementation
**********************************************************************/
package org.eclipse.sensinact.sensorthings.sensing.rest.impl;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryProvider;

import jakarta.ws.rs.core.Application;

/**
 * Resolves the {@link HistoryProvider} a request should use.
 * <p>
 * The {@link Application} injected into providers and delegates is the
 * whiteboard's wrapper, and its {@link Application#getProperties()} is a copy
 * taken once, when the application was registered. A history provider that
 * registers <em>after</em> that moment — the normal case at startup, where the
 * store still has a database to connect to — therefore never appears under
 * {@link #HISTORY_PROVIDER}. The application publishes a {@link Supplier}
 * under {@link #HISTORY_PROVIDER_SUPPLIER} instead: the supplier is what the
 * snapshot holds, and evaluating it consults the component's dynamic
 * reference, so it reflects providers that come and go.
 */
public final class HistoryProviderLookup {

    public static final String HISTORY_PROVIDER = "sensinact.history.service";

    public static final String HISTORY_PROVIDER_SUPPLIER = "sensinact.history.service.supplier";

    private HistoryProviderLookup() {
    }

    /**
     * @return the currently selected history provider, or {@code null} when
     *         none is available
     */
    public static HistoryProvider from(Application application) {
        Map<String, Object> properties = application.getProperties();
        Object supplier = properties.get(HISTORY_PROVIDER_SUPPLIER);
        if (supplier instanceof Supplier<?> live) {
            Object supplied = live.get();
            if (supplied instanceof Optional<?> selected) {
                return selected.filter(HistoryProvider.class::isInstance).map(HistoryProvider.class::cast)
                        .orElse(null);
            }
            return supplied instanceof HistoryProvider provider ? provider : null;
        }
        return (HistoryProvider) properties.get(HISTORY_PROVIDER);
    }
}
