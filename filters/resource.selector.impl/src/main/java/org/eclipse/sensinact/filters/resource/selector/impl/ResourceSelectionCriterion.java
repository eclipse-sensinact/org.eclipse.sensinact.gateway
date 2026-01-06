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
package org.eclipse.sensinact.filters.resource.selector.impl;

import static org.eclipse.sensinact.filters.resource.selector.impl.ResourceSelectorCriterion.ALWAYS;
import static org.eclipse.sensinact.filters.resource.selector.impl.ResourceSelectorCriterion.always;
import static org.eclipse.sensinact.filters.resource.selector.impl.ResourceSelectorCriterion.fromSelection;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector.ResourceSelection;
import org.eclipse.sensinact.filters.resource.selector.api.Selection;
import org.eclipse.sensinact.filters.resource.selector.api.Selection.MatchType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.CheckType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.OperationType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.ValueSelectionMode;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.Converters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceSelectionCriterion {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceSelectionCriterion.class);

    private static final Predicate<TimedValue<?>> CHECK_IS_SET = t -> t != null && !t.isEmpty();

    private final ResourceSelection rs;

    private final Predicate<ServiceSnapshot> serviceFilter;
    private final Predicate<ResourceSnapshot> resourceFilter;

    private final Predicate<ResourceSnapshot> valueFilter;

    public ResourceSelectionCriterion(ResourceSelection rs) {
        this.rs = rs;
        this.serviceFilter = fromSelection(ServiceSnapshot::getName, rs.service());
        this.resourceFilter = combineServiceCheck(this.serviceFilter,
                fromSelection(ResourceSnapshot::getName, rs.resource()));
        this.valueFilter = rs.value().isEmpty() ? this.resourceFilter
                : combineResourceCheck(this.resourceFilter,
                        rs.value().stream()
                                .map(ResourceSelectionCriterion::toValueFilter)
                                .reduce(Predicate::and)
                                .get());
    }

    public Predicate<ServiceSnapshot> serviceFilter() {
        return serviceFilter;
    }

    public Predicate<ResourceSnapshot> resourceFilter() {
        return resourceFilter;
    }

    public Predicate<ResourceSnapshot> resourceValueFilter() {
        return valueFilter;
    }

    public String exactService() {
        return exactSelection(rs.service());
    }

    public String exactResource() {
        return exactSelection(rs.resource());
    }

    static String exactSelection(Selection s) {
        if (s == null || s.type() != MatchType.EXACT) {
            return null;
        }
        return s.value();
    }

    private static Predicate<ResourceSnapshot> combineResourceCheck(Predicate<ResourceSnapshot> r,
            Predicate<TimedValue<?>> v) {
        if (v == ALWAYS) {
            throw new IllegalStateException("There should always be a value check");
        } else {
            Predicate<ResourceSnapshot> valueCheck = rs -> v.test(rs.getValue());
            return r == ALWAYS ? valueCheck : r.and(valueCheck);
        }
    }

    private static Predicate<ResourceSnapshot> combineServiceCheck(Predicate<ServiceSnapshot> s,
            Predicate<ResourceSnapshot> r) {
        if (s == ALWAYS) {
            return r;
        } else {
            Predicate<ResourceSnapshot> svcCheck = rs -> s.test(rs.getService());
            return r == ALWAYS ? svcCheck : svcCheck.and(r);
        }
    }

    private static Predicate<TimedValue<?>> toValueFilter(ValueSelection vs) {
        Predicate<TimedValue<?>> p;

        if (vs == null) {
            p = always();
        } else {
            final CheckType ct = vs.checkType();
            final Function<TimedValue<?>, Object> getterFunction = switch (ct) {
                case SIZE:
                    yield ResourceSelectionCriterion::toSize;
                case TIMESTAMP:
                    yield TimedValue::getTimestamp;
                case VALUE:
                    yield TimedValue::getValue;
                default:
                    throw new UnsupportedOperationException("Unknown value selection check type " + ct);
            };

            final OperationType ot = vs.operation();
            p = switch (ot) {
                case EQUALS:
                    yield check(getterFunction, vs.value(), Objects::equals, vs.negate(), vs.valueSelectionMode());

                case GREATER_THAN:
                    yield compare(getterFunction, vs.value(), i -> i > 0, vs.negate(), vs.valueSelectionMode());

                case GREATER_THAN_OR_EQUAL:
                    yield compare(getterFunction, vs.value(), i -> i >= 0, vs.negate(), vs.valueSelectionMode());

                case IS_SET:
                    yield vs.negate() ? CHECK_IS_SET.negate() : CHECK_IS_SET;

                case LESS_THAN:
                    yield compare(getterFunction, vs.value(), i -> i < 0, vs.negate(), vs.valueSelectionMode());

                case LESS_THAN_OR_EQUAL:
                    yield compare(getterFunction, vs.value(), i -> i <= 0, vs.negate(), vs.valueSelectionMode());

                case REGEX:
                    yield checkString(getterFunction,
                            vs.value().stream().map(pat -> Pattern.compile(pat).asMatchPredicate()).toList(),
                            vs.negate(), vs.valueSelectionMode());

                case REGEX_REGION:
                    yield checkString(getterFunction,
                            vs.value().stream().map(pat -> Pattern.compile(pat).asPredicate()).toList(),
                            vs.negate(), vs.valueSelectionMode());

                case IS_NOT_NULL:
                    yield CHECK_IS_SET.and(t -> vs.negate() ^ getterFunction.apply(t) != null);

                default:
                    throw new UnsupportedOperationException("Unknown value selection operation " + ot);
            };
        }

        return p;
    }

    private static Number toSize(TimedValue<?> t) {
        Object v = t.getValue();
        if (v instanceof Number) {
            Number n = (Number) v;
            double d = n.doubleValue();
            // If we're equal to our rounded self then we're a natural number
            if ((Math.round(d)) == d) {
                return Math.abs(n.longValue());
            } else {
                return Math.abs(d);
            }
        } else if (v instanceof Collection) {
            return ((Collection<?>) v).size();
        } else if (v instanceof Map) {
            return ((Map<?, ?>) v).size();
        }
        LOG.debug("The value {} cannot be converted to a size", v);
        return null;
    }

    /**
     * Ensure we have a list of values from the resource
     *
     * @param rawValue the raw value from the resource
     * @return Input value(s) as a list
     */
    private static List<Object> extractResourceValues(Object rawValue) {
        final List<Object> rcValues;
        if (rawValue instanceof Collection<?> c) {
            rcValues = c.stream().map(Object.class::cast).toList();
        } else if (rawValue instanceof Map<?, ?> m) {
            rcValues = m.values().stream().map(Object.class::cast).toList();
        } else if (rawValue.getClass().isArray()) {
            rcValues = IntStream.range(0, Array.getLength(rawValue))
                    .mapToObj(i -> Array.get(rawValue, i))
                    .toList();
        } else {
            rcValues = List.of(rawValue);
        }
        return rcValues;
    }

    /**
     * Prepares a predicate that compares resource values against selection values
     *
     * @param getterFunction     Resource value getter
     * @param selectionValues    Selection values
     * @param check              Comparison check
     * @param negate             Flag to negate the result of the check
     * @param valueSelectionMode Value selection mode
     * @return The constructed predicate
     */
    private static Predicate<TimedValue<?>> check(
            final Function<TimedValue<?>, Object> getterFunction,
            final List<String> selectionValues,
            final BiPredicate<Object, Object> check,
            final boolean negate,
            final ValueSelectionMode valueSelectionMode) {

        final Converter converter = Converters.standardConverter();
        final Map<Class<?>, Object> conversionCache = new WeakHashMap<>();

        return t -> {
            if (!CHECK_IS_SET.test(t)) {
                return false;
            }

            final Object v = getterFunction.apply(t);
            if (v == null) {
                // Return false as null values are not allowed
                return false;
            }

            final BiFunction<Object, String, Object> valueConverter = (rcValue, value) -> Optional.ofNullable(rcValue)
                    .map(nonNullVal -> conversionCache
                            .computeIfAbsent(nonNullVal.getClass(), k -> {
                                try {
                                    return converter.convert(value).to(k);
                                } catch (Exception e) {
                                    LOG.debug("Unable to convert the value {} to target {} when selecting resources",
                                            value, k);
                                    return null;
                                }
                            }))
                    .orElse(null);

            // Ensure resource value(s) are in a list for comparison
            final List<Object> rcValues = extractResourceValues(v);
            return modeAwareCheck(rcValues, selectionValues, valueConverter::apply, check, negate,
                    valueSelectionMode);
        };
    }

    /**
     * Prepares a predicate that compares resource values against selection values
     *
     * @param getterFunction     Resource value getter
     * @param value              Selection values
     * @param check              Comparison check
     * @param negate             Flag to negate the result of the check
     * @param valueSelectionMode Value selection mode
     * @return The constructed predicate
     */
    @SuppressWarnings("unchecked")
    private static Predicate<TimedValue<?>> compare(
            final Function<TimedValue<?>, Object> getterFunction,
            final List<String> value,
            final IntPredicate check,
            final boolean negate,
            final ValueSelectionMode valueSelectionMode) {

        return check(getterFunction, value,
                (a, b) -> Comparable.class.isInstance(a) && Comparable.class.isInstance(b) &&
                        check.test(((Comparable<Object>) a).compareTo(b)),
                negate, valueSelectionMode);
    }

    /**
     * Prepares a predicate that checks string resource values against regex
     * predicates
     *
     * @param getterFunction     Resource value getter
     * @param regexPredicates    Regex predicates to apply
     * @param negate             Flag to negate the result of the check
     * @param valueSelectionMode Value selection mode
     * @return The constructed predicate
     */
    private static Predicate<TimedValue<?>> checkString(Function<TimedValue<?>, Object> getterFunction,
            List<Predicate<String>> regexPredicates, boolean negate, ValueSelectionMode valueSelectionMode) {
        return t -> {
            if (!CHECK_IS_SET.test(t)) {
                return false;
            }

            Object v = getterFunction.apply(t);
            if (v == null) {
                // Return false as null values are not allowed
                return false;
            }

            final List<Object> rcValues = extractResourceValues(v);
            return modeAwareCheck(rcValues, regexPredicates, (rc, pred) -> pred,
                    (rc, pred) -> Optional.ofNullable(rc)
                            .map(nonNullRc -> pred.test(nonNullRc.toString()))
                            .orElse(false),
                    negate,
                    valueSelectionMode);
        };
    }

    /**
     * Check resources values against selection values according to the given
     * value selection mode
     *
     * @param <S>                     Selection input type
     * @param <W>                     Selection work type
     * @param rcValues                Resource value(s)
     * @param selectionValues         Selection input value(s)
     * @param selectionValueConverter Converts selection input value to its work
     *                                type
     * @param check                   Check to perform between resource value and
     *                                selection work value
     * @param negate                  Flag to negate the result of the check
     * @param valueSelectionMode      Value selection mode
     * @return True if the check passes, false otherwise
     */
    private static <S, W> boolean modeAwareCheck(
            final List<Object> rcValues,
            final List<S> selectionValues,
            final BiFunction<Object, S, W> selectionValueConverter,
            final BiPredicate<Object, W> check,
            final boolean negate,
            final ValueSelectionMode valueSelectionMode) {

        switch (valueSelectionMode) {
            case ANY_MATCH: {
                // At least one of the resource value(s) must match any of the given selection
                // values
                // No match on an empty list
                if (rcValues.isEmpty()) {
                    return false;
                }

                boolean anyMatch = false;

                outerLoop: for (Object rcValue : rcValues) {
                    for (S selectionValue : selectionValues) {
                        W selectionObj = selectionValueConverter.apply(rcValue, selectionValue);
                        if (selectionObj != null && check.test(rcValue, selectionObj)) {
                            anyMatch = true;
                            break outerLoop;
                        }
                    }
                }
                return negate ^ anyMatch;
            }

            case ALL_MATCH: {
                // All resource values must have a match in the selection values
                if (rcValues.isEmpty()) {
                    // No match on an empty list
                    return false;
                }

                boolean allMatch = true;
                for (Object rcValue : rcValues) {
                    boolean rcValueMatched = false;
                    for (S selectionValue : selectionValues) {
                        W selectionObj = selectionValueConverter.apply(rcValue, selectionValue);
                        if (selectionObj != null && check.test(rcValue, selectionObj)) {
                            rcValueMatched = true;
                            break;
                        }
                    }
                    if (!rcValueMatched) {
                        allMatch = false;
                        break;
                    }
                }
                return negate ^ allMatch;
            }

            case EXACT_MATCH: {
                // All resource values must match the selection values, including ordering
                if (rcValues.size() != selectionValues.size()) {
                    // Can't be an exact match if sizes differ
                    return negate;
                }

                final Iterator<Object> rcIterator = rcValues.iterator();
                final Iterator<S> svIterator = selectionValues.iterator();
                boolean exactMatch = true;
                while (rcIterator.hasNext() && svIterator.hasNext()) {
                    Object rcValue = rcIterator.next();
                    S selectionValue = svIterator.next();
                    W selectionObj = selectionValueConverter.apply(rcValue, selectionValue);
                    if (selectionObj == null || !check.test(rcValue, selectionObj)) {
                        exactMatch = false;
                        break;
                    }
                }
                return negate ^ exactMatch;
            }

            case SUPER_SET: {
                // Resource values are a superset of the selection values
                // Every selection value must have a match in the resource values,
                // but some resources might not be a match
                boolean superSet = true;
                for (S selectionValue : selectionValues) {
                    boolean selectionValueMatched = false;
                    for (Object rcValue : rcValues) {
                        W selectionObj = selectionValueConverter.apply(rcValue, selectionValue);
                        if (selectionObj != null && check.test(rcValue, selectionObj)) {
                            selectionValueMatched = true;
                            break;
                        }
                    }

                    if (!selectionValueMatched) {
                        superSet = false;
                        break;
                    }
                }

                return negate ^ superSet;
            }

            default:
                LOG.error("Unsupported value selection mode: {}", valueSelectionMode);
                return false;
        }
    }
}
