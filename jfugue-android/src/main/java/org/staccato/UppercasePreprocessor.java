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

/** Turns to uppercase all tokens that are not lyrics, markers, or functions */
public class UppercasePreprocessor implements Preprocessor 
{
	private static UppercasePreprocessor instance;
	
	public static UppercasePreprocessor getInstance() {
		if (instance == null) {
			instance = new UppercasePreprocessor();
		}
		return instance;
	}

	// The characters indicating a lyric ('), tag (#), or instruction ({)
	// prevent this UppercaseProcessor from uppercasing the following token. Note that these
	// characters must start at the beginning of the token, otherwise they could
	// indicate a sharp (#) or the colon in a tuplet (*2:3).
	private static char[] SAFECHARS = new char[] { '\'', '@', '#', '{' };
	
	@Override
	public String preprocess(String s, StaccatoParserContext context) {
		StringBuilder buddy = new StringBuilder();
		int pos = 0;
		while (pos < s.length()) {
			int upperUntil = StaccatoUtil.findNextOrEnd(s, SAFECHARS, pos);
			if ((upperUntil == 0) || (s.charAt(upperUntil-1) == ' ')) {
				buddy.append(s.substring(pos, upperUntil).toUpperCase());
				if (upperUntil < s.length()) {
					int lowerUntil = upperUntil;
					if ((s.charAt(upperUntil+1) == '(') || (s.charAt(upperUntil) == ':')) {
						lowerUntil = s.indexOf(')', upperUntil+1);
						buddy.append(s.substring(upperUntil, lowerUntil));
					} else {
						lowerUntil = StaccatoUtil.findNextOrEnd(s, ' ', upperUntil);
						buddy.append(s.substring(upperUntil, lowerUntil));
					}
					upperUntil = lowerUntil;
				}
				pos = upperUntil;
			} else {
			    int min = Math.min(s.length(), upperUntil+1);
				buddy.append(s.substring(pos, min).toUpperCase());
				pos = min;
			}
		}
		return buddy.toString();
	}
}
