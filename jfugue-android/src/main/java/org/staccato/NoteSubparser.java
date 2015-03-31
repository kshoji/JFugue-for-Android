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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jfugue.parser.ParserException;
import org.jfugue.provider.ChordProvider;
import org.jfugue.provider.KeyProviderFactory;
import org.jfugue.provider.NoteProvider;
import org.jfugue.theory.Chord;
import org.jfugue.theory.Intervals;
import org.jfugue.theory.Note;

public class NoteSubparser implements Subparser, NoteProvider, ChordProvider {
	private static NoteSubparser instance;
	
	public static NoteSubparser getInstance() {
		if (instance == null) {
			instance = new NoteSubparser();
		}
		return instance;
	}
	
    private List<Character> charArray = new ArrayList<Character>();
    private Logger logger = Logger.getLogger("org.jfugue");

	private NoteSubparser() {
		charArray.add('C'); // Do
		charArray.add('D'); //   Re
		charArray.add('E'); //     Mi
		charArray.add('F'); //       Fa
		charArray.add('G'); //         So
		charArray.add('A'); //           La
		charArray.add('B'); //             Ti
		charArray.add('R'); // Rest
		charArray.add('['); // Note expressed as a value (e.g., "[SNARE_DRUM]q")
		charArray.add('0');
		charArray.add('1');
		charArray.add('2');
		charArray.add('3');
		charArray.add('4');
		charArray.add('5');
		charArray.add('6');
		charArray.add('7');
		charArray.add('8');
		charArray.add('9');
		
		logger.setLevel(Level.OFF);
	}

	@Override
	public boolean matches(String music) {
		return charArray.contains(music.charAt(0));
	}

	@Override
	public int parse(String s, StaccatoParserContext context) {
		return parseNoteElement(s, 0, context);
	}
	
	private int parseNoteElement(String s, int index, StaccatoParserContext parserContext) {
	    boolean repeat = false;
	    NoteContext noteContext = new NoteContext();
        do {
            // Begin the voyage of creating a note by populating the NoteContext
            index = parseNoteElement(s, index, noteContext, parserContext);
            
            if (noteContext.isChord) {
            	Chord chord = noteContext.createChord(parserContext);
            	parserContext.getParser().fireChordParsed(chord);
            } else {
	            Note note = noteContext.createNote(parserContext);
	            parserContext.getParser().fireNoteParsed(note);
            }
            
//            // If the note is a chord, fire all the note events for each note in the chord 
//            if (noteContext.isChord) {
//            	Note[] notes = new Chord(note, noteContext.intervals).getNotes();
//            	for (int i=1; i < notes.length; i++) {
//            		NoteContext context2 = noteContext.createChordNoteContext();
//            		context2.noteNumber = notes[i].getValue();
//            		logger.info("Next note in chord is "+context2.noteNumber);
//            		parserContext.fireNoteParsed(context2.createNote(parserContext));
//            	}
//            }

            // Determine if there is another note to parse, and set up the next NoteContext
            repeat = noteContext.thereIsAnother;
            noteContext = noteContext.createNextNoteContext();
        } while (repeat);
        return index;
	}
	
    /**
     * Parses a note element.
     * @param s the token that contains a note element
     * @throws JFugueException if there is a problem parsing the element
     */
    public int parseNoteElement(String s, int index, NoteContext noteContext, StaccatoParserContext parserContext) {
        logger.info("--Parsing note from string "+s);
        s = s.toUpperCase(); // Ensure s is uppercase - which might not be the case if this is coming in via NoteProvider or ChordProvider
        index = parseRoot(s, index, noteContext);  
        int startInternalInterval = parseOctave(s, index, noteContext); 
        int startChord = parseInternalInterval(s, startInternalInterval, noteContext);
        int startChordInversion = parseChord(s, startChord, noteContext); 
	    if (index == startInternalInterval) {
	    	setDefaultOctave(noteContext);
	    }
        logger.info("Octave: " +  noteContext.octaveNumber);
        computeNoteValue(noteContext, parserContext);

        index = parseChordInversion(s, startChordInversion, noteContext); 
        index = parseDuration(s, index, noteContext, parserContext);
        index = parseVelocity(s, index, noteContext);
        index = parseConnector(s, index, noteContext); 
        return index;
    }

    /**
     * Returns the index with which to start parsing the next part of the
     * string, once this method is done with its part
     */
    private int parseRoot(String s, int index, NoteContext context) {
        if ((s.charAt(index) >= 'A') && (s.charAt(index) <= 'G')) {
            return parseLetterNote(s, index, context);
        } else if (s.charAt(index) == 'R') {
            return parseRest(s, index, context);
        } else if (s.charAt(index) == '[') {
            return parseBracketedNote(s, index, context);
        } else if (s.charAt(index) >= '0' && (s.charAt(index) <= '9')) {
            return parseNumericNote(s, index, context);
        } else {
            // We should never get here; if we do, then there's something wrong with matches()
            return 0; 
        }
    }
    
    /** Returns the index with which to start parsing the next part of the string, once this method is done with its part */
    private int parseLetterNote(String s, int index, NoteContext context) {
        context.isNumericNote = false;
        int originalIndex = index;
        switch(s.charAt(index)) {
            case 'C' : context.noteNumber = 0; break;
            case 'D' : context.noteNumber = 2; break;
            case 'E' : context.noteNumber = 4; break;
            case 'F' : context.noteNumber = 5; break;
            case 'G' : context.noteNumber = 7; break;
            case 'A' : context.noteNumber = 9; break;
            case 'B' : context.noteNumber = 11; break;
            default : break;
        }
        index++;

        // Check for #, b, or n (sharp, flat, or natural) modifier
        boolean checkForModifiers = true;
        while (checkForModifiers) {
            if (index < s.length())
            {
                switch(s.charAt(index)) {
                    case '#' : index++; context.noteNumber++;  if (context.noteNumber == 12) { context.noteNumber = 0; context.octaveBias++; } break;
                    case 'B' : index++; context.noteNumber--;  if (context.noteNumber == -1) { context.noteNumber = 11; context.octaveBias--; } break;
                    case 'N' : index++; context.isNatural = true; checkForModifiers = false; break;
                    default : checkForModifiers = false; break;
                }
            } else {
                checkForModifiers = false;
            }
        }

        context.originalString = s.substring(originalIndex, index);
        
        logger.info("Note number within an octave (C=0, B=11): " +  context.noteNumber+" (with octaveBias = "+context.octaveBias+")");
        return index;
   }
    
    /** Returns the index with which to start parsing the next part of the string, once this method is done with its part */
    private int parseRest(String s, int index, NoteContext context) {
        context.isRest = true;
        logger.info("This note is a Rest");
        return index+1;
    }

    private int parseBracketedNote(String s, int index, NoteContext context) {
        int indexOfEndBracket = s.indexOf(']', index);
        String stringInBrackets = s.substring(index+1,indexOfEndBracket);
        context.noteValueAsString = stringInBrackets;
        context.isNumericNote = true;

        logger.info("This note is a note represented by the dictionary value "+context.noteValueAsString);
        return indexOfEndBracket+1;
    }

    /** Returns the index with which to start parsing the next part of the string, once this method is done with its part */
    private int parseNumericNote(String s, int index, NoteContext context) {
    	int numCharsInNumber = 0;
    	while (numCharsInNumber < s.length() && (s.charAt(index+numCharsInNumber) >= '0') && (s.charAt(index+numCharsInNumber) <= '9')) {
    	  numCharsInNumber++;
    	}
    	String numericNoteString = s.substring(index, index+numCharsInNumber); 
    	context.noteNumber =  Byte.parseByte(numericNoteString);
        context.isNumericNote = true;

        logger.info("This note is a numeric note with value " + context.noteNumber);
        return index+numCharsInNumber;
    }

     /** Returns the index with which to start parsing the next part of the string, once this method is done with its part */
    private int parseOctave(String s, int index, NoteContext context) {
        // Don't parse an octave for a rest or a numeric note
        if (context.isRest || context.isNumericNote) {
            return index;
        }

        // Check for octave.  Remember that octaves are optional.
        // Octaves can be two digits, which is what this next bit is testing for.
        // But, there could be no octave here as well. 
        char possibleOctave1 = '.';
        char possibleOctave2 = '.';

        if (index < s.length()) {
            possibleOctave1 = s.charAt(index);
        }

        if (index+1 < s.length()) {
            possibleOctave2 = s.charAt(index+1);
        }

        byte definiteOctaveLength = 0;
        if ((possibleOctave1 >= '0') && (possibleOctave1 <= '9'))  {
            definiteOctaveLength = 1;
            if (possibleOctave2 == '0') {
                definiteOctaveLength = 2;
            }
//            if ((possibleOctave1 == '-') && (definiteOctaveLength == 1)) {
//                // That '-' isn't representing a -1 octave, it's probably representing the end of a tie! 
//                // It's a duration character, not an octave character!
//                return index;
//            }
            
            logger.info("Octave is " + definiteOctaveLength + " digits long");

            String octaveNumberString = s.substring(index, index+definiteOctaveLength);
            logger.info("Octave string value is " + octaveNumberString);
            try {
                context.octaveNumber = Integer.parseInt(octaveNumberString) + context.octaveBias;
            } catch (NumberFormatException e) {
                throw new ParserException(StaccatoMessages.OCTAVE_OUT_OF_RANGE, s);
            }
            if (context.octaveNumber > Note.MAX_OCTAVE) {
                throw new ParserException(StaccatoMessages.OCTAVE_OUT_OF_RANGE, s);
            }
            if (context.octaveNumber < Note.MIN_OCTAVE) {
                throw new ParserException(StaccatoMessages.OCTAVE_OUT_OF_RANGE, s);
            }
            context.originalString = context.originalString + octaveNumberString;
        }
        return index+definiteOctaveLength;
    }

    private void setDefaultOctave(NoteContext context) {
        logger.info("No octave string found, setting default octave");

        if (context.isChord) {
            context.octaveNumber = DefaultNoteSettingsManager.getInstance().getDefaultBassOctave() + context.octaveBias;
        } else {
            context.octaveNumber = DefaultNoteSettingsManager.getInstance().getDefaultOctave() + context.octaveBias;
        }
    }
    
    /** Returns the index with which to start parsing the next part of the string, once this method is done with its part */
    private int parseInternalInterval(String s, int index, NoteContext context) {
    	if (context.isRest) {
    		return index;
    	}
    	
    	// An internal interval is indicated by a single quote
    	if ((index < s.length()) && (s.charAt(index) == '\'')) {
    		int intervalLength = 0;
    		// Verify that index+1 is a number representing the interval.
    		if (index+1 < s.length() && ((s.charAt(index+1) >= '0') && (s.charAt(index+1) <= '9'))) {
    			intervalLength = 1;
    		}
    		// We'll allow for the possibility of double-sharps and double-flats. 
    		if ((intervalLength == 1) && (index+2 < s.length()) && ((s.charAt(index+2) == '#') || (s.charAt(index+2) == 'B'))) {
    			intervalLength = 2;
    		}
    		if ((intervalLength == 2) && (index+3 < s.length()) && ((s.charAt(index+3) == '#') || (s.charAt(index+3) == 'B'))) {
    			intervalLength = 3;
    		}
        	context.internalInterval = Intervals.getHalfsteps(s.substring(index+1, index+intervalLength+1));
        	return index + intervalLength + 1;
    	} else {
    		return index;
    	}    	
    }
    
    /** Returns the index with which to start parsing the next part of the string, once this method is done with its part */
    private int parseChord(String s, int index, NoteContext context) {
        // Don't parse chord for a rest 
        if (context.isRest) {
            return index;
        }

        int lengthOfChordString = 0;
        boolean chordFound = false;
        String[] chordNames = Chord.getChordNames();
        for (String chordName : chordNames) {
            if (!chordFound && (s.length() >= index + chordName.length()) && chordName.equals(s.substring(index, index+chordName.length()))) {
                chordFound = true;
                lengthOfChordString = chordName.length();
                context.isChord = true;
                context.intervals = Chord.getIntervals(chordName);
                context.chordName = chordName;
                logger.info("Chord: "+chordName+"   Interval Pattern: "+Chord.getIntervals(chordName));
                break;
            }
        }
        return index + lengthOfChordString;
    }

    /** Returns the index with which to start parsing the next part of the string, once this method is done with its part */
    private int parseChordInversion(String s, int index, NoteContext context) {
        if (!context.isChord) {
            return index;
        }

        int inversionCount = 0;
        boolean bassNote = false;

        int startIndex = index;
        boolean checkForInversion = true;
        while (checkForInversion) {
            if (index < s.length())
            {
                switch(s.charAt(index)) {
                case '^': index++; inversionCount++; break;
                case 'C': index++; bassNote = true; break;
                case 'D': index++; bassNote = true; break;
                case 'E': index++; bassNote = true; break;
                case 'F': index++; bassNote = true; break;
                case 'G': index++; bassNote = true; break;
                case 'A': index++; bassNote = true; break;
                case 'B': index++; bassNote = true; break;
                case '#': index++; break; // presumably the sharp mark followed a note
                // For '0', need to differentiate between initial 0 and 0 as a second digit (i.e., 10)
                case '0': index++; inversionCount = (inversionCount == -1) ? 0 : inversionCount + 10; break;
                case '1': index++; inversionCount = 1; break;
                case '2': index++; inversionCount = 2; break;
                case '3': index++; inversionCount = 3; break;
                case '4': index++; inversionCount = 4; break;
                case '5': index++; inversionCount = 5; break;
                case '6': index++; inversionCount = 6; break;
                case '7': index++; inversionCount = 7; break;
                case '8': index++; inversionCount = 8; break;
                case '9': index++; inversionCount = 9; break;
                // For '[', we're checking for a note number after the inversion marker
                case '[':
                    int indexEndBracket = s.indexOf(']', index);
                    context.inversionBassNote = Note.getToneString(Byte.parseByte(s.substring(index + 1, indexEndBracket - 1)));
                    index = indexEndBracket + 1;
                    break;
                default:
                    checkForInversion = false;
                    break;
                }
            } else {
                checkForInversion = false;
            }
        }

        // Modify the note values based on the inversion
        if (bassNote) {
        	context.inversionBassNote = context.inversionBassNote = s.substring(startIndex+1, index);
        } else if (inversionCount > 0) {
        	context.inversionCount = inversionCount;
        }
        return index;
    }
    
    /** This method does a variety of calculations to get the actual value of the note. */
    private void computeNoteValue(NoteContext noteContext, StaccatoParserContext parserContext) {
        // Don't compute note value for a rest 
        if (noteContext.isRest) {
            return;
        }

        // Adjust for Key Signature
        if (parserContext.getKey() != null) {
            int keySig = KeyProviderFactory.getKeyProvider().convertKeyToByte(parserContext.getKey());
            if ((keySig != 0) && (!noteContext.isNatural)) {
                if ((keySig <= -1) && (noteContext.noteNumber == 11)) noteContext.noteNumber = 10;
                if ((keySig <= -2) && (noteContext.noteNumber == 4)) noteContext.noteNumber = 3;
                if ((keySig <= -3) && (noteContext.noteNumber == 9)) noteContext.noteNumber = 8;
                if ((keySig <= -4) && (noteContext.noteNumber == 2)) noteContext.noteNumber = 1;
                if ((keySig <= -5) && (noteContext.noteNumber == 7)) noteContext.noteNumber = 6;
                if ((keySig <= -6) && (noteContext.noteNumber == 0)) { noteContext.noteNumber = 11; noteContext.octaveNumber--; }
                if ((keySig <= -7) && (noteContext.noteNumber == 5)) noteContext.noteNumber = 4;
                if ((keySig >= +1) && (noteContext.noteNumber == 5)) noteContext.noteNumber = 6;
                if ((keySig >= +2) && (noteContext.noteNumber == 0)) noteContext.noteNumber = 1;
                if ((keySig >= +3) && (noteContext.noteNumber == 7)) noteContext.noteNumber = 8;
                if ((keySig >= +4) && (noteContext.noteNumber == 2)) noteContext.noteNumber = 3;
                if ((keySig >= +5) && (noteContext.noteNumber == 9)) noteContext.noteNumber = 10;
                if ((keySig >= +6) && (noteContext.noteNumber == 4)) noteContext.noteNumber = 5;
                if ((keySig >= +7) && (noteContext.noteNumber == 11)) { noteContext.noteNumber = 0; noteContext.octaveNumber++; }
                logger.info("After adjusting for Key Signature, noteNumber=" + noteContext.noteNumber +" octave=" +  noteContext.octaveNumber);
            }
        }
        
        // Compute the actual note number, based on octave and note
        if (!noteContext.isNumericNote)
        {
            int intNoteNumber = ((noteContext.octaveNumber) * 12) + noteContext.noteNumber + noteContext.internalInterval;
            if ( intNoteNumber > 127) {
                throw new ParserException(StaccatoMessages.CALCULATED_NOTE_OUT_OF_RANGE, Integer.toString(intNoteNumber));
            }
            noteContext.noteNumber = (byte)intNoteNumber;
            logger.info("Computed note number: " +  noteContext.noteNumber);
        }
    }
    
    /** Returns the index with which to start parsing the next part of the string, once this method is done with its part */
    private int parseDuration(String s, int index, NoteContext noteContext, StaccatoParserContext parserContext) {
        if (index < s.length()) {
            switch (s.charAt(index)) {
                case '/' : index = parseNumericDuration(s, index, noteContext); break;
                case 'W' :    
                case 'H' :
                case 'Q' :
                case 'I' :
                case 'S' :
                case 'T' :
                case 'X' :
                case 'O' :
                case '-' : index = parseLetterDuration(s, index, noteContext, parserContext); break;
                default : noteContext.decimalDuration = DefaultNoteSettingsManager.getInstance().getDefaultDuration(); noteContext.durationExplicitlySet = false; break; // Could get here if the next character is a velocity char ("a" or "d")
            }
            index = parseTuplet(s, index, noteContext); 
        } else {
        	noteContext.decimalDuration = DefaultNoteSettingsManager.getInstance().getDefaultDuration();
        	noteContext.durationExplicitlySet = false;
        }

        logger.info("Decimal duration is " + noteContext.decimalDuration);

        return index;
    }
    
    /** Returns the index with which to start parsing the next part of the string, once this method is done with its part */
    private int parseNumericDuration(String s, int index, NoteContext context) {
    	// The duration has come in as a number, like 0.25 for a quarter note.
        // Advance pointer past the initial slash (/)
        index++;

        // If first character before the numeric value is a dash, we're ending a tie
        if (s.charAt(index) == '-') {
        	context.isEndOfTie = true;
        	index++;
        }
        
    	context.durationExplicitlySet = true;

    	// Get the duration value
        int endingIndex = seekToEndOfDecimal(s,index);
        String durationNumberString = s.substring(index, endingIndex);
       	context.decimalDuration += Double.parseDouble(durationNumberString);
        logger.info("Decimal duration is " + context.decimalDuration);
        index = endingIndex;
        
        // If the character after all of the value parsing is a dash, we're starting a tie
        if ((index < s.length()) && (s.charAt(index) == '-')) {
        	context.isStartOfTie = true;
        	index++;
        }
        
        return index;
    }
    
    /** Returns the index with which to start parsing the next part of the string, once this method is done with its part */
    private int parseQuantityDuration(String s, int index, NoteContext context) {
        // A quantity is associated with the duration, like the '24' in "w24"
        int endingIndex = seekToEndOfDecimal(s,index);
        String quantityNumberString = s.substring(index, endingIndex);
        context.decimalDuration += (1.0f/context.mostRecentDuration) * (Double.parseDouble(quantityNumberString) - 1.0D); // Subtract 1, because mostRecentDuration has already been added to the total duration
        logger.info("Quantity duration calculation: Duration of 1/"+context.mostRecentDuration+" * "+quantityNumberString+" = "+(1.0/context.mostRecentDuration) * Double.parseDouble(quantityNumberString));
        return endingIndex;
    }

    private int seekToEndOfDecimal(String s, int startingIndex) {
        int cursor = startingIndex;
        while (cursor < s.length() && (s.charAt(cursor) == '.' || ((s.charAt(cursor) >= '0') && (s.charAt(cursor) <= '9')))) {
            cursor++;
        }
        return cursor;
    }
    
    /** Returns the index with which to start parsing the next part of the string, once this method is done with its part */
    private int parseLetterDuration(String s, int index, NoteContext context, StaccatoParserContext parserContext) {
        boolean moreDurationCharsToParse = true;
        boolean isDotted = false;

        while (moreDurationCharsToParse == true) {
            int durationNumber = 0;
            // See if the note has a duration. Duration is optional for a note.
            if (index < s.length()) {
                char durationChar = s.charAt(index);
                switch (durationChar) {
                    case '-' : if ((context.decimalDuration == 0.0D) && (!context.isEndOfTie)) {
                                   context.isEndOfTie = true;
                                   logger.info("Note is end of tie");
                               } else {
                                   context.isStartOfTie = true;
                                   logger.info("Note is start of tie");
                               }
                               break;
                    case 'W' : durationNumber = 1; break;
                    case 'H' : durationNumber = 2; break;
                    case 'Q' : durationNumber = 4; break;
                    case 'I' : durationNumber = 8; break;
                    case 'S' : durationNumber = 16; break;
                    case 'T' : durationNumber = 32; break;
                    case 'X' : durationNumber = 64; break;
                    case 'O' : durationNumber = 128; break;
                    default  : index--; moreDurationCharsToParse = false; break;
                }
                index++;
                if ((index < s.length()) && (s.charAt(index) == '.')) {
                    isDotted = true;
                    index++;
                }

                if (durationNumber > 0) {
                	context.durationExplicitlySet = true;
                    double d = 1.0/durationNumber;
                    if (isDotted) {
                        context.decimalDuration += d + (d/2.0);
                    } else {
                        context.decimalDuration += d;
                    }
                }
                
                context.mostRecentDuration = durationNumber;
                
                if ((index < s.length()) && (s.charAt(index) >= '0') && (s.charAt(index) <= '9')) {
                	index = parseQuantityDuration(s, index, context);
                }
            } else {
                moreDurationCharsToParse = false;
            }
        }

        return index;
    }
    
    /** Returns the index with which to start parsing the next part of the string, once this method is done with its part */
    private int parseTuplet(String s, int index, NoteContext context) {
        if (index < s.length()) {
            if (s.charAt(index) == '*') {
                logger.info("Note is a tuplet");
                index++;

                // Figure out tuplet ratio, or figure out when to stop looking for tuplet info
                boolean stopTupletParsing = false;
                int indexOfUnitsToMatch = 0;
                int indexOfNumNotes = 0;
                int counter = -1;
                while (!stopTupletParsing) {
                    counter++;
                    if (s.length() > index+counter) {
                        if (s.charAt(index+counter) == ':') {
                            indexOfNumNotes = index+counter+1;
                        }
                        else if ((s.charAt(index+counter) >= '0') && (s.charAt(index+counter) <= '9')) {
                            if (indexOfUnitsToMatch == 0) {
                                indexOfUnitsToMatch = index+counter;
                            }
                        }
                        else if ((s.charAt(index+counter) == '*')) {
                            // no op... artifact of parsing
                        }
                        else {
                            stopTupletParsing = true;
                        }
                    } else {
                        stopTupletParsing = true;
                    }
                }

                index += counter;

                double numerator = 2.0d;
                double denominator = 3.0d;
                if ((indexOfUnitsToMatch > 0) && (indexOfNumNotes > 0)) {
                    numerator = Double.parseDouble(s.substring(indexOfUnitsToMatch, indexOfNumNotes-1));
                    denominator = Double.parseDouble(s.substring(indexOfNumNotes, index));
                }
                logger.info("Tuplet ratio is "+numerator+":"+denominator);
                double tupletRatio = numerator / denominator;
                context.decimalDuration = context.decimalDuration * (1.0d / tupletRatio);
                logger.info("Decimal duration after tuplet is " +  context.decimalDuration);
            }
        }

        return index;
    }
    
    /** Returns the index with which to start parsing the next part of the string, once this method is done with its part */
    private int parseVelocity(String s, int index, NoteContext context) {
        // Don't compute note velocity for a rest 
        if (context.isRest) {
            return index;
        }

        // Process velocity attributes, if they exist
        while (index < s.length()) {
            int startPoint = index+1;
            int endPoint = startPoint;

            char velocityChar = s.charAt(index);
            int lengthOfByte = 0;
            if ((velocityChar == '+') || (velocityChar == '_') || (velocityChar == ' ')) break;
            logger.info("Identified Velocity character " + velocityChar);
            boolean byteDone = false;
            while (!byteDone && (index + lengthOfByte+1 < s.length())) {
                char possibleByteChar = s.charAt(index + lengthOfByte+1);
                if ((possibleByteChar >= '0') && (possibleByteChar <= '9')) {
                    lengthOfByte++;
                } else {
                    byteDone = true;
                }
            }
            endPoint = index + lengthOfByte+1;

            if (startPoint == endPoint) {
            	return endPoint;
            }
            
            byte velocityNumber = Byte.parseByte(s.substring(startPoint,endPoint));
            
            // Or maybe a bracketed string was passed in, instead of a byte
            String velocityString = null;
            if ((index+1 < s.length()) && (s.charAt(index+1) == '[')) {
                endPoint = s.indexOf(']',startPoint)+1;
                velocityString = s.substring(startPoint,endPoint);
            }

            switch (velocityChar) {
                case 'A' : if (velocityString == null) { context.noteOnVelocity = velocityNumber; } else { context.noteOnVelocityValueAsString = velocityString; } context.hasNoteOnVelocity = true; break;
                case 'D' : if (velocityString == null) { context.noteOffVelocity = velocityNumber; } else { context.noteOffVelocityValueAsString = velocityString; } context.hasNoteOffVelocity = true; break;
                default  : throw new ParserException(StaccatoMessages.VELOCITY_CHARACTER_NOT_RECOGNIZED, s.substring(startPoint,endPoint));
            }
            index = endPoint;
        }
        
        if (context.hasNoteOnVelocity) { logger.info("Attack velocity = " + context.noteOnVelocity); }
        if (context.hasNoteOffVelocity) { logger.info("Decay velocity = " + context.noteOffVelocity); }
        
        return index;
    }

    /** Returns the String of the next sub-token (the parts after + or _), if one exists; otherwise, returns null */
    private int parseConnector(String s, int index, NoteContext context) {
        context.thereIsAnother = false;
        // See if there's another note to process
        if ((index < s.length()) && ((s.charAt(index) == '+') || (s.charAt(index) == '_'))) {
            logger.info("Another note: string = " + s.substring(index, s.length()-1));
            if (s.charAt(index) == '_') {
                context.anotherNoteIsMelodic = true;
                logger.info("Next note will be melodic");
            } else {
                context.anotherNoteIsHarmonic = true;
                logger.info("Next note will be harmonic");
            }
            index++;
            context.thereIsAnother = true;
        }
        return index;
    }
    
    
    class NoteContext {
    	public String originalString;
    	public byte noteNumber;
        public String noteValueAsString;
    	public boolean isNumericNote;
        public boolean isChord;
        public String chordName;
        public Intervals intervals;
        public int inversionCount;
        public String inversionBassNote;
    	public boolean isRest;
    	public boolean isNatural; 
    	public int octaveBias; 
    	public int octaveNumber; 
    	public int internalInterval; 
    	public double decimalDuration;
    	public String durationValueAsString;
    	public boolean durationExplicitlySet;
    	public int mostRecentDuration;
    	public boolean hasIndeterminateDuration;
    	public boolean isEndOfTie;
    	public boolean isStartOfTie;
    	public boolean hasNoteOnVelocity;
    	public byte noteOnVelocity;
    	public String noteOnVelocityValueAsString;
    	public boolean hasNoteOffVelocity;
        public byte noteOffVelocity;
        public String noteOffVelocityValueAsString;
    	public boolean isFirstNote;
    	public boolean isMelodicNote;
        public boolean anotherNoteIsMelodic;
        public boolean isHarmonicNote;
        public boolean anotherNoteIsHarmonic;
        public boolean thereIsAnother; 

        /** 
         * NoteContext should only be constructed when a note token is first being parsed.
         * Subsequent parsings of notes within the same token must create a NoteContext using 
         * createNextNoteContext()
         */
        public NoteContext() {
            this.isFirstNote = true;
        }
        
        /**
         * Must be called (instead of the constructor) for notes other than the first note
         * being parsed
         * @return
         */
        public NoteContext createNextNoteContext() {
            NoteContext noteContext = new NoteContext();
            noteContext.isFirstNote = false;
            noteContext.isMelodicNote = anotherNoteIsMelodic;
            noteContext.isHarmonicNote = anotherNoteIsHarmonic;
            return noteContext;
        }
        
        public NoteContext createChordNoteContext() {
        	NoteContext noteContext = new NoteContext();
            noteContext.isFirstNote = false;
            noteContext.isMelodicNote = false;
            noteContext.isHarmonicNote = true;
    		noteContext.decimalDuration = decimalDuration;
    		return noteContext;
        }

        /** 
         * Creates a Note based on the settings in this NoteContext
         * @return Note 
         */
        public Note createNote(StaccatoParserContext parserContext) {
        	try {
	        	if (noteValueAsString != null) {
	        		noteNumber = (Byte)parserContext.getDictionary().get(noteValueAsString);
	        	}
        	} catch (NullPointerException e) {
        		throw new RuntimeException("JFugue NoteSubparser: Could not find '"+noteValueAsString+"' in dictionary.");
        	}

        	try {
	        	if (durationValueAsString != null) {
	        		decimalDuration = (Byte)parserContext.getDictionary().get(durationValueAsString);
	        	}
	    	} catch (NullPointerException e) {
	    		throw new RuntimeException("JFugue NoteSubparser: Could not find '"+durationValueAsString+"' in dictionary.");
	    	}

        	Note note = new Note(noteNumber);
        	if (durationExplicitlySet) {
        		note.setDuration(decimalDuration);
        	}
            note.setOriginalString(originalString);
        	note.setRest(isRest);

            if (hasNoteOnVelocity) {
	            if (noteOnVelocityValueAsString != null) {
	            	noteOnVelocity = (Byte)parserContext.getDictionary().get(noteOnVelocityValueAsString);
	            }
	            note.setOnVelocity(noteOnVelocity);
            }
            
            if (hasNoteOffVelocity) {
	            if (noteOffVelocityValueAsString != null) {
	            	noteOffVelocity = (Byte)parserContext.getDictionary().get(noteOffVelocityValueAsString);
	            }
	            note.setOffVelocity(noteOffVelocity);
            }
            
            note.setEndOfTie(isEndOfTie);
            note.setStartOfTie(isStartOfTie);
            note.setFirstNote(isFirstNote);
            note.setHarmonicNote(isHarmonicNote);
            note.setMelodicNote(isMelodicNote);

            return note;
        }
        
        /** 
         * Creates a Note based on the settings in this NoteContext
         * @return Note 
         */
        public Chord createChord(StaccatoParserContext parserContext) {
        	if (noteValueAsString != null) {
        		noteNumber = (Byte)parserContext.getDictionary().get(noteValueAsString);
        	}
        	if (durationValueAsString != null) {
        		decimalDuration = (Byte)parserContext.getDictionary().get(durationValueAsString);
        	}

        	Note rootNote = createNote(parserContext);
        	if (isChord) {
        		Chord chord = new Chord(rootNote, intervals);
        		if (!(inversionBassNote == null)) {
        			chord.setBassNote(inversionBassNote);
        		} else  if (inversionCount > 0) {
        			chord.setInversion(inversionCount);
        		}
        		return chord;
        	} 
        	return null;
        }
    }
    
    // 
    // Methods from NoteProvider
    //
    @Override
    public Note createNote(String noteString) {
    	StaccatoParserContext parserContext = new StaccatoParserContext(new StaccatoParser());
    	NoteContext noteContext = new NoteContext();
    	parseNoteElement(noteString, 0, noteContext, parserContext);
    	return noteContext.createNote(parserContext);
    }

    @Override
    public Note getMiddleC() {
        return createNote("C"); 
    }
    
    @Override
    public double getDurationForString(String s) {
    	NoteContext noteContext = new NoteContext();
    	StaccatoParserContext parserContext = new StaccatoParserContext(new StaccatoParser());
    	this.parseDuration(s, 0, noteContext, parserContext);
    	return noteContext.decimalDuration;
    }

    public static void populateContext(StaccatoParserContext context) {
        for (int i=0; i < Note.PERCUSSION_NAMES.length; i++) {
            context.getDictionary().put(Note.PERCUSSION_NAMES[i], (byte)(i+35));
        }
       
        // Also give a hand to Chord!

	    for (String key : Chord.chordMap.keySet()) {
	        context.getDictionary().put(key, Chord.chordMap.get(key));
	    }
    }


    // 
    // Method from ChordProvider
    //
    
    public Chord createChord(String chordString) {
    	// If the user requested a chord like "C" or "Ab", assume it's MAJOR
    	if (chordString.length() <= 2) {
    		chordString = chordString + "MAJ";
    	}
    	
    	StaccatoParserContext parserContext = new StaccatoParserContext(new StaccatoParser());
    	NoteContext noteContext = new NoteContext();
    	parseNoteElement(chordString, 0, noteContext, parserContext);
    	return noteContext.createChord(parserContext);
    }
}


