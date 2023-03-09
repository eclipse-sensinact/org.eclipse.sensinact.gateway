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
package org.eclipse.sensinact.prototype.model.nexus.emf;

import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.EMFCompare.Builder;
import org.eclipse.emf.compare.match.DefaultComparisonFactory;
import org.eclipse.emf.compare.match.DefaultEqualityHelperFactory;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.compare.match.eobject.IdentifierEObjectMatcher;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryImpl;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryRegistryImpl;
import org.eclipse.emf.compare.merge.BatchMerger;
import org.eclipse.emf.compare.merge.IMerger;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.google.common.base.Function;

/**
 * Helper to Compare EObjects using EMFCompare
 *
 * @author Juergen Albert
 * @since 22 Feb 2023
 */
public class EMFCompareUtil {

    private static IEObjectMatcher matcher = new IdentifierEObjectMatcher(new EClassFunction());
    private static IMatchEngine.Factory matchEngineFactory = new MatchEngineFactoryImpl(matcher,
            new DefaultComparisonFactory(new DefaultEqualityHelperFactory()));
    private static IMatchEngine.Factory.Registry matchEngineRegistry = new MatchEngineFactoryRegistryImpl();

    private static EMFCompare comparator;

    private static BatchMerger merger = new BatchMerger(IMerger.RegistryImpl.createStandaloneInstance());

    static {
        matchEngineFactory.setRanking(20);
        matchEngineRegistry.add(matchEngineFactory);
        Builder comparatorBuilder = EMFCompare.builder().setMatchEngineFactoryRegistry(matchEngineRegistry);
        comparator = comparatorBuilder.build();
    }

    public static Comparison compareRaw(EObject eObjectNew, EObject eObjectOld) {
        if (eObjectNew == null || eObjectOld == null) {
            return null;
        }
        IComparisonScope scope = new DefaultComparisonScope(eObjectNew, eObjectOld, null);

        Comparison comparison = comparator.compare(scope, new BasicMonitor());
        return comparison;
    }

    public static void merge(Comparison comparison) {
        merger.copyAllLeftToRight(comparison.getDifferences(), null);
    }

    /**
     * The default function used to retrieve IDs from EObject. You might want to
     * extend or compose with it if you want to reuse its behavior.
     */
    public static class EClassFunction implements Function<EObject, String> {
        /**
         * Return an ID for an EObject, null if not found.
         *
         * @param eObject The EObject for which we need an identifier.
         * @return The identifier for that EObject if we could determine one.
         *         <code>null</code> if no condition (see description above) is
         *         fulfilled for the given eObject.
         */
        public String apply(EObject eObject) {
            final String identifier;
            if (eObject == null) {
                identifier = null;
            } else if (eObject.eClass().getEIDAttribute() != null) {
                identifier = EcoreUtil.getID(eObject);
            } else {
                identifier = EcoreUtil.getURI(eObject.eClass()).toString();
            }
            return identifier;
        }
    }

}
