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


/**
 * Catches bar lines 
 * 
 * @author dkoelle
 */
public class BarLineSubparser implements Subparser 
{
	public static final char BARLINE = '|';
	
	private static BarLineSubparser instance;
	
	public static BarLineSubparser getInstance() {
		if (instance == null) {
			instance = new BarLineSubparser();
		}
		return instance;
	}
		
	@Override
	public boolean matches(String music) {
		return music.charAt(0) == BARLINE;
	}

	@Override
	public int parse(String music, StaccatoParserContext context) {
		if (music.charAt(0) == BARLINE) {
			int posNextSpace = StaccatoUtil.findNextOrEnd(music, ' ', 0);
			long measure = -1;
			if (posNextSpace > 1) {
				String barId = music.substring(1, posNextSpace);
				if (barId.matches("\\d+")) {
					measure = Long.parseLong(barId);
				} else {
					measure = (Long)context.getDictionary().get(barId);
				}
			}
			context.getParser().fireBarLineParsed(measure);
			return Math.max(1, posNextSpace);
		}
		return 0;
	}
}
