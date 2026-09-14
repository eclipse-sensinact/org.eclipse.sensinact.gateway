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
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.filters.location.api;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;

/**
 * A plug point to allow different location match implementations
 */
public interface LocationMatchFactory {
    /**
     * Standard mathematical relational operators. Note that these may not always
     * be the right choice due to their strict treatment of boundaries
     */
    /**
     * Disjoint - no contact
     */
    public static final List<String> DISJOINT = List.of("FF*FF****");
    /**
     * Intersects - left touches right
     * @deprecated - Intersection is the inverse of Disjointedness. It is
     * faster to check whether the shapes are disjoint and negate the result
     */
    @Deprecated
    public static final List<String> INTERSECTS = List.of("T********", "*T*******", "***T*****", "****T****");
    /**
     * Left contains right
     */
    public static final List<String> CONTAINS = List.of("T*****FF*");
    /**
     * Right contains left
     */
    public static final List<String> WITHIN = List.of("T*F**F***");
    /**
     * Left covers right
     */
    public static final List<String> COVERS = List.of("T*****FF*", "*T****FF*", "***T**FF*", "****T*FF*");
    /**
     * Left covered by right
     */
    public static final List<String> COVERED_BY = List.of("T*F**F***", "*TF**F***", "**FT*F***", "**F*TF***");
    /**
     * Left equals right
     */
    public static final List<String> EQUALS = List.of("T*F**FFF*");
    /**
     * Left touches right (boundaries meet but do not cross)
     */
    public static final List<String> TOUCHES = List.of("FT*******", "F**T*****", "F***T****");
    /**
     * Crosses, when the dimension of Left is less than the dimension of Right
     */
    public static final List<String> CROSSES_DIML_LT_DIMR = List.of("T*T******");
    /**
     * Crosses, when the dimension of Left is greater than the dimension of Right
     */
    public static final List<String> CROSSES_DIML_GT_DIMR = List.of("T*****T**");;
    /**
     * Crosses, when one of the dimensions of Left or Right is one
     */
    public static final List<String> CROSSES_DIM_ANY_EQ_1 = List.of("0********");
    /**
     * Overlaps when Left and Right are Points
     */
    public static final List<String> OVERLAPS_0D = List.of("T*T***T**");
    /**
     * Overlaps when Left and Right are Lines
     */
    public static final List<String> OVERLAPS_1D = List.of("1*T***T**");
    /**
     * Overlaps when Left and Right are Polygons
     */
    public static final List<String> OVERLAPS_2D = OVERLAPS_0D;

    /**
     * Creates a match predicate using the given target, which will be buffered using
     * {@code bufferRadius}, and the supplied intersection matrices.
     * <p>
     * Note that the supplied target will be used as the right hand side operand. This
     * is important for non-commutative operators such as {@link #CONTAINS}
     *
     * @param target the target geometry, will be used as the right hand side operand
     * @param bufferRadius a buffer to apply to the target. If {@code null} then no buffer will be added
     * @param DE_9IM The DE 9IM intersection matrices representing the relation to test
     * @return
     */
    public Predicate<GeoJsonObject> toMatchPredicate(GeoJsonObject target, Double bufferRadius,
            List<String> DE_9IM);
}
