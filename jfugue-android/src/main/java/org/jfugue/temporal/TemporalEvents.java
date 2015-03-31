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

package org.jfugue.temporal;

import org.jfugue.parser.Parser;
import org.jfugue.theory.Chord;
import org.jfugue.theory.Note;

public class TemporalEvents 
{
    public class TrackEvent implements TemporalEvent {
        private byte track;
        public TrackEvent(byte track) { this.track = track; }
        public void execute(Parser parser) { parser.fireTrackChanged(track); }
    }

    public class LayerEvent implements TemporalEvent {
        private byte layer;
        public LayerEvent(byte layer) { this.layer = layer; }
        public void execute(Parser parser) { parser.fireLayerChanged(layer); }
    }

    public class InstrumentEvent implements TemporalEvent {
        private byte instrument;
        public InstrumentEvent(byte instrument) { this.instrument = instrument; }
        public void execute(Parser parser) { parser.fireInstrumentParsed(instrument); }
    }

    public class TempoEvent implements TemporalEvent {
        private int tempoBPM;
        public TempoEvent(int tempoBPM) { this.tempoBPM = tempoBPM; }
        public void execute(Parser parser) { parser.fireTempoChanged(tempoBPM); }
    }

    public class KeySignatureEvent implements TemporalEvent {
        private byte key, scale;
        public KeySignatureEvent(byte key, byte scale) { this.key = key; this.scale = scale; }
        public void execute(Parser parser) { parser.fireKeySignatureParsed(key, scale); }
    }

    public class TimeSignatureEvent implements TemporalEvent {
        private byte numerator, powerOfTwo;
        public TimeSignatureEvent(byte numerator, byte powerOfTwo) { this.numerator = numerator; this.powerOfTwo = powerOfTwo; }
        public void execute(Parser parser) { parser.fireTimeSignatureParsed(numerator, powerOfTwo); }
    }

    public class BarEvent implements TemporalEvent {
        private long barId;
        public BarEvent(long barId) { this.barId = barId; }
        public void execute(Parser parser) { parser.fireBarLineParsed(barId); }
    }

    
//    public void trackBeatTimeBookmarked(String timeBookmarkId);
//    public void trackBeatTimeBookmarkRequested(String timeBookmarkId);
//    public void trackBeatTimeRequested(double time); 

    public class PitchWheelEvent implements TemporalEvent {
        private byte lsb, msb;
        public PitchWheelEvent(byte lsb, byte msb) { this.lsb = lsb; this.msb = msb; }
        public void execute(Parser parser) { parser.fireKeySignatureParsed(lsb, msb); }
    }

    public class ChannelPressureEvent implements TemporalEvent {
        private byte pressure;
        public ChannelPressureEvent(byte pressure) { this.pressure = pressure; }
        public void execute(Parser parser) { parser.fireChannelPressureParsed(pressure); }
    }

    public class PolyphonicPressureEvent implements TemporalEvent {
        private byte key, pressure;
        public PolyphonicPressureEvent(byte key, byte pressure) { this.key = key; this.pressure = pressure; }
        public void execute(Parser parser) { parser.firePolyphonicPressureParsed(key, pressure); }
    }

    public class SystemExclusiveEvent implements TemporalEvent {
        private byte[] bytes;
        public SystemExclusiveEvent(byte... bytes) { this.bytes = bytes; }
        public void execute(Parser parser) { parser.fireSystemExclusiveParsed(bytes); }
    }

    public class ControllerEvent implements TemporalEvent {
        private byte controller, value;
        public ControllerEvent(byte controller, byte value) { this.controller = controller; this.value = value; }
        public void execute(Parser parser) { parser.fireControllerEventParsed(controller, value); }
    }

    public class LyricEvent implements TemporalEvent {
        private String lyric;
        public LyricEvent(String lyric) { this.lyric = lyric; }
        public void execute(Parser parser) { parser.fireLyricParsed(lyric); }
    }

    public class MarkerEvent implements TemporalEvent {
        private String marker;
        public MarkerEvent(String marker) { this.marker = marker; }
        public void execute(Parser parser) { parser.fireMarkerParsed(marker); }
    }

    public class UserEvent implements TemporalEvent {
        private String id;
        private Object message;
        public UserEvent(String id, Object message) { this.id = id; this.message = message; }
        public void execute(Parser parser) { parser.fireFunctionParsed(id, message); }
    }

    public class NoteEvent implements DurationTemporalEvent {
        private Note note;
        public NoteEvent(Note note) { this.note = note; }
        public void execute(Parser parser) { parser.fireNoteParsed(this.note); }
        public double getDuration() { return this.note.getDuration(); }
    }

    public class ChordEvent implements DurationTemporalEvent {
        private Chord chord;
        public ChordEvent(Chord chord) { this.chord = chord; }
        public void execute(Parser parser) { parser.fireChordParsed(this.chord); }
        public double getDuration() { return this.chord.getNotes()[0].getDuration(); }
    }
}
