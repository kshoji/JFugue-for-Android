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

package org.jfugue.pattern;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.jfugue.midi.MidiDictionary;
import org.staccato.IVLSubparser;
import org.staccato.NoteSubparser;
import org.staccato.TempoSubparser;

public class Pattern implements PatternProducer
{
	protected StringBuilder patternSB;
	private int explicitVoice = UNDECLARED_EXPLICIT;
	private int explicitInstrument = UNDECLARED_EXPLICIT;
	private int explicitTempo = UNDECLARED_EXPLICIT;
	
	public Pattern() { 
	    patternSB = new StringBuilder();
	}
	    
	public Pattern(String string) {
		this();
	    patternSB.append(string);
	}
	
	public Pattern(String... strings) {
		this();
	    for (String string : strings) {
	    	patternSB.append(string);
	    	patternSB.append(" ");
	    }
	}
	
    public Pattern(PatternProducer... producers) {
        this();
        this.add(producers);
    }
    
    public Pattern add(PatternProducer... producers) {
        for (PatternProducer producer : producers) {
            this.add(producer.getPattern().toString());
        }
        return this;
    }
    
    public Pattern add(String string) {
        if (patternSB.length() > 0) {
        	patternSB.append(" ");
        }
        patternSB.append(string);
        return this;
    }
    
    public Pattern prepend(PatternProducer... producers) {
        for (PatternProducer producer : producers) {
            this.prepend(producer.getPattern().toString());
        }
        return this;
    }
    
    public Pattern prepend(String string) {
    	if (patternSB.length() > 0) {
    		patternSB.insert(0, " ");
    	}
    	patternSB.insert(0, string);
    	return this;
    }
    
    // This method necessarily digs into Staccato to get the VOICE indicator
    public Pattern addTrack(int trackNumber, PatternProducer producer) {
    	patternSB.append(" ");
    	patternSB.append(IVLSubparser.VOICE);
    	patternSB.append(trackNumber);
    	patternSB.append(" ");
    	patternSB.append(producer);
    	return this;
    }
    
    public Pattern clear() {
    	patternSB.delete(0, patternSB.length());
    	return this;
    }
    
    public Pattern repeat(int n) {
    	Pattern p2 = new Pattern();
    	for (int i=0; i < n; i++) {
    		p2.add(this.patternSB.toString());
    	}
    	this.patternSB = p2.patternSB;
    	return this;
    }
    
	@Override
    public Pattern getPattern() {
	    return this;
	}
	
	public String toString() {
		StringBuilder b2 = new StringBuilder();

		// Add the explicit tempo, if one has been provided
		if (explicitTempo != UNDECLARED_EXPLICIT) {
			b2.append(TempoSubparser.TEMPO);
			b2.append(explicitTempo);
			b2.append(" ");
		}

		// Add the explicit voice, if one has been provided
		if (explicitVoice != UNDECLARED_EXPLICIT) {
			b2.append(IVLSubparser.VOICE);
			b2.append(explicitVoice);
			b2.append(" ");
		}
		
		// Add the explicit voice, if one has been provided
		if (explicitInstrument != UNDECLARED_EXPLICIT) {
			b2.append(IVLSubparser.INSTRUMENT);
			b2.append("[");
			b2.append(MidiDictionary.INSTRUMENT_BYTE_TO_STRING.get((byte)explicitInstrument));
			b2.append("] ");
		}
		
		// Now add the actual contents of the pattern!
		b2.append(patternSB);
		
		return b2.toString();
	}
	
	/*
	 * Explicit setters for tempo, voice, and instrument
	 */
	
	/**
	 * Provides a way to explicitly set the tempo on a Pattern directly
	 * through the pattern rather than by adding text to the contents
	 * of the Pattern.
	 * 
	 * When Pattern.toString() is called, the a tempo will be prepended 
	 * to the beginning of the pattern in the form of "Tx", where x is the
	 * tempo number.
     *
	 * @return this pattern 
	 */
	public Pattern setTempo(int explicitTempo) {
		this.explicitTempo = explicitTempo;
		return this;
	}
	
	/**
	 * Provides a way to explicitly set the tempo on a Pattern directly
	 * through the pattern rather than by adding text to the contents
	 * of the Pattern.
	 * 
	 * When Pattern.toString() is called, the a tempo will be prepended 
	 * to the beginning of the pattern in the form of "Tx", where x is the
	 * tempo number (even though this method takes a string as a parameter)
     *
	 * @return this pattern 
	 */
	public Pattern setTempo(String tempo) {
		if (!MidiDictionary.TEMPO_STRING_TO_INT.containsKey(tempo.toUpperCase())) {
			throw new RuntimeException("The tempo '"+tempo+"' is not recognized");
		}
		return setTempo(MidiDictionary.TEMPO_STRING_TO_INT.get(tempo.toUpperCase()));
	}
	
	/**
	 * Provides a way to explicitly set the instrument on a Pattern directly
	 * through the pattern rather than by adding text to the contents
	 * of the Pattern.
	 * 
	 * When Pattern.toString() is called, the a voice will be prepended 
	 * to the beginning of the pattern after any explicit tempo and before any
	 * explicit instrument in the form of "Vx", where x is the voice number
     *
	 * @return this pattern 
	 */
	public Pattern setVoice(int voice) {
		this.explicitVoice = voice;
		return this;
	}

	/**
	 * Provides a way to explicitly set the instrument on a Pattern directly
	 * through the pattern rather than by adding text to the contents
	 * of the Pattern.
	 * 
	 * When Pattern.toString() is called, the a instrument will be prepended 
	 * to the beginning of the pattern after any explicit voice in the form of 
	 * "I[instrument-name]" (even though this method takes an integer as a parameter)
     *
	 * @return this pattern 
	 */
	public Pattern setInstrument(int instrument) {
		this.explicitInstrument = instrument;
		return this;
	}
	
	/**
	 * Provides a way to explicitly set the instrument on a Pattern directly
	 * through the pattern rather than by adding text to the contents
	 * of the Pattern.
	 * 
	 * When Pattern.toString() is called, the a instrument will be prepended 
	 * to the beginning of the pattern after any explicit voice in the form of 
	 * "I[instrument-name]"
     *
	 * @return this pattern 
	 */
	public Pattern setInstrument(String instrument) {
		if (!MidiDictionary.INSTRUMENT_STRING_TO_BYTE.containsKey(instrument.toUpperCase())) {
			throw new RuntimeException("The instrument '"+instrument+"' is not recognized");
		}
		return setInstrument(MidiDictionary.INSTRUMENT_STRING_TO_BYTE.get(instrument.toUpperCase()));
	}
	
	/*
	 * Decorate each note
	 */

	/**
	 * Expects a parameter of "note decorators" - i.e., things that are added to 
	 * the end of a note, such as duration or attack/decay settings; splits the given 
	 * parameter on spaces and applies each decorator to each note as it is encountered
	 * in the current pattern. 
	 * 
	 * If there is one decorator in the parameter, this method will apply that same
	 * decorator to all note in the pattern.
	 * 
	 * If there are more notes than decorators, a counter resets to 0 and the decorators
	 * starting from the first are applied to the future notes.
	 * 
	 * Examples:
	 * 
	 * new Pattern("A B C").addToEachNoteElement("q") 		--> "Aq Bq Cq"
	 * new Pattern("A B C").addToEachNoteElement("q i") 	--> "Aq Bi Cq" (rolls back to q for third note)
	 * new Pattern("A B C").addToEachNoteElement("q i s") 	--> "Aq Bi Cs"
	 * new Pattern("A B C").addToEachNoteElement("q i s w") --> "Aq Bi Cs" (same as "q i s")
	 * 
	 * @return this pattern
	 */
	public Pattern addToEachNoteElement(String decoratorString) {
		StringBuilder b2 = new StringBuilder();
		int currentDecorator = 0;
		String[] decorators = decoratorString.split(" ");
		String[] elements = patternSB.toString().split(" ");
		for (String element : elements) {
			if (NoteSubparser.getInstance().matches(element)) {
				b2.append(element);
				b2.append(decorators[currentDecorator++ % decorators.length]);
			} else {
				b2.append(element);
			}
			b2.append(" ");
		}
		this.patternSB = new StringBuilder(b2.toString().trim());
		return this;
	}
	
	public Pattern save(File file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(this.toString());
		writer.close();
		return this;
	}
	
	public static Pattern load(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Pattern pattern = new Pattern();
		String line = null;
		while ((line = reader.readLine()) != null) {
			pattern.add(line);
		}
		reader.close();
		return pattern;
	}
	
	private static final int UNDECLARED_EXPLICIT = -1;
}
