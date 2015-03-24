package org.staccato;

import org.jfugue.midi.MidiDefaults;
import org.jfugue.theory.Note;

public class DefaultNoteSettingsManager {
	private static DefaultNoteSettingsManager instance;
	
	public static DefaultNoteSettingsManager getInstance() {
		if (instance == null) {
			instance = new DefaultNoteSettingsManager();
		}
		return instance;
	}
	
	private byte defaultOctave = DEFAULT_DEFAULT_OCTAVE;
	private byte defaultBassOctave = DEFAULT_DEFAULT_BASS_OCTAVE;
	private double defaultDuration = DEFAULT_DEFAULT_DURATION;
	private byte defaultOnVelocity = MidiDefaults.MIDI_DEFAULT_ON_VELOCITY;
	private byte defaultOffVelocity = MidiDefaults.MIDI_DEFAULT_OFF_VELOCITY;
	
	private DefaultNoteSettingsManager() { }
	
	public void setDefaultOctave(byte octave) {
		assert (octave < Note.MIN_OCTAVE) || (octave > Note.MAX_OCTAVE);
		this.defaultOctave = octave;
	}

	public byte getDefaultOctave() {
		 return this.defaultOctave;
	}
	
	public void setDefaultBassOctave(byte octave) {
		assert (octave < Note.MIN_OCTAVE) || (octave > Note.MAX_OCTAVE);
		this.defaultBassOctave = octave;
	}
	
	public byte getDefaultBassOctave() {
		return this.defaultBassOctave;
	}
	
	public void setDefaultDuration(double duration) {
		this.defaultDuration = duration;
	}
	
	public double getDefaultDuration() {
		return this.defaultDuration;
	}
	
	public void setDefaultOnVelocity(byte attack) {
		assert (attack < MidiDefaults.MIN_ON_VELOCITY) || (attack > MidiDefaults.MAX_ON_VELOCITY);
		this.defaultOnVelocity = attack;
	}
	
	public byte getDefaultOnVelocity() {
		return this.defaultOnVelocity;
	}
	
	public void setDefaultOffVelocity(byte decay) {
		assert (decay < MidiDefaults.MIN_OFF_VELOCITY) || (decay > MidiDefaults.MAX_OFF_VELOCITY);
		this.defaultOffVelocity = decay;
	}
	
	public byte getDefaultOffVelocity() {
		return this.defaultOffVelocity;
	}
	
	public static final byte DEFAULT_DEFAULT_OCTAVE = 5;
	public static final byte DEFAULT_DEFAULT_BASS_OCTAVE = 4; // Updated in JFugue5; in previous versions, bass was Octave 3
	public static final double DEFAULT_DEFAULT_DURATION = 0.25d;
	public static final byte DEFAULT_DEFAULT_ON_VELOCITY = MidiDefaults.MIDI_DEFAULT_ON_VELOCITY;
	public static final byte DEFAULT_DEFAULT_OFF_VELOCITY = MidiDefaults.MIDI_DEFAULT_OFF_VELOCITY;
}
