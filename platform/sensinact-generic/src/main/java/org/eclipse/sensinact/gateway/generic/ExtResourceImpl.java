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
/**
 *
 */
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.method.AbstractAccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor.ExecutionPolicy;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;
import org.eclipse.sensinact.gateway.core.method.Signature;
import org.eclipse.sensinact.gateway.generic.annotation.Act;
import org.eclipse.sensinact.gateway.generic.annotation.Get;
import org.eclipse.sensinact.gateway.generic.annotation.Subscribe;
import org.eclipse.sensinact.gateway.generic.annotation.Unsubscribe;
import org.eclipse.sensinact.gateway.generic.parser.MethodDefinition;
import org.eclipse.sensinact.gateway.generic.parser.SignatureDefinition;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Extended {@link ResourceImpl} implementation dedicated to
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ExtResourceImpl extends ResourceImpl {
    /**
     * @param mediator
     * @param resourceConfig
     * @param service
     */
    protected ExtResourceImpl(ExtModelInstance<?> modelInstance, ExtResourceConfig resourceConfig, ExtServiceImpl service) {
        super(modelInstance, resourceConfig, service);
    }

    /**
     * @throws Exception
     * @inheritDoc
     * @see ResourceImpl#
     * passOn(AccessMethod.Type, java.lang.Object[])
     */
    @Override
    protected JSONObject passOn(String type, String uri, Object[] parameters) throws Exception {
        Task task = super.<Task>passOn(type, uri, parameters);

        if (type.equals(AccessMethod.GET) && task != null && task.isResultAvailable() && task.getResult() != AccessMethod.EMPTY) {
            super.getAttribute(defaultAttribute).setValue(task.getResult(), task.getTimestamp());
        }
        if (task == null) {
            return null;
        }
        return new JSONObject(task.getJSON());
    }

    /**
     * Returns the extended {@link Annotation} type mapped to the
     * {@link AccessMethod.Type} passed as parameter
     *
     * @param type {@link AccessMethod.Type} for which to retrieve the
     *             extended {@link Annotation} type
     * @return the extended {@link Annotation} type mapped to the specified
     * {@link AccessMethod.Type}
     */
    private Class<? extends Annotation> annotationFromAccessMethodType(String type) {
        switch (type) {
            case "ACT":
                return Act.class;
            case "DESCRIBE":
                break;
            case "GET":
                return Get.class;
            case "SET":
                return org.eclipse.sensinact.gateway.generic.annotation.Set.class;
            case "SUBSCRIBE":
                return Subscribe.class;
            case "UNSUBSCRIBE":
                return Unsubscribe.class;
        }
        return null;
    }

    /**
     * Builds the {@link ActMethod} of this {@link Resource.Type.ACTION}
     * typed {@link SnaActionResource} using the {@link MethodDefinition}
     * passed as parameter
     *
     * @param resourceConfig the {@link MethodDefinition} describing the {@link ActMethod}
     *                       to build
     * @param snaService
     */
    protected void buildMethod(ExtResourceConfig resourceConfig, ExtServiceImpl service) {
        if (resourceConfig == null) {
            return;
        }
        Iterator<MethodDefinition> iterator = resourceConfig.iterator();
        while (iterator.hasNext()) {
            MethodDefinition methodDefinition = iterator.next();
            if (methodDefinition == null) {
                continue;
            }
            final Class<?> clazz = getClass();
            final ExtResourceImpl self = this;

            AbstractAccessMethod method = (AbstractAccessMethod) super.getAccessMethod(methodDefinition.getType());

            SignatureDefinition definition = null;

            Iterator<SignatureDefinition> signatureIterator = methodDefinition.iterator();

            while (signatureIterator.hasNext()) {
                definition = signatureIterator.next();
                Signature signature;
                try {
                    signature = definition.getSignature(super.modelInstance.mediator(), service);
                } catch (InvalidValueException e) {
                    continue;
                }
                Class<?>[] parameterTypes = signature.getParameterTypes();
                Class<? extends Annotation> annotationClass = this.annotationFromAccessMethodType(method.getType().name());

                if (annotationClass == null) {
                    continue;
                }
                Map<Method, ? extends Annotation> methods = ReflectUtils.getAnnotatedMethods(clazz, annotationClass);

                Set<Method> methodSet = methods.keySet();

                Method javaMethod = ReflectUtils.getDeclaredMethod(methodSet.toArray(new Method[0]), JSONObject.class, null, parameterTypes, true);

                final Method reflectionMethod = javaMethod;
                AccessMethodExecutor executor = null;

                if (reflectionMethod != null) {
                    executor = new AccessMethodExecutor() {
                        @Override
                        public Void execute(AccessMethodResponseBuilder parameter) throws Exception {
                            JSONObject jsonObject = (JSONObject) reflectionMethod.invoke(self, parameter.getParameters());

                            if (!JSONObject.NULL.equals(jsonObject)) {
                                parameter.setAccessMethodObjectResult(jsonObject);
                            }
                            return null;
                        }
                    };
                }
                method.addSignature(signature, executor, ExecutionPolicy.AFTER);
                service.buildTriggers(this.getName(), signature, definition.getReferenceDefinitions());
            }
        }
    }
}
