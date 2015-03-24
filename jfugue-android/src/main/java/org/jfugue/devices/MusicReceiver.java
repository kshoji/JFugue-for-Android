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

import org.jfugue.midi.MidiDefaults;
import org.jfugue.midi.MidiTools;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiDevice;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.Sequence;
import jp.kshoji.javax.sound.midi.ShortMessage;

/**
 * Represents a device that can receive music that is sent to it. For example, you
 * can connect your external MIDI keyboard, then use this class to send music from
 * your JFugue program to the device.
 */
public class MusicReceiver 
{
    private MidiDevice device;
    private boolean isInitiated;
    private Receiver receiver;
    
    public MusicReceiver(MidiDevice device)  {
        this.device = device;
        this.isInitiated = false;
    }
    
    /**
     * Send the given sequence to the MIDI device - use this to send MIDI files
     * to your keyboard!
     *  
     * @param sequence The sequence to send to the MIDI device
     */
    public void sendSequence(Sequence sequence) throws MidiUnavailableException {
        init();
        MidiTools.sendSequenceToReceiver(sequence, receiver);
        cleanup();
        close();
    }
    
    public Receiver getReceiver() {
        return this.receiver;
    }
        
    private void init() throws MidiUnavailableException {
        if (!isInitiated) {
            if (!(device.isOpen())) {
              device.open();
            }
    
            this.receiver = device.getReceiver();

            this.isInitiated = true;
        }
    }
    
    /** Sends messages to turn all controllers and all notes off for all tracks (channels) */
    private void cleanup() {
        ShortMessage allControllersOff = new ShortMessage();
        ShortMessage allNotesOff = new ShortMessage();
        
        for (byte track=0; track < MidiDefaults.TRACKS; track++) {
            try {
                allControllersOff.setMessage(ShortMessage.CONTROL_CHANGE, track, (byte)121, (byte)0);
                receiver.send(allControllersOff, -1);
                
                allNotesOff.setMessage(ShortMessage.CONTROL_CHANGE, track, (byte)123, (byte)0);
                receiver.send(allNotesOff, -1);
            } catch (InvalidMidiDataException e) {
                // Not going to happen, we're keeping a close eye on the data in the MIDI messages
            }
        }
    }
    
    private void close() {
        receiver.close();
        device.close();
    }
}
