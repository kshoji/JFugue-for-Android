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

import org.jfugue.parser.Parser;
import org.jfugue.theory.Note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.kshoji.javax.sound.midi.MetaMessage;
import jp.kshoji.javax.sound.midi.MidiEvent;
import jp.kshoji.javax.sound.midi.MidiMessage;
import jp.kshoji.javax.sound.midi.Sequence;
import jp.kshoji.javax.sound.midi.ShortMessage;
import jp.kshoji.javax.sound.midi.SysexMessage;
import jp.kshoji.javax.sound.midi.Track;

public class MidiParser extends Parser
{
    private List<Map<Byte, TempNote>> noteCache; 
    private float divisionType = MidiDefaults.DEFAULT_DIVISION_TYPE;
    private int resolutionTicksPerBeat = MidiDefaults.DEFAULT_RESOLUTION_TICKS_PER_BEAT;
    private int tempoBPM = MidiDefaults.DEFAULT_TEMPO_BEATS_PER_MINUTE;
    private int currentChannel = -1;
    private double[] currentTimeInBeats;
    private double[] expectedTimeInBeats;
    private List<AuxilliaryMidiParser> auxilliaryParsers;
    
    public MidiParser() {
        super();
        auxilliaryParsers = new ArrayList<AuxilliaryMidiParser>();
    }
    
    public void parse(Sequence sequence) {
        this.startParser();
        
        this.divisionType = sequence.getDivisionType();
        this.resolutionTicksPerBeat = sequence.getResolution();
        
        // Read events from each track
        for (Track track : sequence.getTracks()) {
            for (int i=0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                parseEvent(event);
            }
        }
        
        this.stopParser();
    }
    
    public void startParser() {
        fireBeforeParsingStarts();

        initNoteCache();
        
        this.divisionType = MidiDefaults.DEFAULT_DIVISION_TYPE;
        this.resolutionTicksPerBeat = MidiDefaults.DEFAULT_RESOLUTION_TICKS_PER_BEAT;
    }
    
    public void stopParser() {
        fireAfterParsingFinished();
    }

    private void initNoteCache() {
        noteCache = new ArrayList<Map<Byte, TempNote>>();
        this.currentTimeInBeats = new double[MidiDefaults.TRACKS];
        this.expectedTimeInBeats = new double[MidiDefaults.TRACKS];
        
        for (int i=0; i < MidiDefaults.TRACKS; i++) {
            noteCache.add(new HashMap<Byte, TempNote>());
            this.currentTimeInBeats[i] = 0.0d;
            this.expectedTimeInBeats[i] = 0.0d;
        }
    }
    
    /**
     * Parses the following messages:
     * - Note On events
     * - Note Off events
     * - Polyphonic Aftertouch
     * - Controller Events
     * - Program Change (instrument changes)
     * - Channel Aftertouch
     * - Pitch Wheel
     * - Meta Events: Tempo, Lyric, Marker, Key Signature, Time Signature
     * - SysEx Events
     * 
     * Any other MIDI messages (particularly, other Meta Events) are not handled by this MidiParser.
     * 
     * You may implement an AuxilliaryMidiParser to know when MidiParser has
     * parsed or not parsed a given MIDI message.
     *  
     * @see AuxilliaryMidiParser
     * 
     * @param event the event to parse
     */
    public void parseEvent(MidiEvent event) {
        MidiMessage message = event.getMessage();
        if (message instanceof ShortMessage) {
        	parseShortMessage((ShortMessage)message, event);
        }
        else if (message instanceof MetaMessage) {
        	parseMetaMessage((MetaMessage)message, event);
        } 
        else if (message instanceof SysexMessage) {
        	parseSysexMessage((SysexMessage)message, event);
        }
        else {
        	fireUnhandledMidiEvent(event); 
        }
    }
    
    private void parseShortMessage(ShortMessage message, MidiEvent event) {
        // For any message that isn't a NoteOn event, update the current time and channel.
        // (We don't do this for NoteOn events because NoteOn aren't written until the NoteOff event)
        if (!isNoteOnEvent(message.getCommand(), message.getChannel(), event)) { 
            checkChannel(message.getChannel());
        }
        
        switch (message.getCommand()) {
            case ShortMessage.NOTE_OFF: noteOff(message.getChannel(), event); fireHandledMidiEvent(event); break;
            case ShortMessage.NOTE_ON: noteOn(message.getChannel(), event); fireHandledMidiEvent(event); break;
            case ShortMessage.POLY_PRESSURE: polyphonicAftertouch(message.getChannel(), event); fireHandledMidiEvent(event); break;
            case ShortMessage.CONTROL_CHANGE: controlChange(message.getChannel(), event); fireHandledMidiEvent(event); break;
            case ShortMessage.PROGRAM_CHANGE: programChange(message.getChannel(), event); fireHandledMidiEvent(event); break;
            case ShortMessage.CHANNEL_PRESSURE: channelAftertouch(message.getChannel(), event); fireHandledMidiEvent(event); break;
            case ShortMessage.PITCH_BEND: pitchWheel(message.getChannel(), event); fireHandledMidiEvent(event); break;
            default : fireUnhandledMidiEvent(event); break;
        }
    }
    
    private void parseMetaMessage(MetaMessage message, MidiEvent event) {
    	switch (message.getType()) {
    		case MidiDefaults.META_SEQUENCE_NUMBER: fireUnhandledMidiEvent(event); break;
    		case MidiDefaults.META_TEXT_EVENT: fireUnhandledMidiEvent(event); break;
    		case MidiDefaults.META_COPYRIGHT_NOTICE: fireUnhandledMidiEvent(event); break;
    		case MidiDefaults.META_SEQUENCE_NAME: fireUnhandledMidiEvent(event); break;
    		case MidiDefaults.META_INSTRUMENT_NAME: fireUnhandledMidiEvent(event); break;
    		case MidiDefaults.META_LYRIC: lyricParsed(message); fireHandledMidiEvent(event); break;
    		case MidiDefaults.META_MARKER: markerParsed(message); fireHandledMidiEvent(event); break;
    		case MidiDefaults.META_CUE_POINT: fireUnhandledMidiEvent(event); break;
    		case MidiDefaults.META_MIDI_CHANNEL_PREFIX: fireUnhandledMidiEvent(event); break;
    		case MidiDefaults.META_END_OF_TRACK: fireUnhandledMidiEvent(event); break;
    		case MidiDefaults.META_TEMPO: tempoChanged(message); fireHandledMidiEvent(event); break;
    		case MidiDefaults.META_SMTPE_OFFSET: fireUnhandledMidiEvent(event); break;
    		case MidiDefaults.META_TIMESIG: timeSigParsed(message); fireHandledMidiEvent(event); break;
    		case MidiDefaults.META_KEYSIG: keySigParsed(message); fireHandledMidiEvent(event); break;
    		case MidiDefaults.META_VENDOR: fireUnhandledMidiEvent(event); break;
    		default: fireUnhandledMidiEvent(event); break;
    	}
    }
    
    private void parseSysexMessage(SysexMessage message, MidiEvent event) {
    	sysexParsed(message);
       	fireHandledMidiEvent(event); 
    }
    
    
    private boolean isNoteOnEvent(int command, int channel, MidiEvent event) {
        return ((command == ShortMessage.NOTE_ON) && ! 
                ((noteCache.get(channel).get(event.getMessage().getMessage()[1]) != null) && 
                 (event.getMessage().getMessage()[2] == 0)));
    }
        
    private boolean isNoteOffEvent(int command, int channel, MidiEvent event) {
    	// An event is a NoteOff event if it is actually a NoteOff event, 
    	// or if it is a NoteOn event where the note has already been played and the attack velocity is 0. 
        return ((command == ShortMessage.NOTE_OFF) || 
                ((command == ShortMessage.NOTE_ON) &&
                 (noteCache.get(channel).get(event.getMessage().getMessage()[1]) != null) && 
                 (event.getMessage().getMessage()[2] == 0)));
    }
        
    private void noteOff(int channel, MidiEvent event) {
        byte note = event.getMessage().getMessage()[1];
        TempNote tempNote = noteCache.get(channel).get(note);
        if (tempNote == null) {
        	// A note was turned off when that note was never indicated as having been turned on
        	return;
        }
        noteCache.get(channel).remove(note);
    	checkTime(tempNote.startTick);
        
        long durationInTicks = event.getTick() - tempNote.startTick;
        double durationInBeats = getDurationInBeats(durationInTicks);
        byte decayVelocity = event.getMessage().getMessage()[2];
        this.expectedTimeInBeats[this.currentChannel] = this.currentTimeInBeats[this.currentChannel] + durationInBeats; 
	
        Note noteObject = new Note(note);
        noteObject.setDuration(getDurationInBeats(durationInTicks)); 
        noteObject.setOnVelocity(tempNote.attackVelocity);
        noteObject.setOffVelocity(decayVelocity);
        fireNoteParsed(noteObject);
    }    
    
    private void noteOn(int channel, MidiEvent event) {
        if (isNoteOffEvent(ShortMessage.NOTE_ON, channel, event)) {
        	// Some MIDI files use the Note On event with 0 velocity to indicate Note Off
        	noteOff(channel, event);
        	return;
        }
        
        byte note = event.getMessage().getMessage()[1];
        byte attackVelocity = event.getMessage().getMessage()[2];
        if (noteCache.get(channel).get(note) != null) {
        	// The note already existed in the cache! Nothing to do about it now. This shouldn't happen.
        } else {
        	noteCache.get(channel).put(note, new TempNote(event.getTick(), attackVelocity));
        }
    }
        
    private void polyphonicAftertouch(int channel, MidiEvent event) {
        firePolyphonicPressureParsed(event.getMessage().getMessage()[1], event.getMessage().getMessage()[2]);
    }
    
    private void controlChange(int channel, MidiEvent event) {
        fireControllerEventParsed(event.getMessage().getMessage()[1], event.getMessage().getMessage()[2]); 
    }
        
    private void programChange(int channel, MidiEvent event) {
        fireInstrumentParsed(event.getMessage().getMessage()[1]); 
    }
    
    private void channelAftertouch(int channel, MidiEvent event) {
        fireChannelPressureParsed(event.getMessage().getMessage()[1]); 
    }
    
    private void pitchWheel(int channel, MidiEvent event) {
        firePitchWheelParsed(event.getMessage().getMessage()[1], event.getMessage().getMessage()[2]); 
    }
    
    private void tempoChanged(MetaMessage meta) {
    	int newTempoMSPQ = (meta.getData()[2] & 0xFF) | 
    		((meta.getData()[1] & 0xFF) << 8) | 
    		((meta.getData()[0] & 0xFF) << 16);
    	this.tempoBPM = newTempoMSPQ = 60000000 / newTempoMSPQ;
    	fireTempoChanged(tempoBPM);
    }
    
    private void lyricParsed(MetaMessage meta) {
    	fireLyricParsed(new String(meta.getData()));
    }
    
    private void markerParsed(MetaMessage meta) {
    	fireMarkerParsed(new String(meta.getData()));
    }
    
    private void keySigParsed(MetaMessage meta) {
    	fireKeySignatureParsed(meta.getData()[0], meta.getData()[1]);
    }

    private void timeSigParsed(MetaMessage meta) {
    	fireTimeSignatureParsed(meta.getData()[0], meta.getData()[1]);
    }

    private void sysexParsed(SysexMessage sysex) {
    	fireSystemExclusiveParsed(sysex.getData());   
    }
    
    private void checkTime(long tick) {
        double newTimeInBeats = getDurationInBeats(tick);
        if (this.expectedTimeInBeats[this.currentChannel] != newTimeInBeats) {
        	if (newTimeInBeats > expectedTimeInBeats[this.currentChannel]) {
        		fireNoteParsed(Note.createRest(newTimeInBeats - expectedTimeInBeats[this.currentChannel]));
        	} else {
        		fireTrackBeatTimeRequested(newTimeInBeats);
        	}
        }
        this.currentTimeInBeats[this.currentChannel] = newTimeInBeats;
    }
    
    private void checkChannel(int channel) {
        if (this.currentChannel != channel) {
            fireTrackChanged((byte)channel);
            this.currentChannel = channel;
        }
    }    

    
    //
    // Formulas and converters
    //
    
    private double getDurationInBeats(long durationInTicks) {
        return durationInTicks / (double)this.resolutionTicksPerBeat / 4.0d;  
    }
    
    private long ticksToMs(long ticks) {
    	return (long)((ticks / this.resolutionTicksPerBeat) * (1.0d / this.tempoBPM) * MidiDefaults.MS_PER_MIN); 
    }
    
    private long msToTicks(long ms) {
    	return (long)((ms / MidiDefaults.MS_PER_MIN) * this.tempoBPM * this.resolutionTicksPerBeat); 
    }

    
    //
    // AuxilliaryMidiParser
    //
    
    public void addAuxilliaryMidiParser(AuxilliaryMidiParser auxilliaryParser) {
    	auxilliaryParsers.add(auxilliaryParser);
    }
    
    public void removeAuxilliaryMidiParser(AuxilliaryMidiParser auxilliaryParser) {
    	auxilliaryParsers.remove(auxilliaryParser);
    }
    
    protected void fireHandledMidiEvent(MidiEvent event) {
    	for (AuxilliaryMidiParser auxilliaryParser : auxilliaryParsers) {
    		auxilliaryParser.parseHandledMidiEvent(event, this);
    	}
    }
    
    protected void fireUnhandledMidiEvent(MidiEvent event) {
    	for (AuxilliaryMidiParser auxilliaryParser : auxilliaryParsers) {
    		auxilliaryParser.parseUnhandledMidiEvent(event, this);
    	}
    }
    
    
    // 
    // TempNote data structure
    //
    
    class TempNote {
        long startTick;
        byte attackVelocity;
        
        public TempNote(long startTick, byte attackVelocity) {
            this.startTick = startTick;
            this.attackVelocity = attackVelocity;
        }
    }
}
