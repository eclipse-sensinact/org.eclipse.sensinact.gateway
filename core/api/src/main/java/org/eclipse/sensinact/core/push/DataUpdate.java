/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.core.push;

import org.osgi.util.promise.Promise;

public interface DataUpdate {

    /**
     * Push an update into the sensiNact Digital Twin
     * @param o - the update data
     * @return a Promise representing the status of the update.
     * <p> This promise will resolve successfully if the update(s)
     * all complete normally. If any updates fail then the promise
     * will fail with a {@link FailedUpdatesException} indicating
     * which update(s) failed.</p>
     * <p><strong>N.B.</strong> A failed promise does not indicate
     * that no updates were successfully processed, only that
     * at least one update failed to be applied.</p>
     */
    Promise<?> pushUpdate(Object o);

}
