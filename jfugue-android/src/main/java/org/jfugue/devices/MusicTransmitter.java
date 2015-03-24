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

import org.jfugue.parser.ParserListener;

import java.util.ArrayList;
import java.util.List;

import jp.kshoji.javax.sound.midi.MidiDevice;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Transmitter;

/**
 * Represents a device that will send music. For example, you can attach this
 * to your external MIDI keyboard and play music on the keyboard, which is then recorded here.
 */
public class MusicTransmitter 
{
    private MidiDevice device;
    private boolean isInitiated;
    private Transmitter transmitter;
    private MidiParserReceiver mrftd;
    private List<ParserListener> listeners;
    
    public MusicTransmitter(MidiDevice device) throws MidiUnavailableException {
        this.device = device;
        this.isInitiated = false;
        this.listeners = new ArrayList<ParserListener>();
    }
    
    private void init() throws MidiUnavailableException {
        if (!isInitiated) {
            try  {
                if (!(device.isOpen())) {
                  device.open();
                }
                this.transmitter = device.getTransmitter();
                this.mrftd = new MidiParserReceiver();
                for (ParserListener listener : listeners) {
                    this.mrftd.getParser().addParserListener(listener);
                }
            } catch (MidiUnavailableException e) {
                device.close();
                throw e;
            }
        }
    }
    
    public void addParserListener(ParserListener l) {
        this.listeners.add(l);
    }
    
    public List<ParserListener> getParserListeners() { 
        return this.listeners;
    }
    
    public Transmitter getTransmitter() {
        return this.transmitter;
    }
    
    public MidiParserReceiver getMidiParserReceiver() {
        return this.mrftd;
    }
    
    /**
     * Reads a pattern from the external device - use this to record the
     * keys you're pressing on the keyboard!  
     * 
     * This method will return a JFugue Pattern, which you can then 
     * manipulate to your heart's content.
     * 
     * @return The Pattern representing the music played on the device
     */
    public void startListening() throws MidiUnavailableException {
        init();
        mrftd.getParser().startParser();
        transmitter.setReceiver(this.mrftd);
    }
    
    public void stopListening() {
        mrftd.getParser().stopParser();
        close();
    }

    /**
     * Used instead of startListening() and stopListening() - listens for a pre-defined amount of time.
     * 
     * @param millis
     * @throws MidiUnavailableException
     * @throws InterruptedException
     */
    public void listenForMillis(long millis) throws MidiUnavailableException, InterruptedException {
        startListening();
        Thread.sleep(millis);
        stopListening();
    }
    
    public void close() {
        transmitter.close();
        device.close();
    }
}