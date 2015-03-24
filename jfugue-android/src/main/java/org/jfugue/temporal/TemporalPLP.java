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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfugue.parser.Parser;
import org.jfugue.parser.ParserListener;
import org.jfugue.theory.Chord;
import org.jfugue.theory.Note;

public class TemporalPLP extends Parser implements ParserListener
{
    private TemporalEventManager eventManager;
    private TemporalEvents events;
    
    public TemporalPLP() {
    	super();
    	this.events = new TemporalEvents();
        this.eventManager = new TemporalEventManager();
    } 
    
    public Map<Long, List<TemporalEvent>> getTimeToEventMap() {
    	return eventManager.getTimeToEventMap();
    }
    
    /* ParserListener Events */
    
    @Override
    public void beforeParsingStarts() {
    	this.eventManager.reset();
    }

    @Override
    public void afterParsingFinished() { 
    	this.eventManager.finish();
    }
    
    @Override
    public void onTrackChanged(byte track) {
        this.eventManager.setCurrentTrack(track);
        this.eventManager.addRealTimeEvent(events.new TrackEvent(track)); 
    }

    @Override
    public void onLayerChanged(byte layer) {
        this.eventManager.setCurrentLayer(layer);
        this.eventManager.addRealTimeEvent(events.new LayerEvent(layer)); 
    }

    @Override
    public void onInstrumentParsed(byte instrument) {
        this.eventManager.addRealTimeEvent(events.new InstrumentEvent(instrument)); 
    }

    @Override
    public void onTempoChanged(int tempoBPM) {
    	this.eventManager.setTempo(tempoBPM);
    	this.eventManager.addRealTimeEvent(events.new TempoEvent(tempoBPM));
    }

    @Override
    public void onKeySignatureParsed(byte key, byte scale) {
       this.eventManager.addRealTimeEvent(events.new KeySignatureEvent(key, scale));
    }

    @Override
    public void onTimeSignatureParsed(byte numerator, byte powerOfTwo) {
        this.eventManager.addRealTimeEvent(events.new TimeSignatureEvent(numerator, powerOfTwo));
    }

    @Override
    public void onBarLineParsed(long time) { 
        this.eventManager.addRealTimeEvent(events.new BarEvent(time));
    }

    @Override
    public void onTrackBeatTimeBookmarked(String timeBookmarkID) {
    	this.eventManager.addTrackTickTimeBookmark(timeBookmarkID);
    }

    @Override
    public void onTrackBeatTimeBookmarkRequested(String timeBookmarkID) {
    	double time = this.eventManager.getTrackBeatTimeBookmark(timeBookmarkID);
    	this.eventManager.setTrackBeatTime(time);
    }

    @Override
    public void onTrackBeatTimeRequested(double time) {
        this.eventManager.setTrackBeatTime(time);
    }

    @Override
    public void onPitchWheelParsed(byte lsb, byte msb) {
        this.eventManager.addRealTimeEvent(events.new PitchWheelEvent(lsb, msb));
    }

    @Override
    public void onChannelPressureParsed(byte pressure) {
        this.eventManager.addRealTimeEvent(events.new ChannelPressureEvent(pressure));
    }

    @Override
    public void onPolyphonicPressureParsed(byte key, byte pressure) {
        this.eventManager.addRealTimeEvent(events.new PolyphonicPressureEvent(key, pressure));
    }

    @Override
    public void onSystemExclusiveParsed(byte... bytes) {
        this.eventManager.addRealTimeEvent(events.new SystemExclusiveEvent(bytes));
    }

    @Override
    public void onControllerEventParsed(byte controller, byte value) {
        this.eventManager.addRealTimeEvent(events.new ControllerEvent(controller, value));
    }

    @Override
    public void onLyricParsed(String lyric) { 
        this.eventManager.addRealTimeEvent(events.new LyricEvent(lyric));
    }

    @Override
    public void onMarkerParsed(String marker) { 
        this.eventManager.addRealTimeEvent(events.new MarkerEvent(marker));
    }

    @Override
    public void onFunctionParsed(String id, Object message) { 
        this.eventManager.addRealTimeEvent(events.new UserEvent(id, message));
    }

    @Override
    public void onNoteParsed(Note note) {
        this.eventManager.addRealTimeEvent(events.new NoteEvent(note));
    }
    
    @Override 
    public void onChordParsed(Chord chord) {
        this.eventManager.addRealTimeEvent(events.new ChordEvent(chord));
    }

    private void delay(long millis) {
    	try {
    		Thread.sleep(millis);
    	} catch (Exception e) {
    		// An exception? Ain't no one got time for that!
    	}
    }
    
    public void parse() {
		fireBeforeParsingStarts();
        
        long oldTime = 0;
        Set<Long> times = this.eventManager.getTimeToEventMap().keySet();
        for (long time : times) {
        	delay(time - oldTime);
        	oldTime = time;
        	
        	for (TemporalEvent event : this.eventManager.getTimeToEventMap().get(time)) {
        		event.execute(this);
        	}
        }

		fireAfterParsingFinished();
    }
}
