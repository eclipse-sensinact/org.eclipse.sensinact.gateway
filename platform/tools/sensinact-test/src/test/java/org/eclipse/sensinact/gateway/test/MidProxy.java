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
package org.eclipse.sensinact.gateway.test;

import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;

public class MidProxy<T> implements InvocationHandler {
    private Class<T> serviceType;
    private Object instance;
    private BundleContextProvider contextProvider;
    private FilterOSGiClassLoader filterclassloader;
    private InnerOSGiClassLoader omnipotentclassloader;

    public MidProxy(FilterOSGiClassLoader classloader, BundleContextProvider contextProvider, Class<T> serviceType) throws IOException {
        this.contextProvider = contextProvider;
        this.filterclassloader = classloader;
        this.omnipotentclassloader = new InnerOSGiClassLoader(Thread.currentThread().getContextClassLoader(), contextProvider, filterclassloader);
        this.serviceType = serviceType;
        if (this.serviceType == null) {
            throw new NullPointerException("Proxy type needed");
        }
    }

    public T buildProxy() throws ClassNotFoundException, InvalidSyntaxException {
        String classname = this.serviceType.getCanonicalName();
        Class<?> contextualizedClazz = this.omnipotentclassloader.loadClass(classname);
        if (contextualizedClazz == null ) {
        	return null;
        }
        ServiceReference reference = null;
    	if((reference = this.contextProvider.getBundleContext().getServiceReference(contextualizedClazz)) != null) {
    		return (T)this.buildProxy(this.contextProvider.getBundleContext().getService(reference));
    	} 
		ServiceReference<?>[] fs = this.contextProvider.getBundleContext().getAllServiceReferences(this.serviceType.getCanonicalName(), null);
        if(fs == null || fs.length == 0) {
        	return null;
        }
    	for(ServiceReference<?> f:fs) {
    		Object obj = this.contextProvider.getBundleContext().getService(f);
    	    if(obj == null) {
    	    	this.contextProvider.getBundleContext().ungetService(f);
    	    	continue;
    	    }
    		Class<?>[] interfaces = obj.getClass().getInterfaces();
    		if(interfaces == null || interfaces.length == 0) {
    			this.contextProvider.getBundleContext().ungetService(f);
            	continue;
            }
    		for(Class<?> itf:interfaces) {
    			if(itf != contextualizedClazz) {
    				continue;
    			}
    			return this.buildProxy(obj);
    		}
    		this.contextProvider.getBundleContext().ungetService(f);
    	}
        return null;
    }

    public T buildProxy(String instanceType, Class<?>[] parameterTypes, Object[] objects) throws ClassNotFoundException, IOException {
        String classname = this.serviceType.getCanonicalName();
        Class<?> contextualizedClazz = this.omnipotentclassloader.loadClass(classname);
        Class<?> contextualizedImplementation = this.omnipotentclassloader.loadClass(instanceType);

        if (contextualizedImplementation != null && contextualizedClazz != null && contextualizedClazz.isAssignableFrom(contextualizedImplementation)) {
            try {
                Class[] contextualizedTypes = new Class[parameterTypes.length];
                Object[] contextualizedArgs = new Object[parameterTypes.length];

                for (int i = 0; i < parameterTypes.length; i++) {
                    contextualizedTypes[i] = this.omnipotentclassloader.loadClass(parameterTypes[i].getName());
                    contextualizedArgs[i] = this.toOSGi(parameterTypes[i], objects[i]);
                    if (contextualizedArgs[i] == null && objects[i] != null) {
                        return null;
                    }
                }
                this.instance = contextualizedImplementation.getDeclaredConstructor(contextualizedTypes).newInstance(contextualizedArgs);
                return this.buildProxy(instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public T buildProxy(Object instance) throws ClassNotFoundException {
        if (instance == null) {
            return null;
        }
        String classname = this.serviceType.getCanonicalName();
        Class<?> contextualizedClazz = this.omnipotentclassloader.loadClass(classname);
        this.instance = instance;
        LinkedList<Class<?>> iis = ReflectUtils.getOrderedImplementedInterfaces(serviceType);

        Class[] interfaces = iis.toArray(new Class[0]);
        if (interfaces.length == 0) {
            throw new NullPointerException("No implemented interface");
        }
        if (contextualizedClazz.isAssignableFrom(instance.getClass())) {
            return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, this);
        }
        return null;
    }

    public Object reverseProxy(String instanceType, Class<?>[] parameterTypes, Object[] objects) throws ClassNotFoundException, IOException {
        Class<?> contextualizedImplementation = Thread.currentThread().getContextClassLoader().loadClass(instanceType);

        if (contextualizedImplementation != null && serviceType.isAssignableFrom(contextualizedImplementation)) {
            try {
                Class[] contextualizedTypes = new Class[parameterTypes.length];
                Object[] contextualizedArgs = new Object[parameterTypes.length];

                for (int i = 0; i < parameterTypes.length; i++) {
                    contextualizedTypes[i] = Thread.currentThread().getContextClassLoader().loadClass(parameterTypes[i].getName());
                    if (parameterTypes[i] == contextualizedTypes[i]) {
                        contextualizedArgs[i] = objects[i];

                    } else {
                        contextualizedArgs[i] = this.fromOSGi(parameterTypes[i], objects[i]);
                    }
                    if (contextualizedArgs[i] == null && objects[i] != null) {
                        return null;
                    }
                }
                this.instance = contextualizedImplementation.getDeclaredConstructor(contextualizedTypes).newInstance(contextualizedArgs);
                return this.reverseProxy(instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Object reverseProxy(Object instance) throws ClassNotFoundException {
        String classname = this.serviceType.getCanonicalName();
        if (instance == null || !serviceType.isAssignableFrom(instance.getClass())) {

            return null;
        }
        Class<?> contextualizedClazz = this.omnipotentclassloader.loadClass(classname);
        LinkedList<Class<?>> iis = ReflectUtils.getOrderedImplementedInterfaces(contextualizedClazz);

        Class[] interfaces = iis.toArray(new Class[0]);
        if (interfaces.length == 0) {
            throw new NullPointerException("No implemented interface");
        }
        this.instance = instance;
        return Proxy.newProxyInstance(omnipotentclassloader, interfaces, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (instance == null) {
            return null;
        }
        if (serviceType.isAssignableFrom(instance.getClass())) {
            return fromOSGi(method, args);

        } else {
            return toOSGi(method, args);
        }
    }

    public Object getInstance() {
        return this.instance;
    }

    public Class<T> getServiceType() {
        return this.serviceType;
    }

    public Class<?> getContextualizedType() {
        String classname = this.serviceType.getCanonicalName();
        Class<?> contextualizedClazz = null;
        try {
            contextualizedClazz = this.omnipotentclassloader.loadClass(classname);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return contextualizedClazz;
    }

    public Object fromOSGi(Method method, Object[] args) throws Throwable {
        Method contextualizedMethod = this.fromOSGi(method);
        if (contextualizedMethod == null) {
            return null;
        }
        Object result = null;
        if (args == null || args.length == 0) {
            result = contextualizedMethod.invoke(this.instance);

        } else {
            Class[] parameterTypes = method.getParameterTypes();
            Object[] contextualizedArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                contextualizedArgs[i] = this.fromOSGi(parameterTypes[i], args[i]);
                if (contextualizedArgs[i] == null && args[i] != null) {
                    return null;
                }
            }
            result = contextualizedMethod.invoke(this.instance, contextualizedArgs);
        }
        if (void.class.isAssignableFrom(method.getReturnType())) {
            return null;
        }
        Object toOSGiResult = toOSGi(contextualizedMethod.getReturnType(), result);
        if (toOSGiResult != null) {
            return toOSGiResult;
        }
        return result;
    }

    public Method fromOSGi(Method m) {
        if (m == null) {
            return null;
        }
        try {
            Class<?> cl = null;
            Class clazz = m.getDeclaringClass();

            cl = clazz.isArray() ? Class.forName("[L" + clazz.getComponentType().getName() + ";") : Thread.currentThread().getContextClassLoader().loadClass(clazz.getName());

            if (cl == null) {
                return null;
            }
            if (cl != clazz) {
                Class[] parameterTypes = m.getParameterTypes();
                if (parameterTypes == null || parameterTypes.length == 0) {
                    return cl.getMethod(m.getName());
                }
                Class[] pts = new Class[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class c = parameterTypes[i];

                    pts[i] = c.isPrimitive() ? c : (c.isArray() ? Class.forName("[L" + c.getComponentType().getName() + ";") : Thread.currentThread().getContextClassLoader().loadClass(c.getName()));
                }
                return cl.getMethod(m.getName(), pts);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return m;
    }

    public Object fromOSGi(Class type, Object o) {
        if (o == null) {
            return null;
        }
        if (type != null && (type.isPrimitive() || type == String.class)) {
            return o;
        }
        if (Proxy.isProxyClass(o.getClass()) && Proxy.getInvocationHandler(o).getClass() == MidProxy.class && (type.isAssignableFrom(((MidProxy) Proxy.getInvocationHandler(o)).getContextualizedType()))) {
            return ((MidProxy) Proxy.getInvocationHandler(o)).getInstance();
        }
        if (type != null && !type.isAssignableFrom(o.getClass())) {
            System.out.println("Not of the specified type");
            return null;
        }
        try {
            Class clazz = type == null ? o.getClass() : type;
            Class<?> cl = clazz.isArray() ? Class.forName("[L" + clazz.getComponentType().getName() + ";") : Thread.currentThread().getContextClassLoader().loadClass(clazz.getName());

            if (cl == null) {
                System.out.println(clazz + " not found in the Test environment");
                return null;
            }
            if (clazz.isArray()) {
                int length = Array.getLength(o);
                Object array = Array.newInstance(cl.getComponentType(), length);

                for (int i = 0; i < length; i++) {
                    Array.set(array, i, fromOSGi(clazz.getComponentType(), Array.get(o, i)));
                }
                return array;
            }
            if (cl != clazz) {
                MidProxy proxy = new MidProxy(filterclassloader, contextProvider, cl);
                return proxy.buildProxy(o);
            }
        } catch (Exception e) {
            return null;
        }
        return o;
    }

    public Object toOSGi(Method method, Object[] args) throws Throwable {
        Method contextualizedMethod = this.toOSGi(method);
        if (contextualizedMethod == null) {
            return null;
        }
        Object result = null;
        contextualizedMethod.setAccessible(true);
        if (args == null || args.length == 0) {
            result = contextualizedMethod.invoke(this.instance);

        } else {
            Class[] parameterTypes = method.getParameterTypes();
            Object[] contextualizedArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                contextualizedArgs[i] = this.toOSGi(parameterTypes[i], args[i]);
                if (contextualizedArgs[i] == null && args[i] != null) {
                    return null;
                }
            }
            result = contextualizedMethod.invoke(this.instance, contextualizedArgs);
        }
        if (void.class.isAssignableFrom(method.getReturnType())) {
            return null;
        }
        Object fromOSGiResult = fromOSGi(contextualizedMethod.getReturnType(), result);
        if (fromOSGiResult != null) {
            return fromOSGiResult;
        }
        return result;
    }

    public Method toOSGi(Method m) {
        if (m == null) {
            return null;
        }
        try {
            Class<?> cl = null;
            Class clazz = m.getDeclaringClass();

            cl = omnipotentclassloader.loadClass(clazz.isArray() ? "[L" + clazz.getComponentType().getName() + ";" : clazz.getName());

            if (cl == null) {
                System.out.println(clazz + " not found");
                return null;
            }
            if (cl != clazz) {
                Class[] parameterTypes = m.getParameterTypes();
                if (parameterTypes == null || parameterTypes.length == 0) {
                    return cl.getDeclaredMethod(m.getName());
                }

                Class[] pts = new Class[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class c = parameterTypes[i];
                    if (c.isPrimitive()) {
                        pts[i] = c;

                    } else if (c.isArray()) {
                        pts[i] = Array.newInstance(omnipotentclassloader.loadClass(c.getComponentType().getName()), 0).getClass();

                    } else {
                        pts[i] = omnipotentclassloader.loadClass(c.getName());
                    }
                }
                Method cm = cl.getDeclaredMethod(m.getName(), pts);
                return cm;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return m;
    }

    public Object toOSGi(Class type, Object o) {
        if (o == null) {
            return null;
        }
        if ((type != null && (type.isPrimitive() || type == String.class)) || (Proxy.isProxyClass(o.getClass()) && Proxy.getInvocationHandler(o).getClass() == MidProxy.class && (type.isAssignableFrom(((MidProxy) Proxy.getInvocationHandler(o)).getServiceType())))) {
            return o;
        }
        if (type != null && !type.isAssignableFrom(o.getClass())) {
            System.out.println("Not of the specified type");
            return null;
        }

        try {
            Class clazz = type == null ? o.getClass() : type;
            Class<?> cl = omnipotentclassloader.loadClass(clazz.isArray() ? "[L" + clazz.getComponentType().getName() + ";" : clazz.getName());

            if (cl == null) {
                System.out.println(clazz + " not found in the OSGi environment");
                return null;
            }
            if (clazz.isArray()) {
                int length = Array.getLength(o);
                Object array = Array.newInstance(cl.getComponentType(), length);

                for (int i = 0; i < length; i++) {
                    Array.set(array, i, toOSGi(clazz.getComponentType(), Array.get(o, i)));
                }
                return array;
            }
            if (cl != clazz) {
                MidProxy proxy = new MidProxy(filterclassloader, contextProvider, clazz);
                return proxy.reverseProxy(o);
            }
        } catch (Exception e) {
            return null;
        }
        return o;
    }

}
