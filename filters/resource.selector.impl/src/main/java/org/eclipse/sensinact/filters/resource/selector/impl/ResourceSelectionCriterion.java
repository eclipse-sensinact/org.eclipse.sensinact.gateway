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
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector.ResourceSelection;
import org.eclipse.sensinact.filters.resource.selector.api.Selection;
import org.eclipse.sensinact.filters.resource.selector.api.Selection.MatchType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.CheckType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.OperationType;
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
        this.valueFilter = rs.value().isEmpty() ? this.resourceFilter :
                combineResourceCheck(this.resourceFilter, 
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
        if(s == null || s.type() != MatchType.EXACT) {
            return null;
        }
        return s.value();
    }
    
    private static Predicate<ResourceSnapshot> combineResourceCheck(Predicate<ResourceSnapshot> r, Predicate<TimedValue<?>> v) {
        if(v == ALWAYS) {
            throw new IllegalStateException("There should always be a value check");
        } else {
            Predicate<ResourceSnapshot> valueCheck = rs -> v.test(rs.getValue());
            return r == ALWAYS ? valueCheck : r.and(valueCheck);
        }
    }
    
    private static Predicate<ResourceSnapshot> combineServiceCheck(Predicate<ServiceSnapshot> s, Predicate<ResourceSnapshot> r) {
        if(s == ALWAYS) {
            return r;
        } else {
            Predicate<ResourceSnapshot> svcCheck = rs -> s.test(rs.getService());
            return r == ALWAYS ? svcCheck : svcCheck.and(r);
        }
    }
    
    private static Predicate<TimedValue<?>> toValueFilter(ValueSelection vs) {
        Predicate<TimedValue<?>> p;

        if(vs == null) {
            p = always();
        } else {
            CheckType ct = vs.checkType();
            Function<TimedValue<?>,Object> getterFunction = switch(ct) {
                case SIZE: yield ResourceSelectionCriterion::toSize;
                case TIMESTAMP: yield TimedValue::getTimestamp;
                case VALUE: yield TimedValue::getValue;
                default:
                    throw new UnsupportedOperationException("Unknown value selection check type " + ct);
            };
            OperationType ot = vs.operation();
            p = switch(ot) {
                case EQUALS: yield check(getterFunction, vs.value(), Objects::equals, vs.negate());
                case GREATER_THAN: yield compare(getterFunction, vs.value(), i -> i > 0, vs.negate());
                case GREATER_THAN_OR_EQUAL: yield compare(getterFunction, vs.value(), i -> i >= 0, vs.negate());
                case IS_SET: yield vs.negate() ? CHECK_IS_SET.negate() : CHECK_IS_SET;
                case LESS_THAN: yield compare(getterFunction, vs.value(), i -> i < 0, vs.negate());
                case LESS_THAN_OR_EQUAL: yield compare(getterFunction, vs.value(), i -> i <= 0, vs.negate());
                case REGEX: yield checkString(getterFunction, Pattern.compile(vs.value()).asMatchPredicate(), vs.negate());
                case REGEX_REGION: yield checkString(getterFunction, Pattern.compile(vs.value()).asPredicate(), vs.negate());
                case IS_NOT_NULL: yield CHECK_IS_SET.and(t -> vs.negate() ^ getterFunction.apply(t) != null);
                default:
                    throw new UnsupportedOperationException("Unknown value selection operation " + ot);
            };
        }

        return p;
    }

    private static Number toSize(TimedValue<?> t) {
        Object v = t.getValue();
        if(v instanceof Number) {
            Number n = (Number) v;
            double d = n.doubleValue();
            // If we're equal to our rounded self then we're a natural number
            if((Math.round(d)) == d) {
                return Math.abs(n.longValue());
            } else {
                return Math.abs(d);
            }
        } else if (v instanceof Collection) {
            return ((Collection<?>)v).size();
        } else if (v instanceof Map) {
            return ((Map<?,?>)v).size();
        }
        LOG.debug("The value {} cannot be converted to a size", v);
        return null;
    }

    private static Predicate<TimedValue<?>> check(Function<TimedValue<?>, Object> getterFunction, String value,
            BiPredicate<Object, Object> check, boolean negate) {
        Converter conv = Converters.standardConverter();

        Map<Class<?>, Object> conversionCache = new WeakHashMap<>();

        return t -> {
            if(!CHECK_IS_SET.test(t)) {
                return false;
            }

            Object v = getterFunction.apply(t);
            if (v == null) {
                // Return false as null values are not allowed
                return false;
            }

            Predicate<Object> test = o -> {
                Object valueObj = conversionCache.computeIfAbsent(o.getClass(), k -> {
                    try {
                        return conv.convert(value).to(k);
                    } catch (Exception e) {
                        LOG.debug("Unable to convert the value {} to target {} when selecting resources", value, k);
                        return null;
                    }
                });

                return valueObj == null ? false : check.test(o, valueObj);
            };

            boolean result;
            if (v instanceof Collection<?>) {
                result = ((Collection<?>) v).stream().anyMatch(test);
            } else if (v instanceof Map<?, ?>) {
                result = ((Map<?, ?>) v).values().stream().anyMatch(test);
            } else if (v.getClass().isArray()) {
                int length = Array.getLength(v);
                result = false;
                for (int i = 0; i < length && !result; i++) {
                    result = test.test(Array.get(v, i));
                }
            } else {
                result = test.test(v);
            }
            return negate ^ result;
        };
    }

    @SuppressWarnings("unchecked")
    private static <T> Predicate<TimedValue<?>> compare(Function<TimedValue<?>,Object> getterFunction,
            String value, IntPredicate check, boolean negate) {
        return check(getterFunction, value, (a,b) -> Comparable.class.isInstance(a) && Comparable.class.isInstance(b) &&
                check.test(((Comparable<Object>)a).compareTo(b)), negate);
    }

    private static Predicate<TimedValue<?>> checkString(Function<TimedValue<?>,Object> getterFunction,
            Predicate<String> check, boolean negate) {
        return t -> {
            if(!CHECK_IS_SET.test(t)) {
                return false;
            }

            Object v = getterFunction.apply(t);
            if (v == null) {
                // Return false as null values are not allowed
                return false;
            }

            boolean result;
            if(v instanceof Collection<?>) {
                result = ((Collection<?>)v).stream().anyMatch(cv -> check.test(cv.toString()));
            } else if (v instanceof Map<?,?>) {
                result = ((Map<?,?>)v).values().stream().anyMatch(mv -> check.test(mv.toString()));
            } else if (v.getClass().isArray()) {
                result = Arrays.stream((Object[]) v).anyMatch(av -> check.test(av.toString()));
            } else {
                result = check.test(v.toString());
            }
            return negate ^ result;
        };
    }
}
