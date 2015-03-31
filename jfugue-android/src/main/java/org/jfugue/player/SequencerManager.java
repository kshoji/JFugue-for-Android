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

package org.jfugue.player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jp.kshoji.javax.sound.midi.MetaEventListener;
import jp.kshoji.javax.sound.midi.MetaMessage;
import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Sequencer;
import jp.kshoji.javax.sound.midi.Synthesizer;

/**
 * This class provides operations done on a Sequencer for any
 * method of play. This includes opening and closing the sequencer. 
 */
public class SequencerManager {
	private static SequencerManager instance;
	
	public static SequencerManager getInstance() throws MidiUnavailableException {
		if (instance == null) {
			instance = new SequencerManager();
		}
		return instance;
	}

	private Sequencer sequencer;
    private CopyOnWriteArrayList<EndOfTrackListener> endOfTrackListeners;

	public SequencerManager() throws MidiUnavailableException { 
		this.sequencer = getDefaultSequencer();
		endOfTrackListeners = new CopyOnWriteArrayList<EndOfTrackListener>();
	}
	
	public Sequencer getDefaultSequencer() throws MidiUnavailableException {
		return MidiSystem.getSequencer();
	}
	
	public Sequencer getSequencer() {
		return this.sequencer;
	}
	
	public void setSequencer(Sequencer sequencer) {
		this.sequencer = sequencer;
	}
	
	/**
	 * This method opens the sequencer - but if the sequencer is already open,
	 * it does nothing. 
	 * Returns the sequencer.
	 */
	public Sequencer openSequencer() throws MidiUnavailableException {
		if (!this.sequencer.isOpen()) {
			this.sequencer.open();
			this.sequencer.addMetaEventListener(new MetaEventListener() {
				public void meta(MetaMessage event) {
					if (event.getType() == 47) {
						fireEndOfTrack();
					}
                }
			});
		}
		return this.sequencer;
	}

	public void close() {
		if (this.sequencer == null) {
			return;
		}
		if (this.sequencer.isOpen()) {
			this.sequencer.close();
		}
	}
	
	public void connectSequencerToSynthesizer(Synthesizer synth) throws MidiUnavailableException {
        if (!synth.isOpen()) {
            synth.open();
        }
		openSequencer().getTransmitter().setReceiver(synth.getReceiver());
	}
	
	public void addEndOfTrackListener(EndOfTrackListener listener) {
		endOfTrackListeners.add(listener);
	}
	
	public void removeEndOfTrackListener(EndOfTrackListener listener) {
		endOfTrackListeners.add(listener);
	}
	
	public List<EndOfTrackListener> getEndOfTrackListeners() {
	    return endOfTrackListeners;
	}
	
	protected void fireEndOfTrack() {
	    List<EndOfTrackListener> listeners = getEndOfTrackListeners();
	    for (EndOfTrackListener listener : listeners) {
	        listener.onEndOfTrack();
	    }
	}
}
