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

package org.eclipse.sensinact.gateway.nthbnd.jsonpath;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class JsonOrgJsonArray extends JSONArray implements JsonOrgJson,List {
    /**
     * Construct an empty JSONArray.
     */
    public JsonOrgJsonArray() {
        super();
        init();
    }

    /**
     * Construct a JSONArray from a JSONTokener.
     * @param x A JSONTokener
     * @throws JSONException If there is a syntax error.
     */
    public JsonOrgJsonArray(JsonOrgJsonTokener x) throws JSONException {
        super(x);
        init();
    }

    /**
     * Construct a JSONArray from a source JSON text.
     * @param source     A string that begins with
     * <code>[</code>&nbsp;<small>(left bracket)</small>
     *  and ends with <code>]</code>&nbsp;<small>(right bracket)</small>.
     *  @throws JSONException If there is a syntax error.
     */
    public JsonOrgJsonArray(String source) throws JSONException {
       this(new JsonOrgJsonTokener(source));
       init();
    }


    /**
     * Construct a JSONArray from a Collection.
     * @param collection     A Collection.
     */
    public JsonOrgJsonArray(Collection collection) {
        super(collection);
        init();
    }

    /**
     * Construct a JSONArray from a collection of beans.
     * The collection should have Java Beans.
     * 
     * @throws JSONException If not an array.
     */

    public JsonOrgJsonArray(Collection collection,boolean includeSuperClass) {
		super(collection,includeSuperClass);
        init();
    }

    
    /**
     * Construct a JSONArray from an array
     * @throws JSONException If not an array.
     */
    public JsonOrgJsonArray(Object array) throws JSONException {
        super(array);
        init();
    }

    /**
     * Construct a JSONArray from an array with a bean.
     * The array should have Java Beans.
     * 
     * @throws JSONException If not an array.
     */
    public JsonOrgJsonArray(Object array,boolean includeSuperClass) throws JSONException {
        super(array, includeSuperClass);
        init();
    }

	@Override
	public int size() {
		return parentList.size();
	}

	@Override
	public boolean isEmpty() {
		return parentList.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return parentList.contains(o);
	}

	@Override
	public Iterator iterator() {
		return parentList.iterator();
	}

	@Override
	public Object[] toArray() {
		return parentList.toArray();
	}

	@Override
	public Object[] toArray(Object[] a) {
		return parentList.toArray(a);
	}

	@Override
	public boolean add(Object e) {
		return parentList.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return parentList.remove(o);
	}

	@Override
	public boolean containsAll(Collection c) {
		return parentList.containsAll(c);
	}

	@Override
	public boolean addAll(Collection c) {
		return parentList.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection c) {
		return parentList.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection c) {
		return parentList.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection c) {
		return parentList.retainAll(c);
	}

	@Override
	public void clear() {
		parentList.clear();
	}

	@Override
	public Object set(int index, Object element) {
		return parentList.set(index, element);
	}

	@Override
	public void add(int index, Object element) {
		parentList.add(index, element);
	}

	@Override
	public Object remove(int index) {
		return parentList.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return parentList.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return parentList.lastIndexOf(o);
	}

	@Override
	public ListIterator listIterator() {
		return parentList.listIterator();
	}

	@Override
	public ListIterator listIterator(int index) {
		return parentList.listIterator(index);
	}

	@Override
	public List subList(int fromIndex, int toIndex) {
		return new JsonOrgJsonArray(
			parentList.subList(fromIndex, toIndex));
	}

	@Override
	public Object get(int index) {
		return parentList.get(index);
	}

	private void init()
	{
		if(this.parentList != null)
		{
			return;
		}
		Field[] fields = JSONArray.class.getDeclaredFields();
		for(Field field:fields)
		{
			if(List.class.isAssignableFrom(field.getType()))
			{
				field.setAccessible(true);
				try
				{
					this.parentList = (List) field.get(this);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
		}
	}

	public boolean equals(Object o){
		if(o == null){
			return false;}
		if(List.class.isAssignableFrom(o.getClass()) || 
				JsonOrgJsonArray.class.isAssignableFrom(
						o.getClass())){
			return o.equals(this.parentList);
			
		}if(JSONArray.class.isAssignableFrom(o.getClass())){
			int index = 0;
			int length=((JSONArray)o).length();
			if(this.length()!=length){
				return false;
			}
			for(;index < length;index++){
				try{
					if((((JSONArray)o).get(index)==null && this.get(index)!=null)
						||!((JSONArray)o).get(index).equals(this.get(index))){
						return false;
					}
				} catch(JSONException e){
					return false;
				}
			}
			return true;
		}
		return false;
	}
    
	private List parentList = null;
}
