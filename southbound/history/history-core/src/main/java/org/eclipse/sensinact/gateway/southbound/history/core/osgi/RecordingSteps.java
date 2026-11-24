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
*   Kentyou - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.core.osgi;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryRecordingStep;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryRecordingStepFactory;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Rules for choosing and creating the recording step of a backend. */
final class RecordingSteps {

    private static final Logger logger = LoggerFactory.getLogger(RecordingSteps.class);

    private RecordingSteps() {
    }

    /**
     * Tells whether a factory serves a provider.
     *
     * @param property the value of {@link HistoryRecordingStepFactory#PROP_NAME}
     *                 on the factory: absent means every provider, otherwise a
     *                 name or several names
     */
    static boolean serves(Object property, String providerName) {
        if (property == null) {
            return true;
        }
        if (property instanceof Object[] names) {
            return Arrays.stream(names).anyMatch(name -> providerName.equals(String.valueOf(name)));
        }
        if (property instanceof Collection<?> names) {
            return names.stream().anyMatch(name -> providerName.equals(String.valueOf(name)));
        }
        return providerName.equals(String.valueOf(property));
    }

    /**
     * Creates the step of a backend with a factory.
     *
     * @param factoryId the service id of the factory, for the logs
     * @return the step, or {@code null} when the factory declines the backend
     *         or fails
     */
    static HistoryRecordingStep create(HistoryRecordingStepFactory factory, Object factoryId, String providerName,
            HistoryStorage storage) {
        try {
            HistoryRecordingStep step = factory.create(new RecordingContext(providerName, storage));
            if (step == null) {
                logger.debug("The recording step factory service {} declined the history backend {}", factoryId,
                        providerName);
            }
            return step;
        } catch (RuntimeException | LinkageError e) {
            logger.error("The recording step factory service {} failed for the history backend {}", factoryId,
                    providerName, e);
            return null;
        }
    }

    /** Closes a step that no longer receives records; a failure is logged. */
    static void close(HistoryRecordingStep step, String providerName) {
        try {
            step.close();
        } catch (RuntimeException | LinkageError e) {
            logger.warn("History backend {} failed to close its recording step", providerName, e);
        }
    }
}
