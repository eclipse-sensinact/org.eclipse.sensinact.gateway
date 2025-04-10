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
package org.eclipse.sensinact.core.notification;

public sealed interface ResourceNotification permits LifecycleNotification,
    ResourceActionNotification, ResourceDataNotification, ResourceMetaDataNotification {

    public String modelPackageUri();

    public String model();

    public String provider();

    public String service();

    public String resource();

    public String getTopic();
}
