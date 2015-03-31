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

/**
 * This allows chaining of ParserListeners that enable each listener in
 * the chain to alter the events it passes to the other parsers it knows
 * about. By default, it fires all events to its listeners. You may
 * want to override any of those to include your own behavior.
 * 
 * @author dkoelle
 *
 */
public class ChainingParserListenerAdapter extends Parser implements ParserListener { 

	public ChainingParserListenerAdapter() {
		super();
	}
	
	@Override
	public void beforeParsingStarts() { 
		fireBeforeParsingStarts();
	} 

	@Override
	public void afterParsingFinished() { 
		fireAfterParsingFinished();
	}

	@Override
	public void onTrackChanged(byte track) { 
		fireTrackChanged(track);
	}

	@Override
	public void onLayerChanged(byte layer) { 
		fireLayerChanged(layer);
	}

	@Override
	public void onInstrumentParsed(byte instrument) { 
		fireInstrumentParsed(instrument);
	}

	@Override
	public void onTempoChanged(int tempoBPM) { 
		fireTempoChanged(tempoBPM);
	}

	@Override
	public void onKeySignatureParsed(byte key, byte scale) { 
		fireKeySignatureParsed(key, scale);
	}

	@Override
	public void onTimeSignatureParsed(byte numerator, byte powerOfTwo) { 
		fireTimeSignatureParsed(numerator, powerOfTwo);
	}

	@Override
	public void onBarLineParsed(long id) { 
		fireBarLineParsed(id);
	}

	@Override
	public void onTrackBeatTimeBookmarked(String timeBookmarkId) { 
		fireTrackBeatTimeBookmarked(timeBookmarkId);
	}

	@Override
	public void onTrackBeatTimeBookmarkRequested(String timeBookmarkId) { 
		fireTrackBeatTimeBookmarkRequested(timeBookmarkId);
	}

	@Override
	public void onTrackBeatTimeRequested(double time) { 
		fireTrackBeatTimeRequested(time);
	}

	@Override
	public void onPitchWheelParsed(byte lsb, byte msb) { 
		firePitchWheelParsed(lsb, msb);
	}

	@Override
	public void onChannelPressureParsed(byte pressure) { 
		fireChannelPressureParsed(pressure);
	}

	@Override
	public void onPolyphonicPressureParsed(byte key, byte pressure) { 
		firePolyphonicPressureParsed(key, pressure);
	}

	@Override
	public void onSystemExclusiveParsed(byte... bytes) { 
		fireSystemExclusiveParsed(bytes);
	}

	@Override
	public void onControllerEventParsed(byte controller, byte value) { 
		fireControllerEventParsed(controller, value);
	}

	@Override
	public void onLyricParsed(String lyric) { 
		fireLyricParsed(lyric);
	}

	@Override
	public void onMarkerParsed(String marker) { 
		fireMarkerParsed(marker);
	}

	@Override
	public void onFunctionParsed(String id, Object message) { 
		fireFunctionParsed(id, message);
	}

	@Override
	public void onNoteParsed(Note note) { 
		fireNoteParsed(note);
	}

	@Override
	public void onChordParsed(Chord chord) { 
		fireChordParsed(chord);
	}
}
