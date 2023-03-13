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

import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.diff.FeatureFilter;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Filter for {@link EStructuralFeature} to ignore during model compare.
 *
 * @author Mark Hoffmann
 * @since 20.10.2017
 */
public class IgnoreFeatureFilter extends FeatureFilter {

    private List<EStructuralFeature> ignoreFeatures = new LinkedList<EStructuralFeature>();

    public IgnoreFeatureFilter(List<EStructuralFeature> ignoreFeatures) {
        if (ignoreFeatures != null) {
            this.ignoreFeatures.addAll(ignoreFeatures);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.emf.compare.diff.FeatureFilter#checkForOrderingChanges(org.
     * eclipse.emf.ecore.EStructuralFeature)
     */
    @Override
    public boolean checkForOrderingChanges(EStructuralFeature feature) {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.emf.compare.diff.FeatureFilter#isIgnoredAttribute(org.eclipse.emf
     * .ecore.EAttribute)
     */
    @Override
    protected boolean isIgnoredAttribute(EAttribute attribute) {
        return ignoreFeatures.contains(attribute) || super.isIgnoredAttribute(attribute);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.emf.compare.diff.FeatureFilter#isIgnoredReference(org.eclipse.emf
     * .compare.Match, org.eclipse.emf.ecore.EReference)
     */
    @Override
    protected boolean isIgnoredReference(Match match, EReference reference) {
        return ignoreFeatures.contains(reference) || super.isIgnoredReference(match, reference);
    }

}
