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
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class JsonOrgJsonObject extends JSONObject implements JsonOrgJson,Map {
    /**
     * Construct an empty JsonOrgJsonObject.
     */
    public JsonOrgJsonObject() {
        super();
        init();
    }


    /**
     * Construct a JsonOrgJsonObject from a subset of another JSONObject.
     * An array of strings is used to identify the keys that should be copied.
     * Missing keys are ignored.
     * @param jo A JSONObject.
     * @param names An array of strings.
     * @exception JSONException If a value is a non-finite number.
     */
    public JsonOrgJsonObject(JSONObject jo, String[] names) throws JSONException {
        super(jo,names);
        init();
    }


    /**
     * Construct a JSONObject from a JSONTokener.
     * @param x A JSONTokener object containing the source string.
     * @throws JSONException If there is a syntax error in the source string.
     */
    public JsonOrgJsonObject(JsonOrgJsonTokener x) throws JSONException {
        super(x);
        init();
    }


    /**
     * Construct a JSONObject from a Map.
     * 
     * @param map A map object that can be used to initialize the contents of
     *  the JSONObject.
     */
    public JsonOrgJsonObject(Map map) {
        super(map);
        init();
    }

    /**
     * Construct a JSONObject from a Map.
     * 
     * Note: Use this constructor when the map contains <key,bean>.
     * 
     * @param map - A map with Key-Bean data.
     * @param includeSuperClass - Tell whether to include the super class properties.
     */
    public JsonOrgJsonObject(Map map, boolean includeSuperClass) {
       	super(map, includeSuperClass);
       	init();
    }

    /**
     * Construct a JSONObject from an Object using bean getters.
     * It reflects on all of the public methods of the object.
     * For each of the methods with no parameters and a name starting
     * with <code>"get"</code> or <code>"is"</code> followed by an uppercase letter,
     * the method is invoked, and a key and the value returned from the getter method
     * are put into the new JSONObject.
     *
     * The key is formed by removing the <code>"get"</code> or <code>"is"</code> prefix. If the second remaining
     * character is not upper case, then the first
     * character is converted to lower case.
     *
     * For example, if an object has a method named <code>"getName"</code>, and
     * if the result of calling <code>object.getName()</code> is <code>"Larry Fine"</code>,
     * then the JSONObject will contain <code>"name": "Larry Fine"</code>.
     *
     * @param bean An object that has getter methods that should be used
     * to make a JSONObject.
     */
    public JsonOrgJsonObject(Object bean) {
    	super(bean);
    	init();
    }
    
    
    /**
     * Construct JSONObject from the given bean. This will also create JSONObject
     * for all internal object (List, Map, Inner Objects) of the provided bean.
     * 
     * -- See Documentation of JSONObject(Object bean) also.
     * 
     * @param bean An object that has getter methods that should be used
     * to make a JSONObject.
     * @param includeSuperClass - Tell whether to include the super class properties.
     */
    public JsonOrgJsonObject(Object bean, boolean includeSuperClass) {
    	super(bean, includeSuperClass);
    	init();
    }

 	/**
     * Construct a JSONObject from an Object, using reflection to find the
     * public members. The resulting JSONObject's keys will be the strings
     * from the names array, and the values will be the field values associated
     * with those keys in the object. If a key is not found or not visible,
     * then it will not be copied into the new JSONObject.
     * @param object An object that has fields that should be used to make a
     * JSONObject.
     * @param names An array of strings, the names of the fields to be obtained
     * from the object.
     */
    public JsonOrgJsonObject(Object object, String names[]) {
        super(object, names);
        init();
    }


    /**
     * Construct a JSONObject from a source JSON text string.
     * This is the most commonly used JSONObject constructor.
     * @param source    A string beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @exception JSONException If there is a syntax error in the source string.
     */
    public JsonOrgJsonObject(String source) throws JSONException {
        this(new JsonOrgJsonTokener(source));
        init();
    }

	@Override
	public int size() {
		return super.length();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return parentMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return parentMap.containsValue(value);
	}

	@Override
	public void putAll(Map m) {
		parentMap.putAll(m);		
	}


	@Override
	public void clear() {
		parentMap.clear();
	}


	@Override
	public Set keySet() {
		return parentMap.keySet();
	}


	@Override
	public Collection values() {
		return parentMap.values();
	}


	@Override
	public Set entrySet() {
		return parentMap.entrySet();
	}


	@Override
	public Object get(Object key) {
		return parentMap.get(key);
	}


	@Override
	public Object put(Object key, Object value) {
		return parentMap.put(key.toString(), value);
	}

	@Override
	public Object remove(Object key) {
		return parentMap.remove(key);
	}
    
	private final void init()
	{
		if(this.parentMap != null)
		{
			return;
		}
		Field[] fields = JSONObject.class.getDeclaredFields();
		for(Field field:fields)
		{
			if(Map.class.isAssignableFrom(field.getType()))
			{
				field.setAccessible(true);
				try
				{
					this.parentMap = (Map) field.get(this);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	public boolean equals(Object o)
	{
		if(o == null){
			return false;}
		if(Map.class.isAssignableFrom(o.getClass()) || 
				JsonOrgJsonObject.class.isAssignableFrom(
						o.getClass())){
			return o.equals(this.parentMap);
			
		}if(JSONObject.class.isAssignableFrom(o.getClass())){
			int index = 0;
			int length=((JSONObject)o).length();
			
			if(this.length()!=length){
				return false;
			}
			String[] names = JSONObject.getNames(((JSONObject)o));
			
			for(;index < length;index++){
				try{
					if((((JSONObject)o).get(names[index])==null 
							&& super.get(names[index])!=null)
						||!((JSONObject)o).get(names[index]).equals(
								super.get(names[index]))){
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
	
	private Map parentMap = null;
}
