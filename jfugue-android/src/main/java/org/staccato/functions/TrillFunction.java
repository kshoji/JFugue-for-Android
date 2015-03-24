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

import org.jfugue.provider.NoteProviderFactory;
import org.jfugue.theory.Note;
import org.staccato.PreprocessorFunction;
import org.staccato.StaccatoParserContext;

/**
 * Replaces the given note with multiple 32nd notes of the given note and the note one interval higher.
 * For example, ":trill(Cq)" will become "Ct Dt Ct Dt Ct Dt Ct Dt"   
 *
 * @author dkoelle
 */
public class TrillFunction implements PreprocessorFunction
{
	private static TrillFunction instance;
	
	private TrillFunction() { }
	
	public static TrillFunction getInstance() {
		if (instance == null) {
			instance = new TrillFunction();
		}
		return instance;
	}
	
	@Override
	public String apply(String parameters, StaccatoParserContext context) {
		StringBuilder buddy = new StringBuilder();
		for (String noteString : parameters.split(" ")) {
			try {
				Note note = NoteProviderFactory.getNoteProvider().createNote(noteString);
				int n = (int)(note.getDuration() / THIRTY_SECOND_DURATION);
				for (int i=0; i < n/2; i++) {
					buddy.append(Note.getToneString((byte)note.getValue()));
					buddy.append("t ");
					// This function could really be more intelligent. For example, 
					// in the following line, the value of the trill note should actually
					// be consistent with the scale that is being used, and the note that
					// is being played. In a C-Major scale with an E note, F would be the
					// trill note, and that is only +1 from E. Also, the trill could become
					// increasingly quick. 
					buddy.append(Note.getToneString((byte)(note.getValue() + 2))); 
					buddy.append("t ");
				}
			} catch (Exception e) {
				// Nothing to do
			}
		}
		return buddy.toString().trim();
	}

	@Override
	public String[] getNames() {
		return NAMES;
	}
	
	private static final String[] NAMES = { "TRILL", "TR" };
	private static final double THIRTY_SECOND_DURATION = 1/32D;
}
