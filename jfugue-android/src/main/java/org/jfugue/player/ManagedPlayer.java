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
import java.util.logging.Logger;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Sequence;

/**
 * This is player that can be "managed" - e.g., started, stopped, paused, resumed, seeked, and finished.
 * Additionally, the state of this player can be requested.
 * If you want to create a player that you can control like any standard media player, this is your class.
 */
public class ManagedPlayer implements EndOfTrackListener
{
	private SequencerManager common;
	private boolean started;
	private boolean finished;
	private boolean paused;

    private CopyOnWriteArrayList<ManagedPlayerListener> playerListeners;
    
	public ManagedPlayer() {
    	playerListeners = new CopyOnWriteArrayList<ManagedPlayerListener>();
    	try {
    		common = SequencerManager.getInstance();
    	} catch (MidiUnavailableException e) {
    		Logger.getLogger("org.jfugue").severe(e.getLocalizedMessage());
    	}
    }
    
	public void addManagedPlayerListener(ManagedPlayerListener listener) {
	    playerListeners.add(listener);
	}
	
	public void removeManagedPlayerListener(ManagedPlayerListener listener) {
	    playerListeners.add(listener);
	}
	
	public List<ManagedPlayerListener> getManagedPlayerListeners() {
	    return playerListeners;
	}

	/**
	 * This method opens the sequencer (if it is not already open - @see PlayerCommon),  
	 * sets the sequence, tells listeners that play is starting, and starts the sequence.
	 * @param sequence
	 */
	public void start(Sequence sequence) throws InvalidMidiDataException, MidiUnavailableException {
		common.openSequencer();
		common.addEndOfTrackListener(this);
		common.getSequencer().setSequence(sequence);
		fireOnStarted();
		this.started = true;
		this.paused = false;
		this.finished = false;
		common.getSequencer().start();
	}
	
    /**
     * To resume play, @see resume()
     */
    public void pause() {
    	fireOnPaused();
    	this.paused = true;
    	common.getSequencer().stop();
    }

    /**
     * To pause play, @see pause()
     */
    public void resume() {
    	fireOnResumed();
    	this.paused = false;
    	common.getSequencer().start();
    }
    
    public void seek(long tick) {
    	fireOnSeek(tick);
    	common.getSequencer().setTickPosition(tick);
    }
    
    public void finish() {
    	common.close();
    	this.finished = true;
    	fireOnFinished();
    }
    
    public long getTickLength() {
    	return common.getSequencer().getTickLength();
    }

    public long getTickPosition() {
    	return common.getSequencer().getTickPosition();
    }

	public boolean isStarted() {
        return this.started;
    }
    
    public boolean isFinished() {
        return this.finished;
    }
    
    public boolean isPaused() {
        return this.paused;
    }
	
    public boolean isPlaying() {
        return common.getSequencer().isRunning();
    }

    @Override
    public void onEndOfTrack() {
   		finish();
    }
    
	protected void fireOnStarted() { 
	    List<ManagedPlayerListener> listeners = getManagedPlayerListeners();
	    for (ManagedPlayerListener listener : listeners) {
	        listener.onStarted();
	    }
	}	

	protected void fireOnFinished() { 
	    List<ManagedPlayerListener> listeners = getManagedPlayerListeners();
	    for (ManagedPlayerListener listener : listeners) {
	        listener.onFinished();
	    }
	}	

	protected void fireOnPaused() { 
	    List<ManagedPlayerListener> listeners = getManagedPlayerListeners();
	    for (ManagedPlayerListener listener : listeners) {
	        listener.onPaused();
	    }
	}	

	protected void fireOnResumed() { 
	    List<ManagedPlayerListener> listeners = getManagedPlayerListeners();
	    for (ManagedPlayerListener listener : listeners) {
	        listener.onResumed();
	    }
	}	
	
	protected void fireOnSeek(long tick) { 
	    List<ManagedPlayerListener> listeners = getManagedPlayerListeners();
	    for (ManagedPlayerListener listener : listeners) {
	        listener.onSeek(tick);
	    }
	}	
} 
