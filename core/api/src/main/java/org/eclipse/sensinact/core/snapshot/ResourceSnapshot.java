/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.eclipse.sensinact.core.snapshot;

import java.util.Map;

import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.twin.TimedValue;

public interface ResourceSnapshot extends Snapshot {

    ServiceSnapshot getService();

    boolean isSet();

    TimedValue<?> getValue();

    Map<String, Object> getMetadata();

    ResourceType getResourceType();
}
