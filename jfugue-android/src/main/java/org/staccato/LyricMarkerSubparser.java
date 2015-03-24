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

public class LyricMarkerSubparser implements Subparser 
{
	public static final char LYRIC = '\'';
	public static final char MARKER = '#';
	
	private static LyricMarkerSubparser instance;
	
	public static LyricMarkerSubparser getInstance() {
		if (instance == null) {
			instance = new LyricMarkerSubparser();
		}
		return instance;
	}
		
	@Override
	public boolean matches(String music) {
		return ((music.charAt(0) == LYRIC) || (music.charAt(0) == MARKER));
	}

	@Override
	public int parse(String music, StaccatoParserContext context) {
		if ((music.charAt(0) == LYRIC) || (music.charAt(0) == MARKER)) {
			String lyricOrMarker = null;
			int posNext = 0;
			if (music.charAt(1) == '(') {
				posNext = StaccatoUtil.findNextOrEnd(music, ')', 0); // Find next ending parenthesis
			} else {
				posNext = StaccatoUtil.findNextOrEnd(music, ' ', 0); // Find next space
			}
			lyricOrMarker = music.substring(music.charAt(1) == '(' ? 2 : 1, posNext);
			lyricOrMarker = ParenSpacesPreprocessor.unprocess(lyricOrMarker);
			if (music.charAt(0) == LYRIC) {
				context.getParser().fireLyricParsed(lyricOrMarker);
			} else {
				context.getParser().fireTrackBeatTimeBookmarked(lyricOrMarker);
				context.getParser().fireMarkerParsed(lyricOrMarker);
			}
			return Math.max(1, Math.min(posNext+1, music.length()));
		}
		return 0;
	}
}
