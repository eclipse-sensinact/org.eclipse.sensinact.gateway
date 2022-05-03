/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Bundle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reflection helpers
 */
public abstract class ReflectUtils {
    private static final Logger LOGGER = Logger.getLogger(ReflectUtils.class.getCanonicalName());

    /**
     * Returns {@link Method} contained by the {@link Collection}
     * passed as parameter whose signature is the same than the one of
     * the {@link Method} also passed as parameter. Returns null if
     * the {@link Collection} does not contain such a {@link Method}
     *
     * @param method  the {@link Method} whose signature has to be searched
     *                in the specified {@link Collection}
     * @param methods the {@link Collection} of {@link Method} in which search the
     *                signature
     * @return the {@link Method} contained by the {@link Collection} whose
     * signature is the same than the one of the specified
     * {@link Method}
     */
    public static Method containsSignature(Method method, Collection<Method> methods) {
        if (methods == null || methods.isEmpty()) {
            return null;
        }
        Iterator<Method> iterator = methods.iterator();
        while (iterator.hasNext()) {
            Method containedMethod = iterator.next();
            if (signatureEquals(method, containedMethod)) {
                return containedMethod;
            }
        }
        return null;
    }

    /**
     * Returns true is the signatures of the two {@link Method}s
     * passed as parameters are the same ; returns false
     * otherwise
     *
     * @param first  the first of the two {@link Method}s to compare
     *               the signatures of
     * @param second the second of the two {@link Method}s to compare
     *               the signatures of
     * @return true is the signatures of the two {@link Method}s
     * are the same; <br/>false otherwise
     */
    public static boolean signatureEquals(Method first, Method second) {
        if (first == null || second == null) {
            return false;
        }
        if (!first.getName().equals(second.getName())) {
            return false;
        }
        Class<?>[] firstParameterTypes = first.getParameterTypes();
        Class<?>[] secondParameterTypes = second.getParameterTypes();

        if (firstParameterTypes.length != secondParameterTypes.length) {
            return false;
        }
        int index = 0;
        for (; (index < firstParameterTypes.length) && (firstParameterTypes[index] == secondParameterTypes[index]); index++)
            ;
        return (index == firstParameterTypes.length);
    }

    /**
     * Returns as a list the hierarchy of interfaces implemented by the
     * bottomClass class argument which will be included as the bottom
     * of the hierarchy if it is an interface.
     *
     * @param bottomClass the class from which retrieve the list of implemented
     *                    interfaces
     * @return an ordered list of all interfaces the bottomClass implements
     */
    public static LinkedList<Class<?>> getOrderedImplementedInterfaces(Class<?> bottomClass) {
        LinkedList<Class<?>> list = new LinkedList<Class<?>>();

        if (bottomClass == null) {
            return list;
        }
        if (bottomClass.isInterface()) {
            list.offer(bottomClass);

        } else {
            list = getOrderedImplementedInterfaces(bottomClass.getSuperclass());
        }
        Class<?>[] interfaces = bottomClass.getInterfaces();
        if (interfaces != null) {
            int index = 0;
            for (; index < interfaces.length; index++) {
                LinkedList<Class<?>> inheritedList = getOrderedImplementedInterfaces(interfaces[index]);
                while (!inheritedList.isEmpty()) {
                    Class<?> inheritedClass = inheritedList.removeFirst();
                    if (!list.contains(inheritedClass)) {
                        list.offer(inheritedClass);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Returns the hierarchy of interfaces extending the referenceInterface class
     * argument (which will be included as the top of the hierarchy)as an ordered
     * list, beginning from the specified bottomClass class argument which will be
     * included as the bottom of the hierarchy if it is  an interface.
     *
     * @param referenceInterface the interface for which to retrieve all extending interface
     *                           implemented by the bottomClass class argument
     * @param bottomClass        the class from which retrieve the list of implemented
     *                           extended referenceInterface interfaces
     * @return an ordered list of all extended referenceInterface interfaces
     * the bottomClass implements
     */
    public static <C> LinkedList<Class<C>> getOrderedImplementedInterfaces(Class<C> referenceInterface, Class<?> bottomClass) {
        LinkedList<Class<C>> list = new LinkedList<Class<C>>();

        if (referenceInterface == null || !referenceInterface.isInterface() || bottomClass == null || !referenceInterface.isAssignableFrom(bottomClass)) {
            return list;
        }
        if (bottomClass.isInterface()) {
            list.offer((Class<C>) bottomClass);

        } else {
            list = getOrderedImplementedInterfaces(referenceInterface, bottomClass.getSuperclass());
        }
        Class<?>[] interfaces = bottomClass.getInterfaces();
        if (interfaces != null) {
            int index = 0;
            for (; index < interfaces.length; index++) {
                if (referenceInterface.isAssignableFrom(interfaces[index])) {
                    LinkedList<Class<C>> inheritedList = getOrderedImplementedInterfaces(referenceInterface, interfaces[index]);
                    while (!inheritedList.isEmpty()) {
                        Class<?> inheritedClass = inheritedList.removeFirst();
                        if (!list.contains(inheritedClass)) {
                            list.offer((Class<C>) inheritedClass);
                        }
                    }
                }
            }
        }
        return list;
    }

    /**
     * Searches and returns the value of the constant whose name
     * is passed as parameter in an interface hierarchy specified
     * as an ordered list of classes. If the fromTop argument is
     * set to true the constant value is searched from the top
     * of the hierarchy to the bottom; otherwise it is searched
     * from the bottom to the top of the hierarchy
     *
     * @param list         an interface hierarchy specified as an ordered
     *                     list of classes in which to search for the constant
     *                     value
     * @param constantName the name of the constant to retrieve the value of
     * @param fromTop      <ul>
     *                     <li>true if the constant value must be searched
     *                     from the top to the bottom of the hierarchy</li>
     *                     <li>false if the constant value must be searched
     *                     from the bottom to the top of the hierarchy</li>
     *                     </ul>
     * @return the value of the constant or null if the value cannot
     * be found
     */
    public static <C, T> T getConstantValue(Deque<Class<C>> list, String constantName, boolean fromTop) {
        T constantValue = null;
        Iterator<Class<C>> iterator = fromTop ? list.descendingIterator() : list.iterator();
        while (iterator.hasNext()) {
            Class<C> resourceInterface = iterator.next();
            constantValue = ReflectUtils.<C, T>getConstantValue(resourceInterface, constantName);
            if (constantValue != null) {
                break;
            }
        }
        return constantValue;
    }

    /**
     * Searches and returns the value of the constant whose name
     * is passed as parameter in the Class also passed as parameter
     *
     * @param clazz        Class where to search for the constant value
     * @param constantName the name of the constant to retrieve the value of
     * @return the value of the constant or null if the value cannot
     * be found
     */
    @SuppressWarnings({"unchecked"})
    public static <C, T> T getConstantValue(Class<C> clazz, String constantName) {
        T constantValue = null;
        try {
            constantValue = (T) clazz.getField(constantName).get(null);
        } catch (Exception e) {
            //do nothing;
        }
        return constantValue;
    }

    /**
     * Returns the map of {@link Field}s of the {@link Class} passed
     * as parameter mapped to {@link Annotation} instances whose type is
     * the same as the annotationClass argument.
     *
     * @param annotated       the {@link Class} in which to search properly annotated
     *                        fields
     * @param annotationClass the expected {@link Annotation} type
     * @return the map of fields of the specified class mapped to
     * associated annotations
     */
    public static <A extends Annotation> Map<Field, A> getAnnotatedFields(Class<?> annotated, Class<A> annotationClass) {
        if (annotated == null || annotationClass == null) {
            return Collections.<Field, A>emptyMap();
        }
        Map<Field, A> annotatedFields = new HashMap<Field, A>();

        Field[] fields = annotated.getDeclaredFields();
        A annotation = null;

        int index = 0;
        int length = fields == null ? 0 : fields.length;
        for (; index < length; index++) {
            if ((annotation = fields[index].getAnnotation(annotationClass)) != null) {
                annotatedFields.put(fields[index], annotation);
                annotation = null;
            }
        }
        return annotatedFields;
    }

    /**
     * Returns the map of {@link Method}s of the {@link Class} passed
     * as parameter mapped to {@link Annotation} instances whose type is
     * the same as the annotationClass argument. If the method is not
     * annotated but overwrites an annotated one, it is mapped to the
     * annotation instance of its overwritten counterpart. Synthetic
     * and Bridge methods are excluded from the research.
     *
     * @param annotated       the {@link Class} in which to search properly annotated
     *                        methods
     * @param annotationClass the expected {@link Annotation} type
     * @return the map of methods of the specified class mapped to
     * associated annotations
     */
    public static <A extends Annotation> Map<Method, A> getAnnotatedMethods(Class<?> annotated, Class<A> annotationClass) {
        if (annotated == null || annotationClass == null) {
            return Collections.<Method, A>emptyMap();
        }
        Map<Method, A> annotatedMethods = new HashMap<Method, A>();
        ReflectUtils.getAnnotatedMethods(annotated, annotationClass, annotatedMethods);
        return annotatedMethods;
    }

    /**
     * Feeds the map passed as parameter with {@link Method}s of the
     * annotated {@link Class} argument mapped to annotationClass
     * {@link Annotation} instances annotating them. If a method is not
     * annotated but overwrites/implements an annotated one, it is mapped
     * to the annotation instance of its overwritten/implemented
     * super-class or interface method counterpart. Synthetic and Bridge
     * methods are excluded from the research.
     *
     * @param annotated       the {@link Class} in which to search properly annotated
     *                        methods
     * @param annotationClass the expected {@link Annotation} type
     * @param map             the map of methods of the specified class mapped to
     *                        annotations of the specified type annotating them
     */
    private static final <A extends Annotation> void getAnnotatedMethods(Class<?> annotated, Class<A> annotationClass, Map<Method, A> map) {
        if (annotated == null) {
            return;
        }
        Method[] methods = annotated.getDeclaredMethods();
        Set<Method> methodsSet = map.keySet();

        int index = 0;
        A annotation = null;
        for (; index < methods.length; index++) {
            if (methods[index].isSynthetic() || methods[index].isBridge() || containsSignature(methods[index], methodsSet) != null) {
                continue;
            }
            if ((annotation = ReflectUtils.getAnnotation(methods[index], annotationClass)) != null) {
                map.put(methods[index], annotation);
            }
        }
        getAnnotatedMethods(annotated.getSuperclass(), annotationClass, map);
    }


    /**
     * Returns the {@link Annotation} instance of the type passed as
     * parameter, annotating the {@link Method} also passed as
     * parameter or its overwritten/implemented counterpart from
     * a super-class or an interface
     *
     * @param method          the method where to search for the {@link Annotation}
     * @param annotationClass the type of the {@link Annotation} to search for
     * @return the {@link Annotation} instance annotating the {@link
     * Method} or its overwritten/implemented counterpart from
     * a super-class or an interface
     */
    public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationClass) {
        A annotation = retrieveInheritedAnnotation(method, annotationClass);

        if (annotation == null) {
            LinkedList<Class<?>> implementedInterfaces = ReflectUtils.getOrderedImplementedInterfaces(method.getDeclaringClass());

            Iterator<Class<?>> iterator = implementedInterfaces.iterator();
            while (iterator.hasNext()) {
                Method implementedMethod = ReflectUtils.containsSignature(method, Arrays.<Method>asList(iterator.next().getDeclaredMethods()));

                if (implementedMethod != null && (annotation = implementedMethod.getAnnotation(annotationClass)) != null) {
                    break;
                }
            }
        }
        return annotation;
    }

    /**
     * Returns the {@link Annotation} instance of the type passed as
     * parameter, annotating the {@link Method} also passed as
     * parameter or its overwritten counterpart from a super-class
     *
     * @param method          the method where to search for the {@link Annotation}
     * @param annotationClass the type of the {@link Annotation} to search the instance
     *                        of
     * @return the {@link Annotation} instance annotating the {@link
     * Method} or its overwritten counterpart from a super-class
     */
    private static final <A extends Annotation> A retrieveInheritedAnnotation(Method method, Class<A> annotationClass) {
        if (method == null || annotationClass == null) {
            return null;
        }
        A annotation = method.getAnnotation(annotationClass);
        if (annotation == null) {
            annotation = retrieveInheritedAnnotation(overwritten(method), annotationClass);
        }
        return annotation;
    }

    /**
     * Returns true if the {@link Method} passed as
     * parameter is valid according to the other
     * specified parameter :
     * <ul>
     * <li>the same returned type or void if the
     * acceptVoid argument is set to true</li>
     * <li>the same name if the strict argument
     * is set to true</li>
     * <li>the same parameter types</li>
     * <lu>
     *
     * @param method         the method to validate
     * @param returnedType   the returned type
     * @param methodName     the method name
     * @param parameterTypes the parameter classes array
     * @param acceptVoid     defines if the returned type as to
     *                       be the same as the specified one or if
     *                       a Void returned type is allowed
     * @param strict         defines if the method's name as to be
     *                       the same as the specified one
     * @return true if the method is valid; returns
     * false otherwise
     */
    public static boolean validMethod(Method method, Class<?> returnedType, String methodName, Class<?>[] parameterTypes, boolean acceptVoid, boolean strict) {
        if (method == null) {
            return false;
        }
        Class<?>[] methodParameterTypes = method.getParameterTypes();
        int parametersLength = parameterTypes == null ? 0 : parameterTypes.length;

        if ((parametersLength != methodParameterTypes.length) || (strict && (methodName == null || !methodName.equals(method.getName()))) || ((returnedType == null || !returnedType.isAssignableFrom(method.getReturnType())) && (!acceptVoid || !method.getReturnType().equals(Void.TYPE)))) {
            return false;
        }
        int parameterIndex = 0;
        for (; parameterIndex < parametersLength && (methodParameterTypes[parameterIndex].isAssignableFrom(parameterTypes[parameterIndex])); parameterIndex++)
            ;

        if (parameterIndex != parametersLength) {
            return false;
        }
        return true;
    }

    /**
     * Returns the first {@link Method} validating the
     * specified parameters in the array of the ones
     * passed as parameter
     *
     * @param methods        the array of {@link Method}
     * @param returnedType   the returned type
     * @param parameterTypes the parameter classes array
     * @param acceptVoid     defines if the returned type as to
     *                       be the same as the specified one or if
     *                       a Void returned type is allowed
     * @return the first method validing the parameters
     */
    public static Method getDeclaredMethod(Method[] methods, Class<?> returnedType, Class<?>[] parameterTypes, boolean acceptVoid) {
        int index = 0;
        int length = methods == null ? 0 : methods.length;

        Method method = null;

        for (; index < length; index++) {
            if (validMethod(methods[index], returnedType, null, parameterTypes, acceptVoid, false)) {
                method = methods[index];
                break;
            }
        }
        return method;
    }

    /**
     * Returns the first {@link Method} validating the
     * specified parameters in the array of the ones
     * passed as parameter
     *
     * @param methods        the array of {@link Method}
     * @param returnedType   the returned type
     * @param methodName     the method's name
     * @param parameterTypes the parameter classes array
     * @param acceptVoid     defines if the returned type as to
     *                       be the same as the specified one or if
     *                       a Void returned type is allowed
     * @return the first method validing the parameters
     */
    public static Method getDeclaredMethod(Method[] methods, Class<?> returnedType, String methodName, Class<?>[] parameterTypes, boolean acceptVoid) {
        int index = 0;
        int length = methods == null ? 0 : methods.length;

        Method method = null;

        for (; index < length; index++) {
            if (validMethod(methods[index], returnedType, methodName, parameterTypes, acceptVoid, methodName != null)) {
                method = methods[index];
                break;
            }
        }
        return method;
    }

    /**
     * Returns the first {@link Method} validating the
     * specified parameters in the set of the ones declared
     * for the type passed as parameter
     *
     * @param declaringClass the type in which to search the
     *                       valid {@link Method}
     * @param returnedType   the returned type
     * @param methodName     the method's name
     * @param parameterTypes the parameter classes array
     * @param acceptVoid     defines if the returned type as to
     *                       be the same as the specified one or if
     *                       a Void returned type is allowed
     * @return the first method validing the parameters
     */
    public static Method getDeclaredMethod(Class<?> declaringClass, Class<?> returnedType, String methodName, Class<?>[] parameterTypes, boolean acceptVoid) {
        if (declaringClass == null || methodName == null || (returnedType == null && acceptVoid == false)) {
            return null;
        }
        if (returnedType == null) {
            returnedType = Void.class;
        }
        Method method = getDeclaredMethod(declaringClass.getMethods(), returnedType, methodName, parameterTypes, acceptVoid);

        if (method == null) {
            method = getDeclaredMethod(declaringClass.getDeclaredMethods(), returnedType, methodName, parameterTypes, acceptVoid);
        }
        return method;
    }


    /**
     * Returns the {@link Method} instance the one passed
     * as parameter overwrites if it exists; otherwise
     * returns null
     *
     * @param method the method or which to retrieve the overwritten
     *               one
     * @return the overwritten method or null if the specified
     * method does not overwrite a super class one
     */
    public static Method overwritten(Method method) {
        Method overwritten = null;
        if (method != null) {
            String methodName = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> declaringClass = method.getDeclaringClass().getSuperclass();
            
            while (overwritten == null && declaringClass != null) {
                try {
                    overwritten = declaringClass.getDeclaredMethod(methodName, parameterTypes);
                } catch (Exception e) { }
                declaringClass = declaringClass.getSuperclass();
            }
        }
        return overwritten;
    }

    /**
     * Returns a new instance of the implementation class passed
     * as parameter using the parameters array as argument
     *
     * @param baseClass           the class which the implementation one
     *                            as to extend
     * @param implementationClass the class to instantiate
     * @param parameters          the objects array parameters to use
     * @return a new instance of the implementation class
     */
    public static <E, F> F getInstance(Class<E> baseClass, Class<F> implementationClass, Object... parameters) {

    	if (baseClass == implementationClass) {
            return ReflectUtils.<F>getInstance(implementationClass, parameters);
        }
        F instance = null;

        try {
            if (implementationClass != null && baseClass != null && baseClass.isAssignableFrom(implementationClass)) {
                if (parameters == null || parameters.length == 0) {
                    instance = implementationClass.newInstance();

                } else {
                    @SuppressWarnings("unchecked") Constructor<F>[] constructors = (Constructor<F>[]) implementationClass.getDeclaredConstructors();

                    for (Constructor<F> constructor : constructors) {
                        Class<?>[] parameterTypes = null;

                        if ((parameterTypes = constructor.getParameterTypes()).length == parameters.length) {
                            int index = 0;

                            for (; index < parameterTypes.length && (parameters[index] == null || parameterTypes[index].isAssignableFrom(parameters[index].getClass())); index++)
                                ;

                            if (index == parameterTypes.length) {
                                constructor.setAccessible(true);
                                instance = constructor.newInstance(parameters);
                                break;
                            }
                        }
                    }
                }
            } else {
                LOGGER.log(Level.CONFIG, baseClass.getName() + " is not assignable from " + implementationClass.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.CONFIG, e.getMessage(), e);
        }
        return instance;
    }

    /**
     * Returns a new instance of the implementation class passed
     * as parameter using the parameters array as argument
     *
     * @param implementationClass the class to instantiate
     * @return a new instance of the implementation class
     */
    public static <F> F getInstance(Class<F> implementationClass, Object[] parameters) {
        F instance = null;
        try {
            @SuppressWarnings("unchecked") Constructor<F>[] constructors = (Constructor<F>[]) implementationClass.getDeclaredConstructors();

            for (Constructor<F> constructor : constructors) {
                Class<?>[] parameterTypes = null;

                if ((parameterTypes = constructor.getParameterTypes()).length == parameters.length) {
                    int index = 0;

                    for (; index < parameterTypes.length && (parameters[index] == null || parameterTypes[index].isAssignableFrom(parameters[index].getClass())); index++)
                        ;

                    if (index == parameterTypes.length) {
                        constructor.setAccessible(true);
                        instance = constructor.newInstance(parameters);
                        break;
                    }
                }
            }
            if (instance == null) {
                instance = implementationClass.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.CONFIG, e.getMessage(), e);
        }
        return instance;
    }

    /**
     * Returns a new instance of the implementation class passed
     * as parameter using the parameters array as argument
     *
     * @param implementationClass the class to instantiate
     * @return a new instance of the implementation class
     */
    public static <F> F getTheBestInstance(Class<F> clazz, Object[] parameters) {
        F instance = null;
        Constructor<?>[] constructors = clazz.getConstructors();
        int index = constructors.length - 1;

        //order constructors by decreasing arguments number
        //to use the maximum number of passed parameters
        for (; index >= 0; index--) {
            int pos = index;
            Constructor<?> current = constructors[index];
            while (pos > 0) {
                Constructor<?> previous = constructors[pos - 1];
                if (previous.getParameterTypes().length < current.getParameterTypes().length) {
                    constructors[pos] = previous;
                    constructors[pos - 1] = current;
                    pos--;
                    continue;
                }
                break;
            }
        }
        index = 0;
        int position = 0;
        for (; index < constructors.length; index++) {
            Class<?>[] parameterTypes = constructors[index].getParameterTypes();
            if (parameterTypes.length > parameters.length) {
                continue;
            }
            Object[] params = new Object[parameterTypes.length];

            int paramsIndex = 0;
            int parametersIndex = 0;
            int parametersLength = parameters == null ? 0 : parameters.length;

            for (; parametersIndex < parametersLength && paramsIndex < params.length; parametersIndex++) {
                Object parameter = parameters[parametersIndex];
                if (parameter != null && parameterTypes[paramsIndex].isAssignableFrom(parameter.getClass())) {
                    params[paramsIndex++] = parameter;
                }
            }
            if (paramsIndex == parameterTypes.length) {
                try {
                    instance = (F) constructors[index].newInstance(params);
                    break;
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return instance;
    }

    /**
     * Instantiate the Java object described
     * by the JSON array passed as parameter
     *
     * @param clazz      the Java type to instantiate
     * @param jsonObject the JSON array describing the java object
     *                   to instantiate
     * @return the Java object
     */
    public static <E extends Object, T> T instantiate(Class<T> clazz, JSONArray jsonObject) {
        T instance = null;

        if (jsonObject == null) {
            return instance;
        }
        Constructor<T> constructor = null;
        try {
            constructor = clazz.getConstructor(JSONArray.class);
            instance = constructor.newInstance(jsonObject);

        } catch (Exception e) {
            try {
                constructor = clazz.getConstructor(new Class[]{JSONArray.class});
                instance = constructor.newInstance(new Object[]{jsonObject});

            } catch (Exception ex) {
                LOGGER.log(Level.CONFIG, e.getMessage(), e);
            }
        }
        if (instance != null) {
            return instance;
        }
        Object[][] parameters = null;
        int length = jsonObject.length();
        parameters = new Object[length][2];
        for (int i = 0; i < length; i++) {
            parameters[i][0] = null;
            parameters[i][1] = jsonObject.get(i);
        }
        return newInstance(clazz, parameters);
    }

    /**
     * Instantiate the Java object described by the JSON object
     * (JSONObject or JSONArray ) passed as parameter
     *
     * @param clazz      the Java type to instantiate
     * @param jsonObject the JSON object describing the java one to instantiate
     * @return the Java object
     */
    public static <E extends Object, T> T instantiate(Class<T> clazz, JSONObject jsonObject) {
        T instance = null;
        if (jsonObject == null) {
            return instance;
        }
        Constructor<T> constructor = null;
        try {
            constructor = clazz.getConstructor(JSONObject.class);
            instance = constructor.newInstance(jsonObject);
        } catch (Exception e) {
            try {
                constructor = clazz.getConstructor(new Class[]{JSONObject.class});
                instance = constructor.newInstance(new Object[]{jsonObject});

            } catch (Exception ex) {
                LOGGER.log(Level.CONFIG, e.getMessage(), e);
            }
        }
        if (instance != null) {
            return instance;
        }
        Object[][] parameters = null;
        String[] names = JSONObject.getNames(jsonObject);
        int length = names.length;
        parameters = new Object[length][2];
        for (int i = 0; i < length; i++) {
            parameters[i][0] = names[i];
            parameters[i][1] = (jsonObject).get(names[i]);
        }
        return newInstance(clazz, parameters);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final <E extends Object, T> T newInstance(Class<T> clazz, Object[][] parameters) {
        T instance = null;

        //orders public constructors according to their
        //number of parameters
        Constructor[] constructors = clazz.getConstructors();
        int index = constructors.length - 1;
        for (; index >= 0; index--) {
            int pos = index;
            Constructor current = constructors[index];

            while (pos > 0) {
                Constructor previous = constructors[pos - 1];
                if (previous.getParameterTypes().length < current.getParameterTypes().length) {
                    constructors[pos] = previous;
                    constructors[pos - 1] = current;
                    pos--;
                    continue;
                }
                break;
            }
        }
        index = constructors.length - 1;
        int position = 0;
        for (; index >= 0; index--) {
            if (constructors[index].getParameterTypes().length > parameters.length) {
                continue;
            }
            Class<?>[] parameterTypes = constructors[index].getParameterTypes();
            Object[] params = new Object[parameterTypes.length];
            int typeIndex = 0;
            for (; typeIndex < parameterTypes.length; typeIndex++) {
                Object parameter = CastUtils.getObjectFromJSON(parameterTypes[typeIndex], parameters[typeIndex][1]);

                if (parameter == null && !JSONObject.NULL.equals(parameters[typeIndex][1])) {
                    params = null;
                    break;
                }
                params[typeIndex] = parameter;
            }
            if (params != null) {
                try {
                    instance = (T) constructors[index].newInstance(params);
                    position = (typeIndex + 1);
                    break;

                } catch (Exception e) {
                    continue;
                }
            }
        }
        if (instance != null && position < parameters.length && parameters[position][0] != null) {
            for (; position < parameters.length; position++) {
                try {
                    Field field = clazz.getDeclaredField((String) parameters[position][0]);
                    Object value = parameters[position][1];

                    if (value == null || (field.getModifiers() & Modifier.FINAL) == Modifier.FINAL || (field.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
                        continue;
                    }
                    field.setAccessible(true);
                    if (List.class.isAssignableFrom(field.getType())) {
                        field.set(instance, CastUtils.toList((Class<List<E>>) field.getType(), (Class<E>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0], (JSONArray) value));

                    } else if (Map.class.isAssignableFrom(field.getType())) {
                        field.set(instance, CastUtils.toMap((Class<Map<String, E>>) field.getType(), (Class<E>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1], (JSONObject) value));
                    } else {
                        field.set(instance, CastUtils.getObjectFromJSON(field.getType(), value));
                    }
                } catch (Exception exception) {
                    LOGGER.log(Level.CONFIG, exception.getMessage(), exception);
                    continue;
                }
            }
        }
        return instance;
    }

    /**
     * Returns the array of types from the {@link Bundle} passed as
     * parameter for which the one also passed as parameter is
     * assignable to
     *
     * @param type   the type to find the ones that it is assignable to
     * @param bundle the {@link Bundle} to search for the types in
     * @return the array of types from the specified {@link Bundle}
     * assignable to specified one
     */
    public static List<Class<?>> getAssignableTypes(Class<?> type, Bundle bundle) {
        return ReflectUtils.getAssignableTypes(type, ReflectUtils.getAllTypes(bundle));
    }

    /**
     * Returns the array of types from the List of Class passed as
     * parameter for which the type also passed as parameter is
     * assignable to
     *
     * @param type    the type to find the ones that it is assignable to
     * @param classes the List of Class to search in
     * @return the array of types from the specified List of Class
     * assignable to specified one
     */
    public static List<Class<?>> getAssignableTypes(Class<?> type, List<Class<?>> classes) {
        List<Class<?>> assignables = new ArrayList<Class<?>>();
        Iterator<Class<?>> iterator = classes.iterator();
        while (iterator.hasNext()) {
            Class<?> clazz = iterator.next();
            if (type.isAssignableFrom(clazz)) {
                assignables.add(clazz);
            }
        }
        return assignables;
    }

    /**
     * Returns the array of existing types from the Bundle
     * passed as  parameter
     *
     * @param bundle the Bundle to search for the types in
     * @return the array of types from the specified Bundle
     */
    public static List<String> getAllStringTypes(Bundle bundle) {
        List<String> types = new ArrayList<String>();

        Enumeration<URL> entries = bundle.findEntries("/", "*.class", true);

        if (entries != null) {
            while (entries.hasMoreElements()) {
                String classname = entries.nextElement().getPath();
                int startIndex = 0;
                int endIndex = classname.length() - ".class".length();
                startIndex += classname.startsWith("/") ? 1 : 0;

                classname = classname.substring(startIndex, endIndex);
                classname = classname.replace('/', '.');
                types.add(classname);
            }
        }
        return types;
    }

    /**
     * Returns the array of existing types from the Bundle
     * passed as  parameter
     *
     * @param bundle the Bundle to search for the types in
     * @return the array of types from the specified Bundle
     */
    public static List<Class<?>> getAllTypes(Bundle bundle) {
        List<String> strTypes = ReflectUtils.getAllStringTypes(bundle);

        List<Class<?>> types = new ArrayList<Class<?>>();
        if (strTypes == null || strTypes.size() == 0) {
            return types;
        }
        Iterator<String> iterator = strTypes.iterator();
        while (iterator.hasNext()) {
            try {
                types.add(bundle.loadClass(iterator.next()));

            } catch (ClassNotFoundException ex) {
                continue;
            }
        }
        return types;
    }

    /**
     * Get the underlying class for a type, or null if the type is
     * a variable type.
     *
     * @param type the type
     * @return the underlying class
     */
    public static Class<?> getClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType) type).getRawType());
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> componentClass = getClass(componentType);
            if (componentClass != null) {
                return Array.newInstance(componentClass, 0).getClass();
            }
        }
        return null;
    }

    /**
     * Get the actual type arguments a child class has used to
     * extend a generic base class.
     *
     * @param baseClass  the base class
     * @param childClass the child class
     * @return a list of the raw classes for the actual type arguments.
     */
    public static <T> List<Class<?>> getTypeArguments(Class<T> baseClass, Class<? extends T> childClass) {
        Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
        Type type = childClass;
        // start walking up the inheritance hierarchy until we hit baseClass
        while (!getClass(type).equals(baseClass)) {
            if (type instanceof Class) {
                // there is no useful information for us in raw types, so just keep going.
                type = ((Class<?>) type).getGenericSuperclass();
            } else {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Class<?> rawType = (Class<?>) parameterizedType.getRawType();

                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
                }

                if (!rawType.equals(baseClass)) {
                    type = rawType.getGenericSuperclass();
                }
            }
        }

        // finally, for each actual type argument provided
        //to baseClass, determine (if possible)
        // the raw class for that type argument.
        Type[] actualTypeArguments;
        if (type instanceof Class) {
            actualTypeArguments = ((Class<?>) type).getTypeParameters();
        } else {
            actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
        }
        List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
        // resolve types by chasing down type variables.
        for (Type baseType : actualTypeArguments) {
            while (resolvedTypes.containsKey(baseType)) {
                baseType = resolvedTypes.get(baseType);
            }
            typeArgumentsAsClasses.add(getClass(baseType));
        }
        return typeArgumentsAsClasses;
    }

}
