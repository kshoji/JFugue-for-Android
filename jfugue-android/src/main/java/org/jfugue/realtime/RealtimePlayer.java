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

package org.jfugue.realtime;

import org.jfugue.midi.MidiDictionary;
import org.jfugue.midi.MidiTools;
import org.jfugue.pattern.PatternProducer;
import org.jfugue.player.SynthesizerManager;
import org.jfugue.theory.Chord;
import org.jfugue.theory.Note;
import org.staccato.StaccatoParser;

import jp.kshoji.javax.sound.midi.MidiChannel;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Synthesizer;

/**
 * This player sends messages directly to the MIDI Synthesizer, rather than creating a 
 * sequence with the MIDI Sequencer.
 * 
 * There are two ways that you can send messages to RealTimePlayer, and you can freely intermix these:
 * 1. Pass any Staccato string to the play() method. In this case, start notes should be indicated as the start of a tie
 *    (e.g., "C4s-") and stop notes should be indicated as the end of a tie (e.g., "C4-s")
 * 2. Call specific methods, like startNote or changeInstrument
 */
public class RealtimePlayer 
{
	private Synthesizer synth;
    private MidiChannel[] channels;
    private int currentChannel;
	private StaccatoParser staccatoParser;
	private RealtimeMidiParserListener rtMidiParserListener;
	
	public RealtimePlayer() throws MidiUnavailableException {
        this.synth = SynthesizerManager.getInstance().getSynthesizer();
        this.synth.open();
    	this.channels = this.synth.getChannels();

    	staccatoParser = new StaccatoParser();
		rtMidiParserListener = new RealtimeMidiParserListener(this);
		staccatoParser.addParserListener(rtMidiParserListener);
	}
	
	public void play(PatternProducer pattern) { 
		staccatoParser.parse(pattern);
	}

	public void play(String pattern) {
		staccatoParser.parse(pattern);
	}
	
    protected MidiChannel getCurrentChannel() {
    	return this.channels[this.currentChannel];
    }
	
    public long getCurrentTime() {
        return rtMidiParserListener.getCurrentTime();
    }
    
	public void schedule(long timeInMillis, ScheduledEvent event) {
	    rtMidiParserListener.onEventScheduled(timeInMillis, event);
	}
	
	public void unschedule(long timeInMillis, ScheduledEvent event) {
	    rtMidiParserListener.onEventUnscheduled(timeInMillis, event);
	}
    
	public void startNote(Note note) {
        getCurrentChannel().noteOn(note.getValue(), note.getOnVelocity());
	}

    public void stopNote(Note note) {
    	getCurrentChannel().noteOff(note.getValue(), note.getOffVelocity());    	
    }
    
    public void startChord(Chord chord) {
        for (Note note : chord.getNotes()) {
            startNote(note);
        }
    }

    public void stopChord(Chord chord) {
        for (Note note : chord.getNotes()) {
            stopNote(note);
        }
    }

    public void startInterpolator(RealtimeInterpolator interpolator, long durationInMillis) {
    	rtMidiParserListener.onInterpolatorStarted(interpolator, durationInMillis);
    }
    
    public void stopInterpolator(RealtimeInterpolator interpolator) {
    	rtMidiParserListener.onInterpolatorStopping(interpolator);
    }

    public void changeInstrument(int newInstrument) {
    	getCurrentChannel().programChange(newInstrument);
    }

    public void changeInstrument(String newInstrument) {
        getCurrentChannel().programChange(MidiDictionary.INSTRUMENT_STRING_TO_BYTE.get(newInstrument.toUpperCase()));
    }

    public void changeTrack(int newTrack) {
        this.currentChannel = newTrack;
    }
    
    public void setPitchBend(int pitch) {
        setPitchBend(MidiTools.getLSB(pitch), MidiTools.getMSB(pitch));
    }
   
    public void setPitchBend(byte lsb, byte msb) {
    	getCurrentChannel().setPitchBend(lsb + (msb << 7));
    }
    
    public void changeChannelPressure(byte pressure) {
    	getCurrentChannel().setChannelPressure(pressure);
    }
    
    public void changePolyphonicPressure(byte key, byte pressure) {
    	getCurrentChannel().setPolyPressure(key, pressure);
    }
    
    public void changeController(byte controller, byte value) {
    	getCurrentChannel().controlChange(controller, value);
    }
    
	public void close() {
        for (MidiChannel channel : channels) {
            channel.allNotesOff();
        }
	    
		rtMidiParserListener.finish();
	}
}


