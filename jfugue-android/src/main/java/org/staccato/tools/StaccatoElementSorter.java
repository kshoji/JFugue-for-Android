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

package org.staccato.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfugue.midi.TrackTimeManager;
import org.jfugue.parser.ParserListener;
import org.jfugue.pattern.PatternProducer;
import org.jfugue.theory.Chord;
import org.jfugue.theory.Note;
import org.staccato.StaccatoParser;
import org.staccato.StaccatoUtil;

public class StaccatoElementSorter extends TrackTimeManager implements ParserListener {
	private Map<Double, List<ElementWithTrack>> timeMap;
    private double initialNoteBeatTime;  
	
	public StaccatoElementSorter() {
		super();
		timeMap = new HashMap<Double, List<ElementWithTrack>>();
	}

	/** Returns the map of sorted elements */
	public Map<Double, List<ElementWithTrack>> getSortedElements() {
		return timeMap;
	}
	
	/** Static method to sort elements in a given Staccato pattern */
	public static Map<Double, List<ElementWithTrack>> sortElements(PatternProducer patternProducer) {
		return sortElements(patternProducer.getPattern().toString());
	}
	
	/** Static method to sort elements in a given Staccato string */
	public static Map<Double, List<ElementWithTrack>> sortElements(String string) {
		StaccatoParser parser = new StaccatoParser();
		StaccatoElementSorter sorter = new StaccatoElementSorter();
		parser.addParserListener(sorter);
		parser.parse(string);
		return sorter.getSortedElements();
	}

	@Override
	public void beforeParsingStarts() { }

	@Override
	public void afterParsingFinished() { }

	@Override
	public void onTrackChanged(byte track) {
		super.setCurrentTrack(track);
		// Purposefully, the track change element is not added to the map
	}

	@Override
	public void onLayerChanged(byte layer) {
		super.setCurrentLayer(layer);
		// Purposefully, the layer change element is not added to the map
	}

	@Override
	public void onInstrumentParsed(byte instrument) {
		addToTimeMap(StaccatoUtil.createInstrumentElement(instrument));
	}

	@Override
	public void onTempoChanged(int tempoBPM) {
		addToTimeMap(StaccatoUtil.createTempoElement(tempoBPM));
	}

	@Override
	public void onKeySignatureParsed(byte key, byte scale) {
		addToTimeMap(StaccatoUtil.createKeySignatureElement(key, scale));
	}

	@Override
	public void onTimeSignatureParsed(byte numerator, byte powerOfTwo) {
		addToTimeMap(StaccatoUtil.createTimeSignatureElement(numerator, powerOfTwo));
	}

	@Override
	public void onBarLineParsed(long id) {
		addToTimeMap(StaccatoUtil.createBarLineElement(id));
	}

	@Override
	public void onTrackBeatTimeBookmarked(String timeBookmarkId) {
		super.addTrackTickTimeBookmark(timeBookmarkId);
		// Purposefully, the bookmark element is not added to the map
	}

	@Override
	public void onTrackBeatTimeBookmarkRequested(String timeBookmarkId) {
    	double time = super.getTrackBeatTimeBookmark(timeBookmarkId);
    	super.setTrackBeatTime(time);
		// Purposefully, the bookmark element is not added to the map
	}

	@Override
	public void onTrackBeatTimeRequested(double time) {
        super.setTrackBeatTime(time);
		// Purposefully, the time element is not added to the map
	}

	@Override
	public void onPitchWheelParsed(byte lsb, byte msb) {
		addToTimeMap(StaccatoUtil.createPitchWheelElement(lsb, msb));
	}

	@Override
	public void onChannelPressureParsed(byte pressure) {
		addToTimeMap(StaccatoUtil.createChannelPressureElement(pressure));
	}

	@Override
	public void onPolyphonicPressureParsed(byte key, byte pressure) {
		addToTimeMap(StaccatoUtil.createPolyphonicPressureElement(key, pressure));
	}

	@Override
	public void onSystemExclusiveParsed(byte... bytes) {
		addToTimeMap(StaccatoUtil.createSystemExclusiveElement(bytes));
	}

	@Override
	public void onControllerEventParsed(byte controller, byte value) {
		addToTimeMap(StaccatoUtil.createControllerEventElement(controller, value));
	}

	@Override
	public void onLyricParsed(String lyric) {
		addToTimeMap(StaccatoUtil.createLyricElement(lyric));
	}

	@Override
	public void onMarkerParsed(String marker) {
		addToTimeMap(StaccatoUtil.createMarkerElement(marker));
	}

	@Override
	public void onFunctionParsed(String id, Object message) {
		addToTimeMap(StaccatoUtil.createFunctionElement(id, message));
	}

	@Override
	public void onNoteParsed(Note note) {
		addNote(note);
	}
	
	@Override
	public void onChordParsed(Chord chord) {
    	for (Note note : chord.getNotes()) {
    		this.addNote(note);
    	}
	}

	private void addNote(Note note) {
		if (note.getDuration() == 0.0) {
			note.useDefaultDuration();
		}

		// If this is the first note in a sequence of harmonic or melodic notes, remember what time it is.
		if (note.isFirstNote()) {
			this.initialNoteBeatTime = getTrackBeatTime(); 
		}
		
		// If we're going to the next sequence in a parallel note situation, roll back the time to the beginning of the first note.
		// A note will never be a parallel note if a first note has not happened first.
		if (note.isHarmonicNote()) {
			setTrackBeatTime(this.initialNoteBeatTime);
		} 

		// If the note is a rest, simply advance the track time and get outta here
		if (note.isRest()) {
			advanceTrackBeatTime(note.getDuration());  
			return;
		}

		addToTimeMap(StaccatoUtil.createNoteElement(note));
		advanceTrackBeatTime(note.getDuration());
	}

	private void addToTimeMap(String string) {
		ElementWithTrack elementWithTrack = new ElementWithTrack(getCurrentTrack(), getCurrentLayer(), string);
		List<ElementWithTrack> elementList = timeMap.get(getTrackBeatTime());
		if (elementList == null) {
			elementList = new ArrayList<ElementWithTrack>();
			timeMap.put(getTrackBeatTime(), elementList);
		}
		elementList.add(elementWithTrack);
	}
}

