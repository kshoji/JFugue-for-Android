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

import org.jfugue.theory.TimeSignature;

public interface MidiDefaults 
{
    public static final float DEFAULT_DIVISION_TYPE = 0.0f;
    public static final int DEFAULT_RESOLUTION_TICKS_PER_BEAT = 128;
    public static final int DEFAULT_TEMPO_BEATS_PER_MINUTE = 120;
    public static final int DEFAULT_TEMPO_BEATS_PER_WHOLE = 4;
    public static final int DEFAULT_METRONOME_PULSE = 24;
    public static final int DEFAULT_THIRTYSECOND_NOTES_PER_24_MIDI_CLOCK_SIGNALS = 8;   
    public static final int TRACKS = 16;
    public static final int LAYERS = 16;
    public static final double MS_PER_MIN = 60000.0d;
    public static final int DEFAULT_MPQ = 50; // Milliseconds per quarter note
    public static final byte SET_TEMPO_MESSAGE_TYPE = 0x51;
    public static final byte PERCUSSION_TRACK = 9;
    public static final byte MIN_PERCUSSION_NOTE = 35;
    public static final byte MAX_PERCUSSION_NOTE = 81;
    public static final byte MIN_ON_VELOCITY = 0;
    public static final byte MAX_ON_VELOCITY = 127;
    public static final byte MIDI_DEFAULT_ON_VELOCITY = 64; // See also DefaultNoteSettingsManager 
    public static final byte MIN_OFF_VELOCITY = 0;
    public static final byte MAX_OFF_VELOCITY = 127;    
    public static final byte MIDI_DEFAULT_OFF_VELOCITY = 64; // See also DefaultNoteSettingsManager
    public static final int DEFAULT_PATCH_BANK = 0;
    public static final TimeSignature DEFAULT_TIME_SIGNATURE = new TimeSignature(4, 4);
    
    
    // Meta Message Type Values
    public static final byte META_SEQUENCE_NUMBER = 0x00;
    public static final byte META_TEXT_EVENT = 0x01;
    public static final byte META_COPYRIGHT_NOTICE = 0x02;
    public static final byte META_SEQUENCE_NAME = 0x03;
    public static final byte META_INSTRUMENT_NAME = 0x04;
    public static final byte META_LYRIC = 0x05;
    public static final byte META_MARKER = 0x06;
    public static final byte META_CUE_POINT = 0x07;
    public static final byte META_MIDI_CHANNEL_PREFIX = 0x20;
    public static final byte META_END_OF_TRACK = 0x2F;
    public static final byte META_TEMPO = 0x51;
    public static final byte META_SMTPE_OFFSET = 0x54;
    public static final byte META_TIMESIG = 0x58;
    public static final byte META_KEYSIG = 0x59;
    public static final byte META_VENDOR = 0x7F;    
}
