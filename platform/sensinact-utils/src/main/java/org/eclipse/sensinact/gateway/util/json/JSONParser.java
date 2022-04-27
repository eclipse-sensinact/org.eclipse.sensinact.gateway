/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.util.json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.eclipse.sensinact.gateway.util.json.JSONValidator.JSONToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSONParser allows to extract parts of a json document using specified 
 * String paths
 */
public class JSONParser {
	
	private static final Logger LOG = LoggerFactory.getLogger(JSONParser.class);

	private static final Pattern NUMBER_PATTERN = Pattern.compile("([1-9][0-9]*(\\.[0-9]+)?)|(0(\\.[0-9]+)?)");
	private static final Pattern BOOLEAN_PATTERN = Pattern.compile("((TRUE)|(FALSE))");
	private static final String WILDCARD = "*";
	
	public static final Evaluation END_OF_PARSING = new Evaluation(null,null); 
	
	enum Nested {
		OBJECT,
		ARRAY;
	}
	
	class TokenContextExtended {
		public String key;
		public String value;
		public String path;
		public JSONToken token;
    }

	public static class Evaluation {
		public final String path;
		public final String result;
		
		public Evaluation(String path, String result){
			this.path = path;
			if(result!=null && result.startsWith(","))
				this.result = result.substring(1);
			else
				this.result = result;
		}
	}
	
	class Evaluator {
		String path = null;
		String[] pathElements = null;
		StringBuilder builder = null;
		int lastIndex = -1;
		Stack<Nested> nesteds =new Stack<>();
		Stack<Integer> indexes = new Stack<>();
		boolean complete = false;
		
		Evaluator(String path){
			this.path = path;
			String formatedPath = path;
			if(path.startsWith("/"))
				formatedPath = formatedPath.substring(1);
			if(path.endsWith("/"))
				formatedPath = formatedPath.substring(0,formatedPath.length()-1);
			this.pathElements = formatedPath.split("/");
			this.lastIndex = this.pathElements.length-1;
			this.builder = new StringBuilder();
		}
		
		boolean isComplete() {
			return this.complete;
		}
		
		Evaluation getEvaluation() {
			Evaluation extraction = new Evaluation(path, builder.toString());
			if(WILDCARD.equals(this.pathElements[this.lastIndex])) {
				this.complete = false;
				this.builder = new StringBuilder();
			}
			return extraction;
		}
		
		void process(TokenContextExtended context) {	
			if(this.complete)
				return;			
			String value = null;
			if(context.value != null && !NUMBER_PATTERN.matcher(context.value).matches()
					&& !BOOLEAN_PATTERN.matcher(context.value.toUpperCase()).matches())
				value = String.format("\"%s\"", context.value.replace("\"", "\\\""));
			else
				value = context.value;
			
			String formatedPath = context.path;			
			if(context.path.startsWith("/"))
				formatedPath = formatedPath.substring(1);
			if(context.path.endsWith("/"))
				formatedPath = formatedPath.substring(0,formatedPath.length()-1);
			
			String[] _pathElements = formatedPath.split("/");
			int length = (_pathElements.length < this.pathElements.length)
					?_pathElements.length:this.pathElements.length;			
			
			int i = 0;			
			for(;i < length ;) {
				String pathElement = this.pathElements[i];
				String _pathElement = _pathElements[i];
				if(WILDCARD.equals(pathElement) || _pathElement.equals(pathElement)){
					i+=1;
					continue;
				}
				break;
			}
			if(i == length && length == this.pathElements.length) {
				switch(context.token) {
					case JSON_ARRAY_OPENING:
						if(!indexes.isEmpty() && nesteds.peek().equals(Nested.OBJECT)) {
							int objectIndex = this.indexes.pop().intValue();
							objectIndex+=1;
							this.indexes.push(new Integer(objectIndex));
							if(objectIndex > 0) 
								this.builder.append(",");
							this.builder.append("\"");
							this.builder.append(context.key);
							this.builder.append("\":");
						} else if(!indexes.isEmpty() && nesteds.peek().equals(Nested.ARRAY)) {
							int arrayIndex = this.indexes.pop().intValue();
							arrayIndex+=1;
							this.indexes.push(new Integer(arrayIndex));
							if(arrayIndex > 0) 
								this.builder.append(",");
						}
						this.builder.append("[");
						this.nesteds.push(Nested.ARRAY);
						this.indexes.push(new Integer(-1));
						break;
					case JSON_ARRAY_ITEM:
						if(!indexes.isEmpty() && nesteds.peek().equals(Nested.ARRAY)) {
							int arrayIndex = this.indexes.pop().intValue();
							arrayIndex+=1;
							this.indexes.push(new Integer(arrayIndex));
							if(arrayIndex > 0 && builder.length() > 0)
								this.builder.append(",");
						}
						this.builder.append(value);
						break;
					case JSON_ARRAY_CLOSING:
						this.builder.append("]");
						this.indexes.pop();
						this.nesteds.pop();
						break;
					case JSON_OBJECT_OPENING:
						if(!indexes.isEmpty() && nesteds.peek().equals(Nested.OBJECT)) {
							int objectIndex = this.indexes.pop().intValue();
							objectIndex+=1;
							this.indexes.push(new Integer(objectIndex));
							if(objectIndex > 0) 
								this.builder.append(",");
							this.builder.append("\"");
							this.builder.append(context.key);
							this.builder.append("\":");
						} else if(!indexes.isEmpty() && nesteds.peek().equals(Nested.ARRAY)) {
							int arrayIndex = this.indexes.pop().intValue();
							arrayIndex+=1;
							this.indexes.push(new Integer(arrayIndex));
							if(arrayIndex > 0) 
								this.builder.append(",");
						}
						this.builder.append("{");
						this.nesteds.push(Nested.OBJECT);
						this.indexes.push(new Integer(-1));
						break;
					case JSON_OBJECT_ITEM:
						if(!indexes.isEmpty() && nesteds.peek().equals(Nested.OBJECT)) {
							int objectIndex = this.indexes.pop().intValue();
							objectIndex+=1;
							this.indexes.push(new Integer(objectIndex));
							if(objectIndex > 0  && builder.length() > 0)
								this.builder.append(",");
							this.builder.append("\"");
							this.builder.append(context.key);
							this.builder.append("\"");
							this.builder.append(":");
						}
						this.builder.append(value);
						break;
					case JSON_OBJECT_CLOSING:
						this.builder.append("}");
						this.indexes.pop();
						this.nesteds.pop();
						break;
					default:
						break;
				}
				if(this.pathElements.length == _pathElements.length){					
					if(!WILDCARD.equals(this.pathElements[this.lastIndex])
						|| (!context.token.equals(JSONToken.JSON_ARRAY_OPENING) 
							&& !context.token.equals(JSONToken.JSON_OBJECT_OPENING)
							&& (!context.token.equals(JSONToken.JSON_OBJECT_CLOSING) 
									|| this.indexes.size()>0)
							&& (!context.token.equals(JSONToken.JSON_ARRAY_CLOSING) 
									|| this.indexes.size()>0)))
					    this.complete = true;
					else
						this.builder = new StringBuilder();
				}
			}
		}
	}
	
	public static abstract class JSONParserCallback {

		public abstract void handle(Evaluation extract);
		
	}

	class JSONParserCallable implements Callable<Void> {		
		final String path;
		final Evaluator evaluator;
		TokenContextExtended context;
		
		public JSONParserCallable(String path){
			this.path = path;
			this.evaluator =  new Evaluator(path);
		}
		
		@Override
		public Void call() throws Exception {
			if(this.context == null)
				return null;
			this.evaluator.process(this.context);						
			return null;						
		}		  
		
		public void setTokenContext(TokenContextExtended context) {
			this.context = context;
		}
	}
	
	Reader reader = null;
	
	public JSONParser(String json) {
		try {
			File f = new File(json);
			if(f.exists())
				reader = new FileReader(f);
			else 
				reader = new StringReader(json);
		} catch(IOException e) {
			LOG.error(e.getMessage(),e); 
			if(this.reader == null)
				throw new NullPointerException("Null reader");
		}
	}

	public JSONParser(Reader reader) {
		if(reader == null)
			throw new NullPointerException("Null reader");
		this.reader = reader;
	}
	
	public void close() {
		if(this.reader != null) {
			try {
				this.reader.close();
			} catch (IOException e) {
				LOG.error(e.getMessage(),e);
			}
			this.reader = null;
		}	
	}
	
	public List<Evaluation> parse(List<String> paths) {		
	    if(paths == null || paths.isEmpty())
	    	return Collections.emptyList();	 
	    ExecutorService worker = Executors.newFixedThreadPool(5);	  
	    
	    List<JSONParserCallable> callables = paths.stream().<List<JSONParserCallable>>collect(
	    		ArrayList::new, (l,s)->{l.add(new JSONParserCallable(s));},List::addAll);
	    
	    List<Evaluation> extractions = new ArrayList<>();	
		List<String> pathElements = new ArrayList<>();
		Stack<Integer> indexes = new Stack<>();
	    JSONValidator validator = new JSONValidator(reader);	
						
		while(true) {
			TokenContextExtended context = next(validator, pathElements, indexes);
			if(context == null)
				break;
			try {				
				callables.stream().forEach(c ->c.setTokenContext(context));
				worker.invokeAll(callables);
				for(int i=0; i< callables.size();) {
					JSONParserCallable callable = callables.get(i);
					Evaluator evaluator = callable.evaluator;
					if(evaluator.isComplete()) {
						if(!callable.path.endsWith(WILDCARD))
							callables.remove(i);
						extractions.add(evaluator.getEvaluation());
					} else
						i+=1;
				}
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
		}
		worker.shutdownNow();
		return extractions;
	}
	
	public void parse(List<String> paths, JSONParserCallback callback) {
	    if(paths == null || paths.isEmpty())
	    	return;	    
	    
	    ExecutorService worker = Executors.newFixedThreadPool(5);

	    List<JSONParserCallable> callables = paths.stream().<List<JSONParserCallable>>collect(
	    		ArrayList::new, (l,s)->{l.add(new JSONParserCallable(s));},List::addAll);
	    
	    JSONValidator validator = new JSONValidator(reader);		
		List<String> pathElements = new ArrayList<>();
		Stack<Integer> indexes = new Stack<>();
		
		while(true) {
			TokenContextExtended context = next(validator, pathElements, indexes);
			if(context == null)
				break;
			try {
				callables.stream().forEach(c ->c.setTokenContext(context));
				worker.invokeAll(callables);
				for(int i=0; i< callables.size();) {
					JSONParserCallable callable = callables.get(i);
					Evaluator evaluator = callable.evaluator;
					if(evaluator.isComplete())
						callback.handle(evaluator.getEvaluation());
					else
						i+=1;
				}
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
		}
		callback.handle(END_OF_PARSING);
		worker.shutdownNow();
	}

	private TokenContextExtended next(
			JSONValidator validator, 
			List<String> pathElements, 
			Stack<Integer> indexes) {

		int index = -1;
		String key = null;		
		JSONToken token = null;
		
		try {
			token = validator.nextToken();
		} catch(Exception e) {
			LOG.error(e.getMessage(),e);
		}		
		if(token == null)
			return null;
		
		switch (token) {
            case JSON_ARRAY_OPENING:
            	key = token.getContext()==null?null:token.getContext().key;
	            if(key != null) {
		            pathElements.remove(pathElements.size()-1);
	            	pathElements.add(key);	
	            } else if(indexes.size() > 0) {
	            	index = indexes.pop().intValue();
	            	index+=1;
	            	indexes.push(index);
	            	pathElements.remove(pathElements.size()-1);
	            	pathElements.add(String.format("[%s]", index));
            	}
            	index = -1;
            	indexes.push(index);
            	pathElements.add("[]");
            	break;
            case JSON_ARRAY_ITEM:
            	index = indexes.pop().intValue();
            	index+=1;
            	indexes.push(index);
            	pathElements.remove(pathElements.size()-1);
            	pathElements.add(String.format("[%s]", index));
            	break;
            case JSON_ARRAY_CLOSING:
            	indexes.pop();
            	pathElements.remove(pathElements.size()-1);
            	break;
            case JSON_OBJECT_OPENING:
            	key = token.getContext()==null?null:token.getContext().key;
	            if(key != null) {
		            pathElements.remove(pathElements.size()-1);
	            	pathElements.add(key);	
	            } else if(indexes.size() > 0) {
	            	index = indexes.pop().intValue();
	            	index+=1;
	            	indexes.push(index);
	            	pathElements.remove(pathElements.size()-1);
	            	pathElements.add(String.format("[%s]", index));
            	}
            	pathElements.add("{}");
            	break;
            case JSON_OBJECT_CLOSING:
            	pathElements.remove(pathElements.size()-1);
            	break;	            	
            case JSON_OBJECT_ITEM:
            	pathElements.remove(pathElements.size()-1);
            	key = token.getContext()==null?null:token.getContext().key;
            	if(key != null) 
            		pathElements.add(key);	
            	else
            		pathElements.add("#UNKNOWN#");	
            	break;
            default:
            	break;
		}
		TokenContextExtended context = new TokenContextExtended();
		context.token = token;
		context.value = token.getContext()==null?null:String.valueOf(token.getContext().value);
		context.key = token.getContext()==null?null:token.getContext().key;
		context.path = pathElements.stream().<StringBuilder>collect(
			StringBuilder::new, 
			(sb,s) ->{ sb.append("/"); sb.append(s); },
			(sb1,sb2)->{sb1.append(sb2.toString());}
		).toString();
		return context;
	}

	public static void main(String args[]) {
		
		String s = "{\"key0\":null,\"key1\":[\"machin\",\"chose\",2],"
			+ "\"key2\":{\"key20\":\"truc\",\"key21\":45},"
			+ "\"key3\":{\"key30\":[{\"key300\":\"bidule\",\"key301\":[8,2,1]},[18,\"intermediate\"],\"standalone\",{\"key300\":\"chose\",\"key301\":[10,20,11]}]}}";
		
		List<Evaluation> extractions = new JSONParser(s).parse(
			Arrays.asList("*","/key3/key30/[3]/key301","/key3/key30/[0]","/key3","/key3/key30/[2]","/key2/key20","/key2/key25","/key3/key30/[3]/key301/*"));
		
		extractions.stream().forEach(e->{System.out.println( e.path+ " : " + e.result);});		
		
		JSONParserCallback callback = new JSONParserCallback() {
			@Override
			public void handle(Evaluation e) {
				System.out.println("--------------------------------------------------------");
				if(e == END_OF_PARSING)
					System.out.println( "end of parsing");
				else
					System.out.println( e.path+ " : " + e.result);
				System.out.println("--------------------------------------------------------");
			}
		};
		new JSONParser(s).parse(Arrays.asList("key3/key30/[3]/key301/*", "/key3/key30/*", "/key3/key30/[0]/*", "/key3/*"), callback);	
	
		s = "[null,[\"machin\",\"chose\",2],{\"key20\":\"truc\",\"key21\":45},"
		+ "{\"key30\":[{\"key300\":\"bidule\",\"key301\":[8,2,1]},[18,\"intermediate\"],\"standalone\",{\"key300\":\"chose\",\"key301\":[10,20,11]}]}]";
		
		callback = new JSONParserCallback() {
			@Override
			public void handle(Evaluation e) {
				System.out.println("--------------------------------------------------------");
				if(e == END_OF_PARSING)
					System.out.println( "end of parsing");
				else
					System.out.println( e.path+ " : " + e.result);
				System.out.println("--------------------------------------------------------");
			}
		};
		new JSONParser(s).parse(Arrays.asList("*"), callback);		
	}
}
