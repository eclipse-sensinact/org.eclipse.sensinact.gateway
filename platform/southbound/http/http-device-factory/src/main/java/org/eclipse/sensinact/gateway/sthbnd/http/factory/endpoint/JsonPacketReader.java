/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.factory.endpoint;

import static org.eclipse.sensinact.gateway.sthbnd.http.task.config.MappingDescription.ROOT;

import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.PayloadFragment;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.packet.TaskAwareHttpResponsePacket;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.NestedMappingDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingJsonFactory;

public class JsonPacketReader extends AbstractHttpDevicePacketReader implements Iterator<PayloadFragment> {

	private static final Logger LOG = LoggerFactory.getLogger(JsonPacketReader.class);
	
	private final LinkedList<Object> context = new LinkedList<>();
	private final MappingJsonFactory factory;
	
	private final Map<String, Map<String, String>> mappings = new HashMap<>();
	private JsonParser parser;
	private RootMappingTree tree;
	
	public JsonPacketReader(SimpleDateFormat timestampFormat, 
			String providerIdPattern, MappingJsonFactory factory) {
		super(timestampFormat, providerIdPattern);
		this.factory = factory;
	}

	@Override
	public void load(TaskAwareHttpResponsePacket packet) throws InvalidPacketException {
		Arrays.stream(packet.getMapping())
				.forEach(m -> mappings.put(m.getMappingType() == ROOT ? "" :
					((NestedMappingDescription)m).getPath(), m.getMapping()));
		
		tree = new RootMappingTree(mappings.keySet());
		
		Reader reader = null;
		try {
			reader = packet.getReader();
			parser = factory.createParser(reader);
			
			// Prime the parser to the start of the first struct
			JsonToken token;
			do {
				token = parser.nextToken();
			} while(token != null && !token.isStructStart()); 
			if(token == null) {
				safeClose(parser);
			}
		} catch (Exception e) {
			if(parser != null) {
				safeClose(parser);
			} else if (reader != null) {
				safeClose(reader);
			}
			LOG.error("Failed to load a JSON packet.", e);
			throw new InvalidPacketException("Failed to load JSON packet", e);
		}
	}

	@Override
	public void parse() throws InvalidPacketException {
	}

	@Override
	public void reset() {
		if(parser != null) {
			safeClose(parser);
		}
		context.clear();
		tree = null;
		mappings.clear();
	}

	@Override
	public boolean hasNext() {
		if(parser == null){
			throw new IllegalStateException("This PacketReader is not initialized");
		}
		
		JsonToken currentToken = parser.currentToken();
		// This is necessary as the initial state has no token
		// and #next() clears the END_OBJECT token
		if(currentToken == null) {
			currentToken = JsonToken.NOT_AVAILABLE;
		}
		try {
			while(currentToken != null) {
				if(currentToken.isStructStart()) {
					switch(tree.getAction(context)) {
						case Ignore:
							parser.skipChildren();
							updateContext(parser.currentToken());
							break;
						case Select:
							return true;
						default:
							break;
					}
				} 
					
				currentToken = parser.nextToken();
				if(currentToken != null) {
					updateContext(currentToken);
				}
			}
		} catch (IOException e) {
			safeClose(parser);
			
			LOG.error("Failed to parse a JSON packet.", e);
			throw new IllegalArgumentException("Unable to read the complete packet", e);
		}
		
		safeClose(parser);
		return false;
	}
	
	private void updateContext(JsonToken state) throws IOException {
		String name = parser.currentName();
		Object last = context.peekLast();
		switch(state) {
			case START_OBJECT:
				// Name is null for root, or if a member of an array
				if(name != null)
					context.addLast(name);
				else if (last instanceof Integer) {
					context.removeLast();
					context.addLast(((Integer) last) + 1);
				} else {
					context.addLast(0);
				}
				break;
			case END_OBJECT:
				// If this is a field remove the context
				if(last instanceof String)
					context.removeLast();
				break;
			case START_ARRAY:
				if(name != null)
					context.addLast(name);
				else if (last instanceof Integer) {
					context.removeLast();
					int update = ((Integer)last).intValue() + 1;
					context.addLast("[" + update + "]");
				} else {
					context.addLast("[0]");
				}
				break;
			case END_ARRAY:
				if(last instanceof Integer) {
					context.removeLast();
				}
				last = context.peekLast();
				if(last instanceof String) {
					context.removeLast();
					String s = (String) last;
					if(s.charAt(0) == '[') {
						context.addLast(Integer.parseInt(s.substring(1, s.length() - 1)));
					}
				}
				break;
			default:
				break;
		}
	}

	@Override
	public PayloadFragment next() {
		if(!hasNext()) {
			throw new NoSuchElementException("No more elements");
		}
		try {
			String matchedPath = tree.getMappedPath(context);

			JsonToken currentToken = parser.currentToken();
			Map<String, Object> data;
			
			// The readValueAs call eats the final token, so we update the context here
			if(currentToken == JsonToken.START_OBJECT) {
				data = parser.readValueAs(new TypeReference<Map<String, Object>>() {});
				updateContext(JsonToken.END_OBJECT);
			} else if (currentToken == JsonToken.START_ARRAY) {
				List<Object> list = parser.readValueAs(new TypeReference<List<Object>>() {});
				data = new HashMap<>();
				for(int i = 0; i < list.size(); i++) {
					data.put(String.format("[" + i + "]"), list.get(i));
				}
				updateContext(JsonToken.END_ARRAY);
			} else {
				safeClose(parser);
				LOG.error("Failed to parse a JSON packet and ended up in an inconsistent state");
				throw new IllegalStateException("Failed to read the complete packet");
			}
			
			return createFragments(data, mappings.get(matchedPath));
		} catch (IOException e) {
			safeClose(parser);
			LOG.error("Failed to parse a JSON packet.", e);
			throw new IllegalArgumentException("Failed to read the complete packet", e);
		}
	}
}