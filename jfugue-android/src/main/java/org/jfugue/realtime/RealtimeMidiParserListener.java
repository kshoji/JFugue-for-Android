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

import org.jfugue.midi.MidiDefaults;
import org.jfugue.midi.TrackTimeManager;
import org.jfugue.parser.ParserListener;
import org.jfugue.theory.Chord;
import org.jfugue.theory.Note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.kshoji.javax.sound.midi.MidiUnavailableException;

/**
 * The callbacks in RealtimeMidiParserListener are only called when a user
 * sends a Pattern to the RealtimePlayer. Otherwise, individual events
 * like "note on" or "change instrument" are handled by RealtimePlayer itself.
 * When this listener receives an event from the parser, it schedules the
 * event with a command that will execute directly on the RealtimePlayer.
 */
public class RealtimeMidiParserListener extends TrackTimeManager implements ParserListener
{
	private boolean endDaemon;
    private int bpm = MidiDefaults.DEFAULT_TEMPO_BEATS_PER_MINUTE;
    private long originalClockTimeInMillis;
    private long activeTimeInMillis;
    private Map<Long, List<Command>> millisToScheduledCommands;
    private Map<Long, List<ScheduledEvent>> millisToScheduledEvents;
    private List<RealtimeInterpolator> interpolators;
    private RealtimePlayer realtimePlayer;
    
    public RealtimeMidiParserListener(RealtimePlayer player) throws MidiUnavailableException {
    	super();
    	this.realtimePlayer = player;
    	this.millisToScheduledCommands = new HashMap<Long, List<Command>>();
    	this.millisToScheduledEvents = new HashMap<Long, List<ScheduledEvent>>();
    	this.interpolators = new ArrayList<RealtimeInterpolator>();
    	this.originalClockTimeInMillis = System.currentTimeMillis(); 
    	startDaemon();
    }

    private long getDeltaClockTimeInMillis() {
    	return System.currentTimeMillis() - this.originalClockTimeInMillis;
    }
    
    public long getCurrentTime() {
        return getDeltaClockTimeInMillis();
    }
    
    private void startDaemon() {
    	Runnable daemon = new Runnable() {
    		private long lastMillis = 0L;
    		
    		public void run() {
    			while (!endDaemon) {
    				setAllTrackBeatTime(getDeltaClockTimeInMillis());
    				
    				long deltaMillis = getDeltaClockTimeInMillis() - lastMillis;
    				if (deltaMillis > 0) {
	    				for (long time = lastMillis; time < lastMillis+deltaMillis; time++) {
	    					setActiveTimeInMillis(time);
	    					executeScheduledCommands(time);
	    					executeScheduledEvents(time);
	    					updateInterpolators(time);
	    				}
    				}
    				this.lastMillis = this.lastMillis + deltaMillis;
    			}
    		}
    	};
    	
    	Thread t = new Thread(daemon);
    	t.start();
    }

	// Process any scheduled commands that are internal to this parser 
    private void executeScheduledCommands(long time) {
		if (millisToScheduledCommands.containsKey(time)) {
			List<Command> commands = millisToScheduledCommands.get(time);
			for (Command command : commands) {
				command.execute();
			}
		}
    }
    
	// Process any scheduled events requested by the user 
    private void executeScheduledEvents(long time) {
    	if (millisToScheduledEvents.containsKey(time)) {
			List<ScheduledEvent> scheduledEvents = millisToScheduledEvents.get(time);
			for (ScheduledEvent event : scheduledEvents) {
				event.execute(realtimePlayer, time);
			}
    	}
    }
    
	// Process any active interpolators 
    private void updateInterpolators(long time) {
		for (RealtimeInterpolator interpolator : interpolators) {
			if (!interpolator.isStarted()) {
				interpolator.start(time);
			}
			if (interpolator.isActive()) {
				long elapsedTime = time - interpolator.getStartTime();
				double percentComplete = elapsedTime / interpolator.getDurationInMillis();
				interpolator.update(realtimePlayer, elapsedTime, percentComplete);
				if (elapsedTime == interpolator.getDurationInMillis()) {
					interpolator.end();
				}
			}
		}
    }
    
    public void finish() {
        this.endDaemon = true;
    }

    public RealtimePlayer getRealtimePlayer() {
    	return this.realtimePlayer;
    }
    
    private void setActiveTimeInMillis(long timeInMillis) {
    	this.activeTimeInMillis = timeInMillis;
    }
    
    private long getNextAvailableTimeInMillis(long timeInMillis) {
    	if (timeInMillis <= activeTimeInMillis) {
    		timeInMillis += activeTimeInMillis+1;
    	}
    	return timeInMillis;
    }
    
    public void scheduleCommand(long timeInMillis, Command command) {
    	timeInMillis = getNextAvailableTimeInMillis(timeInMillis);
    	List<Command> commands = millisToScheduledCommands.get(timeInMillis);
    	if (commands == null) {
    		commands = new ArrayList<Command>();
    		millisToScheduledCommands.put(timeInMillis, commands);
    	}
    	commands.add(command);
    }

    public void scheduleEvent(long timeInMillis, ScheduledEvent event) {
    	timeInMillis = getNextAvailableTimeInMillis(timeInMillis);
    	List<ScheduledEvent> events = millisToScheduledEvents.get(timeInMillis);
    	if (events == null) {
    		events = new ArrayList<ScheduledEvent>();
    		millisToScheduledEvents.put(timeInMillis, events);
    	}
    	events.add(event);
    }

    public void unscheduleEvent(long timeInMillis, ScheduledEvent event) {
    	List<ScheduledEvent> events = millisToScheduledEvents.get(timeInMillis);
    	if (events == null) {
    		return;
    	}
    	events.remove(event);
    }
    
    /* ParserListener Events */
    
    @Override
    public void beforeParsingStarts() { }

    @Override
    public void afterParsingFinished() { }
    
    @Override
    public void onTrackChanged(final byte track) {
        setCurrentTrack(track);
    	scheduleCommand((long)getTrackBeatTime(), new Command() {
    		public void execute() {
    			getRealtimePlayer().changeTrack(track);
    		}
    	});        
    }

    @Override 
    public void onLayerChanged(byte layer) { 
        setCurrentLayer(layer);
    }

    @Override 
    public void onInstrumentParsed(final byte instrument) {
    	scheduleCommand((long)getTrackBeatTime(), new Command() {
    		public void execute() {
    			getRealtimePlayer().changeInstrument(instrument);
    		}
    	});
    }

    @Override
    public void onTempoChanged(int tempoBPM) {
    	this.bpm = tempoBPM;
    }

    @Override
    public void onKeySignatureParsed(byte key, byte scale) { }

    @Override 
    public void onTimeSignatureParsed(byte numerator, byte powerOfTwo) { }

    @Override
    public void onBarLineParsed(long time) { }

    @Override 
    public void onTrackBeatTimeBookmarked(String timeBookmarkID) { }

    @Override 
    public void onTrackBeatTimeBookmarkRequested(String timeBookmarkID) { }

    @Override 
    public void onTrackBeatTimeRequested(double time) { }

    @Override
    public void onPitchWheelParsed(final byte lsb, final byte msb) {
    	scheduleCommand((long)getTrackBeatTime(), new Command() {
    		public void execute() {
    			getRealtimePlayer().setPitchBend(lsb + (msb << 7));
    		}
    	});
    }

    @Override
    public void onChannelPressureParsed(final byte pressure) {
    	scheduleCommand((long)getTrackBeatTime(), new Command() {
    		public void execute() {
    			getRealtimePlayer().changeChannelPressure(pressure);
    		}
    	});
    }

    @Override
    public void onPolyphonicPressureParsed(final byte key, final byte pressure) {
    	scheduleCommand((long)getTrackBeatTime(), new Command() {
    		public void execute() {
    			getRealtimePlayer().changePolyphonicPressure(key, pressure);
    		}
    	});
    }

    @Override 
    public void onSystemExclusiveParsed(byte... bytes) { }

    @Override
    public void onControllerEventParsed(final byte controller, final byte value) {
    	scheduleCommand((long)getTrackBeatTime(), new Command() {
    		public void execute() {
    			getRealtimePlayer().changeController(controller, value);
    		}
    	});
    }

    @Override 
    public void onLyricParsed(String lyric) { } 

    @Override 
    public void onMarkerParsed(String marker) { } 

    @Override 
    public void onFunctionParsed(String id, Object message) { }

    @Override  
    public void onNoteParsed(final Note note) {
    	if (note.getDuration() == 0.0) {
    		note.useDefaultDuration();
    	}

    	// If this is the first note in a sequence of harmonic or melodic notes, remember what time it is.
    	if (note.isFirstNote()) {
    		setInitialNoteBeatTimeForHarmonicNotes(getTrackBeatTime()); 
    	}
    	
    	// If we're going to the next sequence in a parallel note situation, roll back the time to the beginning of the first note.
    	// A note will never be a parallel note if a first note has not happened first.
    	if (note.isHarmonicNote()) {
    		setTrackBeatTime(getInitialNoteBeatTimeForHarmonicNotes());
    	} 

    	// If the note is a rest, simply advance the track time and get outta here
    	if (note.isRest()) {
    		advanceTrackBeatTime(convertBeatsToMillis(note.getDuration()));  
    		return;
    	}
    	
    	// Add a NOTE_ON event.
    	// If the note is continuing a tie, it is already sounding, and there is not need to turn the note on
    	if (!note.isEndOfTie()) {
        	scheduleCommand((long)getTrackBeatTime(), new Command() {
        		public void execute() {
        			getRealtimePlayer().startNote(note);
        		}
        	});    		
    	}
    	
    	// Advance the track timer
    	advanceTrackBeatTime(convertBeatsToMillis(note.getDuration()));  
    	
    	// Add a NOTE_OFF event.
    	// If this note is the start of a tie, the note will continue to sound, so we don't want to turn it off.
    	if (!note.isStartOfTie()) {
        	scheduleCommand((long)getTrackBeatTime(), new Command() {
        		public void execute() {
        			getRealtimePlayer().stopNote(note);
        		}
        	});    		
    	}
    }
    
    @Override 
    public void onChordParsed(Chord chord) {
    	for (Note note : chord.getNotes()) {
    		this.onNoteParsed(note);
    	}
    }


    //
    // Scheduled Events 
    //
    
    public void onEventScheduled(long timeInMillis, ScheduledEvent event) {
    	scheduleEvent(timeInMillis, event);
    }

    public void onEventUnscheduled(long timeInMillis, ScheduledEvent event) {
    	unscheduleEvent(timeInMillis, event);
    }
    
    public void onInterpolatorStarted(RealtimeInterpolator interpolator, long durationInMillis) {
        interpolator.setDurationInMillis(durationInMillis);
    	interpolators.add(interpolator);
    }
    
    public void onInterpolatorStopping(RealtimeInterpolator interpolator) {
    	interpolators.remove(interpolator);
    }
    
    private long convertBeatsToMillis(double beats) {
    	return (long)((beats / bpm) * MidiDefaults.MS_PER_MIN * 4); 
    }
    
    interface Command {
    	public void execute();
    }
}


