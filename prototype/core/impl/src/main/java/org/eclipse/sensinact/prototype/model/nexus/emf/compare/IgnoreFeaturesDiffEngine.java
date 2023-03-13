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
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.prototype.model.nexus.emf.compare;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.compare.diff.DefaultDiffEngine;
import org.eclipse.emf.compare.diff.FeatureFilter;
import org.eclipse.emf.compare.diff.IDiffProcessor;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Diff engine that can ignore certain {@link EStructuralFeature} during
 * compare.
 *
 * @author Mark Hoffmann
 * @since 20.10.2017
 */
public class IgnoreFeaturesDiffEngine extends DefaultDiffEngine {

    private List<EStructuralFeature> ignoreFeatures = new LinkedList<EStructuralFeature>();

    public IgnoreFeaturesDiffEngine(IDiffProcessor processor, List<EStructuralFeature> ignoreFeatures) {
        super(processor);
        if (ignoreFeatures != null) {
            this.ignoreFeatures.addAll(ignoreFeatures);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.emf.compare.diff.DefaultDiffEngine#createFeatureFilter()
     */
    @Override
    protected FeatureFilter createFeatureFilter() {
        return new IgnoreFeatureFilter(ignoreFeatures);
    }

}
