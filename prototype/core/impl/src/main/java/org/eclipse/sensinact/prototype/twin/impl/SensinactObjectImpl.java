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
package org.eclipse.sensinact.prototype.twin.impl;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.emf.compare.AttributeChange;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.EMFCompare.Builder;
import org.eclipse.emf.compare.ReferenceChange;
import org.eclipse.emf.compare.diff.DiffBuilder;
import org.eclipse.emf.compare.diff.IDiffEngine;
import org.eclipse.emf.compare.diff.IDiffProcessor;
import org.eclipse.emf.compare.match.DefaultMatchEngine;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryImpl;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryRegistryImpl;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.UseIdentifiers;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sensinact.prototype.command.impl.CommandScopedImpl;
import org.eclipse.sensinact.prototype.model.nexus.impl.ModelNexus;
import org.eclipse.sensinact.prototype.twin.SensinactObject;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class SensinactObjectImpl extends CommandScopedImpl implements SensinactObject {

    private final ModelNexus nexus;

    private final PromiseFactory promiseFactory;

    private final EObject snapshot;

    public SensinactObjectImpl(AtomicBoolean active, EObject snapshot, ModelNexus nexus,
            PromiseFactory promiseFactory) {
        super(active);
        this.snapshot = snapshot;
        this.nexus = nexus;
        this.promiseFactory = promiseFactory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.sensinact.prototype.twin.SensinactObject#update(org.eclipse.emf.
     * ecore.EObject, java.time.Instant)
     */
    @Override
    public Promise<Void> update(EObject eObject, Instant timestamp) {
        EMFCompare compare = 
        return null;
    }

    public Comparison compareRaw(EObject eObjectNew, EObject eObjectOld, List<EStructuralFeature> ignoreFeatures) {
        if (eObjectNew == null || eObjectOld == null) {
            return null;
        }
        IComparisonScope scope = new DefaultComparisonScope(eObjectNew, eObjectOld, null);
        IEObjectMatcher matcher = DefaultMatchEngine.createDefaultEObjectMatcher(UseIdentifiers.NEVER);

        IMatchEngine.Factory matchEngineFactory = new MatchEngineFactoryImpl();
        matchEngineFactory.setRanking(20);
        IMatchEngine.Factory.Registry matchEngineRegistry = new MatchEngineFactoryRegistryImpl();
        matchEngineRegistry.add(matchEngineFactory);

        Builder comparatorBuilder = EMFCompare.builder().setMatchEngineFactoryRegistry(matchEngineRegistry);
        if (ignoreFeatures != null && ignoreFeatures.size() > 0) {
//            IDiffProcessor diffProcessor = new DiffBuilder();
//            IDiffEngine diffEngine = new IgnoreFeaturesDiffEngine(diffProcessor, ignoreFeatures);
//            comparatorBuilder.setDiffEngine(diffEngine);
        }
        EMFCompare comparator = comparatorBuilder.build();
        Comparison comparison = comparator.compare(scope);
//        for (Diff difference : comparison.getDifferences()) {
//            if (difference instanceof AttributeChange) {
//                visitAttributeChanges((AttributeChange) difference);
//            }
//            if (difference instanceof ReferenceChange) {
//                visitReferenceChanges((ReferenceChange) difference);
//            }
//        }
        return comparison;
    }

}
