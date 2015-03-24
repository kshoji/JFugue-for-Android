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

public class CollectedNotesPreprocessor implements Preprocessor
{
	private static CollectedNotesPreprocessor instance;
	
	public static CollectedNotesPreprocessor getInstance() {
		if (instance == null) {
			instance = new CollectedNotesPreprocessor();
		}
		return instance;
	}

	private static Pattern parenPattern = Pattern.compile("\\([^\\)]*\\)\\S");
	
	@Override
	public String preprocess(String s, StaccatoParserContext context) {
		StringBuilder buddy = new StringBuilder();
		int posStart = 0;
		
		Matcher m = parenPattern.matcher(s);
		while (m.find()) {
			// First, add the text that occurs before the parenthesis group starts. That text is
		    // meant to be added to the result without any modifications.
			int posStartOfGroup = m.start();
			String sub = s.substring(posStart, posStartOfGroup);
			buddy.append(sub);
			posStart = StaccatoUtil.findNextOrEnd(s, SPACE, m.end());

			// Now, get the notes that are collected between parentheses. The "replicand" is the
			// thing that immediately follows the closing parenthesis and is the thing that should
			// be applied to each note within the parentheses.
			int posCloseParen = s.indexOf(')', posStartOfGroup);
			String replicand = s.substring(posCloseParen+1, StaccatoUtil.findNextOrEnd(s, SPACE_PLUS, posCloseParen+1));
			String parenContents = s.substring(posStartOfGroup+1, posCloseParen);
			
			// Split the items in parentheses
			int subindex = 0;
			int posSomething = -1;
			while (subindex < parenContents.length()) {
				posSomething = StaccatoUtil.findNextOrEnd(parenContents, SPACE_PLUS, subindex);
				buddy.append(parenContents.substring(subindex, posSomething));
				buddy.append(replicand);
				if (posSomething != parenContents.length()) {
					buddy.append(parenContents.substring(posSomething, posSomething+1));
				}
				subindex = posSomething+1;
			}
		}
		
		buddy.append(s.substring(posStart, s.length()));
		
		return buddy.toString();
	}
	
    private static final char[] SPACE = new char[] { ' ' };
    private static final char[] SPACE_PLUS = new char[] { ' ', '+' };
}
