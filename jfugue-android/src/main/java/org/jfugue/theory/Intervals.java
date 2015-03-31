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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.jfugue.pattern.NotesProducer;
import org.jfugue.pattern.Pattern;
import org.jfugue.pattern.PatternProducer;
import org.jfugue.provider.NoteProviderFactory;
import org.staccato.NoteSubparser;

public class Intervals implements PatternProducer, NotesProducer
{
	private static Map<Integer, Integer> wholeNumberDegreeToHalfsteps;
	private static Map<Integer, Integer> halfstepsToWholeNumberDegree;

	static {
		wholeNumberDegreeToHalfsteps = new HashMap<Integer, Integer>();
		wholeNumberDegreeToHalfsteps.put(1, 0);
		wholeNumberDegreeToHalfsteps.put(2, 2);
		wholeNumberDegreeToHalfsteps.put(3, 4);
		wholeNumberDegreeToHalfsteps.put(4, 5);
		wholeNumberDegreeToHalfsteps.put(5, 7);
		wholeNumberDegreeToHalfsteps.put(6, 9);
		wholeNumberDegreeToHalfsteps.put(7, 11);
		wholeNumberDegreeToHalfsteps.put(8, 12);
		wholeNumberDegreeToHalfsteps.put(9, 14);
		wholeNumberDegreeToHalfsteps.put(10, 16);
		wholeNumberDegreeToHalfsteps.put(11, 17);
		wholeNumberDegreeToHalfsteps.put(12, 19);
		wholeNumberDegreeToHalfsteps.put(13, 21);
		wholeNumberDegreeToHalfsteps.put(14, 23);
		wholeNumberDegreeToHalfsteps.put(15, 24);

		halfstepsToWholeNumberDegree = new HashMap<Integer, Integer>();
		halfstepsToWholeNumberDegree.put(0, 1);
		halfstepsToWholeNumberDegree.put(2, 2);
		halfstepsToWholeNumberDegree.put(4, 3);
		halfstepsToWholeNumberDegree.put(5, 4);
		halfstepsToWholeNumberDegree.put(7, 5);
		halfstepsToWholeNumberDegree.put(9, 6);
		halfstepsToWholeNumberDegree.put(11, 7);
		halfstepsToWholeNumberDegree.put(12, 8);
		halfstepsToWholeNumberDegree.put(14, 9);
		halfstepsToWholeNumberDegree.put(16, 10);
		halfstepsToWholeNumberDegree.put(17, 11);
		halfstepsToWholeNumberDegree.put(19, 12);
		halfstepsToWholeNumberDegree.put(21, 13);
		halfstepsToWholeNumberDegree.put(23, 14);
		halfstepsToWholeNumberDegree.put(24, 15);
	};
	
	private String intervalPattern;
	private Note rootNote;
	private static java.util.regex.Pattern numberPattern = java.util.regex.Pattern.compile("\\d+");
	private String[] splitEachSequence;
	private String[] splitAllSequence;

	public Intervals(String intervalPattern) {
		this.intervalPattern = intervalPattern;
	}

	public Intervals setRoot(String root) {
		return setRoot(NoteProviderFactory.getNoteProvider().createNote(root));
	}
	
	public Intervals setRoot(Note root) {
		this.rootNote = root;
		return this;
	}

	@Override
	public org.jfugue.pattern.Pattern getPattern() {
		assert (rootNote != null);
		
		String[] intervals = intervalPattern.split(" ");
		int allSequenceCounter = 0;
		org.jfugue.pattern.Pattern pattern = new org.jfugue.pattern.Pattern();
		for (String interval : intervals) {
		    Note note = new Note((byte)(rootNote.getValue() + Intervals.getHalfsteps(interval)));
			if (splitEachSequence != null) {
			    for (String add : splitEachSequence) {
			        pattern.add(note.toString() + add);
			    }
			} 
			else if (splitAllSequence != null) {
			    pattern.add(note.toString() + splitAllSequence[allSequenceCounter++]);
			    if (allSequenceCounter == splitAllSequence.length) {
			        allSequenceCounter = 0;
			    }
			} 
			else {
			    pattern.add(note);
			}
		}
		return pattern;
	}
	
	@Override
	public List<Note> getNotes() {
	    List<Note> noteList = new ArrayList<Note>();
	    Pattern pattern = getPattern();
	    for (String split : pattern.toString().split(" ")) {
	        if (NoteSubparser.getInstance().matches(split)) {
	            noteList.add(new Note(split));
	        }
	    }
	    return noteList;
	}
	
	public String getNthInterval(int n) {
		return intervalPattern.split(" ")[n];
	}

	public int size() {
		return intervalPattern.split(" ").length;
	}

	public static int getHalfsteps(String interval) {
		return wholeNumberDegreeToHalfsteps.get(getNumberPortionOfInterval(interval)) + calculateHalfstepsFromFlatsAndSharps(interval);
	}
	
	public int[] toHalfstepArray() {
		String[] intervals = intervalPattern.split(" ");
		int[] halfSteps = new int[intervals.length];
		for (int i=0; i < intervals.length; i++) {
			halfSteps[i] = Intervals.getHalfsteps(intervals[i]); 
		}
		return halfSteps;
	}

	/**
	 * Counts the number of halfsteps reflected by the 
	 * flats or sharps in a given interval. So, for "b3", 
	 * this would return -1. For "##5", this would return 2. 
	 */
	private static int calculateHalfstepsFromFlatsAndSharps(String interval) {
		int numHalfsteps = 0;
		for (char ch : interval.toUpperCase().toCharArray()) {
			if (ch == 'B') {
				numHalfsteps -= 1;
			} else if (ch == '#') {
				numHalfsteps += 1;
			}
		}
		return numHalfsteps;
	}
	
	/** 
	 * Returns the number part of an interval token.
	 * Interval tokens are typically like "1" or "3" but
	 * could also be like "b3" or "#5". So, for example,
	 * given either "3" or "b3", this method would return 3.  
	 * If there is no number in the given String (e.g., "#"),
	 * which would probably be in error anyway, this method
	 * returns 0.
	 */
	private static int getNumberPortionOfInterval(String interval) {
		Matcher m = numberPattern.matcher(interval);
		if (m.find()) {
			return Integer.parseInt(m.group());
		} else {
			return 0;
		}
	}
	
	/**
	 * Rotates an interval string by the given value. For
	 * example, with an Interval like "1 3 5" and rotate(1),
	 * this would return "3 5 1" (not "5 1 3").
	 */
	public Intervals rotate(int n) {
		String[] intervals = intervalPattern.split(" ");
		n %= intervals.length;
		StringBuilder buddy = new StringBuilder();
		for (int i=0; i < intervals.length-n; i++) {
			buddy.append(intervals[n+i]);
			buddy.append(" ");
		}
		for (int i=0; i < n; i++) {
			buddy.append(intervals[i]);
			buddy.append(" ");
		}
		this.intervalPattern = buddy.toString().trim();
		return this;
	}
	
	public Intervals eachIntervalAs(String eachSequence) {
	    this.splitEachSequence = eachSequence.split(" ");
	    return this;
	}
	
	public Intervals allIntervalsAs(String allSequence) {
	    this.splitAllSequence = allSequence.split(" ");
	    return this;
	}

	public String toString() {
		return this.intervalPattern;
	}
	
	@Override
	public boolean equals(Object o) {
		if ((o == null) || (!(o instanceof Intervals))) return false;
		return (((Intervals)o).toString().equals(this.toString()));
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	public static Intervals createIntervalsFromNotes(Pattern pattern) {
		return createIntervalsFromNotes(pattern.toString());
	}

	public static Intervals createIntervalsFromNotes(String noteString) {
		String[] noteStrings = noteString.split(" ");
		Note[] notes = new Note[noteStrings.length];
		for (int i=0; i < noteStrings.length; i++) {
			notes[i] = NoteProviderFactory.getNoteProvider().createNote(noteStrings[i]);
		}
		return createIntervalsFromNotes(notes);
	}
	
	public static Intervals createIntervalsFromNotes(Note[] notes) {
		StringBuilder buddy = new StringBuilder();
		buddy.append("1 ");
		for (int i=1; i < notes.length; i++) {
			int diff = notes[i].getValue()-notes[0].getValue();
			if (!halfstepsToWholeNumberDegree.containsKey(diff)) {
				diff += 1;
				buddy.append("b");
			}
			int wholeNumberDegree = halfstepsToWholeNumberDegree.get(diff);
			buddy.append(wholeNumberDegree);
			buddy.append(" ");
		}
		return new Intervals(buddy.toString().trim());
	}
}
