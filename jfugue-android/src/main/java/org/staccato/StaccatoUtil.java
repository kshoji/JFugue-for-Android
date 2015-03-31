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

import org.jfugue.midi.MidiDefaults;
import org.jfugue.provider.KeyProviderFactory;
import org.jfugue.theory.Chord;
import org.jfugue.theory.Note;
import org.staccato.functions.ChannelPressureFunction;
import org.staccato.functions.ControllerFunction;
import org.staccato.functions.PitchWheelFunction;
import org.staccato.functions.PolyPressureFunction;
import org.staccato.functions.SysexFunction;

public class StaccatoUtil 
{
   /**
	 * Returns the index of the first instance of the charToFind
	 * @param s
	 * @param charToFind
	 * @param startIndex
	 * @return
	 */
	public static int findNextOrEnd(String s, char charToFind, int startIndex) {
		return findNextOrEnd(s, new char[] { charToFind }, startIndex);
	}

   /**
	 * Returns the index of the first instance of any of the charsToFind
	 * @param s
	 * @param charsToFind
	 * @param startIndex
	 * @return
	 */
	public static int findNextOrEnd(String s, char[] charsToFind, int startIndex) {
		int position = Integer.MAX_VALUE;
		for (char ch : charsToFind) {
			int x = s.indexOf(ch, startIndex);
			if ((x != -1) && (x < position)) {
				position = x;
			}
		}
		
		// Wah wahhhh... didn't find what we were looking for
		if (position == Integer.MAX_VALUE) {
			position = s.length();
		}
		
		return position;
	}
	
	/*
	 * Element Generators
	 */

    public static String createTrackElement(byte track) {
        return Character.toString(IVLSubparser.VOICE) + track;
    }

    public static String createLayerElement(byte layer) {
        return Character.toString(IVLSubparser.LAYER) + layer;
    }

    public static String createInstrumentElement(byte instrument) {
        return Character.toString(IVLSubparser.INSTRUMENT) + instrument;
    }

    public static String createTempoElement(int tempoBPM) {
        return Character.toString(TempoSubparser.TEMPO) +tempoBPM;
    }

    public static String createKeySignatureElement(byte notePositionInOctave, byte scale) {
    	return SignatureSubparser.KEY_SIGNATURE + KeyProviderFactory.getKeyProvider().createKeyString(notePositionInOctave, scale);
    }

    public static String createTimeSignatureElement(byte numerator, byte powerOfTwo) {
        return SignatureSubparser.TIME_SIGNATURE + numerator + SignatureSubparser.SEPARATOR + (int)Math.pow(2, powerOfTwo); 
    }

    public static String createBarLineElement(long time) {
        return Character.toString(BarLineSubparser.BARLINE);
    }

    public static String createTrackBeatTimeBookmarkElement(String timeBookmarkId) {
        return Character.toString(LyricMarkerSubparser.MARKER) + timeBookmarkId;
    }

    public static String createTrackBeatTimeBookmarkRequestElement(String timeBookmarkId) {
        return Character.toString(BeatTimeSubparser.BEATTIME) + Character.toString(BeatTimeSubparser.BEATTIME_USE_MARKER) + timeBookmarkId;
    }

    public static String createTrackBeatTimeRequestElement(double time) {
        return Character.toString(BeatTimeSubparser.BEATTIME) + time;
    }

    public static String createPitchWheelElement(byte lsb, byte msb) {
        return FunctionSubparser.generateFunctionCall(PitchWheelFunction.getInstance().getNames()[0], lsb, msb);
    }

    public static String createChannelPressureElement(byte pressure) {
        return FunctionSubparser.generateFunctionCall(ChannelPressureFunction.getInstance().getNames()[0], pressure);
    }

    public static String createPolyphonicPressureElement(byte key, byte pressure) {
        return FunctionSubparser.generateFunctionCall(PolyPressureFunction.getInstance().getNames()[0], key, pressure);
    }

    public static String createSystemExclusiveElement(byte... bytes) {
        return FunctionSubparser.generateFunctionCall(SysexFunction.getInstance().getNames()[0], bytes);
    }

    public static String createControllerEventElement(byte controller, byte value) {
        return FunctionSubparser.generateFunctionCall(ControllerFunction.getInstance().getNames()[0], controller, value);
    }

    public static String createLyricElement(String lyric) {
    	return FunctionSubparser.generateParenParamIfNecessary(Character.toString(LyricMarkerSubparser.LYRIC), lyric);
    }

    public static String createMarkerElement(String marker) {
    	return FunctionSubparser.generateParenParamIfNecessary(Character.toString(LyricMarkerSubparser.MARKER), marker);
    }

    public static String createFunctionElement(String id, Object message) {
    	return FunctionSubparser.generateFunctionCall(id, message);
    }

    public static String createNoteElement(Note note) {
        return note.getPattern().toString();
    }
    
    public static String createNoteElement(Note note, byte track) {
        return (track == MidiDefaults.PERCUSSION_TRACK) ? note.getPercussionPattern().toString() : createNoteElement(note);
    }
    
    public static String createChordElement(Chord chord) {
    	return chord.getPattern().toString();
    }
}

