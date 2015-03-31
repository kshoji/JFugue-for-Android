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

package org.jfugue.midi;

import org.jfugue.pattern.Pattern;
import org.jfugue.pattern.PatternProducer;
import org.jfugue.player.Player;
import org.staccato.StaccatoParserListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.Sequence;

public class MidiFileManager 
{
	public static void save(Sequence sequence, OutputStream out) throws IOException {
        int[] writers = MidiSystem.getMidiFileTypes(sequence);
        if (writers.length == 0) return;
        MidiSystem.write(sequence, writers[0], out);		
	}
	
	/** Convenience method to make it easier to save a file */ 
	public static void save(Sequence sequence, File file) throws IOException {
		MidiFileManager.save(sequence, new FileOutputStream(file));
	}
	
	public static void savePatternToMidi(PatternProducer patternProducer, OutputStream out) throws IOException {
		MidiFileManager.save(new Player().getSequence(patternProducer), out);
	}

	/** Convenience method to make it easier to save a file */ 
	public static void savePatternToMidi(PatternProducer patternProducer, File file) throws IOException {
		MidiFileManager.savePatternToMidi(patternProducer, new FileOutputStream(file));
	}
	
	public static Pattern loadPatternFromMidi(InputStream in) throws IOException, InvalidMidiDataException {
		MidiParser midiParser = new MidiParser();
		StaccatoParserListener staccatoListener = new StaccatoParserListener();
        midiParser.addParserListener(staccatoListener);
		midiParser.parse(MidiSystem.getSequence(in));
		return staccatoListener.getPattern();
	}
	
	/** Convenience method to make it easier to load a Pattern from a file */ 
	public static Pattern loadPatternFromMidi(File file) throws IOException, InvalidMidiDataException { 
		return MidiFileManager.loadPatternFromMidi(new FileInputStream(file));
	}

	/** Convenience method to make it easier to load a Pattern from a URL */ 
	public static Pattern loadPatternFromMidi(URL url) throws IOException, InvalidMidiDataException { 
		return MidiFileManager.loadPatternFromMidi(url.openStream());
	}

}
