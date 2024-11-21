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
package org.eclipse.sensinact.filters.resource.selector.impl;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.filters.resource.selector.api.LocationSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.Selection;
import org.eclipse.sensinact.filters.resource.selector.api.Selection.MatchType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.CheckType;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection.OperationType;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.Converters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceSelectorCriterion implements ICriterion {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceSelectorCriterion.class);

    private final ResourceSelector rs;

    private final boolean allowSingleLevelWildcards;

    private final Predicate<ProviderSnapshot> providerFilter;
    private final Predicate<ServiceSnapshot> serviceFilter;
    private final Predicate<ResourceSnapshot> resourceFilter;

    private final Predicate<TimedValue<?>> valueFilter;

    public ResourceSelectorCriterion(ResourceSelector rs, boolean allowSingleLevelWildcards) {
        this.rs = rs;
        this.allowSingleLevelWildcards = allowSingleLevelWildcards;

        this.providerFilter = toProviderFilter(rs);
        this.serviceFilter = rs.service == null ? null :
            fromSelection(ServiceSnapshot::getName, rs.service);
        this.resourceFilter = rs.resource == null ? null :
            fromSelection(ResourceSnapshot::getName, rs.resource);

        this.valueFilter = rs.value == null ? null :
            rs.value.stream().map(ResourceSelectorCriterion::toValueFilter)
                .reduce(x -> true, Predicate::and);
    }

    private static Predicate<ProviderSnapshot> toProviderFilter(ResourceSelector rs) {
        if(rs.model == null) {
            return rs.provider == null ? null :
                fromSelection(ProviderSnapshot::getName, rs.provider);
        } else if(rs.provider == null) {
            return fromSelection(ProviderSnapshot::getModelName, rs.model);
        } else {
            return fromSelection(ProviderSnapshot::getModelName, rs.model)
                    .and(fromSelection(ProviderSnapshot::getName, rs.provider));
        }
    }

    private static <T> Predicate<T> fromSelection(Function<T,String> nameExtractor, Selection s) {

        MatchType type = s.type == null ? MatchType.EXACT : s.type;
        Predicate<String> test;
        switch(type) {
        case EXACT:
            test = s.value::equals;
            break;
        case REGEX:
            test = Pattern.compile(s.value).asMatchPredicate();
            break;
        case REGEX_REGION:
            test = Pattern.compile(s.value).asPredicate();
            break;
        default:
            throw new UnsupportedOperationException("Unknown selection type " + s.type);
        }

        return t -> test.test(nameExtractor.apply(t));
    }

    private static Predicate<TimedValue<?>> toValueFilter(ValueSelection vs) {
        Predicate<TimedValue<?>> p;

        if(vs == null) {
            p = x -> true;
        } else {
            Function<TimedValue<?>,Object> getterFunction;
            CheckType ct = vs.checkType == null ? CheckType.VALUE : vs.checkType;
            switch(ct) {
            case SIZE:
                getterFunction = ResourceSelectorCriterion::toSize;
                break;
            case TIMESTAMP:
                getterFunction = TimedValue::getTimestamp;
                break;
            case VALUE:
                getterFunction = TimedValue::getValue;
                break;
            default:
                throw new UnsupportedOperationException("Unknown value selection check type " + vs.checkType);
            }
            OperationType ot = vs.operation == null ? OperationType.EQUALS : vs.operation;
            switch(ot) {
                case EQUALS:
                    p = check(getterFunction, vs.value, Objects::equals);
                    break;
                case GREATER_THAN:
                    p = compare(getterFunction, vs.value, i -> i > 0);
                    break;
                case GREATER_THAN_OR_EQUAL:
                    p = compare(getterFunction, vs.value, i -> i >= 0);
                    break;
                case IS_SET:
                    p = ResourceSelectorCriterion::isSet;
                    break;
                case LESS_THAN:
                    p = compare(getterFunction, vs.value, i -> i < 0);
                    break;
                case LESS_THAN_OR_EQUAL:
                    p = compare(getterFunction, vs.value, i -> i <= 0);
                    break;
                case REGEX:
                    p = checkString(getterFunction, Pattern.compile(vs.value).asMatchPredicate());
                    break;
                case REGEX_REGION:
                    p = checkString(getterFunction, Pattern.compile(vs.value).asPredicate());
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown value selection operation " + vs.operation);
            }
        }

        return vs.negate ? p.negate() : p;
    }

    private static Number toSize(TimedValue<?> t) {
        Object v = t.getValue();
        if(v instanceof Number) {
            Number n = (Number) v;
            double d = n.doubleValue();
            // If we're equal to our rounded self then we're a natural number
            if(((double)Math.round(d)) == d) {
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

    private static Predicate<TimedValue<?>> check(Function<TimedValue<?>,Object> getterFunction,
            String value, BiPredicate<Object, Object> check) {
        Converter conv = Converters.standardConverter();

        Map<Class<?>,Object> conversionCache = new WeakHashMap<>();

        return t -> {
            if(t == null) return false;

            Object v  = getterFunction.apply(t);
            if(v == null) return false;

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
            if(v instanceof Collection<?>) {
                result = ((Collection<?>)v).stream().anyMatch(test);
            } else if (v instanceof Map<?,?>) {
                result = ((Map<?,?>)v).values().stream().anyMatch(test);
            } else if (v.getClass().isArray()) {
                int length = Array.getLength(v);
                result = false;
                for(int i = 0; i < length && !result; i++) {
                    result = test.test(Array.get(v, i));
                }
            } else {
                result = test.test(v);
            }
            return result;
        };
    }

    @SuppressWarnings("unchecked")
    private static <T> Predicate<TimedValue<?>> compare(Function<TimedValue<?>,Object> getterFunction,
            String value, IntPredicate check) {
        return check(getterFunction, value, (a,b) -> Comparable.class.isInstance(a) && Comparable.class.isInstance(b) &&
                check.test(((Comparable<Object>)a).compareTo(b)));
    }

    private static boolean isSet(TimedValue<?> t) {
        if(t == null) {
            return false;
        } else {
            Object o = t.getValue();

            if(o == null) {
                return false;
            } else if ("".equals(o)) {
                return false;
            } else if (o instanceof Collection) {
                return !((Collection<?>)o).isEmpty();
            } else if(o instanceof Map) {
                return !((Map<?,?>)o).isEmpty();
            }
        }
        return true;
    }

    private static Predicate<TimedValue<?>> checkString(Function<TimedValue<?>,Object> getterFunction,
            Predicate<String> check) {
        return t -> {
            if(t == null) return false;

            Object v  = getterFunction.apply(t);
            if(v == null) return false;

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
            return result;
        };
    }

    @Override
    public Predicate<GeoJsonObject> getLocationFilter() {
        if(rs.location != null && !rs.location.isEmpty()) {
            LOG.warn("Location filtering is not yet implemented for Resource Selectors.");
        }
        return null;
    }

    @Override
    public Predicate<ProviderSnapshot> getProviderFilter() {
        return providerFilter;
    }

    @Override
    public Predicate<ServiceSnapshot> getServiceFilter() {
        return serviceFilter;
    }

    @Override
    public Predicate<ResourceSnapshot> getResourceFilter() {
        return resourceFilter;
    }

    @Override
    public ResourceValueFilter getResourceValueFilter() {
        return rs.value == null || rs.value.isEmpty() ? null :
            (p, l) -> {
                return (providerFilter == null || providerFilter.test(p)) &&
                        l.stream().anyMatch(r -> (serviceFilter == null || serviceFilter.test(r.getService()))
                                && (resourceFilter == null || resourceFilter.test(r))
                                && (valueFilter == null || valueFilter.test(r.getValue())));
            };
    }

    @Override
    public ICriterion negate() {
        ResourceSelector neg = new ResourceSelector();
        neg.model = negateSelection(rs.model);
        neg.provider = negateSelection(rs.provider);
        neg.service = negateSelection(rs.service);
        neg.resource = negateSelection(rs.resource);
        neg.value =  rs.value == null ? null :
            rs.value.stream().map(this::negateValueSelection)
                .collect(Collectors.toList());
        neg.location =  rs.location == null ? null :
            rs.location.stream().map(this::negateLocationSelection)
            .collect(Collectors.toList());
        return new ResourceSelectorCriterion(neg, allowSingleLevelWildcards);
    }

    private Selection negateSelection(Selection s) {
        if(s == null) return null;
        Selection neg = new Selection();
        neg.type = s.type;
        neg.value = s.value;
        neg.negate = !s.negate;
        return neg;
    }

    private ValueSelection negateValueSelection(ValueSelection s) {
        ValueSelection neg = new ValueSelection();
        neg.operation = s.operation;
        neg.value = s.value;
        neg.checkType = s.checkType;
        neg.negate = !s.negate;
        return neg;
    }

    private LocationSelection negateLocationSelection(LocationSelection s) {
        LocationSelection neg = new LocationSelection();
        neg.type = s.type;
        neg.value = s.value;
        neg.radius = s.radius;
        neg.negate = !s.negate;
        return neg;
    }

    @Override
    public List<String> dataTopics() {
        Stream<Selection> topicSegments = Stream.of(rs.model,
                rs.provider, rs.service, rs.resource);
        Predicate<String> isMultiWildcard = "*"::equals;

        final AtomicBoolean stopStream = new AtomicBoolean(false);

        String topic = topicSegments.map(this::getTopicSegment)
            .takeWhile(s -> stopStream.compareAndSet(false, isMultiWildcard.test(s)))
            .collect(Collectors.joining("/", "DATA/", ""));

        return List.of(topic);
    }

    private String getTopicSegment(Selection s) {
        if(s != null && s.type == MatchType.EXACT && s.negate == false) {
            return s.value;
        }
        return allowSingleLevelWildcards ? "+" : "*";
    }

    @Override
    public Predicate<ResourceDataNotification> dataEventFilter() {
        // TODO Auto-generated method stub
        return ICriterion.super.dataEventFilter();
    }
}
