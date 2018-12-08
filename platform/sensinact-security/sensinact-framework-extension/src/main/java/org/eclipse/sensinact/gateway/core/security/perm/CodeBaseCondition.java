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
package org.eclipse.sensinact.gateway.core.security.perm;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.condpermadmin.Condition;
import org.osgi.service.condpermadmin.ConditionInfo;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Condition to test if the location of a bundle matches or does not match a
 * pattern. Since the bundle's location cannot be changed, this condition is
 * immutable.
 * <p>
 * <p>
 * Pattern matching is done according to the filter string matching rules.
 */
public class CodeBaseCondition implements Condition {
    private static final char ESCAPE = '\\';
    private static final char SPACE = ' ';

    private static final char WILDCARD = '*';

    private static final String CONDITION_TYPE = "org.eclipse.sensinact.gateway.core.security.perm.CodeBaseCondition";
    private static final String CODEBASE_SOURCES_TYPE = "org.eclipse.sensinact.gateway.core.security.perm.CodeBaseCondition$1";

    static final class ConditionWrapper {
        Condition c = null;
    }

    static abstract class CodeBaseSources {
        final String conditionType;
        final String codeBaseSourcesType;
        final Set<String> sources;
        volatile int outerCount = 0;

        CodeBaseSources(List<String> cache, String conditionType, String codeBaseSourcesType) {
            this.outerCount = cache.size();
            this.sources = new HashSet<String>(cache);
            this.codeBaseSourcesType = codeBaseSourcesType;
            this.conditionType = conditionType;
        }

        final void check(String codeBaseSourcesType) {
            if (!this.codeBaseSourcesType.equals(codeBaseSourcesType) || !this.codeBaseSourcesType.equals(getClass().getName()) || this.outerCount != this.sources.size()) {
                throw new SecurityException("Unauthorized modification");
            }
        }

        final Set<String> getCache() {
            return this.sources;
        }

        final boolean add(String s) {
            boolean caller = false;
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                if (this.conditionType.equals(stackTraceElement.getClassName())) {
                    caller = true;
                    break;
                }
            }
            if (!caller) {
                throw new SecurityException("Unauthorized modification");
            }
            outerCount++;
            if (!this.sources.add(s)) {
                outerCount--;
                return false;
            }
            return true;
        }
    }

    static final Bundle findBundle(BundleContext context, String name, boolean location) {
        Bundle bundle = null;
        if (name != null && context != null) {
            if (location) {
                bundle = context.getBundle(name);
                if ((bundle.adapt(BundleRevision.class).getTypes() & BundleRevision.TYPE_FRAGMENT) != 0 && bundle.getState() == Bundle.RESOLVED) {
                    bundle = findBundle(context, bundle.getHeaders().get(Constants.FRAGMENT_HOST), false);
                }
                return bundle;
            }
            Bundle[] bundles = context.getBundles();

            int index = 0;
            int length = bundles == null ? 0 : bundles.length;
            for (; index < length; index++) {
                final Bundle tmp = bundles[index];
                if (name.equals(tmp.getSymbolicName())) {
                    if ((tmp.adapt(BundleRevision.class).getTypes() & BundleRevision.TYPE_FRAGMENT) != 0 && tmp.getState() == Bundle.RESOLVED) {
                        bundle = findBundle(context, tmp.getHeaders().get(Constants.FRAGMENT_HOST), false);
                    } else {
                        bundle = tmp;
                    }
                    break;
                }
            }
        }
        return bundle;
    }

    private static CodeBaseSources _sources = null;

    /**
     * Constructs a condition that tries to match the passed Bundle's location
     * to the location pattern.
     *
     * @param bundle The Bundle being evaluated.
     * @param info   The ConditionInfo from which to construct the condition. The
     *               ConditionInfo must specify one or two arguments. The first
     *               argument of the ConditionInfo specifies the location pattern
     *               against which to match the bundle location. Matching is done
     *               according to the filter string matching rules. Any '*' characters
     *               in the first argument are used as wildcards when matching bundle
     *               locations unless they are escaped with a '\' character. The
     *               Condition is satisfied if the bundle location matches the pattern.
     *               The second argument of the ConditionInfo is optional. If a second
     *               argument is present and equal to "!", then the satisfaction of the
     *               Condition is negated. That is, the Condition is satisfied if the
     *               bundle location does NOT match the pattern. If the second argument
     *               is present but does not equal "!", then the second argument is
     *               ignored.
     * @return Condition object for the requested condition.
     */
    public static Condition getCondition(final Bundle bundle, final ConditionInfo info) {
        if (!CONDITION_TYPE.equals(info.getType())) {
            throw new IllegalArgumentException("ConditionInfo must be of type \"" + CONDITION_TYPE + "\"");
        }
        String[] args = info.getArgs();
        if (args.length != 1 && args.length != 2) {
            throw new IllegalArgumentException("Illegal number of args: " + args.length);
        }
        final char[] expected = info.getArgs()[0].toCharArray();
        if (expected.length == 1 && (expected[0] == WILDCARD)) {
            return Condition.TRUE;
        }
        final Condition complies = (info.getArgs().length == 2 && "!".equals(info.getArgs()[1])) ? Condition.FALSE : Condition.TRUE;

        final Condition uncomplies = complies.equals(Condition.FALSE) ? Condition.TRUE : Condition.FALSE;

        final ConditionWrapper pod = new ConditionWrapper();
        pod.c = uncomplies;
        if (_sources == null) {
            String[] s = info.getArgs()[0].split("\\\\,");
            _sources = new CodeBaseCondition.CodeBaseSources(Arrays.<String>asList(s), CONDITION_TYPE, CODEBASE_SOURCES_TYPE) {};
            System.out.println(Arrays.<String>asList(s));
        }
        _sources.check(CODEBASE_SOURCES_TYPE);

        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                //retieve the appropriate class loader
                ClassLoader classloader = null;
                Bundle b = null;
                try {
                    if ((bundle.adapt(BundleRevision.class).getTypes() & BundleRevision.TYPE_FRAGMENT) != 0 && bundle.getState() == Bundle.RESOLVED) {
                        b = findBundle(bundle.getBundleContext(), bundle.getHeaders().get(Constants.FRAGMENT_HOST), false);
                    } else {
                        b = bundle;
                    }
                    classloader = b.adapt(BundleWiring.class).getClassLoader();

                } catch (Exception ex) {
                    classloader = Thread.currentThread().getContextClassLoader();
                }
                //search into the current thread's calls stack the one
                //coming from the allowed code source
                StackTraceElement[] stacktraceElements = Thread.currentThread().getStackTrace();

                int index = 0;
                int length = stacktraceElements == null ? 0 : stacktraceElements.length;

                for (; index < length; index++) {
                    StackTraceElement e = stacktraceElements[index];
                    if (_sources.getCache().contains(e.getClassName())) {
                        pod.c = complies;
                        break;
                    }
                    Class<?> c = null;
                    try {
                        c = classloader.loadClass(e.getClassName());
                        if (c == null) {
                            continue;
                        }
                        char[] ccs = c.getProtectionDomain().getCodeSource().getLocation().toString().toCharArray();

                        if (ccs.length < expected.length) {
                            continue;
                        }
                        int i = 0;
                        boolean escape = false;
                        for (; i < expected.length; ) {
                            if (expected[i] == SPACE) {
                                i++;
                                continue;
                            }
                            if (!escape && expected[i] == ESCAPE) {
                                i++;
                                escape = true;
                                continue;
                            }
                            if (!escape && expected[i] == WILDCARD) {
                                i++;
                                break;
                            }
                            if (ccs[i] != expected[i]) {
                                i = 0;
                                break;
                            }
                            i++;
                            escape = false;
                        }
                        if (i > 0) {
                            _sources.add(e.getClassName());
                            pod.c = complies;
                            break;
                        }
                    } catch (Exception ex) {
                        continue;
                    }
                }
                return null;
            }
        });
        return pod.c;
    }

    private Bundle bundle;
    private ConditionInfo info;

    private CodeBaseCondition() {
    }

    public CodeBaseCondition(Bundle bundle, ConditionInfo info) {
        this.bundle = bundle;
        this.info = info;
    }

    /**
     * @inheritDoc
     * @see org.osgi.service.condpermadmin.Condition#isPostponed()
     */
    @Override
    public boolean isPostponed() {
        return false;
    }

    /**
     * @inheritDoc
     * @see org.osgi.service.condpermadmin.Condition#isSatisfied()
     */
    @Override
    public boolean isSatisfied() {
        return CodeBaseCondition.getCondition(bundle, info).isSatisfied();
    }

    /**
     * @inheritDoc
     * @see org.osgi.service.condpermadmin.Condition#isMutable()
     */
    @Override
    public boolean isMutable() {
        return true;
    }

    /**
     * @inheritDoc
     * @see org.osgi.service.condpermadmin.Condition#
     * isSatisfied(org.osgi.service.condpermadmin.Condition[], java.util.Dictionary)
     */
    @Override
    public boolean isSatisfied(Condition[] conditions, Dictionary<Object, Object> context) {
        if (conditions != null && conditions.length > 0) {
            for (Condition condition : conditions) {
                if (!condition.isSatisfied()) {
                    return false;
                }
            }
        }
        return true;
    }
}
