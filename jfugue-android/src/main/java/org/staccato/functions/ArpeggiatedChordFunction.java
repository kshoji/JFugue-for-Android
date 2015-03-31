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

package org.staccato.functions;

import org.jfugue.theory.Chord;
import org.jfugue.theory.Note;
import org.staccato.PreprocessorFunction;
import org.staccato.StaccatoParserContext;

public class ArpeggiatedChordFunction implements PreprocessorFunction
{
	private static ArpeggiatedChordFunction instance;
	
	private ArpeggiatedChordFunction() { }
	
	public static ArpeggiatedChordFunction getInstance() {
		if (instance == null) {
			instance = new ArpeggiatedChordFunction();
		}
		return instance;
	}
	
	@Override
	public String apply(String parameters, StaccatoParserContext context) {
		Chord chord = new Chord(parameters);
		Note[] notes = chord.getNotes();
		double duration = chord.getRoot().getDuration();
		double durationPerNote = duration / notes.length;
		
		StringBuilder buddy = new StringBuilder();		
		for (Note note : notes) {
			buddy.append(Note.getToneString(note.getValue()));
			buddy.append("/");
			buddy.append(durationPerNote);
			buddy.append(" ");
		}

		return buddy.toString().trim();
	}

	@Override
	public String[] getNames() {
		return new String[] { "ARPEGGIATED", "AR" };
	}
}
