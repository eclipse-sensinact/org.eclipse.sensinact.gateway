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

import java.util.List;
import java.util.Map.Entry;

public interface WhiteboardActDescription<T> {

    Class<T> getReturnType();

    List<Entry<String, Class<?>>> getNamedParameterTypes();
}
