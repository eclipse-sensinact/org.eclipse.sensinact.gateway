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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.compare.AttributeChange;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.DifferenceKind;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.EMFCompare.Builder;
import org.eclipse.emf.compare.ReferenceChange;
import org.eclipse.emf.compare.diff.DiffBuilder;
import org.eclipse.emf.compare.diff.IDiffEngine;
import org.eclipse.emf.compare.diff.IDiffProcessor;
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
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sensinact.model.core.ResourceMetadata;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.model.core.impl.FeatureMetadataImpl;
import org.eclipse.sensinact.prototype.model.nexus.emf.EMFUtil;

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
        IDiffProcessor diffProcessor = new DiffBuilder();
        IDiffEngine diffEngine = new IgnoreFeaturesDiffEngine(diffProcessor, EMFUtil.METADATA_PRIVATE_LIST);
        comparatorBuilder.setDiffEngine(diffEngine);
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
        merger.copyAllLeftToRight(filterMetadataDiffs(comparison.getDifferences()), null);
    }

    private static List<EAttribute> unnullable = List.of(SensiNactPackage.Literals.ADMIN__FRIENDLY_NAME,
            SensiNactPackage.Literals.ADMIN__MODEL_URI, SensiNactPackage.Literals.METADATA__ORIGINAL_NAME,
            SensiNactPackage.Literals.TIMESTAMPED__TIMESTAMP);

    private static List<Diff> filterMetadataDiffs(List<Diff> diffs) {
        // Filter:
        // 1. Attribute is new; use their timestamp if present or now if not
        // 2. Attribute changed; no timestamp change in diff; update attribute, keep
        // timestamp
        // 3. Attribute changed; timestamp change to null; update with Timestamp Now
        // 4. Attribute changed; timestamp changed; update attribute and timestamp if
        // new is after old timetamp
        // 5. Attribute not changed, but timestamp updated: same as 4.
        List<Diff> result = new ArrayList<>(diffs);
        List<ReferenceChange> metadataChanges = diffs.stream().filter(ReferenceChange.class::isInstance)
                .map(ReferenceChange.class::cast)
                .filter(rc -> rc.getReference() == SensiNactPackage.Literals.SERVICE__METADATA)
                .collect(Collectors.toList());
        List<AttributeChange> attributeChanges = diffs.stream().filter(AttributeChange.class::isInstance)
                .map(AttributeChange.class::cast)
                .filter(rc -> unnullable.contains(rc.getAttribute()) && rc.getKind() == DifferenceKind.DELETE)
                .collect(Collectors.toList());
        result.removeAll(metadataChanges);
        result.removeAll(attributeChanges);
        return result;
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
        @SuppressWarnings("rawtypes")
        public String apply(EObject eObject) {
            final String identifier;
            if (eObject == null) {
                identifier = null;
            } else if (eObject.eClass().getEIDAttribute() != null) {
                identifier = EcoreUtil.getID(eObject);
            } else if (eObject instanceof ResourceMetadata && eObject.eContainer() instanceof FeatureMetadataImpl) {

                return eObject.eContainer().eContainer().eClass().getName() + "_"
                        + ((FeatureMetadataImpl) eObject.eContainer()).getKey().getName();
            } else if (eObject instanceof FeatureMetadataImpl) {
                Object key = ((Map.Entry) eObject).getKey();
                if (key instanceof ENamedElement) {
                    return ((ENamedElement) key).getName();
                }
                return key == null ? null : key.toString();
            } else {
                identifier = EcoreUtil.getURI(eObject.eClass()).toString();
            }
            return identifier;
        }
    }

}
