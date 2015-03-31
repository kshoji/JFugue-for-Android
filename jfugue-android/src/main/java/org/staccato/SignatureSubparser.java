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

import org.jfugue.parser.ParserException;
import org.jfugue.provider.ChordProviderFactory;
import org.jfugue.provider.KeyProvider;
import org.jfugue.theory.Key;
import org.jfugue.theory.Note;
import org.jfugue.theory.Scale;
import org.jfugue.theory.TimeSignature;

/**
 * Parses both Instrument and Layer tokens. Each has values that are parsed as bytes. 
 * 
 * @author dkoelle
 */
public class SignatureSubparser implements Subparser, KeyProvider 
{
	public static final String KEY_SIGNATURE = "KEY:";
	public static final String TIME_SIGNATURE = "TIME:";
	public static final String SEPARATOR = "/";
	
	private static SignatureSubparser instance;
	
	public static SignatureSubparser getInstance() {
		if (instance == null) {
			instance = new SignatureSubparser();
		}
		return instance;
	}
	
	@Override
	public boolean matches(String music) {
		return (matchesKeySignature(music) || matchesTimeSignature(music));
	}

	public boolean matchesKeySignature(String music) {
		return (music.length() >= KEY_SIGNATURE.length()) && (music.substring(0, KEY_SIGNATURE.length()).equals(KEY_SIGNATURE));
	}
	
	public boolean matchesTimeSignature(String music) {
		return (music.length() >= TIME_SIGNATURE.length()) && (music.substring(0, TIME_SIGNATURE.length()).equals(TIME_SIGNATURE));
	}
	
	@Override
	public int parse(String music, StaccatoParserContext context) {
		if (matchesKeySignature(music)) {
			int posNextSpace = StaccatoUtil.findNextOrEnd(music, ' ', 0);
			Key key = createKey(music.substring(KEY_SIGNATURE.length(), posNextSpace));
			context.setKey(key);
			context.getParser().fireKeySignatureParsed(key.getRoot().getPositionInOctave(), key.getScale().getMajorOrMinorIndicator());
			return posNextSpace + 1;
		} else if (matchesTimeSignature(music)) {
			int posNextSpace = StaccatoUtil.findNextOrEnd(music, ' ', 0);
			String timeString = music.substring(TIME_SIGNATURE.length(), posNextSpace);
			int posOfSlash = timeString.indexOf(SEPARATOR);
			if (posOfSlash == -1) {
				throw new ParserException(StaccatoMessages.NO_TIME_SIGNATURE_SEPARATOR, timeString);
			}
			byte numerator = Byte.parseByte(timeString.substring(0, posOfSlash));
			byte denominator = Byte.parseByte(timeString.substring(posOfSlash+1, timeString.length()));
			TimeSignature timeSignature = new TimeSignature(numerator, denominator);
			context.setTimeSignature(timeSignature);
			context.getParser().fireTimeSignatureParsed(numerator, denominator);
			return posNextSpace + 1;
		}
		return 0;
	}
	
	@Override
	public Key createKey(String keySignature) {
		// If the key signature starts with K, it is expected to contain a set of flat or sharp characters equal to the number of flats
		// or sharps one would see on the staff for the corresponding key. Defaults to MAJOR key.
	    if (keySignature.charAt(0) == 'K' && (keySignature.indexOf(SHARP_CHAR) == 1 || (keySignature.toUpperCase().indexOf(FLAT_CHAR) == 1))) {
	        return createKeyFromAccidentals(keySignature);
	    }
	    
	    // Otherwise, pass the string value - something like "Cmaj" - to createChord and generate a Key from the intervals in that chord
		return new Key(ChordProviderFactory.getChordProvider().createChord(keySignature));
	}
	
	/** Returns a Key given a string containing as many flats or sharps as the number one would see on a staff for the corresponding key; e.g., "Kbbbb" = Ab Major */
	public Key createKeyFromAccidentals(String keySignature) {
		return new Key(MAJOR_KEY_SIGNATURES[KEYSIG_MIDPOINT + countAccidentals(keySignature)] + MAJOR_ABBR);
	}
	
	private byte countAccidentals(String keySignatureAsFlatsOrSharps) {
		byte keySig = 0;
		for (char ch: keySignatureAsFlatsOrSharps.toUpperCase().toCharArray()) {
			if (ch == FLAT_CHAR) keySig--;
			if (ch == SHARP_CHAR) keySig++;
		}
		return keySig;
	}
	
	@Override
	public String createKeyString(byte notePositionInOctave, byte scale) {
        StringBuilder buddy = new StringBuilder();
        buddy.append(Note.NOTE_NAMES_COMMON[notePositionInOctave]);
        if (scale == Scale.MAJOR_INDICATOR) {
            buddy.append(MAJOR_ABBR);
        } else {
            buddy.append(MINOR_ABBR);
        }
        return buddy.toString();
	}

	@Override
	public byte convertKeyToByte(Key key) {
		String noteName = Note.getDispositionedToneStringWithoutOctave(key.getScale().getDisposition(), key.getRoot().getValue());
		if (noteName == null) {
			return 0;
		}
		for (byte b = -KEYSIG_MIDPOINT; b < KEYSIG_MIDPOINT+1; b++) {
			if (Note.isSameNote(noteName, (key.getScale() == Scale.MAJOR) ? MAJOR_KEY_SIGNATURES[KEYSIG_MIDPOINT + b] : MINOR_KEY_SIGNATURES[KEYSIG_MIDPOINT + b])) {
				return (byte)(b * key.getScale().getDisposition());
			}
		}
		return 0;
	}
	
	// Major and Minor Key Signatures
	// For the Major Key Signatures, 'C' is at the center position. Key signatures defined with flats are to the left of C; key signatures defined with sharps are to the right of C
	// For the Minor Key Signatures, 'A' is at the center position. Key signatures defined with flats are to the left of A; key signatures defined with sharps are to the right of A
	//
	//                                                                  7b    6b   5b    4b    3b    2b    1b   MID   1#   2#   3#    4#    5#    6#    7#     
    public static final String[] MAJOR_KEY_SIGNATURES = new String[] { "Cb", "Gb", "Db", "Ab", "Eb", "Bb", "F", "C", "G", "D", "A",  "E",  "B",  "F#", "C#" };
    public static final String[] MINOR_KEY_SIGNATURES = new String[] { "Ab", "Eb", "Bb", "F",  "C",  "G",  "D", "A", "E", "B", "F#", "C#", "G#", "D#", "A#" };
    public static final int KEYSIG_MIDPOINT = 7; 
    
    public static final String MAJOR_ABBR = "maj";
    public static final String MINOR_ABBR = "min";

    public static final char SHARP_CHAR = '#';
    public static final char FLAT_CHAR = 'B';
}
