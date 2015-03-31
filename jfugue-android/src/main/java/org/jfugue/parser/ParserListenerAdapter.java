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

package org.jfugue.parser;

import org.jfugue.theory.Chord;
import org.jfugue.theory.Note;

public class ParserListenerAdapter implements ParserListener { 

	@Override
	public void beforeParsingStarts() { } 

	@Override
	public void afterParsingFinished() { }

	@Override
	public void onTrackChanged(byte track) { }

	@Override
	public void onLayerChanged(byte layer) { }

	@Override
	public void onInstrumentParsed(byte instrument) { }

	@Override
	public void onTempoChanged(int tempoBPM) { }

	@Override
	public void onKeySignatureParsed(byte key, byte scale) { }

	@Override
	public void onTimeSignatureParsed(byte numerator, byte powerOfTwo) { }

	@Override
	public void onBarLineParsed(long id) { }

	@Override
	public void onTrackBeatTimeBookmarked(String timeBookmarkId) { }

	@Override
	public void onTrackBeatTimeBookmarkRequested(String timeBookmarkId) { }

	@Override
	public void onTrackBeatTimeRequested(double time) { }

	@Override
	public void onPitchWheelParsed(byte lsb, byte msb) { }

	@Override
	public void onChannelPressureParsed(byte pressure) { }

	@Override
	public void onPolyphonicPressureParsed(byte key, byte pressure) { }

	@Override
	public void onSystemExclusiveParsed(byte... bytes) { }

	@Override
	public void onControllerEventParsed(byte controller, byte value) { }

	@Override
	public void onLyricParsed(String lyric) { }

	@Override
	public void onMarkerParsed(String marker) { }

	@Override
	public void onFunctionParsed(String id, Object message) { }

	@Override
	public void onNoteParsed(Note note) { }

	@Override
	public void onChordParsed(Chord chord) { }
}
