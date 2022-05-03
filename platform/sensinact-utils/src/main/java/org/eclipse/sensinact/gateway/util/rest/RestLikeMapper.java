/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.util.rest;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Rest like URI/Resource mapper : <p/>
 * <code>
 * example : <p/>
 * <p>
 * Initial map configuration array :<p/>
 * <p>
 * PATH   |   LDAP KEY     |             CLASS<p/>
 * [first,    first-key    ,  my.class.package.MyFirstClass]<p/>
 * [second,   second-key   ,  my.class.package.MySecondClass]<p/>
 * [third,    third-key    ,  my.class.package.MyThridClass]<p/>
 * <p>
 * <p>
 * the mapper will build the set of key-words below :<p/>
 * <p>
 * [firsts,first,seconds,second,thirds,third]<p/>
 * <p>
 * then it will map this key-words to OSGi filters as below : <p/>
 * <p>
 * PATH     |                OSGi FILTER     <p/>
 * [firsts  ,  objectClass=my.class.package.MyFirstClass]<p/>
 * [first   ,  first-key=&lt;variable&gt;]<p/>
 * [seconds ,  objectClass=my.class.package.MySecondClass]<p/>
 * [second  ,  second-key=&lt;variable&gt;] <p/>
 * [thirds  ,  objectClass=my.class.package.MyThridClass]<p/>
 * [third   ,  third-key=&lt;variable&gt;]<p/>
 * <p>
 * It means that if the plural form of a key word is found in a treated URI, the next path element
 * of this URI will be interpreted as the value attached to the singular form of the same key-word
 * and will be put in the associated filter in replacement of the variable entry<p/>
 * <p>
 * Hierarchical links between entries have to be defined in another array as below <p/>
 * There must be at least one hierarchical link between each level<p/>
 * <p>
 * [&lt;parent&gt;,&lt;child&gt;,&lt;child-key&gt;,&lt;parent-key&gt;]<p/>
 * <p>
 * the child-key is the used to create a filter to append on the parent's one and vice versa<p/>
 * </code>
 */
public class RestLikeMapper {
    private static final char AND = '&';
    private static final char EQUALITY = '=';
    private static final char LESS = '-';
    private static final char OPEN_BRACKET = '(';
    private static final char CLOSE_BRACKET = ')';
    private static final String SEPARATOR_CHAR = "/";
    private static final String PLURAL_SUFFIX = "s";
    private static final String OBJECTCLASS = "objectClass";
    private static final String VARIABLE = "#VARIABLE#";

    /**
     * the associated {@link BundleContext}
     */
    private BundleContext context;
    /**
     * filter associated to path element
     */
    private String[][] filters;
    /**
     * relationship between path elements
     */
    private String[][] hierarchies;
    /**
     * list of methods to exclude of the research of exposed
     * osgi services
     */
    private List<String> methods;

    /**
     * Constructor
     *
     * @param filters     initial configuration map
     * @param hierarchies map of dependencies between initial configuration map entries
     */
    public RestLikeMapper(BundleContext context, String[][] filters, String[][] hierarchies, String[] methods) throws RestLikeMapperException {

        if (filters == null || filters.length == 0 || filters[0].length != 3) {
            throw new RestLikeMapperException("Invalid initial configuration map");
        }
        this.context = context;
        this.filters = new String[2 * filters.length][2];
        this.hierarchies = new String[2 * hierarchies.length][2];

        int index = 0;
        int pos = 0;
        for (; index < filters.length; index++) {
            String[] mapping = filters[index];

            this.filters[pos][0] = plural(mapping[0]);

            this.filters[pos++][1] = mapping[2] == null ? null : new StringBuilder().append(OPEN_BRACKET).append(OBJECTCLASS).append(EQUALITY).append(mapping[2]).append(CLOSE_BRACKET).toString();

            this.filters[pos][0] = mapping[0];

            this.filters[pos++][1] = mapping[1] == null ? null : new StringBuilder().append(OPEN_BRACKET).append(mapping[1]).append(EQUALITY).append(VARIABLE).append(CLOSE_BRACKET).toString();
        }
        this.methods = methods != null ? Arrays.asList(methods) : Collections.<String>emptyList();

        if (hierarchies == null || hierarchies.length == 0 || hierarchies[0].length != 4) {
            return;
        }
        pos = 0;
        for (String[] hierarchy : hierarchies) {
            if (hierarchy[0] != null && hierarchy[1] != null) {
                if (hierarchy[2] != null) {
                    this.hierarchies[pos][0] = new StringBuilder().append(hierarchy[0]).append(LESS).append(hierarchy[1]).toString();

                    this.hierarchies[pos++][1] = new StringBuilder().append(OPEN_BRACKET).append(hierarchy[2].length() == 0 ? plural(hierarchy[1]) : hierarchy[2]).append(EQUALITY).append(VARIABLE).append(CLOSE_BRACKET).toString();
                }
                if (hierarchy[3] != null) {
                    this.hierarchies[pos][0] = new StringBuilder().append(hierarchy[1]).append(LESS).append(hierarchy[0]).toString();

                    this.hierarchies[pos++][1] = new StringBuilder().append(OPEN_BRACKET).append(hierarchy[3].length() == 0 ? plural(hierarchy[0]) : hierarchy[3]).append(EQUALITY).append(VARIABLE).append(CLOSE_BRACKET).toString();
                }
            }
        }
        this.hierarchies = Arrays.copyOfRange(this.hierarchies, 0, pos);
    }

    /**
     * Returns the plural form of the singular word passed
     * as parameter
     *
     * @param singular the word to convert to the plural form
     * @return the plural form of the singular word passed
     * as parameter
     */
    private String plural(String singular) {
        return new StringBuilder(singular).append(PLURAL_SUFFIX).toString();
    }

    /**
     * Concatenates the two filters passed as parameters
     *
     * @param filterFst one of the two filters to concatenate
     * @param filterSnd one of the two filters to concatenate
     * @return the filter resulting of the concatenation of the
     * two filters passed as parameters
     */
    private String concat(String filterFst, String filterSnd) {
        if (filterFst == null) {
            return filterSnd;
        }
        if (filterSnd == null) {
            return filterFst;
        }
        return new StringBuilder().append(OPEN_BRACKET).append(AND).append((filterFst.charAt(0) == OPEN_BRACKET) ? "" : OPEN_BRACKET).append(filterFst).append((filterFst.charAt(filterFst.length() - 1) == CLOSE_BRACKET) ? "" : CLOSE_BRACKET).append((filterSnd.charAt(0) == OPEN_BRACKET) ? "" : OPEN_BRACKET).append(filterSnd).append((filterSnd.charAt(filterSnd.length() - 1) == CLOSE_BRACKET) ? "" : CLOSE_BRACKET).append(CLOSE_BRACKET).toString();
    }

    /**
     * Parses the URI string passed as parameter and return
     * the corresponding set of registered OSGi services
     * according to defined filters
     *
     * @param uri the URI string to parse
     * @return the set of OSGi services mapped by the
     * URI argument
     */
    public RestLikeMappingReport parseURI(String uri) {
        String[] uriElements = uri.trim().split(SEPARATOR_CHAR);
        if (uri.startsWith(SEPARATOR_CHAR)) {
            uriElements = Arrays.copyOfRange(uriElements, 1, uriElements.length);
        }
        RestLikeMappingReport report = new RestLikeMappingReport(uriElements);

        if (this.methods.contains(uriElements[uriElements.length - 1])) {
            //last uri element is a method call :
            //end of the parsing
            report.reportMethod(uriElements[uriElements.length - 1]);
            uriElements = Arrays.copyOf(uriElements, uriElements.length - 1);
        }
        LinkedList<String> fstack = new LinkedList<String>();
        int last = -1;
        String variable = null;

        for (int i = 0; i < uriElements.length; i += 2) {
            String plural = uriElements[i];

            //singular form is the next element if it exists
            String singular = i < (uriElements.length - 1) ? uriElements[i + 1] : null;

            int j = 0;
            for (; j < this.filters.length && !this.filters[j][0].equals(plural); j += 2) ;

            //no key-word correspondence found
            if (j == this.filters.length) {
                break;
            }
            //current element associated filter string
            String filter = concat(this.filters[j][1], this.filters[j + 1][1] != null && singular != null ? this.filters[j + 1][1].replace(VARIABLE, singular) : null);

            //if first element...
            if (last == -1) {
                if (filter != null) {
                    fstack.offer(filter);
                    report.reportFilter((singular != null ? i + 1 : i), filter);
                }
                last = j;
                variable = singular;
                continue;
            }
            if (filter != null) {
                //if no string filter has been defined to precisely
                //map the singular element to a set of ServiceReferences
                //then define the report's index as its one. Otherwise
                //define the report's index as the one of the next
                //plural element
                report.reportIndex(this.filters[j + 1][1] != null ? i + 2 : i + 1);
                //retrieve implementation class name
                report.reportImplementation(this.filters[j][1] != null ? this.filters[j][1].substring(new StringBuilder().append(OPEN_BRACKET).append(OBJECTCLASS).append(EQUALITY).length(), this.filters[j][1].length() - 1) : null);
            }
            //else
            int k = 0;
            //search for hierarchical link between the current element
            //and the previous one
            for (; k < this.hierarchies.length; k++) {
                String descendant = new StringBuilder(this.filters[last + 1][0]).append(LESS).append(this.filters[j + 1][0]).toString();

                String ascendant = new StringBuilder(this.filters[j + 1][0]).append(LESS).append(this.filters[last + 1][0]).toString();

                String tmpFilter = null;

                //if descendant hierarchical link...
                if (this.hierarchies[k][0].equals(descendant)) {
                    //append the current element key filter part to the previously registered filter
                    tmpFilter = concat(fstack.pollLast(), this.hierarchies[k][1].replace(VARIABLE, singular));

                    fstack.offer(tmpFilter);
                    report.reportFilter(i - 1, tmpFilter);

                    //register the current element filter for intersection purpose with the
                    //next element
                    if (filter != null && singular != null) {
                        fstack.offer(filter);
                    }
                    break;
                }
                //else if ascendant hierarchical link...
                else if (this.hierarchies[k][0].equals(ascendant)) {
                    //replace the filter registered for the previous element
                    //by the one of the current element appended with key filter part
                    //to refer to ascendant
                    fstack.pollLast();
                    report.reportFilter(i - 1, null);
                    tmpFilter = concat(filter, this.hierarchies[k][1].replace(VARIABLE, variable));

                    fstack.offer(tmpFilter);
                    report.reportFilter(i, tmpFilter);

                    //register the current element filter for intersection purpose with the
                    //next element
                    if (filter != null && singular != null) {
                        fstack.offer(filter);
                    }
                    break;
                }
            }
            //if no hierarchical link found
            if (k == this.hierarchies.length) {
                //end of the parsing
                break;
            }
            last = j;
            variable = singular;
        }
        //apply filters and process the intersection
        //of resulting ServiceReference arrays two at a time
        List<ServiceReference<?>> services = Collections.emptyList();

        while (!fstack.isEmpty()) {
            //retrieve the first registered filter...
            String filter = fstack.pop();
            try {
                //retrieve all registered ServiceReferences compliant with the
                //filter
                ServiceReference<?>[] current = context.getServiceReferences((String) null, filter);

                if (current != null) {
                    List<ServiceReference<?>> srs = new LinkedList<ServiceReference<?>>(Arrays.asList(current));

                    List<ServiceReference<?>> intersection = null;
                    List<ServiceReference<?>> references = null;

                    if (services.size() == 0) {
                        services = srs;
                        report.reportReferences(filter, srs);

                    } else {
                        //process the intersection of the retrieved ServiceReferences
                        //and the previous ones
                        ServiceReference<?>[] copy = new ServiceReference[current.length];
                        System.arraycopy(current, 0, copy, 0, current.length);

                        references = new LinkedList<ServiceReference<?>>(Arrays.asList(copy));
                        references.removeAll(services);

                        intersection = new LinkedList<ServiceReference<?>>(Arrays.asList(current));
                        intersection.removeAll(references);

                        services = intersection.size() == 0 ? srs : intersection;
                        if (services.size() == 0) {
                            //empty services set break the process
                            break;

                        } else {
                            //If the intersection set is empty then keep all ServiceReferences
                            report.reportReferences(filter, intersection.size() == 0 ? srs : intersection);
                        }
                    }
                } else {
                    //invalid filter : break
                    break;
                }
            } catch (InvalidSyntaxException e) {
                report.reportError(new RestLikeMapperException(e.getMessage() + " : " + filter, e));
            }
        }
        return report;
    }
}
