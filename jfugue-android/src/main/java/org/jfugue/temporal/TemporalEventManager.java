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

package org.jfugue.temporal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jfugue.midi.MidiDefaults;

/**
 * Places musical data into the MIDI sequence.
 * Package scope, final class.
 * 
 *@author David Koelle
 */
final class TemporalEventManager
{
	private Map<Long, List<TemporalEvent>> timeToEventMap = new TreeMap<Long, List<TemporalEvent>>(); 
    private int tempoBeatsPerMinute = MidiDefaults.DEFAULT_TEMPO_BEATS_PER_MINUTE;
    private int beatsPerWhole = MidiDefaults.DEFAULT_TEMPO_BEATS_PER_WHOLE;
    private byte currentTrack = 0;
    private byte[] currentLayer = new byte[MidiDefaults.TRACKS];
    private double beatTime[][] = new double[MidiDefaults.TRACKS][MidiDefaults.LAYERS];
    private Map<String, Double> bookmarkedTrackTimeMap;

    public TemporalEventManager() { }
    
    public void reset() {
        this.bookmarkedTrackTimeMap = new HashMap<String, Double>();
        this.tempoBeatsPerMinute = MidiDefaults.DEFAULT_TEMPO_BEATS_PER_MINUTE;
        this.currentTrack = 0;
        for (int i=0; i < MidiDefaults.TRACKS; i++) {
            this.currentLayer[i] = 0;
        }
        this.timeToEventMap.clear();
    }
    
    public void finish() { }
    
    public void setTempo(int tempoBPM) {
    	this.tempoBeatsPerMinute = tempoBPM;
    }
    
    /**
     * Sets the current track to which new events will be added.
     * @param layer the track to select
     */
    public void setCurrentTrack(byte track) {
        currentTrack = track;
    }

    /**
     * Sets the current layer within the track to which new events will be added.
     * @param layer the layer to select
     */
    public void setCurrentLayer(byte layer) {
        currentLayer[currentTrack] = layer;
    }

    /**
     * Advances the timer for the current track by the specified duration,
     * which is specified in Pulses Per Quarter (PPQ)
     * @param duration the duration to increase the track timer
     */
    public void advanceTrackBeatTime(double advanceTime) {
        beatTime[currentTrack][currentLayer[currentTrack]] += advanceTime;
    }

    /**
     * Sets the timer for the current track by the given time,
     * which is specified in Pulses Per Quarter (PPQ)
     * @param newTickTime the time at which to set the track timer
     */
    public void setTrackBeatTime(double newTime) {
        beatTime[currentTrack][currentLayer[currentTrack]] = newTime;
    }

    /**
     * Returns the timer for the current track and current layer.
     * @return the timer value for the current track, specified in Pulses Per Quarter (PPQ)
     */
    public double getTrackBeatTime() {
        return beatTime[currentTrack][currentLayer[currentTrack]];
    }

    public void addTrackTickTimeBookmark(String timeBookmarkID) {
        bookmarkedTrackTimeMap.put(timeBookmarkID, getTrackBeatTime());
    }

    public double getTrackBeatTimeBookmark(String timeBookmarkID) {
    	return bookmarkedTrackTimeMap.get(timeBookmarkID);
    }

    public void addRealTimeEvent(DurationTemporalEvent event) {
    	addRealTimeEvent((TemporalEvent)event);
        advanceTrackBeatTime(event.getDuration());
    }

    public void addRealTimeEvent(TemporalEvent event) {
        List<TemporalEvent> eventList = timeToEventMap.get(convertBeatsToMillis(getTrackBeatTime()));
        if (eventList == null) {
            eventList = new ArrayList<TemporalEvent>();
            timeToEventMap.put(convertBeatsToMillis(getTrackBeatTime()), eventList);
        }
        eventList.add(event);
    }
    
    public Map<Long, List<TemporalEvent>> getTimeToEventMap() {
        return this.timeToEventMap;
    }

    private long convertBeatsToMillis(double beats) {
        return (long)((beats * beatsPerWhole * 60000.0D) / tempoBeatsPerMinute);
    }
}