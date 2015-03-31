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

public class BeatTimeSubparser implements Subparser 
{
	public static final char BEATTIME = '@';
	public static final char BEATTIME_USE_MARKER = '#';
	
	private static BeatTimeSubparser instance;
	
	public static BeatTimeSubparser getInstance() {
		if (instance == null) {
			instance = new BeatTimeSubparser();
		}
		return instance;
	}
	
	@Override
	public boolean matches(String music) {
		return (music.charAt(0) == BEATTIME);
	}

	@Override
	public int parse(String music, StaccatoParserContext context) {
		if (music.charAt(0) == BEATTIME) {
			int posNextSpace = StaccatoUtil.findNextOrEnd(music, ' ', 0);
			if (posNextSpace > 1) {
				String timeTrackId = music.substring(1, posNextSpace);
				if (timeTrackId.matches("([0-9]+(\\.[0-9]+)*)")) {
					double time = Double.parseDouble(timeTrackId);
					context.getParser().fireTrackBeatTimeRequested(time);
				} else if (timeTrackId.charAt(0) == BEATTIME_USE_MARKER) {
					String timeBookmarkId = timeTrackId.substring(1, timeTrackId.length());
					context.getParser().fireTrackBeatTimeBookmarkRequested(timeBookmarkId);
				}
			}
			return Math.max(1, posNextSpace);
		}
		return 0;
	}
}
