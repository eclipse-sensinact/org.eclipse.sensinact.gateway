/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.parser;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A XmlElement annotation associates an XML node and a field name
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Inherited
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface XmlElement {
    public static final String DEFAULT_ELEMENT_VALUE = "#DEFAULT_ELEMENT#";

    public String tag() default DEFAULT_ELEMENT_VALUE;

    public String field() default DEFAULT_ELEMENT_VALUE;
}
