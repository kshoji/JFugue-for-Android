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

import org.jfugue.parser.ParserListener;
import org.jfugue.theory.Chord;
import org.jfugue.theory.Note;

import java.util.logging.Logger;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.Sequence;
import jp.kshoji.javax.sound.midi.ShortMessage;

public class MidiParserListener implements ParserListener
{
    private MidiEventManager eventManager;
    private Logger logger = Logger.getLogger("org.jfugue");

    public MidiParserListener() {
        this.eventManager = new MidiEventManager();
    }
    
    public Sequence getSequence() {
    	return eventManager.getSequence();
    }
    
    /* ParserListener Events */
    
    @Override
    public void beforeParsingStarts() {
    	try {
    		this.eventManager.reset();
    	} catch (InvalidMidiDataException e) {
            logger.warning(e.getMessage());
        }
    }

    @Override
    public void afterParsingFinished() { 
    	this.eventManager.finishSequence();
    }
    
    @Override
    public void onTrackChanged(byte track) {
        this.eventManager.setCurrentTrack(track);
    }

    @Override
    public void onLayerChanged(byte layer) {
        this.eventManager.setCurrentLayer(layer);
    }

    @Override
    public void onInstrumentParsed(byte instrument) {
        this.eventManager.addEvent(ShortMessage.PROGRAM_CHANGE, instrument, 0);
    }

    @Override
    public void onTempoChanged(int tempoBPM) {
    	this.eventManager.setTempo(tempoBPM);
    }

    @Override
    public void onKeySignatureParsed(byte key, byte scale) {
        this.eventManager.addMetaMessage(0x59, new byte[] { key, scale }); 
    }

    @Override
    public void onTimeSignatureParsed(byte numerator, byte powerOfTwo) {
    	this.eventManager.setTimeSignature(numerator, powerOfTwo);
    }

    @Override
    public void onBarLineParsed(long time) { }

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
        this.eventManager.addEvent(ShortMessage.PITCH_BEND, lsb, msb);
    }

    @Override
    public void onChannelPressureParsed(byte pressure) {
        this.eventManager.addEvent(ShortMessage.CHANNEL_PRESSURE, pressure);
    }

    @Override
    public void onPolyphonicPressureParsed(byte key, byte pressure) {
        this.eventManager.addEvent(ShortMessage.POLY_PRESSURE, key, pressure);
    }

    @Override
    public void onSystemExclusiveParsed(byte... bytes) {
         this.eventManager.addSystemExclusiveEvent(bytes);
    }

    @Override
    public void onControllerEventParsed(byte controller, byte value) {
        this.eventManager.addEvent(ShortMessage.CONTROL_CHANGE, controller, value);
    }

    @Override
    public void onLyricParsed(String lyric) { 
        this.eventManager.addMetaMessage(0x05, lyric.getBytes());
    }

    @Override
    public void onMarkerParsed(String marker) { 
        this.eventManager.addMetaMessage(0x06, marker.getBytes());
    }

    @Override
    public void onFunctionParsed(String id, Object message) { }

    @Override
    public void onNoteParsed(Note note) {
        this.eventManager.addNote(note);
    }
    
    @Override 
    public void onChordParsed(Chord chord) {
    	for (Note note : chord.getNotes()) {
    		this.eventManager.addNote(note);
    	}
    }
}
