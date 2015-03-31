/*
 * JFugue, an Application Programming Interface (API) for Music Programming
 * http://www.jfugue.org
 *
 * Copyright (C) 2003-2014 David Koelle
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.staccato;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.jfugue.parser.Parser;
import org.jfugue.theory.Key;
import org.jfugue.theory.TimeSignature;


public class StaccatoParserContext {
	private Parser parser;
	private Map<String, Object> dictionary;
	private Key currentKey = Key.DEFAULT_KEY;
	private TimeSignature currentTimeSignature = TimeSignature.DEFAULT_TIMESIG;

	public StaccatoParserContext(Parser parser) {
		this.parser = parser;
		this.dictionary = new HashMap<String, Object>();	
	}
	
	public Map<String, Object> getDictionary() {
		return this.dictionary;
	}

	public StaccatoParserContext loadDictionary(Reader reader) throws IOException {
	    BufferedReader bread = new BufferedReader(reader);
	    while (bread.ready()) {
	        String s = bread.readLine();
	        if ((s != null) && (s.length() > 1)) {
	            if (s.charAt(0) == '#') {
	            	// Skip this line, it's a comment
	            } 
	            else if (s.charAt(0) == '$') {
	            	// This line is a definition
                    String key = s.substring(1, s.indexOf('=')).trim();
                    String value = s.substring(s.indexOf('=')+1, s.length()).trim();
                    dictionary.put(key, value);
	            }
	        }
	    }
	    bread.close();
	    
	    return this;
	}
	
	public Parser getParser() {
		return this.parser;
	}
	
	public StaccatoParserContext loadDictionary(InputStream stream) throws IOException {
		return loadDictionary(new InputStreamReader(stream));
	}
	
	public StaccatoParserContext loadDictionary(File file) throws IOException {
		return loadDictionary(new FileReader(file));
	}
	
	public StaccatoParserContext setKey(Key key) {
		this.currentKey = key;
		return this;
	}
	
	public Key getKey() {
		return this.currentKey;
	}

	public StaccatoParserContext setTimeSignature(TimeSignature timeSignature) {
		this.currentTimeSignature = timeSignature;
		return this;
	}
	
	public TimeSignature getTimeSignature() {
		return this.currentTimeSignature;
	}


}
