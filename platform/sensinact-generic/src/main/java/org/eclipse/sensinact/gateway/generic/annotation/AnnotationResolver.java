/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.generic.annotation;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Annotations resolver service
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AnnotationResolver implements Iterable<Object> {
	
	private static final Logger LOG = LoggerFactory.getLogger(AnnotationResolver.class);
    /**
     * the associated {@link Mediator} used to
     * interact with the OSGi host environment
     */
    private final Mediator mediator;
    /**
     * List of {@link TaskExecution} annotated instance
     */
    private List<Object> instances;

    /**
     * Set of injected object instances mapped to their type
     */
    private Map<Class<?>, Object> injected;

    /**
     * Constructor
     *
     * @param mediator the associated {@link Mediator} used
     *                 to interact with the host OSGi environment
     */
    public AnnotationResolver(Mediator mediator) {
        this.mediator = mediator;
        this.injected = new HashMap<Class<?>, Object>();

        buildInstances(mediator.getContext().getBundle());

    }

    /**
     * Builds the list of {@link Class}es embedded
     * in the {@link Bundle} passed as parameter
     *
     * @param bundle the {@link Bundle} to explore
     * @return the list of {@link Class}es embedded in the
     * specified {@link Bundle}
     */
    private void buildInstances(Bundle bundle) {
        this.instances = new ArrayList<Object>();

        if (bundle == null) {
            return;
        }
        Enumeration<URL> urls = bundle.findEntries("/", "*.class", true);

        if (urls == null) {
            return;
        }
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String path = url.getPath().replaceAll("\\/|\\$", ".");
            path = path.substring(path.startsWith(".") ? 1 : 0, path.length() - 6);
            try {
                Class<?> loaded = bundle.loadClass(path);
                if (loaded.getAnnotation(TaskExecution.class) != null) {
                    Object instance = ReflectUtils.getTheBestInstance(loaded, new Object[]{this.mediator});

                    if (instance == null) {
                        continue;
                    }
                    this.instances.add(instance);
                }
            } catch (ClassNotFoundException e) {
                continue;
            }
        }
    }


    /**
     * Creates or injects Object instances into {@link
     * TaskInject} annotated fields in the list of
     * {@link TaskExecution} annotated types passed
     * as parameter
     *
     * @param classes List of {@link TaskExecution} annotated types
     *                to explore to search {@link SnaTaskInjection}
     *                annotated methods
     */
    public void buildInjected() {
        Iterator<Object> iterator = this.instances.iterator();

        while (iterator.hasNext()) {
            final Object instance = iterator.next();

            Map<Field, TaskInject> fieldsMap = ReflectUtils.getAnnotatedFields(instance.getClass(), TaskInject.class);

            Set<Field> fields = fieldsMap.keySet();

            if (fields.isEmpty()) {
                continue;
            }
            Iterator<Field> fieldsIterator = fields.iterator();

            while (fieldsIterator.hasNext()) {
                Field field = fieldsIterator.next();
                field.setAccessible(true);

                if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL) {
                    continue;
                }
                Class<?> type = field.getType();
                Object value = null;

                if ((value = this.injected.get(type)) == null) {
                    boolean defaultInjectedService = true;

                    String filter = fieldsMap.get(field).filter();
                    String completeFilter = null;

                    if (filter == null || TaskInject.DEFAULT_FILTER.equals(filter)) {
                        defaultInjectedService = false;

                    } else {
                        completeFilter = filter;
                    }
                    try {
                        value = new InjectedService(mediator, type, completeFilter);
                        if (!((InjectedService) value).exists() && !defaultInjectedService) {
                            value = null;
                        }
                    } catch (InvalidSyntaxException e) {
                        if (LOG.isDebugEnabled()) {
                            LOG.error( e.getMessage(), e);
                        }
                    }
                    if (value != null || (value = ReflectUtils.getTheBestInstance(type, new Object[]{this.mediator})) != null) {
                        this.injected.put(type, value);
                    }
                }
                if (value != null) {
                    try {
                        field.set(instance, value);

                    } catch (Exception e) {
                        if (LOG.isDebugEnabled()) {
                            LOG.error( e.getMessage(), e);
                        }
                        continue;
                    }
                }
            }
        }
    }

    /**
     * Adds the specified Object instance whose type is passed
     * as parameter to this AnnotationResolver's injection Map
     * to make it accessible to {@link TaskExecution} annotated
     * types
     *
     * @param injectableType the Object instance type
     * @param injectable     the Object instance to make accessible to {@link
     *                       TaskExecution} annotated types
     */
    public <T> void addInjectableInstance(Class<T> injectableType, T injectable) {
        this.injected.put(injectableType, injectable);
    }

    /**
     * @inheritDoc
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<Object> iterator() {
        return Collections.unmodifiableList(this.instances).iterator();
    }
}