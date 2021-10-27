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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.eclipse.sensinact.gateway.util.json.JSONParser;
import org.eclipse.sensinact.gateway.util.json.JSONParser.Evaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JSONParserListener extends JSONParser.JSONParserCallback{

	static final Logger LOG = LoggerFactory.getLogger(JSONParserListener.class);
	
	private final Object lock = new Object(); 
	private CountDownLatch countDown = null ;
	private Evaluation evaluation = null;
	private boolean endOfParsing;
	
	public JSONParserListener() {
		this.countDown = new CountDownLatch(1) ;
		this.endOfParsing = false;
	}

	@Override
	public void handle(Evaluation evaluation) {
		synchronized(lock) {
			this.evaluation = evaluation;
		}
		await();
	}

	public void setEndOfParsing() {
		this.endOfParsing = true;
	}

	public boolean isEndOfParsing() {
		return this.endOfParsing;
	}
	
	public Map<String,String> getEvent(Map<String,List<String>> jsonMapping) {
		Evaluation eval = null;
		synchronized(lock) {
			eval = this.evaluation;
		}
		if(eval != null) {
			if(eval == JSONParser.END_OF_PARSING) 
				setEndOfParsing();
			else if(eval.result!=null && eval.result.length()>0) {
				JSONParser parser = null;
				try {
					parser = new JSONParser(new StringReader(eval.result));
					List<Evaluation> evaluations = parser.parse(jsonMapping.get(eval.path));
					Map<String,String> map = evaluations.stream().<Map<String,String>>collect(
						HashMap::new,
						(m,e)-> {m.put(e.path,e.result);},
						Map::putAll);
					return map;
				} catch(Exception e) {
					HttpMappingPacket.LOG.error(e.getMessage(),e);
					setEndOfParsing();
				} finally {
					if(parser!=null)
						parser.close();
				}
			}
		}
		return null;
	}
	
	public void countDown() {
		this.evaluation = null;
		this.countDown.countDown();
	}
	
	public void await() {
		try {
			this.countDown.await();
		} catch (InterruptedException e) {
			Thread.interrupted();
		}
		this.countDown  = new CountDownLatch(1) ;
	}
}