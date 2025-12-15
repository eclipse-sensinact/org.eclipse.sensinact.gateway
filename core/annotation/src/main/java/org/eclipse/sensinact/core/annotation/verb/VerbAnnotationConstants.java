/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.core.annotation.verb;

/**
 * Constants that are used in the verb annotations, typically to
 * indicate that no value has been set in an optional member.
 */
public final class VerbAnnotationConstants {

    private VerbAnnotationConstants() {};

    /**
     * This marker value is used to indicate that no resource
     * type has been set in the annotation and that inference
     * should be used to determine the type of this resource.
     */
    public static final Class<?> NO_TYPE_SET = Marker.class;

    /**
     * This type is default visibility so that it can be used in
     * the verb annotations, but not by anyone else. Users of the
     * annotation can see whether no type is set by using the
     * constant {@link VerbAnnotationConstants#NO_TYPE_SET}.
     */
    static final class Marker {}
}
