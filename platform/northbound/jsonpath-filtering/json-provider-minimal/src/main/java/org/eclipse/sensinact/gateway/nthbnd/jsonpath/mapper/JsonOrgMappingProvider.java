/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.nthbnd.jsonpath.mapper;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.mapper.MappingException;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class JsonOrgMappingProvider implements MappingProvider {
    private static Map<Class, Converter> DEFAULT = new HashMap<Class, Converter>();

    static {
        DEFAULT.put(Long.class, new LongConverter());
        DEFAULT.put(long.class, new LongConverter());
        DEFAULT.put(Integer.class, new IntegerConverter());
        DEFAULT.put(int.class, new IntegerConverter());
        DEFAULT.put(Double.class, new DoubleConverter());
        DEFAULT.put(double.class, new DoubleConverter());
        DEFAULT.put(Float.class, new FloatConverter());
        DEFAULT.put(float.class, new FloatConverter());
        DEFAULT.put(BigDecimal.class, new BigDecimalConverter());
        DEFAULT.put(String.class, new StringConverter());
        DEFAULT.put(Date.class, new DateConverter());
        DEFAULT.put(BigInteger.class, new BigIntegerConverter());
        DEFAULT.put(boolean.class, new BooleanConverter());
    }

    public interface Converter<T> {
        T convert(Object o);
    }

    private static class StringConverter implements Converter<String> {
        public String convert(Object src) {
            if (src == null) {
                return null;
            }
            return src.toString();
        }
    }

    private static class IntegerConverter implements Converter<Integer> {
        public Integer convert(Object src) {
            if (src == null) {
                return null;
            }
            if (Integer.class.isAssignableFrom(src.getClass())) {
                return (Integer) src;
            } else if (Long.class.isAssignableFrom(src.getClass())) {
                return ((Long) src).intValue();
            } else if (Double.class.isAssignableFrom(src.getClass())) {
                return ((Double) src).intValue();
            } else if (BigDecimal.class.isAssignableFrom(src.getClass())) {
                return ((BigDecimal) src).intValue();
            } else if (Float.class.isAssignableFrom(src.getClass())) {
                return ((Float) src).intValue();
            } else if (String.class.isAssignableFrom(src.getClass())) {
                return Integer.valueOf(src.toString());
            }
            throw new MappingException("can not map a " + src.getClass() + " to " + Integer.class.getName());
        }
    }

    private static class LongConverter implements Converter<Long> {
        public Long convert(Object src) {
            if (src == null) {
                return null;
            }
            if (Long.class.isAssignableFrom(src.getClass())) {
                return (Long) src;
            } else if (Integer.class.isAssignableFrom(src.getClass())) {
                return ((Integer) src).longValue();
            } else if (Double.class.isAssignableFrom(src.getClass())) {
                return ((Double) src).longValue();
            } else if (BigDecimal.class.isAssignableFrom(src.getClass())) {
                return ((BigDecimal) src).longValue();
            } else if (Float.class.isAssignableFrom(src.getClass())) {
                return ((Float) src).longValue();
            } else if (String.class.isAssignableFrom(src.getClass())) {
                return Long.valueOf(src.toString());
            }
            throw new MappingException("can not map a " + src.getClass() + " to " + Long.class.getName());
        }
    }

    private static class DoubleConverter implements Converter<Double> {
        public Double convert(Object src) {
            if (src == null) {
                return null;
            }
            if (Double.class.isAssignableFrom(src.getClass())) {
                return (Double) src;
            } else if (Integer.class.isAssignableFrom(src.getClass())) {
                return ((Integer) src).doubleValue();
            } else if (Long.class.isAssignableFrom(src.getClass())) {
                return ((Long) src).doubleValue();
            } else if (BigDecimal.class.isAssignableFrom(src.getClass())) {
                return ((BigDecimal) src).doubleValue();
            } else if (Float.class.isAssignableFrom(src.getClass())) {
                return ((Float) src).doubleValue();
            } else if (String.class.isAssignableFrom(src.getClass())) {
                return Double.valueOf(src.toString());
            }
            throw new MappingException("can not map a " + src.getClass() + " to " + Double.class.getName());
        }
    }

    private static class FloatConverter implements Converter<Float> {
        public Float convert(Object src) {
            if (src == null) {
                return null;
            }
            if (Float.class.isAssignableFrom(src.getClass())) {
                return (Float) src;
            } else if (Integer.class.isAssignableFrom(src.getClass())) {
                return ((Integer) src).floatValue();
            } else if (Long.class.isAssignableFrom(src.getClass())) {
                return ((Long) src).floatValue();
            } else if (BigDecimal.class.isAssignableFrom(src.getClass())) {
                return ((BigDecimal) src).floatValue();
            } else if (Double.class.isAssignableFrom(src.getClass())) {
                return ((Double) src).floatValue();
            } else if (String.class.isAssignableFrom(src.getClass())) {
                return Float.valueOf(src.toString());
            }
            throw new MappingException("can not map a " + src.getClass() + " to " + Float.class.getName());
        }
    }

    private static class BigDecimalConverter implements Converter<BigDecimal> {
        public BigDecimal convert(Object src) {
            if (src == null) {
                return null;
            }
            return new BigDecimal(src.toString());
        }
    }

    private static class BigIntegerConverter implements Converter<BigInteger> {
        public BigInteger convert(Object src) {
            if (src == null) {
                return null;
            }
            return new BigInteger(src.toString());
        }
    }

    private static class DateConverter implements Converter<Date> {
        public Date convert(Object src) {
            if (src == null) {
                return null;
            }
            if (Date.class.isAssignableFrom(src.getClass())) {
                return (Date) src;
            } else if (Long.class.isAssignableFrom(src.getClass())) {
                return new Date((Long) src);
            } else if (String.class.isAssignableFrom(src.getClass())) {
                try {
                    return DateFormat.getInstance().parse(src.toString());
                } catch (ParseException e) {
                    throw new MappingException(e);
                }
            }
            throw new MappingException("can not map a " + src.getClass() + " to " + Date.class.getName());
        }
    }

    private static class BooleanConverter implements Converter<Boolean> {
        public Boolean convert(Object src) {
            if (src == null) {
                return null;
            }
            if (Boolean.class.isAssignableFrom(src.getClass())) {
                return (Boolean) src;
            }
            throw new MappingException("can not map a " + src.getClass() + " to " + Boolean.class.getName());
        }
    }

    @Override
    public <T> T map(Object source, TypeRef<T> targetType, Configuration configuration) {
        throw new UnsupportedOperationException("JsonOrg provider does not support TypeRef! Use a Jackson or Gson based provider");
    }

    private Object mapToObject(Object source) {
        if (source instanceof List) {
            List<Object> mapped = new ArrayList<Object>();
            List array = (List) source;
            for (int i = 0; i < array.size(); i++) {
                mapped.add(mapToObject(array.get(i)));
            }
            return mapped;
        } else if (source instanceof Map) {
            Map<String, Object> mapped = new HashMap<String, Object>();
            Map obj = (Map) source;
            Iterator<?> iterator = obj.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next().toString();
                mapped.put(key, mapToObject(obj.get(key)));
            }
            return mapped;
        } else {
            return source;
        }
    }

    @Override
    public <T> T map(Object source, Class<T> targetType, Configuration configuration) {

        if (source == null) {
            return null;
        }
        if (targetType.isAssignableFrom(source.getClass())) {
            return (T) source;
        }
        if (targetType.equals(Object.class) || targetType.equals(List.class) || targetType.equals(Map.class)) {
            return (T) mapToObject(source);
        }
        if (!configuration.jsonProvider().isMap(source) && !configuration.jsonProvider().isArray(source)) {
            return (T) DEFAULT.get(targetType).convert(source);
        }
        return (T) source;
    }
}
