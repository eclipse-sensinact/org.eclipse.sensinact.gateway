/*
 * Copyright (c) 2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.http.factory.packet;

import java.io.StringReader;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
//import java.util.Stack;
//import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CountDownLatch;

import org.eclipse.sensinact.gateway.util.json.JSONParser;
import org.eclipse.sensinact.gateway.util.json.JSONParser.Evaluation;

class JSONParserListener extends JSONParser.JSONParserCallback{
	
	private final Object lock = new Object(); 
	private CountDownLatch countDown = null ;
	//private Deque<Extraction> extractions = null;
	private Evaluation extraction = null;
	private boolean endOfParsing;
	
	public JSONParserListener() {
		this.countDown = new CountDownLatch(1) ;
		//this.extractions = new LinkedList<Extraction>();
		this.endOfParsing = false;
	}

	@Override
	public void handle(Evaluation extraction) {
		synchronized(lock) {
			//this.extractions.addFirst(extraction);
			this.extraction = extraction;
		}	
		try {
			this.countDown.await();
		} catch (InterruptedException e) {
			Thread.interrupted();
		}
	}

	public void setEndOfParsing() {
		this.endOfParsing = true;
	}

	public boolean isEndOfParsing() {
		return this.endOfParsing;
	}
	
	public Map<String,String> getEvent(Map<String,List<String>> jsonMapping) {
		Evaluation ex = null;
		synchronized(lock) {
			//ex = this.extractions.pollLast();
			ex = this.extraction;
		}
		if(ex != null) {
			if(ex == JSONParser.END_OF_PARSING) 
				setEndOfParsing();
			else if(ex.result!=null && ex.result.length()>0) {
				JSONParser parser = null;
				try {
					parser = new JSONParser(new StringReader(ex.result));
					List<Evaluation> extractions = parser.parse(jsonMapping.get(ex.path));
					return extractions.stream().<Map<String,String>>collect(
						HashMap::new,
						(m,e)-> {m.put(e.path,e.result);},
						Map::putAll);
				} catch(Exception e) {
					HttpMappingPacket.LOG.error(e.getMessage(),e);
				} finally {
					if(parser!=null)
						parser.close();
				}
			}
		}
		return null;
	}
	
	public void countDown() {
		this.countDown.countDown();
	}
	
	public void await() {
		try {
			this.countDown.await();
		} catch (InterruptedException e) {
			Thread.interrupted();
			return;
		}
		this.countDown  = new CountDownLatch(1) ;
	}
}