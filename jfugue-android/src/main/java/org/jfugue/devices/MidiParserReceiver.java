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

package org.jfugue.devices;

import org.jfugue.midi.MidiParser;

import jp.kshoji.javax.sound.midi.MidiEvent;
import jp.kshoji.javax.sound.midi.MidiMessage;
import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.Sequence;
import jp.kshoji.javax.sound.midi.Sequencer;

/**
 * This class represents a MidiParser that is also a MIDI Receiver.
 * As a MidiParser, it can have ParserListeners 
 * As a Receiver, it overrides send() and sends the resulting MidiMessage and timestamp to MidiParser's parseEvent method.
 */
public class MidiParserReceiver implements Receiver
{
    private MidiParser parser;
    private Sequencer sequencer;
    private Receiver sequencerReceiver;
    
    public MidiParserReceiver() throws MidiUnavailableException {
        this.parser = new MidiParser();  
        this.sequencer = MidiSystem.getSequencer();
        this.sequencerReceiver = sequencer.getReceiver();
    }
    
    public MidiParser getParser() {
        return this.parser;
    }
    
    @Override
    public void send(MidiMessage message, long timestamp) {
        parser.parseEvent(new MidiEvent(message, timestamp));
        sequencerReceiver.send(message, timestamp);
    }
    
    public void close()  {
        sequencerReceiver.close();
    }
    
    public Sequence getSequence() {
        return sequencer.getSequence();
    }
}