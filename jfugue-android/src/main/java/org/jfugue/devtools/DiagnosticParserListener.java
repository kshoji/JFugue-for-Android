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

package org.jfugue.devtools;

import java.util.Arrays;
import java.util.logging.Logger;

import org.jfugue.parser.ParserListener;
import org.jfugue.theory.Chord;
import org.jfugue.theory.Note;

/**
 * This ParserListener simply logs, or prints to System.out, anything it hears from a parser.
 * If you build a new Parser, you can use DiagnosticParserListener to make sure it works!
 * 
 * @author David Koelle
 *
 */
public class DiagnosticParserListener implements ParserListener
{
    private Logger logger = Logger.getLogger("org.jfugue");

    public DiagnosticParserListener() { }
    
    private void print(String message) {
        System.out.println(message);
        logger.info(message);
    }

    @Override
    public void beforeParsingStarts() {
        print("Before parsing starts");
    }

    @Override
    public void afterParsingFinished() {
        print("After parsing finished");
    }

    @Override
    public void onTrackChanged(byte track) {
        print("Track changed to "+track);
    }

    @Override
    public void onLayerChanged(byte layer) {
        print("Layer changed to "+layer);
    }

    @Override
    public void onInstrumentParsed(byte instrument) {
        print("Instrument parsed: "+instrument);
    }

    @Override
    public void onTempoChanged(int tempoBPM) {
        print("Tempo changed to "+tempoBPM+" BPM");
    }

    @Override
    public void onKeySignatureParsed(byte key, byte scale) {
        print("Key signature parsed: key = "+key+"  scale = "+scale);
    }

    @Override
    public void onTimeSignatureParsed(byte numerator, byte powerOfTwo) {
        print("Time signature parsed: "+numerator+"/"+(int)(Math.pow(2, powerOfTwo)));
    }

    @Override
    public void onBarLineParsed(long time) {
        print("Bar line parsed at time = "+time);
    }

    @Override
    public void onTrackBeatTimeBookmarked(String timeBookmarkId) {
        print("Track time bookmarked into '"+timeBookmarkId+"'");
    }

    @Override
    public void onTrackBeatTimeBookmarkRequested(String timeBookmarkId) {
        print("Track time bookmark looked up: '"+timeBookmarkId+"'");
    }

    @Override
    public void onTrackBeatTimeRequested(double time) {
        print("Track time requested: "+time);
    }

    @Override
    public void onPitchWheelParsed(byte lsb, byte msb) {
        print("Pitch wheel parsed, lsb = "+lsb+"  msb = "+msb);
    }

    @Override
    public void onChannelPressureParsed(byte pressure) {
        print("Channel pressure parsed: "+pressure);
    }

    @Override
    public void onPolyphonicPressureParsed(byte key, byte pressure) {
    	print("Polyphonic pressure parsed, key = "+key+"  pressure = "+pressure);
    }

    @Override
    public void onSystemExclusiveParsed(byte... bytes) {
    	print("Sysex parsed, bytes = "+Arrays.toString(bytes));
    }

    @Override
    public void onControllerEventParsed(byte controller, byte value) {
    	print("Controller event parsed, controller = "+controller+"  value = "+value);
    }

    @Override
    public void onLyricParsed(String lyric) {
    	print("Lyric parsed: "+lyric);
    }

    @Override
    public void onMarkerParsed(String marker) {
    	print("Marker parsed: "+marker);
    }

    @Override
    public void onFunctionParsed(String id, Object message) {
    	print("User event parsed, id = "+id+"  message = "+message);
    }

    @Override
    public void onNoteParsed(Note note) {
        print("Note parsed: value = "+note.getValue()+"  duration = "+note.getDuration()+"  onVelocity = "+note.getOnVelocity()+"  offVelocity = "+note.getOffVelocity());
    }

    @Override
    public void onChordParsed(Chord chord) {
        print("Chord parsed: rootnote = "+chord.getRoot().getValue()+"  intervals = "+chord.getIntervals().toString()+"  duration = "+chord.getRoot().getDuration()+"  onVelocity = "+chord.getRoot().getOnVelocity()+"  offVelocity = "+chord.getRoot().getOffVelocity());
    }
}
