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

import java.io.BufferedReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.PayloadFragment;
import org.eclipse.sensinact.gateway.sthbnd.http.factory.packet.TaskAwareHttpResponsePacket;
import org.eclipse.sensinact.gateway.sthbnd.http.task.config.MappingDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvPacketReader extends AbstractHttpDevicePacketReader {

	private static final Logger LOG = LoggerFactory.getLogger(CsvPacketReader.class);

	private static final Pattern BOOLEAN_PATTERN = Pattern.compile("((TRUE)|(FALSE))");
	private static final Pattern INT_PATTERN = Pattern.compile("[0-9]+");
	private static final Pattern NUMBER_PATTERN = Pattern.compile("([1-9][0-9]*(\\.[0-9]+)?)|(0(\\.[0-9]+)?)");
	
	private final Map<String, String> mappings = new HashMap<>();
	private final List<String> headers = new ArrayList<>();
	private final char delimiterChar;
	private final boolean firstRowHeaders;
	private final NumberFormat numberLocale;
	private final Integer maxRows;

	private BufferedReader reader;
	private int currentRow;
	private String nextLine;
	
	public CsvPacketReader(SimpleDateFormat timestampFormat, String serviceProviderIdPattern,
			char delimiterChar, boolean firstRowHeaders, String csvNumberLocale, Integer maxRows) {
		super(timestampFormat, serviceProviderIdPattern);
		this.delimiterChar = delimiterChar;
		this.firstRowHeaders = firstRowHeaders;
		this.maxRows = maxRows;
		if(csvNumberLocale == null || "".equals(csvNumberLocale)) {
			numberLocale = null;
		} else {
			String[] segments = csvNumberLocale.split("_");
			switch(segments.length) {
				case 1:
					numberLocale = NumberFormat.getNumberInstance(new Locale(segments[0]));
					break;
				case 2:
					numberLocale = NumberFormat.getNumberInstance(new Locale(segments[0], segments[1]));
					break;
				case 3:
					numberLocale = NumberFormat.getNumberInstance(new Locale(segments[0], segments[1], segments[2]));
					break;
				default:
					LOG.error("The locale format {} is not valid", csvNumberLocale);
					throw new IllegalArgumentException("The format " + csvNumberLocale + " is not valid");
			}
		}
	}

	@Override
	public void load(TaskAwareHttpResponsePacket packet) throws InvalidPacketException {
		MappingDescription[] rawMappings = packet.getMapping();
		
		if(Arrays.stream(rawMappings)
			.anyMatch(m -> m.getMappingType() != ROOT)) {
			LOG.error("The configured mapping {} contains non-root mappings", (Object[]) rawMappings);
			throw new InvalidPacketException("Only root mappings are supported for CSV parsing");
		}
		
		Arrays.stream(rawMappings).forEach(m -> mappings.putAll(m.getMapping()));
		
		try {
			reader = packet.getReader();
			advanceLine();
			if(firstRowHeaders && nextLine != null) {
				List<Object> tokenise = tokenise(nextLine, false);
				for (int i = 0; i < tokenise.size(); i++) {
					Object name = tokenise.get(i);
					if(!"".equals(name)) {
						headers.add((String) name);
					} else {
						headers.add(Integer.toString(i));
					}
				}
				currentRow--;
				advanceLine();
			}
		} catch (Exception e) {
			if(reader != null) {
				safeClose(reader);
			}
			nextLine = null;
			LOG.error("Failed to initialize the CSV packet reader", e);
			throw new InvalidPacketException("Unable to read the packet", e);
		}
		
		if(nextLine == null) {
			safeClose(reader);
		}
	}

	private void advanceLine() throws IOException {
		if(maxRows != null && (++currentRow > maxRows)) {
			nextLine = null;
			safeClose(reader);
		} else {
			do {
				nextLine = reader.readLine();
			} while("".equals(nextLine));
			
			if(nextLine == null) {
				safeClose(reader);
			}
		}
	}

	private List<Object> tokenise(String line, boolean inferTypes) throws InvalidPacketException {
		List<Object> tokens = new ArrayList<>();
		int tokenStartIdx = 0;
		int tokenStopIdx;
		while(tokenStartIdx < line.length()) {
			if(line.charAt(tokenStartIdx) == delimiterChar) {
				tokens.add("");
				tokenStartIdx += 1;
				continue;
			}
			boolean stripQuote = false;
			tokenStopIdx = tokenStartIdx + 1;
			if(line.charAt(tokenStartIdx) == '"') {
				stripQuote = true;
				tokenStopIdx = line.indexOf('"', tokenStopIdx);
				if(tokenStopIdx < 0) {
					LOG.error("CSV Parsing failed. The line {} had an unbalanced \" starting at index {}", line, tokenStartIdx);
					throw new InvalidPacketException("CSV parsing failed due to an unbalanced \" in the input");
				}
				if(tokenStopIdx < line.length() -1 && line.charAt(tokenStopIdx + 1) != delimiterChar) {
					LOG.error("CSV Parsing failed. The line {} had a quoted token starting at index {} and finishing at index {} that was not followed by a delimiter", line, tokenStartIdx, tokenStopIdx);
					throw new InvalidPacketException("CSV parsing failed due to a misquoted token in the input");
				}
				tokenStopIdx++;
			} else {
				tokenStopIdx = line.indexOf(delimiterChar, tokenStopIdx);
			}
			if(tokenStopIdx < 0) {
				tokenStopIdx = line.length();
			}
			String token = line.substring(stripQuote ? tokenStartIdx + 1 : tokenStartIdx,
					stripQuote ? tokenStopIdx - 1 : tokenStopIdx);
			
			tokens.add(inferTypes ? inferType(token) : token);
			tokenStartIdx = tokenStopIdx + 1;
		}
		return tokens;
	}

	private Object inferType(String token) {
		if(BOOLEAN_PATTERN.matcher(token.toUpperCase()).matches())
			return Boolean.parseBoolean(token);
		
		if(numberLocale == null) {
			if(INT_PATTERN.matcher(token).matches())
				return Long.parseLong(token);
			else if(NUMBER_PATTERN.matcher(token).matches())
				return Double.parseDouble(token);
		} else {
			ParsePosition pp = new ParsePosition(0);
			Number n = numberLocale.parse(token, pp);
			if(pp.getErrorIndex() == -1 && pp.getIndex() == token.length()) {
				// The entire token counted as a number
				return n;
			}
		}
		return token;
	}

	@Override
	public void parse() throws InvalidPacketException {}

	@Override
	public void reset() {
		if(reader != null) {
			safeClose(reader);
		}
		headers.clear();
		mappings.clear();
	}

	@Override
	public boolean hasNext() {
		if(reader == null) {
			throw new IllegalStateException("This PacketReader is not initialized");
		}
		
		if(nextLine == null) {
			try {
				advanceLine();
			} catch (IOException e) {
				safeClose(reader);
				LOG.error("Failed to parse a CSV packet.", e);
				throw new IllegalArgumentException("Unable to read the complete packet", e);
			}
		}
		
		return nextLine != null;
	}

	@Override
	public PayloadFragment next() {
		if(nextLine == null) {
			throw new NoSuchElementException("No next line of the CSV to parse");
		}
		
		try {
			Map<String, Object> data = new HashMap<>();
			List<Object> tokens = tokenise(nextLine, true);
			for (int i = 0; i < tokens.size(); i++) {
				Object token = tokens.get(i);
				if(i < headers.size()) {
					data.put(headers.get(i), token);
				} else {
					data.put("[" + i + "]", token);
				}
			}
			return createFragments(data, mappings); 
		} catch (InvalidPacketException e) {
			safeClose(reader);
			LOG.error("Failed to parse a CSV packet.", e);
			throw new IllegalArgumentException("Failed to read the complete packet", e);
		} finally {
			nextLine = null;
		}
	}

}
