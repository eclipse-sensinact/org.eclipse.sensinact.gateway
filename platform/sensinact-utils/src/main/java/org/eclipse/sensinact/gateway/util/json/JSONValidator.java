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

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Stack;

/**
 * A JSONValidator parse a String and allow to validate that it
 * specifies a valid JSONObject or JSONArray
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
//TODO: to be extended to be able to provide the last parsed 
//item's [key and] value, when relevant
public class JSONValidator {
	
	private static final Logger LOG = LoggerFactory.getLogger(JSONValidator.class);
	
    public static class TokenContext {
        public final String path;
        public final String key;
        public final Object value;

        TokenContext(String path, String key, Object value) {
            this.path = path;
            this.key = key;
            this.value = value;
        }
    }

    public static enum JSONToken {
        JSON_OBJECT_OPENING,
        JSON_OBJECT_CLOSING, 
        JSON_OBJECT_ITEM,
        JSON_ARRAY_OPENING,
        JSON_ARRAY_CLOSING,
        JSON_ARRAY_ITEM;

        private TokenContext context;

        JSONToken() {}

        public void clear() {
            this.context = null;
        }

        public TokenContext getContext() {
            return this.context;
        }

        public void setContext(TokenContext context) {
            this.context = context;
        }
    }

    private int index;
    private Reader reader;
    private char lastChar;
    private boolean useLastChar;
    private Stack<JSONToken> tokens = new Stack<JSONToken>();

    /**
     * Constructor
     *
     * @param reader the {@link Reader} on the String
     *               to validate by the JSONValidator to be instantiated
     */
    public JSONValidator(Reader reader) {
        this.reader = reader; //reader.markSupported() ? reader : new BufferedReader(reader);
        this.useLastChar = false;
        this.index = 0;
    }

    /**
     * Constructor
     *
     * @param s the String to validate by the
     *          JSONValidator to be instantiated
     */
    public JSONValidator(String s) {
        this(new StringReader(s));
    }

    /**
     * Returns true if the parsed String
     * attached to this JSONValidator describes
     * a valid JSONObject or JSONArray
     *
     * @return <ul>
     * <li>true if attached String describes a valid
     * JSONObject or JSONArray </li>
     * <li>false otherwise</li>
     * </ul>
     */
    public boolean valid() {
        try {
            int count = 0;
            while (true) {
                JSONToken t = nextToken();
                if (t == null) {
                    break;
                }
                count++;
            }
            return count > 0;

        } catch (Exception e) {
           LOG.error(e.getMessage(),e);
        }
        return false;
    }

    public void clear() {
    	this.tokens.clear();
    }
    
    /**
     * Returns the last identified JSONToken :
     * <ul>
     * <li>JSON_OBJECT_OPENING</li>
     * <li>JSON_OBJECT_CLOSING</li>
     * <li>JSON_OBJECT_ITEM</li>
     * <li>JSON_ARRAY_OPENING</li>
     * <li>JSON_ARRAY_CLOSING</li>
     * <li>JSON_ARRAY_ITEM</li>
     * </ul>
     * while parsing of the attached String
     *
     * @returns the last identified JSONToken
     */
    public JSONToken nextToken() throws JSONException {
        char c = nextClean();
        if (this.tokens.isEmpty()) {
            JSONToken co = checkOpening(c, null);
            if (co != null) 
                return co;
            return null;
        }
        JSONToken cc = checkClosing(c);
        if (cc != null) 
            return cc;
        JSONToken lastToken = tokens.pop();

        if (lastToken != null && (lastToken.equals(JSONToken.JSON_ARRAY_OPENING) 
        		|| lastToken.equals(JSONToken.JSON_OBJECT_OPENING))) 
            tokens.push(lastToken);
        
        switch (lastToken) {
            case JSON_OBJECT_OPENING:
                String key = null;
                Object value = null;
                switch (c) {
                    case '\'':
                    case '"':
                        key = nextString(c);
                        break;
                    default:
                        throw syntaxError(new StringBuilder(
                        		).append("Found '").append(c).append("', but String delimiter was expected"
                        		).toString());
                }
                c = nextClean();
                if (c == '=') {
                    if (next() != '>') 
                        back();                    
                } else if (c != ':') 
                    throw syntaxError("Expected a ':' after a key");
                
                c = nextClean();
                switch (c) {
                    case '"':
                    case '\'':
                        value = nextString(c);
                    default:
                        break;
                }
                if (value == null) {
                    JSONToken co = checkOpening(c, key);
                    if (co != null) 
                        return co;                    
                    StringBuffer sb = new StringBuffer();
                    while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
                        sb.append(c);
                        c = next();
                    }
                    back();
                    String s = sb.toString().trim();
                    if (s.equals("")) 
                        throw syntaxError("Missing value");                    
                    value = JSONObject.stringToValue(s);
                }
                c = nextClean();
                switch (c) {
                    case ';':
                    case ',':
                        break;
                    default:
                        back();
                }
                JSONToken t = JSONToken.JSON_OBJECT_ITEM;
                t.clear();
                t.setContext(new TokenContext(null, key, value));
                return t;
            case JSON_ARRAY_OPENING:
                value = null;
                switch (c) {
                    case ';':
                    case ',':
                        t = JSONToken.JSON_ARRAY_ITEM;
                        t.clear();
                        t.setContext(new TokenContext(null, null, null));
                        return t;
                    default:
                        break;
                }
                cc = checkClosing(c);
                if (cc != null) 
                    return cc;                
                switch (c) {
                    case '"':
                    case '\'':
                        value = nextString(c);
                    default:
                        break;
                }
                if (value == null) {
                    JSONToken co = checkOpening(c, null);
                    if (co != null) 
                        return co;                    
                    StringBuffer sb = new StringBuffer();
                    while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
                        sb.append(c);
                        c = next();
                    }
                    back();
                    String s = sb.toString().trim();
                    if (s.equals("")) 
                        throw syntaxError("Missing value");                    
                    value = JSONObject.stringToValue(s);
                }
                c = nextClean();
                switch (c) {
                    case ';':
                    case ',':
                        break;
                    default:
                        back();
                }
                t = JSONToken.JSON_ARRAY_ITEM;
                t.clear();
                t.setContext(new TokenContext(null, null, value));
                return t;
            case JSON_OBJECT_CLOSING:
            case JSON_OBJECT_ITEM:
            case JSON_ARRAY_CLOSING:
            case JSON_ARRAY_ITEM:
            default:
                break;
        }
        return null;
    }

    private void checkClosingArray() {
        if (tokens.isEmpty()) {
            throw syntaxError("Unexpected array closing");
        }
        JSONToken previousToken = tokens.pop();
        if (!previousToken.equals(JSONToken.JSON_ARRAY_OPENING)) {
            throw syntaxError("Unexpected array closing");
        }
    }

    private void checkClosingObject() {
        if (tokens.isEmpty()) {
            throw syntaxError("Unexpected object closing");
        }
        JSONToken previousToken = tokens.pop();
        if (!previousToken.equals(JSONToken.JSON_OBJECT_OPENING)) {
            throw syntaxError("Unexpected object closing");
        }
    }

    private JSONToken checkClosing(char c) {
        JSONToken cc = null;
        switch (c) {
            case 0:
                throw syntaxError("Unexpected end of stream");
            case '}':
                checkClosingObject();
                cc = JSONToken.JSON_OBJECT_CLOSING;
                break;
            case ']':
                checkClosingArray();
                cc = JSONToken.JSON_ARRAY_CLOSING;
            default:
                break;
        }
        if (cc != null) {
            c = nextClean();
            switch (c) {
                case ';':
                case ',':
                    break;
                default:
                    back();
            }
        }
        return cc;
    }

    private JSONToken checkOpening(char c, String key) {
        JSONToken o = null;
        switch (c) {
            case '{':
                o = JSONToken.JSON_OBJECT_OPENING;
                o.setContext(new TokenContext(null, key, null));
                this.tokens.push(o);
                break;
            case '[':
                o = JSONToken.JSON_ARRAY_OPENING;
                o.setContext(new TokenContext(null, key, null));
                this.tokens.push(o);
                break;
            default:
                break;
        }
        return o;
    }

    private void back() throws JSONException {
        if (useLastChar || index <= 0) {
            throw new JSONException("Stepping back two steps is not supported");
        }
        index -= 1;
        useLastChar = true;
    }

    private char next() throws JSONException {
        if (this.useLastChar) {
            this.useLastChar = false;
            if (this.lastChar != 0) {
                this.index += 1;
            }
            return this.lastChar;
        }
        int c;
        try {
            c = this.reader.read();

        } catch (IOException exc) {
            throw new JSONException(exc);
        }
        if (c <= 0) { // End of stream
            this.lastChar = 0;
            return 0;
        }
        this.index += 1;
        this.lastChar = (char) c;
        return this.lastChar;
    }

    private String next(int n) throws JSONException {
        if (n == 0) {
            return "";
        }
        char[] buffer = new char[n];
        int pos = 0;
        if (this.useLastChar) {
            this.useLastChar = false;
            buffer[0] = this.lastChar;
            pos = 1;
        }
        try {
            int len;
            while ((pos < n) && ((len = reader.read(buffer, pos, n - pos)) != -1)) {
                pos += len;
            }
        } catch (IOException exc) {
            throw new JSONException(exc);
        }
        this.index += pos;
        if (pos < n) {
            throw syntaxError("Substring bounds error");
        }
        this.lastChar = buffer[n - 1];
        return new String(buffer);
    }

    private char nextClean() throws JSONException {
        for (; ; ) {
            char c = next();
            if (c == 0 || c > ' ') {
                return c;
            }
        }
    }

    private String nextString(char quote) throws JSONException {
        char c;
        StringBuffer sb = new StringBuffer();
        for (; ; ) {
            c = next();
            switch (c) {
                case 0:
                case '\n':
                case '\r':
                    throw syntaxError("Unterminated string");
                case '\\':
                    c = next();
                    switch (c) {
                        case 'b':
                            sb.append('\b');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'u':
                            sb.append((char) Integer.parseInt(next(4), 16));
                            break;
                        case '"':
                        case '\'':
                        case '\\':
                        case '/':
                            sb.append(c);
                            break;
                        default:
                            throw syntaxError("Illegal escape.");
                    }
                    break;
                default:
                    if (c == quote) {
                        return sb.toString();
                    }
                    sb.append(c);
            }
        }
    }

    private JSONException syntaxError(String message) {
        return new JSONException(message + "\n at position " + this.index + "\n" + toString());
    }
}