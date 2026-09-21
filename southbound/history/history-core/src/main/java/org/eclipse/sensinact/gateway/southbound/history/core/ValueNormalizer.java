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
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.core;

import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.ValueKind;

/**
 * Turns resource data notifications into normalized {@link HistoricalRecord}s
 * so every backend receives identically typed input. The kind is derived from
 * the resource's declared type; the boxed value keeps its original Java class
 * (float specials are widened to Double per the {@link ValueKind#NUMBER}
 * contract).
 */
public final class ValueNormalizer {

    private static final Set<Class<?>> PRIMITIVE_NUMBERS = Set.of(byte.class, short.class, int.class, long.class,
            float.class, double.class);

    private ValueNormalizer() {
    }

    public static HistoricalRecord normalize(ResourceDataNotification event) {
        ResourcePath path = new ResourcePath(event.provider(), event.service(), event.resource());
        ValueKind kind = kindOf(event.type(), event.newValue());
        return new HistoricalRecord(event.modelPackageUri(), event.model(), path, event.timestamp(), null, kind,
                normalizeValue(kind, event.newValue()), Map.of());
    }

    private static ValueKind kindOf(Class<?> type, Object value) {
        if (type == null) {
            return kindOfValue(value);
        }
        if (GeoJsonObject.class.isAssignableFrom(type)) {
            return value == null || value instanceof GeoJsonObject ? ValueKind.GEOJSON : ValueKind.OBJECT;
        }
        if (type == boolean.class || type == Boolean.class) {
            return ValueKind.BOOLEAN;
        }
        if (PRIMITIVE_NUMBERS.contains(type) || Number.class.isAssignableFrom(type)) {
            return ValueKind.NUMBER;
        }
        if (type == char.class || CharSequence.class.isAssignableFrom(type) || Character.class == type) {
            return ValueKind.STRING;
        }
        return ValueKind.OBJECT;
    }

    private static ValueKind kindOfValue(Object value) {
        if (value == null) {
            return ValueKind.OBJECT;
        }
        return kindOf(value.getClass(), value);
    }

    private static Object normalizeValue(ValueKind kind, Object value) {
        if (value == null) {
            return null;
        }
        return switch (kind) {
        case NUMBER -> {
            if (value instanceof Float f && (f.isNaN() || f.isInfinite())) {
                yield f.doubleValue();
            }
            yield value;
        }
        case STRING -> value instanceof String ? value : value.toString();
        case BOOLEAN, GEOJSON, OBJECT -> value;
        };
    }
}
