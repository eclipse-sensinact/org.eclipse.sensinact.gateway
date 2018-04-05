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

package org.eclipse.sensinact.gateway.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides helpers to process cast operations
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class CastUtils
{
	private static final Logger LOGGER = Logger.getLogger(CastUtils.class.getCanonicalName());
	
	public static final String SPACE_DELIMITER = " ";
	public static final String COMMA_DELIMITER = ",";
	public static final String SEMICOLON_DELIMITER = ";";
	
	private static final List<Class<?>> PRIMITIVE_AND_WRAPPERS = 
			new ArrayList<Class<?>>();
	
	static
	{
		PRIMITIVE_AND_WRAPPERS.add(byte.class);
		PRIMITIVE_AND_WRAPPERS.add(short.class);
		PRIMITIVE_AND_WRAPPERS.add(int.class);
		PRIMITIVE_AND_WRAPPERS.add(float.class);
		PRIMITIVE_AND_WRAPPERS.add(long.class);
		PRIMITIVE_AND_WRAPPERS.add(double.class);
		PRIMITIVE_AND_WRAPPERS.add(boolean.class);
		PRIMITIVE_AND_WRAPPERS.add(char.class);
		PRIMITIVE_AND_WRAPPERS.add(Byte.class);
		PRIMITIVE_AND_WRAPPERS.add(Short.class);
		PRIMITIVE_AND_WRAPPERS.add(Long.class);
		PRIMITIVE_AND_WRAPPERS.add(Integer.class);
		PRIMITIVE_AND_WRAPPERS.add(Float.class);
		PRIMITIVE_AND_WRAPPERS.add(Double.class);
		PRIMITIVE_AND_WRAPPERS.add(Boolean.class);
		PRIMITIVE_AND_WRAPPERS.add(Character.class);
		PRIMITIVE_AND_WRAPPERS.add(String.class);
	}
	  
	/**
	 * Extends the {@link java.lang.Class#isPrimitive} method 
	 * by including the {@link java.lang.String} and java 
	 * primitive wrappers types in the test 
	 *   
	 * @param clazz
	 * 		the {@link Class} to test whether it is a
	 * 		"primitive" one
	 * @return
	 * 		<ul>
	 * 			<li>true if the {@link Class} is a "primitive" one;</li>
	 * 			<li>false otherwise</li>
	 * 		</ul>
	 */
	public static boolean isPrimitive(Class<?> clazz)
	{
		return PRIMITIVE_AND_WRAPPERS.contains(clazz);
	}
	
	/**
	 * Checks whether an instance of the clazz2 argument can 
	 * be casted into clazz1
	 * 
	 * @param clazz1
	 * 		the destination type
	 * @param clazz2
	 * 		the source type
	 * @return
	 * 		true if an instance of the specified source 
	 * 		type can be casted into the destination type
	 */
	public static boolean isAssignableFrom(Class<?> clazz1, Class<?> clazz2)
	{
		if(!clazz1.isAssignableFrom(clazz2))
		{
			Class<?> wrapperclazz = CastUtils.getWrapperClass(clazz1);
			Class<?> wrapperClass2 = CastUtils.getWrapperClass(clazz2);
			
			if(wrapperclazz != null && wrapperClass2!=null 
					&& wrapperclazz.isAssignableFrom(wrapperClass2))
			{
				return true;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Returns the wrapper class of the primitive one passed
	 * as parameter
	 * 
	 * @param clazz
	 * 		the primitive type to return the wrapper class of
	 * @return
	 * 		 the wrapper class of the specified primitive one
	 */
	public static Class<?> getWrapperClass(Class<?> clazz)
	{
		if(!CastUtils.isPrimitive(clazz) 
				|| clazz == String.class)
		{
			return null;				
		}
		if(!clazz.isPrimitive())
    	{
    		return clazz;
    	}
    	if(clazz ==  byte.class)
    	{
    		return Byte.class;
    	}
    	if(clazz ==  short.class)
    	{
    		return Short.class;
    	}
    	if(clazz ==  int.class)
    	{
    		return Integer.class;
    	}
    	if(clazz ==  float.class)
    	{
    		return Float.class;
    	}
    	if(clazz ==  long.class)
    	{
    		return Long.class;
    	}
    	if(clazz ==  double.class)
    	{
    		return Double.class;
    	}
        if(clazz ==  boolean.class)
        {
            return Boolean.class;
        }
        if(clazz   ==  char.class)
        {
            return Character.class;
        }
		return null;
	}
	
	/**
	 * Loads and returns the Class whose name is passed 
	 * as parameter, using the appropriate ClassLoader
	 * 
	 * @param classloader
	 * 		the appropriate ClassLoader
	 * @param type
	 * 		the name of the Class to load
	 * @return		
	 * 		the load Class, or null if it has not been resolved
	 * @throws ClassNotFoundException 
	 */
	public static Class<?> loadClass(ClassLoader classloader,
			String type) throws ClassNotFoundException
	{
		if(type == null || classloader == null)
		{
			return null;
		}
		Class<?> clazz = null;
		
		boolean arrayType = type.startsWith(
				CastUtils.ARRAY_OF_PREFIX);
		
		// retrieves type
		String rawType = (arrayType? type.substring(
				CastUtils.ARRAY_OF_PREFIX.length())
				: type).trim();
		
		Class<?> rawClass = CastUtils.jsonTypeToJavaType(
				rawType);
		
		if (rawClass == null)
		{
			rawClass = classloader.loadClass(rawType);
		}
		if (!arrayType)
		{
			clazz = rawClass;
			
		} else
		{
			clazz = Array.newInstance(rawClass, 0
					).getClass();
		}
		return clazz;
	}
	
	/**
	 * Returns the name of the Class passed as parameter in a
	 * JSON compatible way (tries to at least)
	 * 
	 * @param clazz
	 * 		the Class to return the name of
	 * @return		
	 * 		the JSON compatible formated specified Class's name
	 */
	public static String writeClass(Class<?> clazz)
	{		
		Class<?> rawType = clazz.isArray()
				?clazz.getComponentType():clazz;
		
		String jsonType = CastUtils.javaTypeToJsonType(rawType);
		if(jsonType == null)
		{
			jsonType = rawType.getCanonicalName();
		}
		String typeName = clazz.isArray()
				?CastUtils.ARRAY_OF_PREFIX.concat(jsonType)
						:jsonType;
		return typeName;
	}
	
    /**
     * Returns the java class corresponding to the 
     * specified JSON type
     *  
     * @param jsonType
     *      the json type
     * @return
     *      the java class
     */
    public static Class<?> jsonTypeToJavaType(String jsonType)
    {
    	if (jsonType == null || jsonType.length()==0)
    	{
    		return null;
    	}
    	int index = jsonType.lastIndexOf('.');
    	String type = jsonType.substring(index+1).toLowerCase();    	
    		
    	if(type.intern() ==  "string".intern())
    	{
    		return String.class;
    	}
        if(type.intern() ==  "byte".intern())
        {
            return byte.class;
        }
        if(type.intern() ==  "short".intern())
        {
            return short.class;
        }
        if(type.intern() ==  "int".intern() 
        		|| type.intern()=="integer".intern())
        {
            return int.class;
        }
        if(type.intern() ==  "float".intern())
        {
            return float.class;
        }
        if(type.intern() ==  "long".intern())
        {
            return long.class;
        }
        if(type.intern() ==  "double".intern())
        {
            return double.class;
        }
        if(type.intern() ==  "boolean".intern())
        {
            return boolean.class;
        }
        if(type.intern() ==  "char".intern())
        {
            return char.class;
        }
        if(type.intern() ==  "object".intern() || 
        		type.intern() == "jsonobject".intern())
    	{
    		return JSONObject.class;
    	}
        if(type.intern() ==  "array".intern() || 
        		type.intern() == "jsonarray".intern())
    	{
    		return JSONArray.class;
    	}
        return null;
    }
    
    /**
     * Returns the JSON Type corresponding to the 
     * specified Java class passed as parameter
     *  
     * @param javaType
     *      the java class
     * @return
     *      the JSON type
     */
    public static String javaTypeToJsonType(Class<?> javaType)
    {
        if(char.class == javaType || Character.class == javaType)
        {
            return "string";
        }
        if(JSONObject.class == javaType)
    	{
    		return "object";
    	}
        if(JSONArray.class ==  javaType)
    	{
    		return "array";
    	}
        if(CastUtils.isPrimitive(javaType))
        {
        	String canonicalName =  javaType.getCanonicalName();
        	int index = canonicalName.lastIndexOf('.');
        	
        	String jsonType = canonicalName.substring(index+1);
        	jsonType = jsonType.toLowerCase();
        	if(jsonType.intern()=="integer".intern())
        	{
        		jsonType = "int";
        	}
        	return jsonType;
        }
        return null;
    }
    
    /**
     * Returns the Comparable wrapper of the 
     * java primitive type passed as parameter
     *  
     * @param type
     *      the java primitive type
     * @return
     *      the java Comparable wrapper class of 
     *      the specified primitive type
     */
    public static Class<?> primitiveToComparable(
    		Class<?> type)
    {
    	if(!type.isPrimitive())
    	{
    		return null;
    	}
    	if(type ==  byte.class)
    	{
    		return Byte.class;
    	}
    	if(type ==  short.class)
    	{
    		return Short.class;
    	}
    	if(type ==  int.class)
    	{
    		return Integer.class;
    	}
    	if(type ==  float.class)
    	{
    		return Float.class;
    	}
    	if(type ==  long.class)
    	{
    		return Long.class;
    	}
    	if(type ==  double.class)
    	{
    		return Double.class;
    	}
        if(type ==  boolean.class)
        {
            return Boolean.class;
        }
        if(type ==  char.class)
        {
            return Character.class;
        }
        return null;
    }

	/**
	 * Casts the specified object argument into 
	 * the type passed as parameter
	 *
     * @param classloader
     * 		the appropriate classloader
	 * @param clazz
	 * 		the destination type
	 * @param object
	 * 		the object to cast 
	 * @return
	 * 		the typed object
	 * 
	 * @throws ClassCastException
	 * 		if the cast is not possible
	 */
	public static <T> T cast(ClassLoader classloader, 
			Class<T> clazz, Object object) 
			throws ClassCastException
	{
		if(object == null)
		{ 
			return null;
		}
    	if(clazz.isAssignableFrom(object.getClass()))
    	{
    		return (T) object;
    	}
        if(JSONObject.class.isAssignableFrom(object.getClass()) || 
           JSONArray.class.isAssignableFrom(object.getClass()))
        {
            return CastUtils.<T>getObjectFromJSON(
            		classloader, clazz, object);
        }
    	if(CastUtils.isPrimitive(clazz))
		{
			return (T) CastUtils.castPrimitive(clazz,object);
		}  
        if(clazz.isArray())
        {
            return (T) CastUtils.castArray(classloader, clazz,object);
        } 
    	String message = new StringBuilder("Unable to cast "
            ).append(object.getClass().getSimpleName()).append(" into "
                  ).append(clazz.getSimpleName()).toString();

		if(String.class.isAssignableFrom(object.getClass()))
		{
	        if(clazz.isEnum())
	        {
	            return (T) Enum.valueOf((Class<Enum>)clazz,
	            		(String)object);
	            
	        } else
	        {
				JSONObject objectJSONObject = null;
				JSONArray objectJSONArray = null;
				Object objectJSON = null;         
	            try
	            {
	               objectJSONObject = new JSONObject((String)object);  
	               objectJSON = objectJSONObject;
	                
	            }catch(JSONException e)
	            {
	    			try
	    			{
	    			    objectJSONArray = new JSONArray((String)object);
	    			    objectJSON = objectJSONArray;	
	    			    
	    			}catch(JSONException ej)
	    			{
	    			    if(JSONObject.class.isAssignableFrom(clazz))
				        {
	                        throw new ClassCastException(e.getMessage());
	                        
				        } else if(JSONArray.class.isAssignableFrom(clazz))
				        {
				            throw new ClassCastException(ej.getMessage());
				        }
	    			    throw new ClassCastException(message);
	    			}
	            }
	            return CastUtils.cast(classloader, clazz, objectJSON);		    
	        }
		}
        throw new ClassCastException(message);
	}
	
	/**
	 * Cast the object passed as parameter into the Array class 
	 * also passed as parameter
	 *
     * @param classloader
     * 		the appropriate classloader
	 * @param clazz
	 * 		the destination Array class to cast the object argument into
	 * @param object
	 * 		object to cast into the Array class
	 * @return
	 * 		the clazz argument typed array
	 * 
	 * @throws ClassCastException
	 * 		if the cast is not possible
	 */
	public static <T> T castArray(ClassLoader classloader, 
			Class<T> clazz, Object object)
			throws ClassCastException
	{
		if(!clazz.isArray())
		{
			throw new ClassCastException(
				"Destination Class is not an Array one");
		}
    	if(clazz.isAssignableFrom(object.getClass()))
    	{
    		return (T) object;
    	}
		if(object.getClass().isArray())
		{
			int length = Array.getLength(object);
			Class<?> componentType = clazz.getComponentType();
			Object array = Array.newInstance(componentType, length);
			
			for(int i = 0; i<length; i++)
			{
				Array.set(array, i, CastUtils.cast(classloader,
					componentType, Array.get(object,i)));
			}
			return (T) array;
		}
		if(JSONArray.class.isAssignableFrom(object.getClass()))
		{
			int length = ((JSONArray)object).length();
			Class<?> componentType = clazz.getComponentType();
			Object array = Array.newInstance(componentType, length);
			
			for(int i = 0; i<length; i++)
			{
				Array.set(array, i, CastUtils.cast(classloader,
					componentType, ((JSONArray)object).get(i)));
			}
			return (T) array;
		}
		if(String.class.isAssignableFrom(object.getClass()))
		{   
			String objectStr = (String)object;

			T jsonArray = CastUtils.getObjectFromJSON(classloader, 
					clazz, objectStr);
			if(jsonArray != null)
			{
				return jsonArray;
			}
			if(char[].class.equals(clazz))
			{
				return (T) objectStr.toCharArray();
			}
			if(Character[].class.equals(clazz))
			{
				char[] chars = objectStr.toCharArray();
				Character[] characters = new Character[chars.length];
				for(int i = 0; i< chars.length; i++)
				{
					characters[i] = Character.valueOf(chars[i]);
				}
				return (T) characters;
			}
			objectStr = objectStr.replace('[', ' ').replace(
					']', ' ').trim();
			return (T) CastUtils.<T>castArray(classloader, 
					clazz, objectStr, CastUtils.COMMA_DELIMITER);
		}
		throw new ClassCastException(
		"the object to cast is neither an instance of an Array class nor a String one");
	}
	
	/**
	 * Cast the String object passed as parameter into the Array class 
	 * also passed as parameter using the String delimiter argument
	 * to distinguish each element of the array to create
	 *
     * @param classloader
     * 		the appropriate classloader
	 * @param clazz
	 * 		the destination Array class to cast the String argument into
	 * @param objectStr
	 * 		the String object to cast into the Array class
	 * @param delimiter
	 * 		the String delimiter used to distinguish each element
	 * 		of the array
	 * @return
	 * 		the clazz argument typed array
	 * 
	 * @throws ClassCastException
	 * 		if the cast is not possible
	 */
	public static <T> T castArray(ClassLoader classloader,
			Class<T> clazz, String objectStr, 
			String delimiter) throws ClassCastException
	{
		if(!clazz.isArray())
		{
			throw new ClassCastException(
				"Destination Class is not an Array one");
		}
		if(objectStr.indexOf(delimiter) == -1)
		{
			throw new ClassCastException("Delimiter not found");
		}
		return (T) CastUtils.castArray(classloader, 
				clazz, objectStr.split(delimiter));
	}
	
	/**
	 * Cast the object passed as parameter into the primitive 
	 * class also passed as parameter
	 * 
	 * @param clazz
	 * 		the destination primitive class to cast the object 
	 * 		argument into
	 * @param object
	 * 		the object to cast into the primitive class
	 * @return
	 * 		the clazz argument typed primitive
	 * 
	 * @throws ClassCastException
	 * 		if the cast is not possible
	 */
	public static <T> T castPrimitive(Class<T> clazz, Object object)
		throws ClassCastException
	{
		if(!CastUtils.isPrimitive(clazz))
		{
			throw new ClassCastException(
					"Destination Class is not a Primitive one");
		}
    	if(clazz.isAssignableFrom(object.getClass()))
    	{
    		return (T) object;
    	}
		Object primitive = null;
		
		if(String.class == object.getClass())
		{
			primitive = CastUtils.<T>getPrimitiveFromString(
				clazz, (String)object);
		}
		else if(Character.class.isAssignableFrom(object.getClass())
			|| char.class.isAssignableFrom(object.getClass()))
        {
            primitive = CastUtils.<T>getPrimitiveFromString(clazz,
            	new String(new char[]{((Character)object).charValue()}));
        } 
        else if(Boolean.class.isAssignableFrom(object.getClass())
        	|| boolean.class.isAssignableFrom(object.getClass()))
        {
            primitive = CastUtils.<T>getPrimitiveFromString(clazz, 
            	Boolean.toString((Boolean) object));
            
        } else if(Number.class.isAssignableFrom(object.getClass()))
        {
            primitive = CastUtils.<T>getPrimitiveFromNumber(
            	clazz,(Number)object);
            
        } else if(object.getClass().isPrimitive())
        {
            primitive = CastUtils.<T>getPrimitiveFromNumber(
            	clazz, Double.valueOf(String.valueOf(object)));
        }  
        else if(Enum.class.isAssignableFrom(object.getClass()))
        {
        	if(String.class == clazz)
        	{
        		primitive = CastUtils.<T>getPrimitiveFromString(
        			clazz, ((Enum) object).name());
        	} else
        	{
        		primitive = CastUtils.<T>getPrimitiveFromNumber(
        			clazz, ((Enum) object).ordinal());
        	}
        } 
		if(primitive == null)
		{
			throw new ClassCastException(String.format(
			" [%s as %s] The object cannot be cast into neither a String nor a Number",
			String.valueOf(object),clazz.getName()));
		}
		return (T) primitive;
	}

    /**
     * Cast the object passed as parameter into a Comparable one
     * 
     * @param object
     *      the Object to cast
     * @return
     *      the object passed as parameter casted into a Comparable 
     *      one
     */
    @SuppressWarnings("rawtypes")
    public static Comparable castComparable(Object object)
    { 
        Class<?> clazz = object.getClass();
        if(Comparable.class.isAssignableFrom(clazz))
        {
            return (Comparable)object;
        } 
        //boolean, byte, char, short, int, long, float, double
        //and wrappers
        Double dble = primitiveNumberToDouble(object);
        if(dble != null)
        {
            return dble;
        }
        if(char.class == clazz)
        {
            return (Character)object;
        }
        if(boolean.class == clazz)
        {
            return (Boolean)object;
        }
        if(clazz.isEnum())
        {
            return ((Enum<?>)object).name();
        }       
        return null;
    }
    
	/**
     * Types and returns the String value passed as parameter
     * according to its destination type also passed as parameter
     * 
     * @param clazz
     * 		the destination type of the string value passed
     * 		as parameter 
     * @param value
     * 		the string value to cast
     * @return
     * 		the casted number value
     */
	public static <T> Object getPrimitiveFromString(
			Class<T> clazz, String value) 
    {
    	if(clazz == String.class)
    	{
    		return value;
    	}
    	if(clazz == char.class)
    	{
    		return value.charAt(0);
    	}
        if(clazz == Character.class)
        {
            return new Character(value.charAt(0));
        }
        if(clazz == boolean.class)
        {
            return Boolean.parseBoolean(value);
        }
        if(clazz == Boolean.class)
        {
            return new Boolean(Boolean.parseBoolean(value));
        }
        Double valueDouble = null;
        try
        {
            valueDouble = new Double(Double.parseDouble(value)); 
        
        } catch(NumberFormatException e)
        {
            throw new ClassCastException(e.getMessage()); 
        }
        if (clazz == Byte.class)
        {
            return new Byte(valueDouble.byteValue());
        }
        if(clazz == byte.class)
        {
            return valueDouble.byteValue();
        }
        if (clazz == short.class)
        {
            return valueDouble.shortValue();
        }
        if (clazz == Short.class)
        {
            return new Short(valueDouble.shortValue());
        }
        if (clazz == int.class) 
        {
            return valueDouble.intValue();
        }
        if (clazz == Integer.class)
        {
            return new Integer(valueDouble.intValue());
        }
        if (clazz == double.class)
        {
            return valueDouble.doubleValue();
        }
        if (clazz == Double.class)
        {
            return valueDouble;
        }
        if (clazz == float.class) 
        {
            return valueDouble.floatValue();
        }
        if (clazz == Float.class)
        {
            return new Float(valueDouble.floatValue());
        }
        if (clazz == long.class)
        {
            return valueDouble.longValue();
        }
        if (clazz == Long.class)
        {
            return new Long(valueDouble.longValue());
        }
        return null;
    }
    
    /**
     * Types and returns the number value passed as parameter
     * according to its destination type also passed as parameter
     * 
     * @param clazz
     * 		the destination type of the number value passed
     * 		as parameter 
     * @param value
     * 		the number value to cast
     * @return
     * 		the casted number value
     */
    public static <T> Object getPrimitiveFromNumber(
    		Class<T> clazz, Number value) 
    {
        if (clazz == Byte.class)
        {
            return new Byte(value.byteValue());
        }
        if (clazz == byte.class)
        {
            return value.byteValue();
        }
        if (clazz == Short.class)
        {
            return new Short(value.shortValue());
        }
        if (clazz == short.class)
        {
            return value.shortValue();
        }
        if (clazz == Integer.class) 
        {
            return new Integer(value.intValue());
        }
        if (clazz == int.class) 
        {
            return value.intValue();
        }
        if (clazz == Double.class) 
        {
            return new Double(value.doubleValue());
        }
        if (clazz == double.class) 
        {
            return value.doubleValue();
        }
        if (clazz == Float.class) 
        {
            return new Float(value.floatValue());
        }
        if (clazz == float.class) 
        {
            return value.floatValue();
        }
        if (clazz == Long.class)
        {
            return new Long(value.longValue());
        }
        if (clazz == long.class)
        {
            return value.longValue();
        }
        if (clazz == String.class)
        {
            return String.valueOf(value);
        }
        if (clazz == char.class)
        {
            return value.toString().charAt(0);
        }
        if (clazz == Character.class)
        {
            return Character.valueOf(value.toString().charAt(0));
        }
        return null;
    }

    /**
     * Cast the number object passed as parameter into 
     * a Double one
     * 
     * @param object
     *      the number object to cast
     * @return
     *      the object passed as parameter casted into a 
     *      Double one
     */
    public static Double primitiveNumberToDouble(Object object)
    {
        Class<?> clazz = object.getClass();
        
        if(Number.class.isAssignableFrom(clazz))
        {
            return (Double)((Number)object).doubleValue();
        }            
        //byte, short, int, long, float, and double
        if(clazz.isPrimitive())
        {
            if(byte.class == object.getClass())
            {
                return new Double(((Byte) object).doubleValue());
            }
            if(short.class == object.getClass())
            {
                return new Double(((Short) object).doubleValue());
            }
            if(int.class == object.getClass())
            {
                return new Double(((Integer) object).doubleValue());
            }     
            if(long.class == object.getClass())
            {
                return new Double(((Long) object).doubleValue());
            }     
            if(float.class == object.getClass())
            {
                return new Double(((Float) object).doubleValue());
            }     
            if(double.class == object.getClass())
            {
                return (Double) object;
            } 
        }	
        return null;
    }
        
    /**
	 * Converts and returns a JSON object into the Java one
	 * it describes
	 *  
	 * @param clazz
	 * 		the Class of the Java object to instantiate
	 * @param json
	 * 		the JSON object to convert
	 * @return
	 * 		the created Java object
	 */
	@SuppressWarnings("unchecked")
    public static <T> T getObjectFromJSON(
    		ClassLoader classLoader, 
    		Class<T> clazz, Object json) 
	{	
	    if(json == null)
	    {
	        return (T)null;
	    }
	    if(clazz.isAssignableFrom(json.getClass()))
	    {
	        return (T)json;
	    }
	    String message = new StringBuilder("Unable to turn JSON ").append(
				json.toString()).append(" into Java [").append(clazz.getName()+ "]"
						).toString();
	   
		if(CastUtils.isPrimitive(clazz))
		{
			if(json.getClass().equals(String.class))
			{
				return (T) CastUtils.getPrimitiveFromString(clazz,(String)json);
			
			} else if(Number.class.isAssignableFrom(json.getClass()))
			{
				return (T) CastUtils.getPrimitiveFromNumber(clazz,(Number)json);
			
			} else if(JSONObject.class.isAssignableFrom(json.getClass()) && 
					String.class.isAssignableFrom(clazz))
			{
                	return (T)((JSONObject)json).toString();
                	
			} else if(JSONArray.class.isAssignableFrom(json.getClass()) && 
					String.class.isAssignableFrom(clazz))
			{
                	return (T)((JSONArray)json).toString();
			} 
			return CastUtils.castPrimitive(clazz, json);
			
		} else if(JSONObject.class.isAssignableFrom(json.getClass()))
        {
            return getObjectFromJSON(classLoader, clazz, (JSONObject)json);
                
        } else if(JSONArray.class.isAssignableFrom(json.getClass()))
        {
            return getObjectFromJSON(classLoader, clazz, (JSONArray)json);
            
        } else if(json.getClass().equals(String.class))
		{	
			try
			{
				return getObjectFromJSON(classLoader, 
						clazz, new JSONObject((String)json));
				
			} catch(JSONException e)
			{
				try
				{
					return getObjectFromJSON(classLoader, 
							clazz, new JSONArray((String)json));
					
				} catch(JSONException je)
				{
					LOGGER.log(Level.CONFIG, je.getMessage(),je);
				}
			}
		}
		//last chance
	    try
        {
           return clazz.getConstructor(json.getClass()
        		   ).newInstance(json);
           
        }catch(Exception e)
        {
			LOGGER.log(Level.CONFIG, e.getMessage(), e);
        }
		return null;
	}
	
	/**
	 * Converts and returns a JSONObject instance into the Java
	 * object it describes
	 *
	 * @param classloader
	 * 		the appropriate classloader
	 * @param clazz
	 * 		the Class of the Java object to instantiate
	 * @param jsonObject
	 * 		the JSONObject to convert
	 * @return
	 * 		the created Java object
	 */
	@SuppressWarnings("unchecked")
    public static <T> T getObjectFromJSON(
    		ClassLoader classloader,
			Class<T> clazz, JSONObject jsonObject) 
	{	
	    if(JSONObject.NULL.equals(jsonObject))
	    {
	        return (T)null;
	    }
		if(String.class.isAssignableFrom(clazz))
		{
            return (T)jsonObject.toString();
         }		
		if(clazz.isArray())
		{
		   Class<?> componentType = clazz.getComponentType();
		   Object array = Array.newInstance(componentType, 
				   jsonObject.length());
		   String[] names = JSONObject.getNames(jsonObject);
		   
		   int index = 0;	    		
	       for(;index<names.length;index++)
	       {
	    		Array.set(array, index, getObjectFromJSON(
	    			classloader, componentType,jsonObject.get(
	    					names[index])));
	       }
	       return (T) array;	    	
		}
		return ReflectUtils.instantiate(classloader, clazz, jsonObject);	
	}

	/**
	 * Converts and returns a JSONArray instance into the Java
	 * object it describes
	 *
     * @param classloader
     * 		the appropriate classloader
	 * @param clazz
	 * 		the Class of the Java object to instantiate
	 * @param jsonArray
	 * 		the JSONArray to convert
	 * @return
	 * 		the created Java object
	 */
	public static <T> T getObjectFromJSON(ClassLoader classloader, 
			Class<T> clazz, JSONArray jsonArray) 
	{	
	    if(JSONObject.NULL.equals(jsonArray))
	    {
	        return (T)null;
	    }
		if(clazz.isAssignableFrom(JSONArray.class))
		{
             return (T)jsonArray;
         }	
		if(String.class.isAssignableFrom(clazz))
		{
            return (T)jsonArray.toString();
        }		
		if(clazz.isArray())
		{
			Class<?> componentType = clazz.getComponentType();
			Object array = Array.newInstance(componentType, jsonArray.length());
		    int index = 0;	    		
		   
		    for(;index<jsonArray.length();index++)
		    {
		    	Array.set(array, index, getObjectFromJSON(
		    		classloader, componentType, jsonArray.get(
		    				index)));
		    }		    
		    return (T) array;
		}
		return ReflectUtils.instantiate(classloader, clazz, jsonArray);
	}
	
	
	
	/**
	 * Converts and returns the JSON formated string passed as 
	 * parameter into a {@link Map}
	 *
     * @param classloader
     * 		the appropriate classloader
     * @param clazz
     *      the Class of the Java object to instantiate
     * @param subtype
     *      the type of the Java object to instantiate
	 * @param jsonString
	 * 		the JSON formated string to convert into a {@link Map}
	 * @return
	 * 		the {@link Map} built from the {@link JSONObject} 
	 */
	public static <S extends Object, T extends Map<String,S>> T toMap(
    		ClassLoader classloader,
	        Class<T> clazz, 
	        Class<S> subtype, 
	        String jsonString) 
	{
		if(jsonString != null && jsonString.length() > 0)
		{
			try
			{
				return toMap(classloader,
				 clazz,  subtype,  new JSONObject(jsonString));
				
			}catch(JSONException e)
			{
				LOGGER.log(Level.CONFIG,e.getMessage(),e);
			}
		}
		return null;
	}
	
	/**
	 * Converts and returns the {@link JSONObject} object passed as 
	 * parameter into a {@link Map}
	 *
     * @param classloader
     * 		the appropriate classloader
     * @param clazz
     *      the Class of the Java object to instantiate
     * @param subtype
     *      the type of the Java object to instantiate
	 * @param jsonObject
	 * 		the {@link JSONObject} object to convert into a {@link Map}
	 * @return
	 * 		the {@link Map} built from the {@link JSONObject} 
	 */
	@SuppressWarnings("unchecked")
    public static <S extends Object,T extends Map<String,S>> T toMap(
    		ClassLoader classloader,
    		Class<T> clazz, 
    		Class<S> subtype, 
    		JSONObject jsonObject)
	{
		T map = null;	
		if(clazz.equals(Map.class))
		{
		    clazz = (Class<T>) new HashMap<String,S>().getClass();
		}
		try
        {
			map = clazz.newInstance();       
    		if(!JSONObject.NULL.equals(jsonObject))
    		{
    			String[] names = JSONObject.getNames(jsonObject);
    			if(names == null)
    			{
    				return null;
    			}
    			int position = 0;			
    			int length = names.length;
    			for(;position<length;position++)
    			{
    				S object = getObjectFromJSON(
    					classloader, subtype,  jsonObject.get(
    							names[position]));
    				if(object == null)
    				{
    					continue;
    				}			
    				map.put(names[position],object);
    			}
    		}
        } catch (Exception e)
        {
			LOGGER.log(Level.CONFIG, e.getMessage(),e);
        }				
		return map;
	}
	
	/**
	 * Converts and returns the {@link JSONArray} object passed as 
	 * parameter into a {@link List}
	 *
     * @param classloader
     * 		the appropriate classloader
     * @param clazz
     *      the Class of the Java object to instantiate
     * @param subtype
     *      the type of the Java object to instantiate
	 * @param jsonArray
	 * 		the {@link JSONArray} object to convert into a {@link List}
	 * @return
	 * 		the {@link List} built using the {@link JSONArray} 
	 */
	public static <S extends Object,T extends List<S>> T toList(
    		ClassLoader classloader,
	        Class<T> clazz, 
	        Class<S> subtype, 
	        JSONArray jsonArray)
	{
	    T list = null;
        if(clazz.equals(List.class))
        {
            clazz = (Class<T>) new ArrayList<S>().getClass();
        }
        try
        {
            list = clazz.newInstance();
            if(!JSONObject.NULL.equals(jsonArray))
            {
    			int position = 0;
    			int length = jsonArray.length();
    			for(;position<length;position++)
    			{
    				S object = getObjectFromJSON(
    			    	classloader, subtype, jsonArray.get(
    			    			position));
    				if(object == null)
    				{
    					continue;
    				}			
    				list.add(object);
    			}
            }
        } catch (Exception e)
        {
			LOGGER.log(Level.CONFIG, e.getMessage(),e );
            return null;
        }
		return list;
	}
	
	/**
	 * Converts and returns the JSON formated string passed as 
	 * parameter into a {@link Map}
	 * 
	 * @param jsonString
	 * 		the JSON formated string to convert into a {@link Map}
	 * @return
	 * 		the {@link Map} built from the {@link JSONObject} 
	 */
	public static Map toMap(String jsonString) 
	{
		if(jsonString != null && jsonString.length() > 0)
		{
			try
			{
				return toMap(new JSONObject(jsonString));
				
			}catch(JSONException e)
			{
				//e.printStackTrace();
				System.out.println(e.getMessage());
			}
		}
		return null;
	}
	
	/**
	 * Converts and returns the {@link JSONObject} object passed as 
	 * parameter into a {@link Map}
	 * 
	 * @param jsonObject
	 * 		the {@link JSONObject} object to convert into a {@link Map}
	 * @return
	 * 		the {@link Map} built from the {@link JSONObject} 
	 */
	public static Map toMap(JSONObject jsonObject)
	{
		if(JSONObject.NULL.equals(jsonObject) ||(jsonObject.length() == 0))
		{
			return Collections.emptyMap();
		}
		Map<String, Object> map = new HashMap<String,Object>();
		String[] names = JSONObject.getNames(jsonObject);
		if(names == null)
		{
			return null;
		}
		int position = 0;			
		int length = names.length;
		for(;position<length;position++)
		{
			Object object = jsonObject.get(names[position]);
			if(object == null)
			{
				continue;
			}					
			if(JSONObject.class.isAssignableFrom(object.getClass()))
			{
				map.put(names[position],toMap((JSONObject)object));
				
			} else if(JSONArray.class.isAssignableFrom(object.getClass()))
			{
				map.put(names[position],toList((JSONArray)object));
			
			} else
			{
				map.put(names[position], object);
			}
		}
		return map;
	}
	
	/**
	 * Converts and returns the {@link JSONArray} object passed as 
	 * parameter into a {@link List}
	 * 
	 * @param jsonArray
	 * 		the {@link JSONArray} object to convert into a {@link List}
	 * @return
	 * 		the {@link List} built using the {@link JSONArray} 
	 */
	public static List toList(JSONArray jsonArray)
	{
		if(jsonArray == null || (jsonArray.length() == 0))
		{
			return Collections.emptyList();
		}
		List<Object> list = new ArrayList<Object>();
		int position = 0;
		int length = jsonArray.length();
		for(;position<length;position++)
		{
			Object object = jsonArray.get(position);
			if(object == null)
			{
				continue;
			}					
			if(JSONObject.class.isAssignableFrom(object.getClass()))
			{
				list.add(toMap((JSONObject)object));
				
			} else if(JSONArray.class.isAssignableFrom(object.getClass()))
			{
				list.add(toList((JSONArray)object));
			
			} else
			{
				list.add(object);
			}
		}	
		return list;
	}
	
	/**
	 * Returns a copy of the value of this Primitive.
	 * Only allowed types are handled
	 *
     * @param type
     *      the type of the object to copy
	 * @param value
	 * 		the object to return the copy of
	 * @return 
	 * 		a copy of this Primitive's value
	 */
	public static Object copy(Class<?> type, Object value)
	{
		if(value == null)
		{
			return null;
		}
		if (type.isPrimitive()||type.isEnum())
		{
			return value;
		}
		if(type.isArray())
		{
			int length = Array.getLength(value);
			int index = 0;
			
			Object copy = Array.newInstance(
					type.getComponentType(), length);
			
			for(;index < length; index++)
			{
				Array.set(copy, index, CastUtils.copy(
					type.getComponentType(), Array.get(
							value, index)));
			}
			return copy;
		}
		if (CastUtils.isPrimitive(type))
		{
			if (type == String.class)
			{
				return new String((String) value);
			}
			if (type == Character.class)
			{
				return ((Character) value).charValue();
			}
			if (type == Boolean.class)
			{
				return ((Boolean) value).booleanValue();
			}
			if (type == Byte.class)
			{
				return ((Byte) value).byteValue();
			}
			if (type == Short.class)
			{
				return ((Short) value).shortValue();
			}
			if (type == Integer.class)
			{
				return ((Integer) value).intValue();
			}
			if (type == Long.class)
			{
				return ((Long) value).longValue();
			}
			if (type == Float.class)
			{
				return ((Float) value).floatValue();
			}
			if (type == Double.class)
			{
				return ((Double) value).doubleValue();
			}
		}
		if (JSONObject.class.isAssignableFrom(type))
		{
			if(JSONObject.NULL.equals(value))
			{
				return null;
			}
			String[] names = JSONObject.getNames((JSONObject) value);
			if(names == null || names.length == 0)
			{
				return new JSONObject();
			}
			return new JSONObject(((JSONObject) value),names);
		}
		if (JSONArray.class.isAssignableFrom(type))
		{
			return new JSONArray(((JSONArray) value).toString());
		}
		Class<?> alternativeClass = null;
		if(Object.class == type 
		&& Object.class!=(alternativeClass=value.getClass()))
		{
			return CastUtils.copy(alternativeClass,value);
		}
		return value;
	}

	/**
	 * {@link Array} type name prefix
	 */
	public static final String ARRAY_OF_PREFIX = "Array of ";
}
