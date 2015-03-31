/*
 * JFugue, an Application Programming Interface (API) for Music Programming
 * http://www.jfugue.org
 *
 * Copyright (C) 2003-2013 David Koelle
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jfugue.integration;

import java.util.StringTokenizer;

import org.jfugue.midi.MidiDictionary;
import org.jfugue.parser.ParserListenerAdapter;
import org.jfugue.theory.Chord;
import org.jfugue.theory.Note;

/**
 * This class listens to events from the MusicString parser. In response to this
 * events, a Lilypond string is produced. The Lilypond string is produced with
 * relative octave notation.
 * 
 * @author Hans Beemsterboer
 */
public class LilyPondParserListener extends ParserListenerAdapter {
	boolean closeStaff = false;
	boolean handleChord = false;
	private boolean closeChord = false;
	private boolean handlePolyphony = false;
	private boolean closePolyphony = false;
	private NoteWindow noteWindow = new NoteWindow();
	private StringBuffer lyString = new StringBuffer(" ");
	private boolean isDebug = false;

	private void log(String message) {
		if (isDebug) {
			System.out.println(message);
		}
	}

	@Override
	public void onTrackChanged(byte track) {
		log("Track change: " + track);
		if (lyString.length() > 1) {
			save(true);
			handleLastNote();
			noteWindow.emptyAll();
			lyString.append("}\n");
		} else {
			lyString = new StringBuffer();
		}
		closeStaff = true;
		lyString.append("\\new Staff { ");

	}

	@Override
	public void onInstrumentParsed(byte instrument) {
		log("Instrument change");
		String id = Byte.toString(instrument);
		String instrument2 = "\\set Staff.instrumentName = \"" + MidiDictionary.INSTRUMENT_BYTE_TO_STRING.get(Byte.parseByte(id)) + "\" ";
		lyString.append(instrument2);
	}

	@Override
	public void onNoteParsed(Note note2) {
		noteWindow.addNote(note2);
		if (note2.isFirstNote()) {
			handleChord = false;
			if (handlePolyphony) {
				handlePolyphony = false;
			}
		} else {
			if (note2.isHarmonicNote()) {
				handleChord = true;
				handlePolyphony = false;
			} else {
				handleChord = false;
				handlePolyphony = true;
			}
		}
		if (noteWindow.getSecondPreviousNote() != null) {
			save(false);
		}

	}

	private void printChord(Chord chord) {
		log("Chord parsed: rootnote = " + chord.getRoot().getValue() + "  intervals = " + chord.getIntervals().toString() + "  duration = " + chord.getRoot().getDuration() + "  attack = "
				+ chord.getRoot().getOnVelocity() + "  decay = " + chord.getRoot().getOffVelocity());
		log(chord.getPatternWithNotes().toString());
		for (Note note : chord.getNotes()) {
			printNote(note);
		}

	}

	private void printNote(Note note) {
		log(note.toDebugString());
	}

	@Override
	public void onChordParsed(Chord chord) {
		printChord(chord);
		noteWindow.addChordOctave(chord.getRoot());
		String musicString = chord.getPatternWithNotes().toString();
		String duration = LilyPondNoteDurationHelper.getDuration2(Double.toString(chord.getRoot().getDuration()));
		parallelNoteEvent(musicString, duration, chord.getRoot().originalString);
		lyString.append(">");
		lyString.append(duration);
		lyString.append(" ");
	}

	private void parallelNoteEvent(String musicString, String duration, String rootNote) {
		boolean isFirst = true;
		lyString.append("<");
		StringTokenizer tokenizer = new StringTokenizer(musicString, "+");
		while (tokenizer.hasMoreElements()) {
			String note = tokenizer.nextToken();
			String firstLetter = note.substring(0, 1).toLowerCase();
			lyString.append(firstLetter);

			if (isFirst) {
				int octaveChange = noteWindow.getOctaveChange(firstLetter.charAt(0));
				if (octaveChange > 0) {
					for (int i = 0; i < octaveChange; i++) {
						log("Add octave change");
						lyString.append("'");
					}
				}
				if (octaveChange < 0) {
					for (int i = 0; i > octaveChange; i--) {
						log("Add octave change");
						lyString.append(",");
					}
				}
				isFirst = false;
				noteWindow.setLastNote(firstLetter.charAt(0));
			}

			if (tokenizer.hasMoreElements()) {
				lyString.append(" ");
			}
		}
	}

	public String getLyString() {
		noteWindow.print();
		save(true);
		handleLastNote();
		lyString.append("}");
		return lyString.toString();
	}

	private void handleLastNote() {
		log("Current note: " + noteWindow.getCurrentNote());
		if (noteWindow.getCurrentNoteLy().length() > 0 && !closeChord) {
			lyString.append(noteWindow.getCurrentNoteLy());
		}
		if (noteWindow.getCurrentNoteDuration() != null && !closeChord) {
			lyString.append(noteWindow.getCurrentNoteDuration());
		}
		if (noteWindow.getCurrentNote() != null && !closeChord) {
			lyString.append(" ");
		}

		if (!lyString.toString().contains("new Staff")) {
			closeStaff = true;
			StringBuffer lyBuffer = new StringBuffer();
			lyBuffer.append("\\new Staff {");
			lyBuffer.append(lyString);
			lyString = lyBuffer;
		}
		if (closeChord) {
			closeChord = false;
			lyString.append(noteWindow.getCurrentNoteLy());
			lyString.append(">");
			lyString.append(noteWindow.getCurrentNoteDuration());
			lyString.append(" ");
		}
		if (closePolyphony) {
			lyString.append("} >> ");
		}

	}

	private void save(boolean isLastSave) {
		log("==> Save called, lyString before: " + lyString + ", last save: " + isLastSave);
		noteWindow.print();

		if (!isLastSave) {
			log("secondPreviousNote: " + noteWindow.getSecondPreviousNote().originalString);
			if (noteWindow.getSecondPreviousNote().isFirstNote() && noteWindow.getPreviousNote().isHarmonicNote() && !noteWindow.getCurrentNote().isMelodicNote()) {
				lyString.append("<");
				lyString.append(noteWindow.getSecondPreviousNoteLy());
				lyString.append(" ");
				closeChord = true;
			} else if (noteWindow.getSecondPreviousNote().isFirstNote() && handlePolyphony) {
				if (closePolyphony) {
					closePolyphony = false;
					lyString.append("} >>");
				}
				lyString.append("<< { ");
				closePolyphony = true;
				lyString.append(noteWindow.getSecondPreviousNoteLy());
				lyString.append(noteWindow.getSecondPreviousNoteDuration());
				if (noteWindow.getPreviousNote().isHarmonicNote()) {
					lyString.append(" } \\\\ { ");
				}
			} else if (noteWindow.getSecondPreviousNote().isHarmonicNote() && noteWindow.getPreviousNote().isFirstNote()) {
				// close parallel
				lyString.append(noteWindow.getSecondPreviousNoteLy());
				lyString.append(">");
				lyString.append(noteWindow.getPreviousNoteDuration());
				lyString.append(" ");
				closeChord = false;
			} else {
				lyString.append(noteWindow.getSecondPreviousNoteLy());
				if (!noteWindow.getSecondPreviousNote().isHarmonicNote()) {
					lyString.append(noteWindow.getSecondPreviousNoteDuration());
				}
				lyString.append(" ");
			}
			if (noteWindow.getSecondPreviousNote().isStartOfTie()) {
				lyString.append("~ ");
			}
		}

		if (!isLastSave && noteWindow.getPreviousNote() != null && noteWindow.getPreviousNote().isFirstNote() && noteWindow.getCurrentNote().isHarmonicNote()) {
			log("We don't know yet");
			return;
		}

		if (noteWindow.getSecondPreviousNote() != null && !noteWindow.getCurrentNote().isHarmonicNote() && (noteWindow.getPreviousNote() != null || noteWindow.getCurrentNote() != null)) {
			lyString.append(noteWindow.getPreviousNoteLy());
		}

		if (noteWindow.getPreviousNote() != null) {
			log("previousnote: " + noteWindow.getPreviousNote().originalString);
			if (isLastSave && noteWindow.getPreviousNote().isFirstNote() && noteWindow.getCurrentNote().isHarmonicNote()) {

				lyString.append("<");
				lyString.append(noteWindow.getPreviousNoteLy());
				lyString.append(" ");
				closeChord = true;
			}
			if (noteWindow.getPreviousNote().isHarmonicNote() && noteWindow.getCurrentNote().isHarmonicNote()) {
				lyString.append(noteWindow.getPreviousNoteLy());
				lyString.append(" ");
			}

			if (noteWindow.getPreviousNote().isHarmonicNote() && noteWindow.getCurrentNote().isFirstNote()) {
				// close parallel
				lyString.append(">");
				lyString.append(noteWindow.getPreviousNoteDuration());
				lyString.append(" ");
				closeChord = false;
			} else if (!noteWindow.getCurrentNote().isHarmonicNote()) {
				if (noteWindow.getSecondPreviousNote() == null) {
					lyString.append(noteWindow.getPreviousNoteLy());
				}
				lyString.append(noteWindow.getPreviousNoteDuration());
				lyString.append(" ");
			}

			if (noteWindow.getPreviousNote().isStartOfTie()) {
				lyString.append("~ ");
			}
		}
		log("Current note not handled: " + noteWindow.getCurrentNoteLy());

		log("==> Save called, lyString after: " + lyString);
		noteWindow.empty();
	}
}

class NoteWindow {
	private Note currentNote = null;
	private Note previousNote = null;
	private Note secondPreviousNote = null;
	private StringBuffer currentNoteLy = new StringBuffer();
	private String currentNoteDuration = null;
	private String previousNoteLy = null;
	private String previousNoteDuration = null;
	private String secondPreviousNoteLy = null;
	private String secondPreviousNoteDuration = null;
	private int currentOctave = 4;
	private char lastNote = 'c';
	private int lastOctave = 4;
	boolean isDebug = false;
	
	void log(String message) {
		if (isDebug) {
			System.out.println(message);
		}
	}

	void empty() {
		secondPreviousNote = null;
		previousNote = null;
	}

	void emptyAll() {
		secondPreviousNote = null;
		previousNote = null;
		currentNote = null;
		currentNoteLy = new StringBuffer();
		currentNoteDuration = null;
	}

	void addNote(Note note) {
		log(note.toDebugString());
		secondPreviousNote = previousNote;
		secondPreviousNoteLy = previousNoteLy;
		previousNote = currentNote;
		previousNoteLy = currentNoteLy.toString();
		currentNoteLy = new StringBuffer();
		currentNote = note;
		if (!note.isRest()) {
			String firstLetter = note.originalString.substring(0, 1).toLowerCase();
			currentNoteLy.append(firstLetter);
			currentOctave = note.getOctave();
			if (note.originalString.length() > 1) {
				String secondLetter = note.originalString.substring(1, 2).toLowerCase();
				log("Second letter: " + secondLetter);
				if (secondLetter.equals("b")) {
					currentNoteLy.append("es");
				} else if (secondLetter.equals("#")) {
					currentNoteLy.append("is");
				}
			}
			int octaveChange = getOctaveChange(firstLetter.charAt(0));
			if (octaveChange > 0) {
				for (int i = 0; i < octaveChange; i++) {
					log("Add octave change");
					currentNoteLy.append("'");
				}
			}
			if (octaveChange < 0) {
				for (int i = 0; i > octaveChange; i--) {
					log("Add octave change");
					currentNoteLy.append(",");
				}
			}

			lastNote = firstLetter.charAt(0);
		} else {
			currentNoteLy.append("r");
		}
		secondPreviousNoteDuration = previousNoteDuration;
		previousNoteDuration = currentNoteDuration;
		currentNoteDuration = LilyPondNoteDurationHelper.getDuration2(Double.toString(note.getDuration()));
	}

	void addChordOctave(Note rootNote) {
		log("chord rootnote: " + rootNote.toDebugString());
		currentOctave = rootNote.getOctave();
		log("Chord octave: " + currentOctave);
	}

	/**
	 * This method determines octave changes that are relative to the previous
	 * note.
	 * <ul>
	 * <li>JFugue: [ c4 d4 e4 f4 g4 a4 b4 ] c5 d5 e5 f5 g5 a5 b5 | c6</li>
	 * <li>Lilypond: c d e f [g' a b c d e f ] g' a b c</li>
	 * </ul>
	 * 
	 * @param lyString
	 */
	int getOctaveChange(char currentNoteChar) {
		int octaveChange = currentOctave - lastOctave;
		log("Current octave: " + currentOctave + ", last octave: " + lastOctave);
		int lilypondChange = 0;
		if (previousNote != null && !previousNote.isRest()) {
			lilypondChange = lilypondRelativeDirection(previousNote.originalString.toLowerCase().charAt(0), currentNoteChar);
		} else {
			lilypondChange = lilypondRelativeDirection(lastNote, currentNoteChar);
		}
		log("LilyPond change: " + lilypondChange);

		int jfugueChange = 0;
		if (previousNote != null && !previousNote.isRest()) {
			jfugueChange = jfugueOctaveChange(previousNote.originalString.toLowerCase().charAt(0), currentNoteChar, lilypondChange);
		} else {
			jfugueChange = jfugueOctaveChange(lastNote, currentNoteChar, lilypondChange);
		}
		octaveChange += jfugueChange;
		lastOctave = currentOctave;
		return octaveChange;
	}

	private int lilypondRelativeDirection(char firstNote, char secondNote) {
		char curChar = firstNote;
		if (firstNote == secondNote) {
			return 0;
		}
		for (int i = 1; i < 4; i++) {
			curChar++;
			if (curChar > 'g') {
				curChar = 'a';
			}

			if (curChar == secondNote) {
				return i;
			}
		}
		curChar = firstNote;
		for (int i = 1; i < 4; i++) {
			curChar--;
			if (curChar < 'a') {
				curChar = 'g';
			}

			if (curChar == secondNote) {
				return -i;
			}
		}
		return 0;
	}

	private int jfugueOctaveChange(char firstNote, char secondNote, int lilypondDirection) {
		log("jfugue change: firstNote: " + firstNote + ", secondNote: " + secondNote);
		char curChar = firstNote;
		int steps = Math.abs(lilypondDirection) + 1;
		for (int i = 1; Math.abs(i) < steps; i += 1) {
			if (lilypondDirection > 0) {
				curChar++;
				if (curChar > 'g') {
					curChar = 'a';
				}
				if ((firstNote < 'c' || firstNote >= 'g') && curChar > 'b') {
					return -1;
				}
			} else {
				curChar--;
				if (curChar < 'a') {
					curChar = 'g';
				}
				if ((firstNote >= 'c' && firstNote < 'f') && curChar < 'c') {
					return 1;
				}
			}
		}
		return 0;
	}

	void print() {
		log("Note window: " + secondPreviousNoteLy + "&" + secondPreviousNoteDuration + "&" + previousNoteLy + "&" + previousNoteDuration + "&" + currentNoteLy + "&"
				+ currentNoteDuration);
	}

	Note getCurrentNote() {
		return currentNote;
	}

	Note getPreviousNote() {
		return previousNote;
	}

	Note getSecondPreviousNote() {
		return secondPreviousNote;
	}

	public StringBuffer getCurrentNoteLy() {
		return currentNoteLy;
	}

	public String getPreviousNoteLy() {
		return previousNoteLy;
	}

	public String getSecondPreviousNoteLy() {
		return secondPreviousNoteLy;
	}

	public String getCurrentNoteDuration() {
		return currentNoteDuration;
	}

	public String getPreviousNoteDuration() {
		return previousNoteDuration;
	}

	public String getSecondPreviousNoteDuration() {
		return secondPreviousNoteDuration;
	}

	public int getCurrentOctave() {
		return currentOctave;
	}

	public void setCurrentOctave(int currentOctave) {
		this.currentOctave = currentOctave;
	}

	public char getLastNote() {
		return lastNote;
	}

	public void setLastNote(char lastNote) {
		this.lastNote = lastNote;
	}

	public int getLastOctave() {
		return lastOctave;
	}

	public void setLastOctave(int lastOctave) {
		this.lastOctave = lastOctave;
	}
}

class LilyPondNoteDurationHelper {
	static String getDuration2(String duration) {
		String durationLy = "4";
		double durationVal = Double.parseDouble(duration);
		if (durationVal == 0.0625) {
			durationLy = "16";
		} else if (durationVal == 0.125) {
			durationLy = "8";
		} else if (durationVal == 0.25) {
			durationLy = "4";
		} else if (durationVal == 0.375) {
			durationLy = "4.";
		} else if (durationVal == 0.5) {
			durationLy = "2";
		} else if (durationVal == 0.75) {
			durationLy = "2.";
		} else if (durationVal == 1.0) {
			durationLy = "1";
		} else if (durationVal == 2.0) {
			durationLy = "\\breve";
		} else if (durationVal == 3.0) {
			durationLy = "\\breve.";
		} else if (durationVal == 4.0) {
			durationLy = "\\longa";
		} else {
			durationLy = "4";
		}
		return durationLy;
	}
}
