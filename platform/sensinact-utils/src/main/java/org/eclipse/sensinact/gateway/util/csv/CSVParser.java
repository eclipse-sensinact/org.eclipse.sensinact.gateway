/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.util.csv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CSVParser allows to parse csv document
 */
public class CSVParser {

	private static final Logger LOG = LoggerFactory.getLogger(CSVParser.class);

	private static final String EMPTY_TITLE = "#EMPTY_TITLE";
	public static final String EMPTY_CONTENT = "#EMPTY_CONTENT";

	private static final Pattern BOOLEAN_PATTERN = Pattern.compile("((TRUE)|(FALSE))");
	private static final Pattern INT_PATTERN = Pattern.compile("[0-9]+");
	private static final Pattern NUMBER_PATTERN = Pattern.compile("([1-9][0-9]*(\\.[0-9]+)?)|(0(\\.[0-9]+)?)");
	private static final Pattern GROUP_PATTERN = Pattern.compile("(([^\",]*)|(\"[^\"]+\")),");

	Reader reader = null;
	
	public CSVParser(String csv) {
		if(csv == null)
			throw new NullPointerException("Null reader");
		File f = new File(csv);
		if(f.exists()) {
			try {
				reader = new FileReader(f);
			} catch (IOException e) {
				LOG.error(e.getMessage(),e);
				throw new NullPointerException("Null reader"); 
			}
		} else {
			reader = new StringReader(csv);
		}
	}

	public CSVParser(Reader reader) {
		if(reader == null)
			throw new NullPointerException("Null reader");
		this.reader = reader;
	}
	
	public void parse(boolean firstLineAsTitles, boolean handleTitles, CVSParserCallback callback) {				
		if(callback == null)
			return;
		int pos;
		int index = -1;
				
		while(true) {	
			try {	
				StringBuilder lineBuilder = new StringBuilder();		
				while(true) {
					int a = reader.read();
					if(a < 0 || a == '\n') 
						break;
					lineBuilder.append((char)a);
				}
				if(lineBuilder.length() == 0)
					break;
				String line = lineBuilder.toString();
				index+=1;
				if(index==0 && firstLineAsTitles && !handleTitles)
					continue;
				final CVSParserEvent lineEvent = new CVSParserLineEvent(index, line);
				callback.handle(lineEvent);
				line = line.concat(",");
				pos = 0;
				Matcher matcher = GROUP_PATTERN.matcher(line);
				while(true) {	
					if(!matcher.find())
							break;				
					String lineElement = matcher.group(1);
					
					if(lineElement.startsWith("'") || lineElement.startsWith("\""))
						lineElement = lineElement.substring(1);
					if(lineElement.endsWith("'") || lineElement.endsWith("\"")|| lineElement.endsWith(","))
						lineElement = lineElement.substring(0,lineElement.length()-1);
				
					CVSParserEvent event = null;
					if(index==0 && firstLineAsTitles) {
						String title = null;
						if(lineElement.length() == 0)
							title = EMPTY_TITLE;	
						else
							title = lineElement;					
						event = new CVSParserTitleEvent(pos, title);
					} else if(lineElement.length() > 0) {
					    String type = null;
						if(INT_PATTERN.matcher(lineElement).matches())
							type = "INT";
						else if(NUMBER_PATTERN.matcher(lineElement).matches())
							type = "DOUBLE";
						else if(BOOLEAN_PATTERN.matcher(lineElement.toUpperCase()).matches())
							type = "BOOL";
						else 
							type="TEXT";
						event  = new CVSParserContentEvent(pos, type, lineElement);
					} else {
						event = new CVSParserContentEvent(pos, null, EMPTY_CONTENT);
					}
					if(event!=null)
						callback.handle(event);
					pos+=1;		
				}
			} catch(IOException e) {
				LOG.error(e.getMessage(),e);
				break;
			}
		}
		final CVSParserEvent eofEvent = new CVSParserEOFEvent(index);
		callback.handle(eofEvent);
		close();
	}
	
	public void close() {
		if(reader != null) {
			try {
				reader.close();
			} catch(Exception e) {
				LOG.error(e.getMessage(),e);
			} finally {
				reader = null;
			}
		}
	}
		
}
