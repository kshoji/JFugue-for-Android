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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Consumes whitespace between tokens in the music string
 * 
 * @author dkoelle
 */
public class WhitespaceConsumer implements Subparser 
{
	private static final Pattern whitespacePattern = Pattern.compile("^\\s+");
	
	private static WhitespaceConsumer instance;
	
	public static WhitespaceConsumer getInstance() {
		if (instance == null) {
			instance = new WhitespaceConsumer();
		}
		return instance;
	}
			
	@Override
	public boolean matches(String music) {
		Matcher m = whitespacePattern.matcher(music);
		return m.find();
	}

	@Override
	public int parse(String music, StaccatoParserContext context) {
		Matcher m = whitespacePattern.matcher(music);
		return (m.find() ? m.end() : 0);
	}
}
