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

package org.jfugue.theory;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.jfugue.pattern.Pattern;
import org.jfugue.pattern.PatternProducer;
import org.jfugue.provider.ChordProviderFactory;

public class Chord implements PatternProducer
{
	public static Map<String, Intervals> chordMap;
	static {
        // @formatter:off
	    chordMap = new TreeMap<String, Intervals>(new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				int result = compareLength(s1, s2);
				if (result == 0) { result = s1.compareTo(s2); }
				return result;
			}
			
			/** Compare two strings and the bigger of the two is deemed to come first in order */
			private int compareLength(String s1, String s2) {
				if (s1.length() < s2.length()) {
					return 1;
				} else if (s1.length() > s2.length()) {
					return -1;
				} else {
					return 0;
				}
			}
	    });
		
		// Major Chords
		chordMap.put("MAJ",    new Intervals("1 3 5"));
		chordMap.put("MAJ6",   new Intervals("1 3 5 6"));
		chordMap.put("MAJ7",   new Intervals("1 3 5 7"));
		chordMap.put("MAJ9",   new Intervals("1 3 5 7 9"));
		chordMap.put("ADD9",   new Intervals("1 3 5 9"));
		chordMap.put("MAJ6%9", new Intervals("1 3 5 6 9"));
		chordMap.put("MAJ7%6", new Intervals("1 3 5 6 7"));
		chordMap.put("MAJ13",  new Intervals("1 3 5 7 9 13"));

		// Minor Chords
		chordMap.put("MIN",     new Intervals("1 b3 5"));
		chordMap.put("MIN6",    new Intervals("1 b3 5 6"));
		chordMap.put("MIN7",    new Intervals("1 b3 5 b7"));
		chordMap.put("MIN9",    new Intervals("1 b3 5 b7 9"));
		chordMap.put("MIN11",   new Intervals("1 b3 5 b7 9 11"));
		chordMap.put("MIN7%11", new Intervals("1 b3 5 b7 11"));
		chordMap.put("MINADD9", new Intervals("1 b3 5 9"));
		chordMap.put("MIN6%9",  new Intervals("1 b3 5 6"));
		chordMap.put("MINMAJ7", new Intervals("1 b3 5 7"));
		chordMap.put("MINMAJ9", new Intervals("1 b3 5 7 9"));

		// Dominant Chords
		chordMap.put("DOM7",      new Intervals("1 3 5 b7"));
		chordMap.put("DOM7%6",    new Intervals("1 3 5 6 b7"));
		chordMap.put("DOM7%11",   new Intervals("1 3 5 b7 11"));
		chordMap.put("DOM7SUS",   new Intervals("1 4 5 b7"));
		chordMap.put("DOM7%6SUS", new Intervals("1 4 5 6 b7"));
		chordMap.put("DOM9",      new Intervals("1 3 5 b7 9")); 
		chordMap.put("DOM11",     new Intervals("1 3 5 b7 9 11"));
		chordMap.put("DOM13",     new Intervals("1 3 5 b7 9 13"));
		chordMap.put("DOM13SUS",  new Intervals("1 3 5 b7 11 13"));
		chordMap.put("DOM7%6%11", new Intervals("1 3 5 b7 9 11 13"));

		// Augmented Chords
		chordMap.put("AUG",  new Intervals("1 3 #5"));
		chordMap.put("AUG7", new Intervals("1 3 #5 b7"));
		
		// Diminished Chords
		chordMap.put("DIM",  new Intervals("1 b3 b5"));
		chordMap.put("DIM7", new Intervals("1 b3 b5 6"));

		// Suspended Chords
		chordMap.put("SUS4", new Intervals("1 4 5"));
		chordMap.put("SUS2", new Intervals("1 2 5"));
		
		// @formatter:on
	}
	
	public static String[] getChordNames() {
		return chordMap.keySet().toArray(new String[0]);
	}
	
	public static void addChord(String name, String intervalPattern) {
		Chord.addChord(name, new Intervals(intervalPattern));
	}
	
	public static void addChord(String name, Intervals intervalPattern) {
		chordMap.put(name, intervalPattern);
	}
	
	public static Intervals getIntervals(String name) {
		return chordMap.get(name);
	}
	
	public static void removeChord(String name) {
		chordMap.remove(name);
	}
	
	private Note rootNote;
	private Intervals intervals;
	private int inversion;
	
	public Chord(String s) {
		this(ChordProviderFactory.getChordProvider().createChord(s));
	}

	public Chord(Chord chord) {
		this.rootNote = chord.getRoot();
		this.intervals = chord.getIntervals();
		this.inversion = chord.getInversion();
	}
	
	public Chord(Note root, Intervals intervals) {
		this.rootNote = root;
		this.intervals = intervals;
	}
	
	public Chord(Key key) {
		this.rootNote = key.getRoot();
		this.intervals = key.getScale().getIntervals();
	}
	
	public Note getRoot() {
		return this.rootNote;
	}
	
	public Intervals getIntervals() {
		return this.intervals;
	}
	
	public int getInversion() {
		return this.inversion;
	}
	
	public Chord setInversion(int nth) {
		this.inversion = nth;
		return this;
	}
	
	public Chord setBassNote(String newBass) {
		return setBassNote(new Note(newBass));
	}
	
	public Chord setBassNote(Note newBass) {
		if (rootNote == null) {
			return this; 
		}
		
		for (int i=0; i < intervals.size(); i++) {
			if (newBass.getValue() % 12 == (rootNote.getValue() + Intervals.getHalfsteps(intervals.getNthInterval(i))) % 12) {
				this.inversion = i;
			}
		}
		
		return this;
	}
	
	public Note[] getNotes() {
		int[] halfsteps = this.intervals.toHalfstepArray();
		Note[] retVal = new Note[halfsteps.length];
		retVal[0] = new Note(this.getRoot());
		for (int i=0; i < halfsteps.length-1; i++) {
			retVal[i+1] = new Note(retVal[i].getValue() + halfsteps[i+1] - halfsteps[i]).setFirstNote(false).setMelodicNote(false).setHarmonicNote(true).useSameDurationAs(getRoot());
		}
		
		// For notes Now calculate inversion
		for (int i=0; i < this.inversion; i++) {
			if (i < retVal.length) {
				retVal[i].setValue((byte)(retVal[i].getValue() + OCTAVE));
			}
		}
		
		return retVal;
	}

	public String insertChordNameIntoNote(Note note, String chordName) {
		StringBuilder buddy = new StringBuilder();
		buddy.append(Note.getToneString(note.getValue()));
		buddy.append(chordName);
		if (note.isDurationExplicitlySet()) {
			buddy.append(Note.getDurationString(note.getDuration()));
		}
		buddy.append(note.getVelocityString());
		return buddy.toString();
	}
	
	@Override
	public Pattern getPattern() {
		Pattern pattern = new Pattern();
		boolean foundChord = false;
		for (Map.Entry<String, Intervals> entry : chordMap.entrySet()) {
			if (this.getIntervals().equals(entry.getValue())) {
				pattern.add(insertChordNameIntoNote(this.rootNote, entry.getKey()));
				foundChord = true;
			}
		}
		if (!foundChord) {
			return getPatternWithNotes();
		} 
		return pattern;
	}
	
	public Pattern getPatternWithNotes() {
		// A better way of creating a Chord: Check to see if the intervals are in the map; if so, use the associated name. 
		// (Then you'd need to check for inversions, too)
		StringBuilder buddy = new StringBuilder();
		Note[] notes = getNotes();
		for (int i=0; i < notes.length-1; i++) {
			buddy.append(notes[i].getPattern());
			buddy.append("+");
		}
		buddy.append(notes[notes.length-1]);
		return new Pattern(buddy.toString());
	}

	public boolean isMajor() {
		return this.intervals.equals(MAJOR_INTERVALS);
	}
	
	public boolean isMinor() {
		return this.intervals.equals(MINOR_INTERVALS);
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Chord)) {
			return false;
		}
		
		Chord c2 = (Chord)o;
		return (c2.rootNote.equals(rootNote) && 
				c2.intervals.equals(intervals) &&
				(c2.inversion == inversion));		
	}

	@Override
	public String toString() {
		return getPattern().toString();
	}

	public String toDebugString() {
		StringBuilder buddy = new StringBuilder();
		int counter = 0;
		for (Note note : getNotes()) {
			buddy.append("Note ").append(counter++).append(": ").append(note.toDebugString()).append("\n");
		}
		buddy.append("Chord Intervals = "+getIntervals().toString());
		return buddy.toString();
	}
	
	public static final Intervals MAJOR_INTERVALS = new Intervals("1 3 5");
	public static final Intervals MINOR_INTERVALS = new Intervals("1 b3 5");
	public static final Intervals DIMINISHED_INTERVALS = new Intervals("1 b3 b5");
    public static final Intervals MAJOR_SEVENTH_INTERVALS = new Intervals("1 3 5 7"); 
    public static final Intervals MINOR_SEVENTH_INTERVALS = new Intervals("1 b3 5 b7");
    public static final Intervals DIMINISHED_SEVENTH_INTERVALS = new Intervals("1 b3 b5 6");
    public static final Intervals MAJOR_SEVENTH_SIXTH_INTERVALS = new Intervals("1 3 5 6 7"); 
    public static final Intervals MINOR_SEVENTH_SIXTH_INTERVALS = new Intervals("1 3 5 6 7"); 
	public static final byte OCTAVE = 12;
}
