/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A StringPatternValue wraps the fixed String part of a String pattern 
 */
public class StringPatternValue {
	
	private static final Logger LOG = Logger.getLogger(StringPatternValue.class.getName());
	
	public static final String PATTERN_STR = "\\$\\(([^\\)]+)\\)";
	
	public static final Pattern PATTERN;
	
	static {
		Pattern pattern = null;
		try {
			pattern = Pattern.compile(PATTERN_STR);
		}catch(Exception e) {
			LOG.log(Level.SEVERE,e.getMessage(),e);
		}
		PATTERN = pattern;
	}
	
	private final class DefaultVariablePatternValue implements VariablePatternValue {

		private final AtomicInteger count;
		
		/**
		 * Constructor
		 */
		DefaultVariablePatternValue(){
			this.count = new AtomicInteger(0);
		}
		
		@Override
		public String next() {
			return String.valueOf(this.count.getAndIncrement());
		}
		
		@Override
		public String get() {
			int c = this.count.get();
			if(c == 0)
				return null;
			return String.valueOf((c-1));
		}
		
		@Override
		public void reset() {
			this.count.set(0);
		}
	}
	
	private final String value;
	private final String raw;
	private final boolean isPattern;

	private VariablePatternValue variable;
	
	/**
	 * Constructor
	 * 
	 * @param value the String value wrapped by the StringPatternValue to be instantiated
	 */
	public StringPatternValue(String value) {
		this(null,value);
	}
	
	/**
	 * Constructor
	 * 
	 * @param variable 
	 * @param value the String value wrapped by the StringPatternValue to be instantiated
	 */
	public StringPatternValue(VariablePatternValue variable, String value) {
		if(value == null || PATTERN == null) {
			this.value = null;
			this.raw = null;
			this.isPattern = false;
		} else {
			this.value = value;
			Matcher matcher = PATTERN.matcher(value);
			this.isPattern = matcher.matches();
			if(this.isPattern)
				this.raw = matcher.group(1);
			else 
				this.raw = null;
		}
		this.variable = variable==null?new DefaultVariablePatternValue():variable;
	}

	/**
	 * Returns true if this StringPatternValue's wrapped String described a pattern;
	 * returns false otherwise
	 * 
	 * @return true if the wrapped String is a pattern; false otherwise
	 */
	public boolean isPattern() {
		return this.isPattern;
	}
	
	/**
	 * Returns the value of this StringPatternValue, meaning the all wrapped String if it 
	 * does not describe a pattern, or the concatenation of the fixed part of the pattern
	 * with an integer counter value
	 *  
	 * @return this StringPatternValue's String value
	 */
	public String build() {
		if(!this.isPattern)
			return this.value;		
		return new StringBuilder().append(this.raw
			).append("_").append(variable.next()).toString();
	}	

	/**
	 * Returns the raw value of this StringPatternValue, meaning the all wrapped String if it 
	 * does not describe a pattern, or the fixed part of the pattern
	 *  
	 * @return this StringPatternValue's raw String value
	 */
	public String getLast() {
		if(!this.isPattern)
			return this.value;	
		String var = this.variable.get();
		if(var == null)
			var = this.variable.next();
		return new StringBuilder().append(this.raw
			).append("_").append(var).toString();
	}	
	
	/**
	 * Returns the raw value of this StringPatternValue, meaning the all wrapped String if it 
	 * does not describe a pattern, or the fixed part of the pattern
	 *  
	 * @return this StringPatternValue's raw String value
	 */
	public String getRaw() {
		if(!this.isPattern)
			return this.value;
		return this.raw;
	}	
	
	/**
	 * Resets the variable part of this StringPatternValue's wrapped String if it is a pattern; this method does 
	 * nothing if this StringPatternValue's wrapped String is not a pattern
	 */
	public void reset() {
		this.variable.reset();
	}
}
