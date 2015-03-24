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

package org.staccato;

import org.jfugue.midi.MidiDefaults;
import org.jfugue.parser.ParserListener;
import org.jfugue.pattern.Pattern;
import org.jfugue.pattern.PatternProducer;
import org.jfugue.theory.Chord;
import org.jfugue.theory.Note;

public class StaccatoParserListener implements ParserListener, PatternProducer {
    private Pattern pattern;
    private byte track;
    
    public StaccatoParserListener() {
        pattern = new Pattern();
    }
    
    public Pattern getPattern() {
        return this.pattern;
    }
    
    @Override
    public void beforeParsingStarts() { 
        pattern = new Pattern();
    }

    @Override
    public void afterParsingFinished() { }

    @Override
    public void onTrackChanged(byte track) {
        pattern.add(StaccatoUtil.createTrackElement(track));
        this.track = track;
    }

    @Override
    public void onLayerChanged(byte layer) {
        pattern.add(StaccatoUtil.createLayerElement(layer));
    }

    @Override
    public void onInstrumentParsed(byte instrument) {
        pattern.add(StaccatoUtil.createInstrumentElement(instrument));
    }

    @Override
    public void onTempoChanged(int tempoBPM) {
        pattern.add(StaccatoUtil.createTempoElement(tempoBPM));
    }

    @Override
    public void onKeySignatureParsed(byte key, byte scale) {
    	pattern.add(StaccatoUtil.createKeySignatureElement(key, scale));
    }

    @Override
    public void onTimeSignatureParsed(byte numerator, byte powerOfTwo) {
        pattern.add(StaccatoUtil.createTimeSignatureElement(numerator, powerOfTwo));
    }

    @Override
    public void onBarLineParsed(long time) {
        pattern.add(StaccatoUtil.createBarLineElement(time));
    }

    @Override
    public void onTrackBeatTimeBookmarked(String timeBookmarkId) {
        pattern.add(StaccatoUtil.createTrackBeatTimeBookmarkElement(timeBookmarkId));
    }

    @Override
    public void onTrackBeatTimeBookmarkRequested(String timeBookmarkId) {
        pattern.add(StaccatoUtil.createTrackBeatTimeBookmarkRequestElement(timeBookmarkId));
    }

    @Override
    public void onTrackBeatTimeRequested(double time) {
        pattern.add(StaccatoUtil.createTrackBeatTimeRequestElement(time));
    }

    @Override
    public void onPitchWheelParsed(byte lsb, byte msb) {
        pattern.add(StaccatoUtil.createPitchWheelElement(lsb, msb));
    }

    @Override
    public void onChannelPressureParsed(byte pressure) {
        pattern.add(StaccatoUtil.createChannelPressureElement(pressure));
    }

    @Override
    public void onPolyphonicPressureParsed(byte key, byte pressure) {
        pattern.add(StaccatoUtil.createPolyphonicPressureElement(key, pressure));
    }

    @Override
    public void onSystemExclusiveParsed(byte... bytes) {
        pattern.add(StaccatoUtil.createSystemExclusiveElement(bytes));
    }

    @Override
    public void onControllerEventParsed(byte controller, byte value) {
        pattern.add(StaccatoUtil.createControllerEventElement(controller, value));
    }

    @Override
    public void onLyricParsed(String lyric) {
    	pattern.add(StaccatoUtil.createLyricElement(lyric));
    }

    @Override
    public void onMarkerParsed(String marker) {
    	pattern.add(StaccatoUtil.createMarkerElement(marker));
    }

    @Override
    public void onFunctionParsed(String id, Object message) {
    	pattern.add(StaccatoUtil.createFunctionElement(id, message));
    }

    @Override
    public void onNoteParsed(Note note) {
        pattern.add(StaccatoUtil.createNoteElement(note, track));
    }
    
    @Override
    public void onChordParsed(Chord chord) {
    	pattern.add(StaccatoUtil.createChordElement(chord));
    }
}
