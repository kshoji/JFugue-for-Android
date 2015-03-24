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

/** Changes spaces in parentheses to underscores, since the Staccato string is split on parentheses */
public class ParenSpacesPreprocessor implements Preprocessor 
{
	private static ParenSpacesPreprocessor instance;
	
	public static ParenSpacesPreprocessor getInstance() {
		if (instance == null) {
			instance = new ParenSpacesPreprocessor();
		}
		return instance;
	}
	
	@Override
	public String preprocess(String s, StaccatoParserContext context) {
		StringBuilder buddy = new StringBuilder();
		int pos = 0;
		while (pos < s.length()) {
			int keepSpacesUntil = StaccatoUtil.findNextOrEnd(s, '(', pos);
			int endParen = StaccatoUtil.findNextOrEnd(s, ')', keepSpacesUntil);
			buddy.append(s.substring(pos, keepSpacesUntil));
			for (int i=keepSpacesUntil; i < endParen; i++) {
				if (s.charAt(i) == ' ') {
					buddy.append('_');
				} else {
					buddy.append(s.charAt(i));
				}
			}
			pos = endParen;
		}
		
		return buddy.toString();
	}
	
	public static String unprocess(String s) {
		StringBuilder buddy = new StringBuilder();
		for (int i=0; i < s.length(); i++) {
			if (s.charAt(i) == '_') {
				buddy.append(' ');
			} else {
				buddy.append(s.charAt(i));
			}
		}
		return buddy.toString();
	}
}
