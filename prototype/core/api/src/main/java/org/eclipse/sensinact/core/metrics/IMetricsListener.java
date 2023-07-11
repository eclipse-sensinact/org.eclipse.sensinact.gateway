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
package org.eclipse.sensinact.core.metrics;

import org.eclipse.sensinact.core.push.dto.BulkGenericDto;

/**
 * Specification of a service that will be notified of metrics reports
 */
public interface IMetricsListener {

    /**
     * Notification of a new metrics report
     *
     * @param dto Bulk of metrics updates
     */
    void onMetricsReport(final BulkGenericDto dto);
}
